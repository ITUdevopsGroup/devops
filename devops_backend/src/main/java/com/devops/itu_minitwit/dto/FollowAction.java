package com.devops.itu_minitwit.dto;

public class FollowAction {
  private String follow;
  private String unfollow;

  public FollowAction() {}

  public String getFollow() { return follow; }
  public void setFollow(String follow) { this.follow = follow; }

  public String getUnfollow() { return unfollow; }
  public void setUnfollow(String unfollow) { this.unfollow = unfollow; }
}
