/*
 * Copyright (C) 2014 OpenSilk Productions LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opensilk.music.ui2.main;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.andrew.apollo.MusicPlaybackService;

import org.opensilk.music.ui2.core.lifecycle.PauseAndResumeRegistrar;
import org.opensilk.music.ui2.core.lifecycle.PausesAndResumes;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import mortar.MortarScope;
import mortar.ViewPresenter;
import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.observers.Observers;
import rx.operators.OperatorBroadcastRegister;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by drew on 10/15/14.
 */
public class FooterScreen {

    @Singleton
    public static class Presenter extends ViewPresenter<FooterView> implements PausesAndResumes {

        final PauseAndResumeRegistrar pauseAndResumeRegistrar;
        final MusicServiceConnection musicService;

        Subscription playStateSubscription;
        Subscription metaSubscription;
        Subscription progressSubscription;

        Observable<String[]> metaObservable;
        Observable<Boolean> playStateObservable;
        Observable<Long> currentPositionObservable;
        Observable<Long> progressObservable;

        Observer<String[]> metaObserver;
        Observer<Boolean> playStateObserver;
        Observer<Long> progressObserver;

        @Inject
        public Presenter(PauseAndResumeRegistrar pauseAndResumeRegistrar,
                         MusicServiceConnection musicService) {
            this.pauseAndResumeRegistrar = pauseAndResumeRegistrar;
            this.musicService = musicService;
        }

        @Override
        protected void onEnterScope(MortarScope scope) {
            Timber.v("onEnterScope()");
            super.onEnterScope(scope);
            pauseAndResumeRegistrar.register(scope, this);
        }

        @Override
        protected void onExitScope() {
            super.onExitScope();
            unsubscribeAll();
        }

        @Override
        protected void onLoad(Bundle savedInstanceState) {
            Timber.v("onLoad()");
            super.onLoad(savedInstanceState);
            setupObserables();
            setupObservers();
            subscribePlaystate();
            subscribeMeta();
            //playstate will kick off progress subscription
        }

        @Override
        protected void onSave(Bundle outState) {
            Timber.v("onSave()");
            super.onSave(outState);
            if (getView() == null) return;
        }

        @Override
        public void onResume() {
            Timber.v("onResume()");
            if (getView() == null) return;
            subscribePlaystate();
            subscribeMeta();
            //playstate will kick off progress subscription
        }

        @Override
        public void onPause() {
            Timber.v("onPause");
            unsubscribeAll();
        }

        @Override
        public void dropView(FooterView view) {
            Timber.v("dropView()");
            super.dropView(view);
            unsubscribeAll();
        }

        void setTrackName(String s) {
            FooterView v = getView();
            if (v == null) return;
            v.trackTitle.setText(s);
        }

        void setArtistName(String s) {
            FooterView v = getView();
            if (v == null) return;
            v.artistName.setText(s);
        }

        void setProgress(int progress) {
            FooterView v = getView();
            if (v == null) return;
            v.progressBar.setProgress(progress);
        }

        void setupObserables() {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(MusicPlaybackService.PLAYSTATE_CHANGED);
            intentFilter.addAction(MusicPlaybackService.META_CHANGED);
            OperatorBroadcastRegister obr = new OperatorBroadcastRegister(getView().getContext(), intentFilter, null, null);
            // we want to use the same thread for all operations
            Scheduler scheduler = Schedulers.computation();
            // obr will call onNext on the main thread so we observeOn computation
            // so our chained operators will be called on computation instead of main.
            Observable<Intent> intentObservable = Observable.create(obr).observeOn(scheduler);
            playStateObservable = intentObservable
                    // Filter for only PLAYSTATE_CHANGED actions
                    .filter(new Func1<Intent, Boolean>() {
                        // called on computation
                        @Override
                        public Boolean call(Intent intent) {
                            Timber.v("playstateSubscripion filter called on %s", Thread.currentThread().getName());
                            return intent.getAction() != null && intent.getAction().equals(MusicPlaybackService.PLAYSTATE_CHANGED);
                        }
                    })
                    // filter out repeats only taking most recent
                    .debounce(20, TimeUnit.MILLISECONDS, scheduler)
                    // flatMap the intent into a boolean by requesting the playstate
                    // XXX the intent contains the playstate as an extra but
                    //     it could be out of date
                    .flatMap(new Func1<Intent, Observable<Boolean>>() {
                        @Override
                        public Observable<Boolean> call(Intent intent) {
                            Timber.v("playstateSubscription flatMap called on %s", Thread.currentThread().getName());
                            return musicService.isPlaying();
                        }
                    })
                    // observe final result on main thread
                    .observeOn(AndroidSchedulers.mainThread());
            metaObservable = intentObservable
                    // filter only the META_CHANGED actions
                    .filter(new Func1<Intent, Boolean>() {
                        // will be called on computation
                        @Override
                        public Boolean call(Intent intent) {
                            Timber.v("metaObservable(filter) %s", Thread.currentThread().getName());
                            return intent.getAction() != null && intent.getAction().equals(MusicPlaybackService.META_CHANGED);
                        }
                    })
                    // buffer quick successive calls and only emit the most recent
                    .debounce(20, TimeUnit.MILLISECONDS, scheduler)
                    // flatmap the intent into a String[] containing the trackname and artistname
                    // XXX these are included in the intent as extras but could be out of date
                    .flatMap(new Func1<Intent, Observable<String[]>>() {
                        // called on computation
                        @Override
                        public Observable<String[]> call(Intent intent) {
                            Timber.v("metaObservable(flatMap) %s", Thread.currentThread().getName());
                            return Observable.zip(musicService.getTrackName(), musicService.getArtistName(), new Func2<String, String, String[]>() {
                                // getTrackName and getArtistName will emit their values on IO threads so this gets called on an IO thread
                                // as a side note the getTrackName and getArtistName operate in parallel here
                                @Override
                                public String[] call(String trackName, String artistName) {
                                    Timber.v("metaObservable(zip) called on %s", Thread.currentThread().getName());
                                    return new String[]{trackName, artistName};
                                }
                            });
                        }
                    })
                    // we want the final value to come in on the main thread
                    .observeOn(AndroidSchedulers.mainThread());
            currentPositionObservable =
                    Observable.zip(musicService.getPosition(), musicService.getDuration(), new Func2<Long, Long, Long>() {
                        @Override
                        public Long call(Long position, Long duration) {
                            Timber.d("currentPositionObservable(zip) %s", Thread.currentThread().getName());
                            if (position > 0 && duration > 0) {
                                return (1000 * position / duration);
                            } else {
                                return (long) 1000;
                            }
                        }
                    });
            progressObservable =
                    // construct an Observable than repeats every .5s,
                    Observable.interval(1000, TimeUnit.MILLISECONDS, Schedulers.computation())
                    // we then fetch the progress as a percentage,
                    .flatMap(new Func1<Long, Observable<Long>>() {
                        @Override
                        public Observable<Long> call(Long aLong) {
                            Timber.d("progressObservable(flatMap) %s", Thread.currentThread().getName());
                            return currentPositionObservable;
                        }
                    })
                    // we want the final result on the ui thread
                    .observeOn(AndroidSchedulers.mainThread());
        }

        void setupObservers() {
            metaObserver = Observers.create(new Action1<String[]>() {
                @Override
                public void call(String[] strings) {
                    Timber.v("metaObserver(result) %s", Thread.currentThread().getName());
                    if (strings.length == 2) {
                        setTrackName(strings[0]);
                        setArtistName(strings[1]);
                    }
                }
            });
            playStateObserver = Observers.create(new Action1<Boolean>() {
                @Override
                public void call(Boolean playing) {
                    Timber.v("playStateObserver(result) %s", Thread.currentThread().getName());
                    if (playing) {
                        subscribeProgress();
                    } else {
                        unsubscribeProgress();
                        // update the current position
                        currentPositionObservable.observeOn(AndroidSchedulers.mainThread()).subscribe(progressObserver);
                    }
                }
            });
            progressObserver = Observers.create(new Action1<Long>() {
                @Override
                public void call(Long progress) {
                    Timber.d("progressObserver(result) %s", Thread.currentThread().getName());
                    setProgress(progress.intValue());
                }
            });
        }

        void subscribePlaystate() {
            if (notSubscribed(progressSubscription)) playStateSubscription = playStateObservable.subscribe(playStateObserver);
        }

        void unsubscribePlaystate() {
            if (notSubscribed(playStateSubscription)) return;
            playStateSubscription.unsubscribe();
            playStateSubscription = null;
        }

        void subscribeMeta() {
            if (notSubscribed(metaSubscription)) metaSubscription = metaObservable.subscribe(metaObserver);
        }

        void unsubscribeMeta() {
            if (notSubscribed(metaSubscription)) return;
            metaSubscription.unsubscribe();
            metaSubscription = null;
        }

        void subscribeProgress() {
            if (notSubscribed(progressSubscription)) progressSubscription = progressObservable.subscribe(progressObserver);
        }

        void unsubscribeProgress() {
            if (notSubscribed(progressSubscription)) return;
            progressSubscription.unsubscribe();
            progressSubscription = null;
        }

        void unsubscribeAll() {
            unsubscribePlaystate();
            unsubscribeMeta();
            unsubscribeProgress();
        }

        static boolean notSubscribed(Subscription subscription) {
            return subscription == null || subscription.isUnsubscribed();
        }

    }
}
