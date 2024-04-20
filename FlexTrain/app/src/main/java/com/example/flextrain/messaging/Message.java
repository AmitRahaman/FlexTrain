package com.example.flextrain.messaging;

public class Message {
    private String text;
    private String imageUrl;
    private String senderId;
    private String messageId;
    private long timestamp;

    // Default constructor required for Firebase
    public Message() {
    }

    public Message(String text, String imageUrl, String senderId,String messageId, long timestamp) {
        this.text = text;
        this.imageUrl = imageUrl;
        this.senderId = senderId;
        this.timestamp = timestamp;
        this.messageId = messageId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    // Getters and setters
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
