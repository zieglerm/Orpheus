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

package org.opensilk.music.playback.service;

import android.content.Context;

import org.opensilk.common.core.dagger2.AppContextModule;
import org.opensilk.music.artwork.service.ArtworkProviderHelperModule;
import org.opensilk.music.playback.SystemServicesModule;

import javax.inject.Singleton;

import dagger.Component;
import rx.functions.Func1;

/**
 * Created by drew on 5/6/15.
 */
@Singleton
@Component(
        modules = {
                AppContextModule.class,
                PlaybackServiceModule.class,
                ArtworkProviderHelperModule.class,
                SystemServicesModule.class,
        }
)
public interface PlaybackServiceComponent {
    Func1<Context, PlaybackServiceComponent> FACTORY =
            new Func1<Context, PlaybackServiceComponent>() {
                @Override
                public PlaybackServiceComponent call(Context context) {
                    return DaggerPlaybackServiceComponent.builder()
                            .appContextModule(new AppContextModule(context))
                            .build();
                }
            };
    void inject(PlaybackService service);
}
