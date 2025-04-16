package services;

import models.Song;
import java.io.*;
        import java.util.*;

public class MusicService {
    private static final String SONGS_FILE = "src/main/java/files/songs_data.txt";
    private List<Song> songs = new ArrayList<>();

    public MusicService() {
        loadSongs();
    }

    private void loadSongs() {
        try (Scanner scanner = new Scanner(new File(SONGS_FILE))) {
            String currentSection = "";
            Map<String, String> lyricsMap = new HashMap<>();

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.equals("albums:") || line.equals("lyrics:")) {
                    currentSection = line;
                    continue;
                }

                if (currentSection.equals("albums:")) {
                    String[] parts = line.split(",");
                    String artist = parts[0];
                    String album = parts[1];

                    for (int i = 2; i < parts.length; i++) {
                        songs.add(new Song(parts[i], artist, album, ""));
                    }
                }
                else if (currentSection.equals("lyrics:")) {
                    String[] parts = line.split(",", 2);
                    if (parts.length == 2) {
                        lyricsMap.put(parts[0], parts[1]);
                    }
                }
            }

            // تطبیق متن آهنگ‌ها
            for (Song song : songs) {
                if (lyricsMap.containsKey(song.getTitle())) {
                    song = new Song(song.getTitle(), song.getArtist(),
                            song.getAlbum(), lyricsMap.get(song.getTitle()));
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Songs data file not found!");
        }
    }

    public List<Song> searchSongs(String query) {
        List<Song> results = new ArrayList<>();
        for (Song song : songs) {
            if (song.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                    song.getArtist().toLowerCase().contains(query.toLowerCase()) ||
                    song.getAlbum().toLowerCase().contains(query.toLowerCase())) {
                results.add(song);
            }
        }
        return results;
    }

    public void displaySongLyrics(String title) {
        for (Song song : songs) {
            if (song.getTitle().equalsIgnoreCase(title)) {
                System.out.println("\n=== " + song.getTitle() + " ===");
                System.out.println("Artist: " + song.getArtist());
                System.out.println("Album: " + song.getAlbum());
                System.out.println("\nLyrics:\n" + song.getLyrics());
                return;
            }
        }
        System.out.println("Song not found!");
    }
}