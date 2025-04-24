package models;

import models.*;

public class CommentRequest {
    private Song song;
    private Comment comment;
    private boolean approved;

    public CommentRequest(Song song, Comment comment) {
        this.song = song;
        this.comment = comment;
        this.approved = false;
    }

    // Getters and Setters
    public Song getSong() { return song; }
    public void setSong(Song song) {
        this.song = song;
    }

    public Comment getComment() { return comment; }
    public void setComment(Comment comment) {
        this.comment = comment;
    }
    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }
}
