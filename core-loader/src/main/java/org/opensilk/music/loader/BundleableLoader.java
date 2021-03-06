/*
 * Copyright (c) 2015 OpenSilk Productions LLC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.opensilk.music.loader;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;

import org.opensilk.common.core.dagger2.ForApplication;
import org.opensilk.common.core.dagger2.ScreenScope;
import org.opensilk.common.core.rx.RxLoader;
import org.opensilk.common.core.util.Preconditions;
import org.opensilk.music.library.internal.BundleableListSlice;
import org.opensilk.music.library.internal.IBundleableObserver;
import org.opensilk.music.library.internal.LibraryException;
import org.opensilk.music.model.spi.Bundleable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import timber.log.Timber;

import static org.opensilk.music.library.provider.LibraryMethods.*;

/**
 * Created by drew on 5/2/15.
 */
@ScreenScope
public class BundleableLoader implements RxLoader<Bundleable> {

    class UriObserver extends ContentObserver {
        UriObserver(Handler handler) {
            super(handler);
        }
        @Override
        public void onChange(boolean selfChange) {
            reset();
            for (ContentChangedListener l : contentChangedListeners) {
                l.reload();
            }
        }
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            onChange(selfChange);
        }
    }

    final List<ContentChangedListener> contentChangedListeners;
    final Context context;
    final Uri uri;

    String sortOrder;

    Scheduler observeOnScheduler = AndroidSchedulers.mainThread();

    private UriObserver uriObserver;
    Observable<List<Bundleable>> cachedObservable;

    @Inject
    public BundleableLoader(
            @ForApplication Context context,
            @Named("loader_uri") Uri uri,
            @Named("loader_sortorder") String sortOrder
    ) {
        this.context = context;
        this.uri = uri;
        this.sortOrder = sortOrder;
        contentChangedListeners = new ArrayList<>();
    }

    public Observable<List<Bundleable>> getListObservable() {
        registerContentObserver();
        if (cachedObservable == null) {
            cachedObservable = createObservable()
                    .doOnError(new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            reset();
                            dump(throwable);
                        }
                    })
                    .onErrorResumeNext(Observable.<List<Bundleable>>empty())
                    .observeOn(observeOnScheduler)
                    .cache();
        }
        return cachedObservable;
    }

    public Observable<Bundleable> getObservable() {
        return getListObservable().flatMap(new Func1<List<Bundleable>, Observable<Bundleable>>() {
            @Override
            public Observable<Bundleable> call(List<Bundleable> bundleables) {
                return Observable.from(bundleables);
            }
        });
    }

    public Observable<List<Bundleable>> createObservable() {
        return Observable.create(new Observable.OnSubscribe<List<Bundleable>>() {
            @Override
            public void call(final Subscriber<? super List<Bundleable>> subscriber) {
                final IBundleableObserver o = new IBundleableObserver.Stub() {
                    @Override
                    public void onNext(BundleableListSlice slice) throws RemoteException {
                        List<Bundleable> list = new ArrayList<>(slice.getList());
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onNext(list);
                        }
                    }

                    @Override
                    public void onError(LibraryException e) throws RemoteException {
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onError(e);
                        }
                    }

                    @Override
                    public void onCompleted() throws RemoteException {
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onCompleted();
                        }
                    }
                };

                final Bundle extras = new Bundle();
                putBinderInBundle(extras, o.asBinder());
                extras.putParcelable(Extras.URI, uri);
                extras.putString(Extras.SORTORDER, sortOrder);

                Bundle ok = context.getContentResolver().call(uri, QUERY, null, extras);
                if (!ok.getBoolean(Extras.OK)) {
                    ok.setClassLoader(getClass().getClassLoader());
                    subscriber.onError(readCause(ok));
                }
            }
        });
    }

    public void reset() {
        cachedObservable = null;
    }

    protected void registerContentObserver() {
        if (uriObserver == null) {
            uriObserver = new UriObserver(new Handler(Looper.getMainLooper()));
            context.getContentResolver().registerContentObserver(uri, true, uriObserver);
        }
    }

    public void addContentChangedListener(ContentChangedListener l) {
        contentChangedListeners.add(l);
    }

    public void removeContentChangedListener(ContentChangedListener l) {
        contentChangedListeners.remove(l);
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    public void setObserveOnScheduler(Scheduler scheduler) {
        this.observeOnScheduler = Preconditions.checkNotNull(scheduler, "Scheduler must not be null");
    }

    protected void emmitError(Throwable t, Subscriber<? super Bundleable> subscriber) {
        if (subscriber.isUnsubscribed()) return;
        subscriber.onError(t);
        dump(t);
    }

    protected void dump(Throwable throwable) {
        Timber.e(throwable, "BundleableLoader(\nuri=%s\nsortOrder=%s\n) ex=",
                uri, sortOrder);
    }

    LibraryException readCause(Bundle ok) {
        Bundle b = ok.getBundle(Extras.CAUSE);
        b.setClassLoader(getClass().getClassLoader());
        return b.getParcelable(Extras.CAUSE);
    }

    Method _putIBinder = null;
    void putBinderInBundle(Bundle b, IBinder binder) {
        if (Build.VERSION.SDK_INT >= 18) {
            b.putBinder(Extras.CALLBACK, binder);
        } else {
            try {
                synchronized (this) {
                    if (_putIBinder == null) {
                        _putIBinder = Bundle.class.getDeclaredMethod("putIBinder", String.class, IBinder.class);
                    }
                    _putIBinder.invoke(b, Extras.CALLBACK, binder);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

}
