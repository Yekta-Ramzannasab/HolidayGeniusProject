package roles;

import models.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Artist {
    private String name;
    private String username;
    private String password;
    private String email;
    private String bio;
    private int followers;
    private boolean isActive;
    private List<Song> singles;
    private List<Album> albums;
    private List<LyricRequest> lyricRequests;
    private List<CommentRequest> commentRequests;

    public Artist(String name) {
        this.name = name;
        this.singles = new ArrayList<>();
        this.albums = new ArrayList<>();
        this.lyricRequests = new ArrayList<>();
        this.commentRequests = new ArrayList<>();
        this.followers = 0;
        this.isActive = true;
    }

    // Setters and Getters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public int getFollowers() {
        return followers;
    }

    public void setFollowers(int followers) {
        this.followers = followers;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public List<Song> getSingles() {
        return singles;
    }

    public void addSingle(Song song) {
        singles.add(song);
    }

    public void removeSingle(Song song) {
        singles.remove(song);
    }

    public List<Album> getAlbums() {
        return albums;
    }

    public void addAlbum(Album album) {
        albums.add(album);
    }

    public void removeAlbum(Album album) {
        albums.remove(album);
    }

    public List<LyricRequest> getLyricRequests() {
        return lyricRequests;
    }

    public void addLyricRequest(LyricRequest request) {
        lyricRequests.add(request);
    }

    public void approveLyricRequest(int index) {
        if (index >= 0 && index < lyricRequests.size()) {
            LyricRequest request = lyricRequests.get(index);
            request.getSong().setLyrics(request.getNewLyrics());
            lyricRequests.remove(index);
        }
    }

    public void rejectLyricRequest(int index) {
        if (index >= 0 && index < lyricRequests.size()) {
            lyricRequests.remove(index);
        }
    }

    public List<CommentRequest> getCommentRequests() {
        return commentRequests;
    }

    public void addCommentRequest(CommentRequest request) {
        commentRequests.add(request);
    }

    public void approveCommentRequest(int index) {
        if (index >= 0 && index < commentRequests.size()) {
            CommentRequest request = commentRequests.get(index);
            request.getSong().addComment(request.getComment());
            commentRequests.remove(index);
        }
    }

    public void rejectCommentRequest(int index) {
        if (index >= 0 && index < commentRequests.size()) {
            commentRequests.remove(index);
        }
    }

    public List<Song> getMostPopularSongs(int count) {
        List<Song> allSongs = new ArrayList<>(singles);
        albums.forEach(album -> allSongs.addAll(album.getSongs()));

        allSongs.sort((s1, s2) -> Integer.compare(s2.getViews(), s1.getViews()));

        return allSongs.subList(0, Math.min(count, allSongs.size()));
    }

    public void saveToDirectory() throws IOException {
        Path artistPath = Paths.get("src/main/java/files/Artist", name);
        Files.createDirectories(artistPath);

        // Save artist info
        Path infoFile = artistPath.resolve("info.txt");
        try (PrintWriter writer = new PrintWriter(new FileWriter(infoFile.toFile()))) {
            writer.println("Name: " + name);
            writer.println("Username: " + username);
            writer.println("Email: " + email);
            writer.println("Bio: " + (bio != null ? bio : ""));
            writer.println("Followers: " + followers);
            writer.println("Active: " + isActive);
        }

        // Save songs
        Path songsDir = artistPath.resolve("Songs");
        Files.createDirectories(songsDir);

        for (Song song : singles) {
            song.saveToFile(songsDir);
        }

        // Save albums
        Path albumsDir = artistPath.resolve("Albums");
        Files.createDirectories(albumsDir);

        for (Album album : albums) {
            album.saveToDirectory(albumsDir);
        }

        // Save requests
        Path requestsDir = artistPath.resolve("Requests");
        Files.createDirectories(requestsDir);

        saveRequests(requestsDir);
    }

    private void saveRequests(Path requestsDir) throws IOException {
        Path lyricRequestsFile = requestsDir.resolve("lyric_requests.txt");
        try (PrintWriter writer = new PrintWriter(new FileWriter(lyricRequestsFile.toFile()))) {
            for (LyricRequest request : lyricRequests) {
                writer.println(request.getSong().getTitle() + "|" +
                        request.getNewLyrics().replace("\n", "\\n") + "|" +
                        request.getRequestedBy());
            }
        }

        Path commentRequestsFile = requestsDir.resolve("comment_requests.txt");
        try (PrintWriter writer = new PrintWriter(new FileWriter(commentRequestsFile.toFile()))) {
            for (CommentRequest request : commentRequests) {
                writer.println(request.getSong().getTitle() + "|" +
                        request.getComment().getUsername() + "|" +
                        request.getComment().getText().replace("\n", "\\n"));
            }
        }
    }

    public static Artist loadFromDirectory(Path artistDir) {
        try {
            String name = artistDir.getFileName().toString();
            Artist artist = new Artist(name);

            // Load artist info
            Path infoFile = artistDir.resolve("info.txt");
            if (Files.exists(infoFile)) {
                List<String> infoLines = Files.readAllLines(infoFile);
                for (String line : infoLines) {
                    if (line.startsWith("Username: ")) {
                        artist.setUsername(line.substring("Username: ".length()));
                    } else if (line.startsWith("Email: ")) {
                        artist.setEmail(line.substring("Email: ".length()));
                    } else if (line.startsWith("Bio: ")) {
                        artist.setBio(line.substring("Bio: ".length()));
                    } else if (line.startsWith("Followers: ")) {
                        artist.setFollowers(Integer.parseInt(line.substring("Followers: ".length())));
                    } else if (line.startsWith("Active: ")) {
                        artist.setActive(Boolean.parseBoolean(line.substring("Active: ".length())));
                    }
                }
            }

            // Load songs
            Path songsDir = artistDir.resolve("Songs");
            if (Files.exists(songsDir)) {
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(songsDir, "*.txt")) {
                    for (Path songFile : stream) {
                        Song song = Song.loadFromFile(songFile);
                        if (song != null) {
                            artist.addSingle(song);
                        }
                    }
                }
            }

            // Load albums
            Path albumsDir = artistDir.resolve("Albums");
            if (Files.exists(albumsDir)) {
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(albumsDir, Files::isDirectory)) {
                    for (Path albumDir : stream) {
                        Album album = Album.loadFromDirectory(albumDir);
                        if (album != null) {
                            artist.addAlbum(album);
                        }
                    }
                }
            }

            // Load requests
            Path requestsDir = artistDir.resolve("Requests");
            if (Files.exists(requestsDir)) {
                // Load lyric requests
                Path lyricRequestsFile = requestsDir.resolve("lyric_requests.txt");
                if (Files.exists(lyricRequestsFile)) {
                    List<String> lyricRequestLines = Files.readAllLines(lyricRequestsFile);
                    for (String line : lyricRequestLines) {
                        String[] parts = line.split("\\|");
                        if (parts.length == 3) {
                            Song song = artist.findSongByTitle(parts[0]);
                            if (song != null) {
                                artist.addLyricRequest(new LyricRequest(song,
                                        parts[1].replace("\\n", "\n"), parts[2]));
                            }
                        }
                    }
                }

                // Load comment requests
                Path commentRequestsFile = requestsDir.resolve("comment_requests.txt");
                if (Files.exists(commentRequestsFile)) {
                    List<String> commentRequestLines = Files.readAllLines(commentRequestsFile);
                    for (String line : commentRequestLines) {
                        String[] parts = line.split("\\|");
                        if (parts.length == 3) {
                            Song song = artist.findSongByTitle(parts[0]);
                            if (song != null) {
                                artist.addCommentRequest(new CommentRequest(song,
                                        new Comment(parts[1], parts[2].replace("\\n", "\n"))));
                            }
                        }
                    }
                }
            }

            return artist;
        } catch (IOException e) {
            System.err.println("Error loading artist from directory: " + e.getMessage());
            return null;
        }
    }

    private Song findSongByTitle(String title) {
        for (Song song : singles) {
            if (song.getTitle().equals(title)) {
                return song;
            }
        }

        for (Album album : albums) {
            for (Song song : album.getSongs()) {
                if (song.getTitle().equals(title)) {
                    return song;
                }
            }
        }

        return null;
    }
}