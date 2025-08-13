package com.taskmanager.dto;

public class MessageResponse {
    private String message;
    private String data; // For additional data like photo URL

    public MessageResponse(String message) {
        this.message = message;
    }

    public MessageResponse(String message, String data) {
        this.message = message;
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}


