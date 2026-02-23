package com.devops.itu_minitwit.dto;

public class MessageResponse {
  private String content;
  private String pub_date;
  private String user;

  public MessageResponse() {}

  public MessageResponse(String content, String pub_date, String user) {
    this.content = content;
    this.pub_date = pub_date;
    this.user = user;
  }

  public String getContent() { return content; }
  public void setContent(String content) { this.content = content; }

  public String getPub_date() { return pub_date; }
  public void setPub_date(String pub_date) { this.pub_date = pub_date; }

  public String getUser() { return user; }
  public void setUser(String user) { this.user = user; }
}
