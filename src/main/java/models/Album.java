package models;

import java.util.ArrayList;
import java.util.List;

public class Album {
    private String name;
    private String artist;
    private List<Song> songs;

    public Album(String name, String artist) {
        this.name = name;
        this.artist = artist;
        this.songs = new ArrayList<>();
    }

    public void addSong(Song song) {
        songs.add(song);
    }

    public String getName() { return name; }
    public String getArtist() { return artist; }
    public List<Song> getSongs() { return songs; }
}