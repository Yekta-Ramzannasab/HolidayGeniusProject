package models;

import java.io.IOException;
import java.nio.file.*;

public class Song {
    private String title;
    private String artist;
    private String album;
    private String lyrics;

    public Song(String title, String artist, String album, String lyrics) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.lyrics = lyrics;
    }

    public static Song fromFile(Path filePath, String artistName, String albumName) throws IOException {
        String title = filePath.getFileName().toString().replace(".txt", "");
        String lyrics = Files.readString(filePath);
        return new Song(title, artistName, albumName, lyrics);
    }

    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getAlbum() { return album; }
    public String getLyrics() { return lyrics; }
}