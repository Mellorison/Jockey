package com.marverenic.music.data.store;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.marverenic.music.R;
import com.marverenic.music.instances.AutoPlaylist;
import com.marverenic.music.instances.Playlist;
import com.marverenic.music.instances.Song;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.subjects.BehaviorSubject;

public class LocalPlaylistStore implements PlaylistStore {

    private static final String TAG = "LocalPlaylistStore";
    private static final String AUTO_PLAYLIST_EXTENSION = ".jpl";

    // Used to generate Auto Playlist contents
    private MusicStore mMusicStore;
    private PlayCountStore mPlayCountStore;

    private Context mContext;
    private BehaviorSubject<List<Playlist>> mPlaylists;

    public LocalPlaylistStore(Context context, MusicStore musicStore,
                              PlayCountStore playCountStore) {
        mContext = context;
        mMusicStore = musicStore;
        mPlayCountStore = playCountStore;
    }

    @Override
    public Observable<Boolean> refresh() {
        return MediaStoreUtil.promptPermission(mContext).map(
                granted -> {
                    if (mPlaylists != null) {
                        mPlaylists.onNext(getAllPlaylists());
                    }
                    return granted;
                });
    }

    @Override
    public Observable<List<Playlist>> getPlaylists() {
        if (mPlaylists == null) {
            mPlaylists = BehaviorSubject.create();

            MediaStoreUtil.getPermission(mContext).subscribe(granted -> {
                if (granted) {
                    mPlaylists.onNext(getAllPlaylists());
                } else {
                    mPlaylists.onNext(Collections.emptyList());
                }
            });
        }
        return mPlaylists;
    }

    private List<Playlist> getAllPlaylists() {
        return MediaStoreUtil.getAllPlaylists(mContext);
    }

    @Override
    public Observable<List<Song>> getSongs(Playlist playlist) {
        return Observable.just(MediaStoreUtil.getPlaylistSongs(mContext, playlist));
    }

    @Override
    public Observable<List<Playlist>> searchForPlaylists(String query) {
        return Observable.just(MediaStoreUtil.searchForPlaylists(mContext, query));
    }

    @Override
    public String verifyPlaylistName(String playlistName) {
        if (playlistName == null || playlistName.trim().isEmpty()) {
            return mContext.getString(R.string.error_hint_empty_playlist);
        }

        if (MediaStoreUtil.findPlaylistByName(mContext, playlistName) != null) {
            return mContext.getString(R.string.error_hint_duplicate_playlist);
        }

        return null;
    }

    @Override
    public Playlist makePlaylist(String name) {
        return makePlaylist(name, null);
    }

    @Override
    public AutoPlaylist makePlaylist(AutoPlaylist playlist) {
        Playlist localReference = MediaStoreUtil.createPlaylist(mContext,
                playlist.getPlaylistName(), Collections.emptyList());

        AutoPlaylist created = new AutoPlaylist.Builder(playlist)
                .setId(localReference.getPlaylistId())
                .build();

        saveAutoPlaylistConfiguration(created);

        if (mPlaylists != null && mPlaylists.getValue() != null) {
            List<Playlist> updatedPlaylists = new ArrayList<>(mPlaylists.getValue());
            updatedPlaylists.add(created);
            Collections.sort(updatedPlaylists);

            mPlaylists.onNext(updatedPlaylists);
        }

        return created;
    }

    @Override
    public Playlist makePlaylist(String name, @Nullable List<Song> songs) {
        Playlist created = MediaStoreUtil.createPlaylist(mContext, name, songs);

        if (mPlaylists != null && mPlaylists.getValue() != null) {
            List<Playlist> updated = new ArrayList<>(mPlaylists.getValue());
            updated.add(created);
            Collections.sort(updated);

            mPlaylists.onNext(updated);
        }

        return created;
    }

    @Override
    public void removePlaylist(Playlist playlist) {
        MediaStoreUtil.deletePlaylist(mContext, playlist);

        if (mPlaylists != null && mPlaylists.getValue() != null) {
            List<Playlist> updated = new ArrayList<>(mPlaylists.getValue());
            updated.remove(playlist);

            mPlaylists.onNext(updated);
        }
    }

    @Override
    public void editPlaylist(Playlist playlist, List<Song> newSongs) {
        MediaStoreUtil.editPlaylist(mContext, playlist, newSongs);
    }

    @Override
    public void editPlaylist(AutoPlaylist replacement) {
        saveAutoPlaylistConfiguration(replacement);

        if (mPlaylists != null && mPlaylists.getValue() != null) {
            List<Playlist> updatedPlaylists = new ArrayList<>(mPlaylists.getValue());

            int index = updatedPlaylists.indexOf(replacement);
            updatedPlaylists.set(index, replacement);

            mPlaylists.onNext(updatedPlaylists);
        }
    }

    private void saveAutoPlaylistConfiguration(AutoPlaylist playlist) {
        // Write an initial set of values to the MediaStore so other apps can see this playlist
        playlist.generatePlaylist(mMusicStore, this, mPlayCountStore)
                .take(1)
                .subscribe(contents -> {
                    editPlaylist(playlist, contents);
                }, throwable -> {
                    Log.e(TAG, "makePlaylist: Failed to initialize contents", throwable);
                });

        try {
            writeAutoPlaylistConfiguration(playlist);
        } catch (IOException e) {
            Crashlytics.logException(e);
        }
    }

    private void writeAutoPlaylistConfiguration(AutoPlaylist playlist) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        FileWriter writer = null;

        try {
            String filename = playlist.getPlaylistName() + AUTO_PLAYLIST_EXTENSION;
            String fullPath = mContext.getExternalFilesDir(null) + File.separator + filename;

            writer = new FileWriter(fullPath);
            writer.write(gson.toJson(playlist, AutoPlaylist.class));
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    @Override
    public void addToPlaylist(Playlist playlist, Song song) {
        MediaStoreUtil.appendToPlaylist(mContext, playlist, song);
    }

    @Override
    public void addToPlaylist(Playlist playlist, List<Song> songs) {
        MediaStoreUtil.appendToPlaylist(mContext, playlist, songs);
    }
}
