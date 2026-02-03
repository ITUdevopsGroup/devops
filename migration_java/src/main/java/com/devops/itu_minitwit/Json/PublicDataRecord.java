package com.devops.itu_minitwit.Json;

public class PublicDataRecord {
    private int messageId;
    private String authorId;
    private String text;
    private int pubDate;
    private int flagged;
        
    public PublicDataRecord() {
    }
    public PublicDataRecord(int messageId, String authorId, String text, int pubDate, int flagged) {
        this.messageId = messageId;
        this.authorId = authorId;
        this.text = text;
        this.pubDate = pubDate;
        this.flagged = flagged;
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
    public int getPubDate() {
        return pubDate;
    }
    public void setPubDate(int pubDate) {
        this.pubDate = pubDate;
    }
    public int getFlagged() {
        return flagged;
    }
    public void setFlagged(int flagged) {
        this.flagged = flagged;
    }

    
}
