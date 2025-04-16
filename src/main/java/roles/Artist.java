package roles;


import models.Album;
import models.Song;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class Artist {
    private String name;
    private List<Album> albums;
    private List<Song> singles;

    public Artist(String name) {
        this.name = name;
        this.albums = new ArrayList<>();
        this.singles = new ArrayList<>();
    }

    public static Artist loadFromDirectory(Path artistPath) throws IOException {
        Artist artist = new Artist(artistPath.getFileName().toString());

        // Load singles (directly in artist folder)
        try (DirectoryStream<Path> singlesStream = Files.newDirectoryStream(artistPath, "*.txt")) {
            for (Path songFile : singlesStream) {
                artist.addSingle(Song.fromFile(songFile, artist.getName(), null));
            }
        }

        // Load albums (subfolders)
        try (DirectoryStream<Path> albumsStream = Files.newDirectoryStream(artistPath, Files::isDirectory)) {
            for (Path albumPath : albumsStream) {
                Album album = new Album(albumPath.getFileName().toString(), artist.getName());

                try (DirectoryStream<Path> songsStream = Files.newDirectoryStream(albumPath, "*.txt")) {
                    for (Path songFile : songsStream) {
                        album.addSong(Song.fromFile(songFile, artist.getName(), album.getName()));
                    }
                }

                artist.addAlbum(album);
            }
        }

        return artist;
    }

    public void addAlbum(Album album) { albums.add(album); }
    public void addSingle(Song song) { singles.add(song); }
    public String getName() { return name; }
    public List<Album> getAlbums() { return albums; }
    public List<Song> getSingles() { return singles; }
}