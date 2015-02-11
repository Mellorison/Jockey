package com.marverenic.music.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.marverenic.music.LibraryPageActivity;
import com.marverenic.music.Player;
import com.marverenic.music.R;
import com.marverenic.music.instances.Genre;
import com.marverenic.music.instances.Library;
import com.marverenic.music.instances.Song;
import com.marverenic.music.utils.Debug;
import com.marverenic.music.utils.Themes;

import java.util.ArrayList;

public class GenreListAdapter extends BaseAdapter implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    private ArrayList<Genre> data;
    private Context context;

    public GenreListAdapter(Context context) {
        super();
        this.data = Library.getGenres();
        this.context = context;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        View v = convertView;
        if (convertView == null) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.instance_genre, parent, false);
        }
        Genre p = data.get(position);

        if (p != null) {
            TextView tt = (TextView) v.findViewById(R.id.textGenreName);
            if (tt != null) {
                tt.setText(p.genreName);
                tt.setTextColor(Themes.getListText());
            }
        } else {
            Debug.log(Debug.WTF, "GenreListAdapter", "The requested entry is null", context);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ((ListView) parent).setSelector(Themes.getTouchRipple(context));
        }

        return v;
    }

    @Override
    public int getCount() {
        if (data != null) {
            return data.size();
        } else return 0;
    }

    @Override
    public Object getItem(int position) {
        if (data != null) {
            return data.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return (long) position;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Genre item = data.get(position);

        Intent intent = new Intent(context, LibraryPageActivity.class);
        intent.putExtra("entry", item);

        context.startActivity(intent);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        final Genre item = data.get(position);
        final ArrayList<Song> contents = new ArrayList<>();

        AlertDialog.Builder dialog = new AlertDialog.Builder(context);

        dialog.setTitle(item.genreName)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // There's nothing to do here
                    }
                })
                .setItems(R.array.queue_options_genre, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0 || which == 1) {
                            Cursor cur = context.getContentResolver().query(
                                    MediaStore.Audio.Genres.Members.getContentUri("external", item.genreId),
                                    new String[]{
                                            MediaStore.Audio.Genres.Members.TITLE,
                                            MediaStore.Audio.Genres.Members.ARTIST,
                                            MediaStore.Audio.Genres.Members.ALBUM,
                                            MediaStore.Audio.Genres.Members.DURATION,
                                            MediaStore.Audio.Genres.Members.DATA,
                                            MediaStore.Audio.Genres.Members.ALBUM_ID},
                                    MediaStore.Audio.Media.IS_MUSIC + " != 0 ", null, null);
                            cur.moveToFirst();

                            for (int i = 0; i < cur.getCount(); i++) {
                                cur.moveToPosition(i);
                                contents.add(new Song(
                                        cur.getString(cur.getColumnIndex(MediaStore.Audio.Playlists.Members.TITLE)),
                                        cur.getString(cur.getColumnIndex(MediaStore.Audio.Playlists.Members.ARTIST)),
                                        cur.getString(cur.getColumnIndex(MediaStore.Audio.Playlists.Members.ALBUM)),
                                        cur.getInt(cur.getColumnIndex(MediaStore.Audio.Playlists.Members.DURATION)),
                                        cur.getString(cur.getColumnIndex(MediaStore.Audio.Playlists.Members.DATA)),
                                        cur.getString(cur.getColumnIndex(MediaStore.Audio.Playlists.Members.ALBUM_ID))));
                            }
                            cur.close();
                        }
                        switch (which) {
                            case 0: //Queue this playlist next
                                Player.getInstance().queueNext(contents);
                                break;
                            case 1: //Queue this playlist last
                                Player.getInstance().queueLast(contents);
                                break;
                            default:
                                break;
                        }
                    }
                });
        dialog.create().show();
        return true;
    }

    public void updateData(ArrayList<Genre> data) {
        this.data = data;
        notifyDataSetChanged();
    }
}