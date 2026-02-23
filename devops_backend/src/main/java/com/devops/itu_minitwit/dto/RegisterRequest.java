package com.devops.itu_minitwit.dto;

public class RegisterRequest {
    private String username;
  private String email;
  private String pwd;

  public RegisterRequest() {}

  public String getUsername() { return username; }
  public void setUsername(String username) { this.username = username; }

  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }

  public String getPwd() { return pwd; }
  public void setPwd(String pwd) { this.pwd = pwd; }
}
