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

package org.opensilk.music.ui3;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.widget.Toolbar;

import org.apache.commons.lang3.ArrayUtils;
import org.opensilk.common.core.mortar.DaggerService;
import org.opensilk.common.ui.mortar.ActionBarConfig;
import org.opensilk.common.ui.mortar.ActionBarMenuConfig;
import org.opensilk.common.ui.mortar.ActionBarOwnerDelegate;
import org.opensilk.music.AppComponent;
import org.opensilk.music.R;
import org.opensilk.music.ui3.profile.ProfileScreen;

import butterknife.ButterKnife;
import butterknife.InjectView;
import mortar.MortarScope;

/**
 * Created by drew on 5/5/15.
 */
public class ProfileActivity extends MusicActivity {

    public static void startSelf(Context context, ProfileScreen screen) {
        Intent i = new Intent(context, ProfileActivity.class)
                .putExtra("screen", screen);
        context.startActivity(i);
    }

    @Override
    protected void onCreateScope(MortarScope.Builder builder) {
        AppComponent appComponent = DaggerService.getDaggerComponent(getApplicationContext());
        builder.withService(DaggerService.DAGGER_SERVICE, ProfileActivityComponent.FACTORY.call(appComponent));
    }

    @Override
    protected void performInjection() {
        ProfileActivityComponent activityComponent = DaggerService.getDaggerComponent(this);
        activityComponent.inject(this);
    }

    @Override
    public int getContainerViewId() {
        return R.id.main;
    }

    @Override
    protected void setupContentView() {
        setContentView(R.layout.activity_profile);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBarConfig config = ActionBarConfig.builder()
                .setUpButtonEnabled(true)
                .setTitle("")
                .setSubtitle("")
                .setTransparentBackground(!getResources().getBoolean(R.bool.in_landscape))
                .build();
        mActionBarOwner.setConfig(config);

        ProfileScreen screen = getIntent().getParcelableExtra("screen");
        mFragmentManagerOwner.replaceMainContent(screen.getFragment(this), false);
    }

}
