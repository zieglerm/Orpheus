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

package org.opensilk.music.plugin.drive.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;
import org.opensilk.common.core.mortar.DaggerService;
import org.opensilk.music.library.LibraryConstants;
import org.opensilk.music.library.LibraryInfo;
import org.opensilk.music.plugin.common.AbsSettingsActivity;
import org.opensilk.music.plugin.common.FolderPickerActivity;
import org.opensilk.music.plugin.common.LibraryPreferences;
import org.opensilk.music.plugin.drive.GlobalComponent;
import org.opensilk.music.plugin.drive.R;
import org.opensilk.music.plugin.drive.provider.DriveLibraryProvider;

import static org.opensilk.music.plugin.common.LibraryPreferences.ROOT_FOLDER;
import static org.opensilk.music.plugin.common.LibraryPreferences.ROOT_FOLDER_NAME;

/**
 * Created by drew on 4/29/15.
 */
public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {

    public static final String CLEAR_CACHE = "clear_cache";
    public static final String LICENSES = "licenses";

    public static SettingsFragment newInstance(String libraryId) {
        SettingsFragment f = new SettingsFragment();
        Bundle b = new Bundle();
        b.putString("__id", libraryId);
        f.setArguments(b);
        return f;
    }

    private String mLibraryId;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
//        DaggerService.<ActivityComponent>getDaggerComponent(activity).inject(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLibraryId = getArguments().getString("__id");
        // Change preferences file per Orpheus api guidelines
        getPreferenceManager().setSharedPreferencesName(LibraryPreferences.posixSafe(mLibraryId));
        addPreferencesFromResource(R.xml.settings);

        // default browse folder
        findPreference(ROOT_FOLDER).setOnPreferenceClickListener(this);
        String rootFolderTitle = getPreferenceManager().getSharedPreferences().getString(ROOT_FOLDER_NAME, null);
        if (!StringUtils.isEmpty(rootFolderTitle)) {
            findPreference(ROOT_FOLDER).setSummary(rootFolderTitle);
        }

        // clear cache
        findPreference(CLEAR_CACHE).setOnPreferenceClickListener(this);

        // licenses dialog
        findPreference(LICENSES).setOnPreferenceClickListener(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 44) {
            if (resultCode == Activity.RESULT_OK) {
                LibraryInfo libraryInfo = data.getParcelableExtra(LibraryConstants.EXTRA_LIBRARY_INFO);
                if (libraryInfo != null && !StringUtils.isEmpty(libraryInfo.folderId)) {
                    getPreferenceManager().getSharedPreferences().edit()
                            .putString(ROOT_FOLDER, libraryInfo.folderId)
                            .putString(ROOT_FOLDER_NAME, libraryInfo.folderName).apply();
                    findPreference(ROOT_FOLDER).setSummary(libraryInfo.folderName);
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (findPreference(ROOT_FOLDER) == preference) {
            getPreferenceManager().getSharedPreferences().edit().remove(ROOT_FOLDER).remove(ROOT_FOLDER_NAME).apply();
            findPreference(ROOT_FOLDER).setSummary(null);

            final Intent parentIntent = getActivity().getIntent();
            final String authority = DriveLibraryProvider.AUTHORITY_PFX+
                    DaggerService.<GlobalComponent>getDaggerComponent(getActivity().getApplicationContext()).baseAuthority();
            final LibraryInfo libraryInfo = new LibraryInfo(mLibraryId, null, null, null);

            startActivityForResult(FolderPickerActivity.buildIntent(parentIntent, getActivity(), authority, libraryInfo), 44);
            return true;
        } else if (findPreference(CLEAR_CACHE) == preference) {
            //TODO
            Toast.makeText(getActivity(), R.string.msg_cache_cleared, Toast.LENGTH_SHORT).show();
            return true;
        } else if (findPreference(LICENSES) == preference) {
            AbsSettingsActivity.showLicences(getActivity());
            return true;
        }
        return false;
    }
}
