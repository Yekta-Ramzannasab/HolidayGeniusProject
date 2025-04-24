package models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Comment {
    private String username;
    private String text;
    //private String timestamp;

    public Comment(String username, String text) {
        this.username = username;
        this.text = text;
        //this.timestamp = timestamp;
        //this.dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    //Getters
    public String getUsername() { return username; }
    public void setUser(String username) {
        this.username = username;
    }
    public String getText() { return text; }
    public void setText(String text) {
        this.text = text;
    }

//    public String getTimestamp() { return timestamp; }
//    public void setDateTime(String dateTime) {
//        this.timestamp = dateTime;
//    }
//
//    @Override
//    public String toString() {
//        return "[" + timestamp + "] " + username + ": " + text;
//    }


}
