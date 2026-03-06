package com.devops.itu_minitwit.domain;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "follower")
public class Follower {

  @EmbeddedId
  private FollowerId id;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("whoId")
  @JoinColumn(name = "who_id")
  private User who;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("whomId")
  @JoinColumn(name = "whom_id")
  private User whom;

  public FollowerId getId() {
    return id;
  }

  public void setId(FollowerId id) {
    this.id = id;
  }

  public User getWho() {
    return who;
  }

  public void setWho(User who) {
    this.who = who;
  }

  public User getWhom() {
    return whom;
  }

  public void setWhom(User whom) {
    this.whom = whom;
  }
}

