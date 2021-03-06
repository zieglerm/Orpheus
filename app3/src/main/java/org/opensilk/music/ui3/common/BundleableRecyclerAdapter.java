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

package org.opensilk.music.ui3.common;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;


import org.opensilk.common.core.rx.SimpleObserver;
import org.opensilk.common.ui.recycler.RecyclerListAdapter;
import org.opensilk.common.ui.widget.AnimatedImageView;
import org.opensilk.common.ui.widget.LetterTileDrawable;
import org.opensilk.music.R;
import org.opensilk.music.artwork.PaletteObserver;
import org.opensilk.music.artwork.requestor.ArtworkRequestManager;
import org.opensilk.music.model.Album;
import org.opensilk.music.model.ArtInfo;
import org.opensilk.music.model.Artist;
import org.opensilk.music.model.Folder;
import org.opensilk.music.model.Genre;
import org.opensilk.music.model.Playlist;
import org.opensilk.music.model.Track;
import org.opensilk.music.artwork.ArtworkType;
import org.opensilk.music.model.spi.Bundleable;
import org.opensilk.music.widgets.GridTileDescription;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;
import rx.Observer;
import rx.Subscription;
import rx.android.events.OnClickEvent;
import rx.android.observables.ViewObservable;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by drew on 10/20/14.
 */
public class BundleableRecyclerAdapter extends RecyclerListAdapter<Bundleable, BundleableRecyclerAdapter.ViewHolder> {

    final BundleablePresenter presenter;

    boolean gridStyle;

    final Map<View, SubCont> itemClickSubscriptions = new WeakHashMap<>();
    final Map<View, SubCont> overflowClickSubscriptions = new WeakHashMap<>();

    @Inject
    public BundleableRecyclerAdapter(
            BundleablePresenter presenter
    ) {
        this.presenter = presenter;
        setHasStableIds(true);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(inflate(parent, viewType));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        final Bundleable b = getItem(position);
        if (b instanceof Album) {
            bindAlbum(viewHolder, (Album) b);
        } else if (b instanceof Artist) {
            bindArtist(viewHolder, (Artist)b);
        } else if (b instanceof Folder) {
            bindFolder(viewHolder, (Folder) b);
        } else if (b instanceof Genre) {
            bindGenre(viewHolder, (Genre) b);
        } else if (b instanceof Playlist) {
            bindPlaylist(viewHolder, (Playlist)b);
        } else if (b instanceof Track) {
            bindTrack(viewHolder, (Track) b);
        } else {
            Timber.e("Somehow an invalid Bundleable slipped through.");
        }
        itemClickSubscriptions.put(viewHolder.itemView,
                SubCont.ni(position, ViewObservable.clicks(viewHolder.itemView).subscribe(itemClickObserver)));
        overflowClickSubscriptions.put(viewHolder.overflow,
                SubCont.ni(position, ViewObservable.clicks(viewHolder.overflow).subscribe(overflowClickObserver)));
    }

    void bindAlbum(ViewHolder holder, Album album) {
        ArtInfo artInfo = UtilsCommon.makeBestfitArtInfo(album.artistName, null, album.name, album.artworkUri);
        holder.title.setText(album.name);
        holder.subtitle.setText(album.artistName);
        if (artInfo == ArtInfo.NULLINSTANCE) {
            setLetterTileDrawable(holder, album.name);
        } else {
            PaletteObserver paletteObserver = holder.descriptionContainer != null
                    ? holder.descriptionContainer.getPaletteObserver() : null;
            holder.subscriptions.add(presenter.getRequestor().newRequest(holder.artwork,
                    paletteObserver, artInfo, ArtworkType.THUMBNAIL));
        }
    }

    void bindArtist(ViewHolder holder, Artist artist) {
        ArtInfo artInfo = ArtInfo.forArtist(artist.name, null);
        holder.title.setText(artist.name);
        Context context = holder.itemView.getContext();
        String subtitle = "";
        if (artist.albumCount > 0) {
            subtitle += UtilsCommon.makeLabel(context, R.plurals.Nalbums, artist.albumCount);
        }
        if (artist.trackCount > 0) {
            if (!TextUtils.isEmpty(subtitle)) subtitle += ", ";
            subtitle += UtilsCommon.makeLabel(context, R.plurals.Nsongs, artist.trackCount);
        }
        holder.subtitle.setText(subtitle);
        if (artInfo == ArtInfo.NULLINSTANCE) {
            setLetterTileDrawable(holder, artist.name);
        } else {
            PaletteObserver paletteObserver = holder.descriptionContainer != null
                    ? holder.descriptionContainer.getPaletteObserver() : null;
            holder.subscriptions.add(presenter.getRequestor().newRequest(holder.artwork,
                    paletteObserver, artInfo, ArtworkType.THUMBNAIL));
        }
    }

    void bindFolder(ViewHolder holder, Folder folder) {
        holder.title.setText(folder.name);
        Context context = holder.itemView.getContext();
        if (folder.childCount > 0) {
            holder.subtitle.setText(UtilsCommon.makeLabel(context, R.plurals.Nitems, folder.childCount));
        } else {
            holder.subtitle.setText(" ");
        }
        if (holder.extraInfo != null) {
            holder.extraInfo.setText(folder.date);
            holder.extraInfo.setVisibility(View.VISIBLE);
        }
        setLetterTileDrawable(holder, folder.name);
    }

    void bindGenre(ViewHolder holder, Genre genre) {
        holder.title.setText(genre.name);
        Context context = holder.itemView.getContext();
        String l2 = UtilsCommon.makeLabel(context, R.plurals.Nalbums, genre.albumUris.size())
                + ", " + UtilsCommon.makeLabel(context, R.plurals.Nsongs, genre.trackUris.size());
        holder.subtitle.setText(l2);
        if (gridStyle && (genre.albumUris.size() > 0 || genre.artInfos.size() > 0)) {
            loadMultiArtwork(holder, genre.artInfos);
        } else {
            setLetterTileDrawable(holder, genre.name);
        }
    }

    void bindPlaylist(ViewHolder holder, Playlist playlist) {
        holder.title.setText(playlist.name);
        Context context = holder.itemView.getContext();
        holder.subtitle.setText(UtilsCommon.makeLabel(context, R.plurals.Nsongs, playlist.trackUris.size()));
        if (gridStyle && (playlist.artInfos.size() > 0)) {
            loadMultiArtwork(holder, playlist.artInfos);
        } else {
            setLetterTileDrawable(holder, playlist.name);
        }
    }

    void bindTrack(ViewHolder holder, Track track) {
        ArtInfo artInfo = UtilsCommon.makeBestfitArtInfo(track.albumArtistName, track.artistName, track.albumName, track.artworkUri);
        holder.title.setText(track.name);
        holder.subtitle.setText(track.artistName);
        if (holder.extraInfo != null && track.duration > 0) {
            holder.extraInfo.setText(UtilsCommon.makeTimeString(holder.itemView.getContext(), track.duration));
            holder.extraInfo.setVisibility(View.VISIBLE);
        }
        if (artInfo == ArtInfo.NULLINSTANCE) {
            setLetterTileDrawable(holder, track.name);
        } else {
            holder.subscriptions.add(presenter.getRequestor().newRequest(holder.artwork,
                    null, artInfo, ArtworkType.THUMBNAIL));
        }
    }

    void setLetterTileDrawable(ViewHolder holder, String text) {
        Resources resources = holder.itemView.getResources();
        LetterTileDrawable drawable = LetterTileDrawable.fromText(resources, text);
        holder.artwork.setImageDrawable(drawable);
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        SubCont c = itemClickSubscriptions.remove(holder.itemView);
        if (c != null) {
            c.sub.unsubscribe();
        }
        c = overflowClickSubscriptions.remove(holder.overflow);
        if (c != null) {
            c.sub.unsubscribe();
        }
        holder.reset();
    }

    @Override
    public int getItemViewType(int position) {
        if (!gridStyle) {
            return R.layout.gallery_list_item_artwork;
        } else if (wantsMultiArtwork(position)) {
            return R.layout.gallery_grid_item_artwork4;
        } else {
            return R.layout.gallery_grid_item_artwork;
        }
    }

    public void setGridStyle(boolean gridStyle) {
        this.gridStyle = gridStyle;
    }

    protected boolean wantsMultiArtwork(int position) {
        Bundleable item = getItem(position);
        if (item instanceof Genre) {
            Genre g = (Genre) item;
            return g.artInfos.size() > 1 || g.albumUris.size() > 1;
        } else if (item instanceof Playlist) {
            return ((Playlist) item).artInfos.size() > 1;
        } else {
            return false;
        }
    }

    void loadMultiArtwork(ViewHolder holder, List<ArtInfo> artInfos) {
        ArtworkRequestManager requestor = presenter.getRequestor();
        CompositeSubscription cs = holder.subscriptions;
        AnimatedImageView artwork = holder.artwork;
        AnimatedImageView artwork2 = holder.artwork2;
        AnimatedImageView artwork3 = holder.artwork3;
        AnimatedImageView artwork4 = holder.artwork4;
        ArtworkType artworkType = ArtworkType.THUMBNAIL;
        UtilsCommon.loadMultiArtwork(requestor, cs, artwork, artwork2, artwork3, artwork4, artInfos, artworkType);
    }

    final Observer<OnClickEvent> itemClickObserver = new SimpleObserver<OnClickEvent>() {
        @Override
        public void onNext(OnClickEvent onClickEvent) {
            SubCont c = itemClickSubscriptions.get(onClickEvent.view);
            if (c != null) {
                Context context = onClickEvent.view.getContext();
                presenter.onItemClicked(context, getItem(c.pos));
            }
        }
    };

    final Observer<OnClickEvent> overflowClickObserver = new SimpleObserver<OnClickEvent>() {
        @Override
        public void onNext(OnClickEvent onClickEvent) {
            SubCont c = overflowClickSubscriptions.get(onClickEvent.view);
            if (c != null) {
                final Context context = onClickEvent.view.getContext();
                final Bundleable bundleable = getItem(c.pos);
                PopupMenu m = new PopupMenu(context, onClickEvent.view);
                presenter.onOverflowClicked(context, m, bundleable);
                m.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        try {
                            OverflowAction action = OverflowAction.valueOf(item.getItemId());
                            return presenter.onOverflowActionClicked(context, action, bundleable);
                        } catch (IllegalArgumentException e) {
                            return false;
                        }
                    }
                });
                m.show();
            }
        }
    };

    public static class SubCont {
        final int pos;
        final Subscription sub;

        public SubCont(int pos, Subscription sub) {
            this.pos = pos;
            this.sub = sub;
        }

        public static SubCont ni(int pos, Subscription sub) {
            return new SubCont(pos, sub);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.artwork_thumb) public AnimatedImageView artwork;
        @InjectView(R.id.artwork_thumb2) @Optional public AnimatedImageView artwork2;
        @InjectView(R.id.artwork_thumb3) @Optional public AnimatedImageView artwork3;
        @InjectView(R.id.artwork_thumb4) @Optional public AnimatedImageView artwork4;
        @InjectView(R.id.grid_description) @Optional GridTileDescription descriptionContainer;
        @InjectView(R.id.tile_title) TextView title;
        @InjectView(R.id.tile_subtitle) TextView subtitle;
        @InjectView(R.id.tile_info) @Optional TextView extraInfo;
        @InjectView(R.id.tile_overflow) ImageButton overflow;

        final CompositeSubscription subscriptions;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
            subscriptions = new CompositeSubscription();
        }

        public void reset() {
//            Timber.v("Reset title=%s", title.getText());
            subscriptions.clear();
            if (artwork != null) artwork.setImageBitmap(null);
            if (artwork2 != null) artwork2.setImageBitmap(null);
            if (artwork3 != null) artwork3.setImageBitmap(null);
            if (artwork4 != null) artwork4.setImageBitmap(null);
            if (descriptionContainer != null) descriptionContainer.resetBackground();
            if (extraInfo != null && extraInfo.getVisibility() != View.GONE) extraInfo.setVisibility(View.GONE);
        }
    }

}
