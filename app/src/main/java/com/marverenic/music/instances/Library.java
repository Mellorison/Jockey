package com.marverenic.music.instances;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

public class Library {
    private static ArrayList<Song> songLib = new ArrayList<>();
    private static ArrayList<Album> albumLib = new ArrayList<>();
    private static ArrayList<Artist> artistLib = new ArrayList<>();
    private static ArrayList<Playlist> playlistLib = new ArrayList<>();
    private static ArrayList<Genre> genreLib = new ArrayList<>();

    public static void add(Song s) {
        songLib.add(s);
    }

    public static void add(Album a) {
        albumLib.add(a);
    }

    public static void add(Artist a) {
        artistLib.add(a);
    }

    public static void add(Playlist p) {
        playlistLib.add(p);
    }

    public static void add(Genre g) {
        genreLib.add(g);
    }

    public static ArrayList<Song> getSongs() {
        Comparator<Song> songComparator = new Comparator<Song>() {
            @Override
            public int compare(Song o1, Song o2) {
                String o1c = o1.songName.toLowerCase(Locale.ENGLISH);
                String o2c = o2.songName.toLowerCase(Locale.ENGLISH);
                if (o1c.startsWith("the ")) {
                    o1c = o1c.substring(4);
                } else if (o1c.startsWith("a ")) {
                    o1c = o1c.substring(2);
                }
                if (o2c.startsWith("the ")) {
                    o2c = o2c.substring(4);
                } else if (o2c.startsWith("a ")) {
                    o2c = o2c.substring(2);
                }
                if (!o1c.matches("[a-z]") && o2c.matches("[a-z]")) {
                    return o2c.compareTo(o1c);
                }
                return o1c.compareTo(o2c);
            }
        };
        Collections.sort(songLib, songComparator);
        return songLib;
    }

    public static ArrayList<Album> getAlbums() {
        Comparator<Album> songComparator = new Comparator<Album>() {
            @Override
            public int compare(Album o1, Album o2) {
                String o1c = o1.albumName.toLowerCase(Locale.ENGLISH);
                String o2c = o2.albumName.toLowerCase(Locale.ENGLISH);
                if (o1c.startsWith("the ")) {
                    o1c = o1c.substring(4);
                } else if (o1c.startsWith("a ")) {
                    o1c = o1c.substring(2);
                }
                if (o2c.startsWith("the ")) {
                    o2c = o2c.substring(4);
                } else if (o2c.startsWith("a ")) {
                    o2c = o2c.substring(2);
                }
                if (!o1c.matches("[a-z]") && o2c.matches("[a-z]")) {
                    return o2c.compareTo(o1c);
                }
                return o1c.compareTo(o2c);
            }
        };
        Collections.sort(albumLib, songComparator);
        return albumLib;
    }

    public static ArrayList<Artist> getArtists() {
        Comparator<Artist> songComparator = new Comparator<Artist>() {
            @Override
            public int compare(Artist o1, Artist o2) {
                String o1c = o1.artistName.toLowerCase(Locale.ENGLISH);
                String o2c = o2.artistName.toLowerCase(Locale.ENGLISH);
                if (!o1c.matches("[a-z]") && o2c.matches("[a-z]")) {
                    return o2c.compareTo(o1c);
                }
                return o1c.compareTo(o2c);
            }
        };
        Collections.sort(artistLib, songComparator);
        return artistLib;
    }

    public static ArrayList<Playlist> getPlaylists() {
        Comparator<Playlist> songComparator = new Comparator<Playlist>() {
            @Override
            public int compare(Playlist o1, Playlist o2) {
                String o1c = o1.playlistName.toLowerCase(Locale.ENGLISH);
                String o2c = o2.playlistName.toLowerCase(Locale.ENGLISH);
                if (!o1c.matches("[a-z]") && o2c.matches("[a-z]")) {
                    return o2c.compareTo(o1c);
                }
                return o1c.compareTo(o2c);
            }
        };
        Collections.sort(playlistLib, songComparator);
        return playlistLib;
    }

    public static ArrayList<Genre> getGenres() {
        Comparator<Genre> songComparator = new Comparator<Genre>() {
            @Override
            public int compare(Genre o1, Genre o2) {
                String o1c = o1.genreName.toLowerCase(Locale.ENGLISH);
                String o2c = o2.genreName.toLowerCase(Locale.ENGLISH);
                if (!o1c.matches("[a-z]") && o2c.matches("[a-z]")) {
                    return o2c.compareTo(o1c);
                }
                return o1c.compareTo(o2c);
            }
        };
        Collections.sort(genreLib, songComparator);
        return genreLib;
    }

    public static void resetAll() {
        songLib = new ArrayList<>();
        albumLib = new ArrayList<>();
        artistLib = new ArrayList<>();
        playlistLib = new ArrayList<>();
        genreLib = new ArrayList<>();
    }

    public static boolean isEmpty() {
        return (songLib == null || albumLib == null || artistLib == null || playlistLib == null || genreLib == null) ||
                (songLib.size() == 0 && albumLib.size() == 0 && artistLib.size() == 0 && playlistLib.size() == 0 && genreLib.size() == 0);
    }
}
