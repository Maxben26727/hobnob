package com.max.hobnob.Model;

public class Chat {

    private String sender;

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    private String senderName;
    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    private String messageID;
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    private  String type;

    public Chat(String sender, String time, String receiver, String message, boolean isseen,String type,String messageID,String senderName) {
        this.sender = sender;
        this.time = time;
        this.receiver = receiver;
        this.message = message;
        this.isseen = isseen;
        this.type = type;
        this.messageID = messageID;
        this.senderName = senderName;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    private String time;
    private String receiver;
    private String message;
    private boolean isseen;



    public Chat() {
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isIsseen() {
        return isseen;
    }

    public void setIsseen(boolean isseen) {
        this.isseen = isseen;
    }
}
