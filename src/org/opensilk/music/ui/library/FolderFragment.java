/*
 * Copyright (C) 2014 OpenSilk Productions LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opensilk.music.ui.library;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;

import com.andrew.apollo.R;
import com.andrew.apollo.meta.LibraryInfo;

import org.opensilk.music.ui.home.CardListGridFragment;
import org.opensilk.music.ui.library.adapter.FolderGridArrayAdapter;
import org.opensilk.music.ui.library.adapter.FolderListArrayAdapter;
import org.opensilk.music.ui.library.adapter.LibraryAdapter;
import org.opensilk.music.ui.modules.DrawerHelper;
import org.opensilk.silkdagger.DaggerInjector;
import org.opensilk.silkdagger.qualifier.ForActivity;
import org.opensilk.silkdagger.qualifier.ForFragment;

import javax.inject.Inject;

/**
 * Created by drew on 7/2/14.
 */
public class FolderFragment extends CardListGridFragment implements LibraryAdapter.Callback {

    @Inject @ForActivity
    protected DrawerHelper mDrawerHelper;
    @Inject @ForFragment
    protected RemoteLibraryHelper mLibrary;

    protected LibraryInfo mLibraryInfo;
    protected LibraryAdapter mAdapter;

    public static FolderFragment newInstance(LibraryInfo libraryInfo) {
        FolderFragment f = new FolderFragment();
        Bundle b = new Bundle(1);
        b.putParcelable(LibraryFragment.ARG_LIBRARY_INFO, libraryInfo);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((DaggerInjector) getParentFragment()).inject(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLibraryInfo = getArguments().getParcelable(LibraryFragment.ARG_LIBRARY_INFO);

        mAdapter = createAdapter();
        if (savedInstanceState != null) {
            mAdapter.restoreInstanceState(savedInstanceState);
        } else {
            mAdapter.startLoad();
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setListAdapter((ArrayAdapter) mAdapter);
        if (mAdapter.isOnFirstLoad()) {
            setListShown(false);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (!mDrawerHelper.isDrawerOpen()) {
            inflater.inflate(R.menu.refresh, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                mAdapter.startLoad();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mAdapter.saveInstanceState(outState);
    }

    protected LibraryAdapter createAdapter() {
        if (wantGridView()) {
            return new FolderGridArrayAdapter(getActivity(), mLibrary, mLibraryInfo, this, (DaggerInjector)getParentFragment());
        } else {
            return new FolderListArrayAdapter(getActivity(), mLibrary, mLibraryInfo, this, (DaggerInjector)getParentFragment());
        }
    }

    public boolean wantGridView() {
        return false; //TODO
    }

    /*
     * AbsLibraryArrayAdapter.LoaderCallback
     */

    @Override
    //@DebugLog
    public void onFirstLoadComplete() {
        setListShown(true);
    }

    /*
     * Abstract methods
     */

    @Override
    public int getListViewLayout() {
        return wantGridView() ? R.layout.card_gridview : R.layout.card_listview;
    }

    @Override
    public int getEmptyViewLayout() {
        return R.layout.list_empty_view;
    }

}