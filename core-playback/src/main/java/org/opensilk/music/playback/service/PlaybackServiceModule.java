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

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.session.MediaSession;
import android.os.*;
import android.os.Process;

import org.opensilk.common.core.dagger2.AppContextComponent;
import org.opensilk.common.core.dagger2.AppContextModule;
import org.opensilk.common.core.dagger2.ForApplication;
import org.opensilk.music.artwork.service.ArtworkProviderHelperModule;
import org.opensilk.music.playback.NavUtils;
import org.opensilk.music.playback.SystemServicesModule;

import dagger.Module;
import dagger.Provides;

/**
 * Created by drew on 5/6/15.
 */
@Module(
        includes = {
        }
)
public class PlaybackServiceModule {
    @Provides
    public HandlerThread provideServiceHandlerTHread() {
        return new HandlerThread(PlaybackService.NAME, Process.THREAD_PRIORITY_BACKGROUND);
    }
    @Provides
    public MediaSession provideMediaSession(@ForApplication Context context) {
        MediaSession mMediaSession = new MediaSession(context, PlaybackService.NAME);
        mMediaSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS
                | MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
//        mMediaSession.setMediaButtonReceiver(PendingIntent.getBroadcast(
//                this,
//                1,
//                new Intent(this, MediaButtonIntentReceiver.class),
//                PendingIntent.FLAG_UPDATE_CURRENT
//        ));
        mMediaSession.setSessionActivity(PendingIntent.getActivity(context,
                2, NavUtils.makeLauncherIntent(context), PendingIntent.FLAG_UPDATE_CURRENT));
        final ComponentName mediaButtonReceiverComponent
                = new ComponentName(context, MediaButtonIntentReceiver.class);
        final Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON)
                .setComponent(mediaButtonReceiverComponent);
        final PendingIntent mediaButtonReceiverIntent = PendingIntent.getBroadcast(context,
                0, mediaButtonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mMediaSession.setMediaButtonReceiver(mediaButtonReceiverIntent);
        return mMediaSession;
    }
}
