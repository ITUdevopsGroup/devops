package com.devops.itu_minitwit.Json;

public class PublicDataRecord {
    private int messageId;
    private String authorId;
    private String text;
    private String pubDate;
    private int flagged;
    private int userId;
    private String username;
    private String email;
    private String pwHash;

        
    public PublicDataRecord() {
    }

    
    public PublicDataRecord(int messageId, String authorId, String text, String pubDate, int flagged, int userId,
            String username, String email, String pwHash) {
        this.messageId = messageId;
        this.authorId = authorId;
        this.text = text;
        this.pubDate = pubDate;
        this.flagged = flagged;
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.pwHash = pwHash;
    }


    public int getUserId() {
        return userId;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getPwHash() {
        return pwHash;
    }
    public void setPwHash(String pwHash) {
        this.pwHash = pwHash;
    }
    public int getMessageId() {
        return messageId;
    }
    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }
    public String getAuthorId() {
        return authorId;
    }
    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }
    public String getPubDate() {
        return pubDate;
    }
    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }
    public int getFlagged() {
        return flagged;
    }
    public void setFlagged(int flagged) {
        this.flagged = flagged;
    }

    
}
