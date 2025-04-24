package models;

import javax.sound.sampled.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Song {
    private String title;
    private String artist;
    private String album;
    private String lyrics;
    private String audioFilePath;
    private int views;
    private int likes;
    private List<Comment> comments;

    public Song(String title, String artist, String album, String lyrics) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.lyrics = lyrics;
        this.views = 0;
        this.likes = 0;
        this.comments = new ArrayList<>();
    }

    // Setters and Getters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getLyrics() {
        return lyrics;
    }

    public void setLyrics(String lyrics) {
        this.lyrics = lyrics;
    }

    public String getAudioFilePath() {
        return audioFilePath;
    }

    public void setAudioFilePath(String audioFilePath) {
        this.audioFilePath = audioFilePath;
    }

    public boolean hasAudio() {
        return audioFilePath != null && !audioFilePath.isEmpty() &&
                new File(audioFilePath).exists();
    }

    public int getViews() {
        return views;
    }

    public void incrementViews() {
        views++;
    }

    public int getLikes() {
        return likes;
    }

    public void incrementLikes() {
        likes++;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void addComment(Comment comment) {
        comments.add(comment);
    }

    public void playAudio() {
        if (!hasAudio()) {
            System.out.println("No audio file available for this song.");
            return;
        }

        try {

            String normalizedPath = audioFilePath.replace("\\", "/");
            File audioFile = new File(normalizedPath);

            if (!audioFile.exists()) {
                System.out.println("Audio file not found at: " + audioFile.getAbsolutePath());
                return;
            }

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();

            System.out.println("Now playing: " + title);


            new Thread(() -> {
                try {
                    while (clip.isRunning()) {
                        Thread.sleep(1000);
                    }
                    clip.close();
                    audioStream.close();
                } catch (Exception e) {
                    System.out.println("Playback error: " + e.getMessage());
                }
            }).start();

        } catch (Exception e) {
            System.out.println("Error playing audio: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void saveToFile(Path directory) throws IOException {
        Path songFile = directory.resolve(title.replaceAll("[^a-zA-Z0-9]", "_") + ".txt");

        try (PrintWriter writer = new PrintWriter(new FileWriter(songFile.toFile()))) {
            writer.println("Title: " + title);
            writer.println("Artist: " + artist);
            writer.println("Album: " + (album != null ? album : ""));
            writer.println("Audio: " + (audioFilePath != null ? audioFilePath : ""));
            writer.println("Views: " + views);
            writer.println("Likes: " + likes);
            writer.println("Lyrics:");
            writer.println(lyrics);

            if (!comments.isEmpty()) {
                writer.println("Comments:");
                for (Comment comment : comments) {
                    writer.println(comment.getUsername() + "|" + comment.getText().replace("\n", "\\n"));
                }
            }
        }
    }

    public static Song loadFromFile(Path songFile) {
        try {
            List<String> lines = Files.readAllLines(songFile);
            String title = "";
            String artist = "";
            String album = "";
            String audioPath = "";
            int views = 0;
            int likes = 0;
            StringBuilder lyrics = new StringBuilder();
            List<Comment> comments = new ArrayList<>();

            boolean inLyrics = false;
            boolean inComments = false;

            for (String line : lines) {
                if (line.startsWith("Title: ")) {
                    title = line.substring(7).trim();
                } else if (line.startsWith("Artist: ")) {
                    artist = line.substring(8).trim();
                } else if (line.startsWith("Album: ")) {
                    album = line.substring(7).trim();
                    if (album.isEmpty()) album = null;
                } else if (line.startsWith("Audio: ")) {
                    audioPath = line.substring(7).trim();
                } else if (line.startsWith("Views: ")) {
                    views = Integer.parseInt(line.substring(7).trim());
                } else if (line.startsWith("Likes: ")) {
                    likes = Integer.parseInt(line.substring(7).trim());
                } else if (line.equals("Lyrics:")) {
                    inLyrics = true;
                    inComments = false;
                } else if (line.equals("Comments:")) {
                    inLyrics = false;
                    inComments = true;
                } else if (inLyrics) {
                    if (lyrics.length() > 0) lyrics.append("\n");
                    lyrics.append(line);
                } else if (inComments) {
                    String[] parts = line.split("\\|");
                    if (parts.length == 2) {
                        comments.add(new Comment(parts[0], parts[1].replace("\\n", "\n")));
                    }
                }
            }

            Song song = new Song(title, artist, album, lyrics.toString());
            song.setAudioFilePath(audioPath.isEmpty() ? null : audioPath);
            song.views = views;
            song.likes = likes;
            song.comments = comments;

            return song;
        } catch (Exception e) {
            System.err.println("Error loading song from file: " + songFile);
            e.printStackTrace();
            return null;
        }
    }
}




