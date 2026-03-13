package com.devops.itu_minitwit.domain;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class FollowerId implements Serializable {

  @Column(name = "who_id")
  private Integer whoId;

  @Column(name = "whom_id")
  private Integer whomId;

  public FollowerId() {
  }

  public FollowerId(Integer whoId, Integer whomId) {
    this.whoId = whoId;
    this.whomId = whomId;
  }

  public Integer getWhoId() {
    return whoId;
  }

  public void setWhoId(Integer whoId) {
    this.whoId = whoId;
  }

  public Integer getWhomId() {
    return whomId;
  }

  public void setWhomId(Integer whomId) {
    this.whomId = whomId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    FollowerId that = (FollowerId) o;
    return Objects.equals(whoId, that.whoId) && Objects.equals(whomId, that.whomId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(whoId, whomId);
  }
}

