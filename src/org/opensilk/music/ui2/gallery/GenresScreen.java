/*
 * Copyright (c) 2014 OpenSilk Productions LLC
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

package org.opensilk.music.ui2.gallery;

import android.content.Context;
import android.database.Cursor;

import com.andrew.apollo.model.Genre;
import com.andrew.apollo.provider.MusicProvider;
import com.andrew.apollo.utils.MusicUtils;

import org.opensilk.common.flow.Screen;
import org.opensilk.common.mortar.WithModule;
import org.opensilk.music.AppPreferences;
import org.opensilk.music.R;
import org.opensilk.music.api.meta.ArtInfo;
import org.opensilk.music.artwork.ArtworkRequestManager;
import org.opensilk.music.artwork.ArtworkType;
import org.opensilk.music.ui2.core.android.ActionBarOwner;
import org.opensilk.music.ui2.loader.AlbumArtInfoLoader;
import org.opensilk.music.ui2.loader.RxCursorLoader;
import org.opensilk.music.util.CursorHelpers;
import org.opensilk.silkdagger.qualifier.ForApplication;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.functions.Action1;

/**
 * Created by drew on 10/19/14.
 */
@WithModule(GenresScreen.Module.class)
@WithRecyclerViewPresenter(GenresScreen.Presenter.class)
public class GenresScreen extends Screen {

    @dagger.Module(
            addsTo = GalleryScreen.Module.class,
            injects = Presenter.class
    )
    public static class Module {

    }

    @Singleton
    public static class Presenter extends BasePresenter<Genre> {

        final Loader loader;

        @Inject
        public Presenter(AppPreferences preferences, ArtworkRequestManager artworkRequestor, Loader loader) {
            super(preferences, artworkRequestor);
            this.loader = loader;
        }

        @Override
        protected BaseAdapter<Genre> newAdapter(List<Genre> items) {
            return new Adapter(items, artworkRequestor);
        }

        @Override
        protected void load() {
            subscription = loader.getListObservable().subscribe(new Action1<List<Genre>>() {
                @Override
                public void call(List<Genre> genres) {
                    addItems(genres);
                }
            });
        }

        @Override
        protected boolean isStaggered() {
            return true;
        }

        @Override
        public ActionBarOwner.MenuConfig getMenuConfig() {
            return null;
        }

    }

    @Singleton
    public static class Loader extends RxCursorLoader<Genre> {

        @Inject
        public Loader(@ForApplication Context context) {
            super(context);
            setUri(MusicProvider.GENRES_URI);
            // Dont need anything else
        }

        @Override
        protected Genre makeFromCursor(Cursor c) {
            return CursorHelpers.makeGenreFromCursor(c);
        }
    }

    static class Adapter extends BaseAdapter<Genre> {

        Adapter(List<Genre> items, ArtworkRequestManager artworkRequestor) {
            super(items, artworkRequestor);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            Genre genre = getItem(position);
            holder.title.setText(genre.mGenreName);
            Context context = holder.itemView.getContext();
            String l2 = MusicUtils.makeLabel(context, R.plurals.Nalbums, genre.mAlbumNumber)
                    + ", " + MusicUtils.makeLabel(context, R.plurals.Nsongs, genre.mSongNumber);
            holder.subtitle.setText(l2);
            switch (holder.artNumber) {
                case 4:
                    if (genre.mAlbumIds.length >= 4) {
                        holder.subscriptions.add(artworkRequestor.newAlbumRequest(holder.artwork4,
                                genre.mAlbumIds[3], ArtworkType.THUMBNAIL));
                        holder.subscriptions.add(artworkRequestor.newAlbumRequest(holder.artwork3,
                                genre.mAlbumIds[2], ArtworkType.THUMBNAIL));
                    }
                    //fall
                case 2:
                    if (genre.mAlbumIds.length >= 2) {
                        holder.subscriptions.add(artworkRequestor.newAlbumRequest(holder.artwork2,
                                genre.mAlbumIds[1], ArtworkType.THUMBNAIL));
                    }
                    //fall
                case 1:
                    if (genre.mAlbumIds.length >= 1) {
                        holder.subscriptions.add(artworkRequestor.newAlbumRequest(holder.artwork,
                                genre.mAlbumIds[0], ArtworkType.THUMBNAIL));
                    }
            }
        }

        @Override
        protected boolean quadArtwork(int position) {
            return getItem(position).mAlbumIds.length >= 4;
        }

        @Override
        protected boolean dualArtwork(int position) {
            return getItem(position).mAlbumIds.length >= 2;
        }

    }
}