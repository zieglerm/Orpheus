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

import com.andrew.apollo.R;

import mortar.Blueprint;

/**
 * Created by drew on 10/3/14.
 */
public enum Page {
    PLAYLIST(AlbumScreen.class, R.string.page_playlists),
    //    RECENT(R.string.page_recent),
    ARTIST(AlbumScreen.class, R.string.page_artists),
    ALBUM(AlbumScreen.class, R.string.page_albums),
    SONG(AlbumScreen.class, R.string.page_songs),
    GENRE(AlbumScreen.class, R.string.page_genres);

    public final Class<? extends Blueprint> clazz;
    public final int titleResource;

    private Page(Class<? extends Blueprint> clazz, int titleResource) {
        this.clazz = clazz;
        this.titleResource = titleResource;
    }
}
