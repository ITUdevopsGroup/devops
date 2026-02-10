package com.devops.itu_minitwit.Json;

public class UserData {
    private String username = "";
    private int userId;
    private boolean pwOK = false;

    public UserData() {}

       
    public UserData(String username,int userId) {
        this.username = username;
        this.userId = userId;
    }

    
    
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }



    public boolean isPwOK() {
        return pwOK;
    }


    public void setPwOK(boolean pwOK) {
        this.pwOK = pwOK;
    }


    public int getUserId() {
        return userId;
    }


    public void setUserId(int userId) {
        this.userId = userId;
    }



    
}
