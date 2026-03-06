package com.devops.itu_minitwit.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "message")
public class Message {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "message_id")
  private Integer id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "author_id", nullable = false)
  private User author;

  @Column(name = "text", nullable = false)
  private String text;

  @Column(name = "pub_date")
  private Long pubDate;

  @Column(name = "flagged")
  private Integer flagged;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public User getAuthor() {
    return author;
  }

  public void setAuthor(User author) {
    this.author = author;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public Long getPubDate() {
    return pubDate;
  }

  public void setPubDate(Long pubDate) {
    this.pubDate = pubDate;
  }

  public Integer getFlagged() {
    return flagged;
  }

  public void setFlagged(Integer flagged) {
    this.flagged = flagged;
  }
}

