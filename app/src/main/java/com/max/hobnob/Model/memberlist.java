package com.max.hobnob.Model;

public class memberlist {
    public memberlist(String memberID) {
        this.memberID = memberID;
    }

    String memberID;

    public memberlist() {
    }

    public String getMemberID() {
        return memberID;
    }

    public void setMemberID(String memberID) {
        this.memberID = memberID;
    }
}
