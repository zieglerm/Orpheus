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

package org.opensilk.music.ui3.common;

import org.opensilk.common.ui.mortar.Screen;
import org.opensilk.music.library.LibraryConfig;
import org.opensilk.music.library.LibraryInfo;

/**
 * Created by drew on 5/5/15.
 */
public class BundleableScreen extends Screen {

    public final LibraryConfig libraryConfig;
    public final LibraryInfo libraryInfo;

    public BundleableScreen(LibraryConfig libraryConfig, LibraryInfo libraryInfo) {
        this.libraryConfig = libraryConfig;
        this.libraryInfo = libraryInfo;
    }

    @Override
    public String getName() {
        return getClass().getSimpleName() + "-" + libraryConfig.authority;
    }
}
