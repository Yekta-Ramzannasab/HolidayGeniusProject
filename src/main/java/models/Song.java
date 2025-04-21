package models;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Locale;

public class Song {
    private String title;
    private String artist;
    private String album;
    private String lyrics;
    private Path audioPath;

    public Song(String title, String artist, String album, String lyrics) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.lyrics = lyrics;
        this.audioPath = findAudioFile();
    }

    public static Song fromFile(Path filePath, String artistName, String albumName) throws IOException {
        String title = filePath.getFileName().toString().replace(".txt", "");
        String lyrics = Files.readString(filePath);
        return new Song(title, artistName, albumName, lyrics);
    }

    private Path findAudioFile() {
        String artistDir = "src/main/java/files/Artist/" + artist;
        Path artistPath = Paths.get(artistDir);

        if (!Files.exists(artistPath)) return null;

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(artistPath, "*.wav")) {
            for (Path audioFile : stream) {
                String normalizedAudioName = audioFile.getFileName().toString().toLowerCase(Locale.ROOT);
                String normalizedTitle = title.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");

                if (normalizedAudioName.replaceAll("[^a-z0-9]", "").contains(normalizedTitle)) {
                    return audioFile;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void playAudio() {
        if (audioPath == null || !Files.exists(audioPath)) {
            System.out.println("Audio file not available for this song.");
            return;
        }

        try (AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioPath.toFile())) {
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();

            System.out.println("Playing: " + title + "...");
            System.out.println("Press ENTER to stop playback.");
            System.in.read();
            clip.stop();
            clip.close();
        } catch (Exception e) {
            System.out.println("Failed to play audio: " + e.getMessage());
        }
    }

    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getAlbum() { return album; }
    public String getLyrics() { return lyrics; }
    public boolean hasAudio() { return audioPath != null && Files.exists(audioPath); }
}