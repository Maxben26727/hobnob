package com.max.hobnob.Model;

public class Chatlist {
    public String id;

    public Chatlist(String id, String chatkey) {
        this.id = id;
        this.chatkey = chatkey;
    }

    public String getChatkey() {
        return chatkey;
    }

    public void setChatkey(String chatkey) {
        this.chatkey = chatkey;
    }

    public String chatkey;



    public Chatlist() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
