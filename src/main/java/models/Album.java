package models;

import models.*;
import roles.*;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Album {
    private String name;
    private String artist;
    private int year;
    private List<Song> songs;

    public Album(String name, String artist, int year) {
        this.name = name;
        this.artist = artist;
        this.year = year;
        this.songs = new ArrayList<>();
    }

    public void addSong(Song song) {
        songs.add(song);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public List<Song> getSongs() {
        return songs;
    }


    public void removeSong(Song song) {
        songs.remove(song);
    }


    public void saveToDirectory(Path albumsDir) throws IOException {
        Path albumDir = albumsDir.resolve(name);
        Files.createDirectories(albumDir);

        Path infoFile = albumDir.resolve("info.txt");
        try (PrintWriter writer = new PrintWriter(new FileWriter(infoFile.toFile()))) {
            writer.println("Name: " + name);
            writer.println("Artist: " + artist);
            writer.println("Year: " + year);
        }

        Path songsDir = albumDir.resolve("Songs");
        Files.createDirectories(songsDir);

        for (Song song : songs) {
            song.saveToFile(songsDir);
        }
    }

    public static Album loadFromDirectory(Path albumDir) {
        try {
            Path infoFile = albumDir.resolve("info.txt");
            if (!Files.exists(infoFile)) {
                return null;
            }

            List<String> infoLines = Files.readAllLines(infoFile);
            String name = "";
            String artist = "";
            int year = 0;

            for (String line : infoLines) {
                if (line.startsWith("Name: ")) {
                    name = line.substring("Name: ".length());
                } else if (line.startsWith("Artist: ")) {
                    artist = line.substring("Artist: ".length());
                } else if (line.startsWith("Year: ")) {
                    year = Integer.parseInt(line.substring("Year: ".length()));
                }
            }

            Album album = new Album(name, artist, year);

            Path songsDir = albumDir.resolve("Songs");
            if (Files.exists(songsDir)) {
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(songsDir, "*.txt")) {
                    for (Path songFile : stream) {
                        Song song = Song.loadFromFile(songFile);
                        if (song != null) {
                            album.addSong(song);
                        }
                    }
                }
            }

            return album;
        } catch (IOException e) {
            System.err.println("Error loading album from directory: " + e.getMessage());
            return null;
        }
    }
}
