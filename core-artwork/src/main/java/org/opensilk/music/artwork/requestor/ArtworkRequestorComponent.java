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

package org.opensilk.music.artwork.requestor;

import org.opensilk.common.core.dagger2.AppContextModule;
import org.opensilk.common.core.gson.GsonComponentStub;
import org.opensilk.music.artwork.shared.ArtworkComponentCommon;
import org.opensilk.music.artwork.shared.GsonModule;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Extend this as part of root component
 *
 * Annotations for documentation and build purposes, dont use directly
 *
 * Created by drew on 5/1/15.
 */
@Singleton
@Component(
        modules = {
                ArtworkRequestorModule.class,
                GsonModule.class,
                AppContextModule.class
        }
)
public interface ArtworkRequestorComponent extends ArtworkComponentCommon, GsonComponentStub {
    ArtworkRequestManager artworkRequestManager();
}
