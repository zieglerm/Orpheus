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

package org.opensilk.music.library.provider;

/**
 * Method parameters sent to the call
 *
 * Created by drew on 5/3/15.
 */
public interface LibraryMethods {
    /**
     * Request plugin config
     * returned bundle from the call is the dematerialized {@link org.opensilk.music.library.LibraryConfig}
     */
    String LIBRARYCONF = "conf";
    /**
     * Query the library
     */
    String QUERY = "query";

    /**
     * Bundle keys for the extras bundle for the call
     */
    interface Extras {
        /**
         * Request uri: always built with {@link LibraryUris}, never null
         */
        String URI = "uri";
        /**
         * Sortorder: one of the strings in the sort package. never null for {@link #QUERY}
         */
        String SORTORDER = "sortorder";
        /**
         * Internal use: {@link org.opensilk.music.library.IBundleableObserver}, never null
         */
        String CALLBACK = "callback";

        /**
         * Internal use: argument in returned bundle if not true. {@link #CAUSE} must be set
         */
        String OK = "ok";
        /**
         * Internal use: argument in returned bundle containing {@link org.opensilk.music.library.internal.LibraryException}
         * when {@link #OK} is false.
         */
        String CAUSE = "cause";
    }
}
