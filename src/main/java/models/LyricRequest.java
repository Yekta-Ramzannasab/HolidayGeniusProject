package models;

import models.*;

public class LyricRequest {
    private Song song;
    private String newLyrics;
    private String requestedBy;
    private boolean approved;

    public LyricRequest(Song song, String newLyrics, String requestedBy) {
        this.song = song;
        this.newLyrics = newLyrics;
        this.requestedBy = requestedBy;
        this.approved = false;
    }

    public Song getSong() { return song; }
    public String getNewLyrics() { return newLyrics; }
    public String getRequestedBy() { return requestedBy; }
    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }

    public void setSong(Song song) {
        this.song = song;
    }

    public void setNewLyrics(String newLyrics) {
        this.newLyrics = newLyrics;
    }

    public void setRequestedBy(String requestedBy) {
        this.requestedBy = requestedBy;
    }

}


