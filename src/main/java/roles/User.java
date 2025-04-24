package roles;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String username;
    private String password;
    private String fullName;
    private String email;
    private String role;
    private List<String> followedArtists;
    private List<String> likedSongs;
    private List<String> comments;

    public User(String username, String password, String fullName, String email) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.followedArtists = new ArrayList<>();
        this.likedSongs = new ArrayList<>();
        this.comments = new ArrayList<>();
    }

    // Getters and Setters
    public void setUsername(String username) {
        this.username = username;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public List<String> getFollowedArtists() { return followedArtists; }
    public List<String> getLikedSongs() { return likedSongs; }
    public List<String> getComments() { return comments; }

    public void followArtist(String artistName) {
        if (!followedArtists.contains(artistName)) {
            followedArtists.add(artistName);
        }
    }

    public void unfollowArtist(String artistName) {
        followedArtists.remove(artistName);
    }

    public void likeSong(String songId) {
        if (!likedSongs.contains(songId)) {
            likedSongs.add(songId);
        }
    }

    public void unlikeSong(String songId) {
        likedSongs.remove(songId);
    }

    public void addComment(String commentId) {
        comments.add(commentId);
    }


}