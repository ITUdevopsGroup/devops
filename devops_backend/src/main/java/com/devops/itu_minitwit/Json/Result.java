package com.devops.itu_minitwit.Json;

public class Result {
    private String status;
    private boolean error;
    private boolean result;

    
    public Result() {
    }
    public Result(String status, boolean error,boolean result) {
        this.status = status;
        this.error = error;
        this.result = result;
    }

    
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public boolean isError() {
        return error;
    }
    public void setError(boolean error) {
        this.error = error;
    }
    public boolean isResult() {
        return result;
    }
    public void setResult(boolean result) {
        this.result = result;
    }

    
}
