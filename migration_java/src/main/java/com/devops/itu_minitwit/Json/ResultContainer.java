package com.devops.itu_minitwit.Json;

public class ResultContainer {
    private Result result;

    
    public ResultContainer() {
    }

    public ResultContainer(Result result) {
        this.result = result;
    }

    public Result getUserData() {
        return result;
    }

    public void setUserData(Result result) {
        this.result = result;
    }

    
}
