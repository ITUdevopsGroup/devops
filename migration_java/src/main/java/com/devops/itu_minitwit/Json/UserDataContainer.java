package com.devops.itu_minitwit.Json;

public class UserDataContainer {
    private UserData userData;


    public UserDataContainer() {
    }

    public UserDataContainer(UserData userData) {
        this.userData = userData;
    }

    public UserData getUserData() {
        return userData;
    }

    public void setUserData(UserData userData) {
        this.userData = userData;
    }

    
}
