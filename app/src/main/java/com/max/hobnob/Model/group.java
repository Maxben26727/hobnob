package com.max.hobnob.Model;

public class group {
    String groupID;
    String groupName;
    String adminId;
    String groupDesc;
    String groupPic;
    String date;
    public String getDate() {
        return date;
    }

    public group() {
    }

    public void setDate(String date) {
        this.date = date;
    }



    public group(String groupID, String groupName, String adminId, String groupDesc, String groupPic,String date) {
        this.groupID = groupID;
        this.groupName = groupName;
        this.adminId = adminId;
        this.groupDesc = groupDesc;
        this.groupPic = groupPic;
        this.date = date;
    }

    public String getGroupID() {
        return groupID;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public String getGroupDesc() {
        return groupDesc;
    }

    public void setGroupDesc(String groupDesc) {
        this.groupDesc = groupDesc;
    }

    public String getGroupPic() {
        return groupPic;
    }

    public void setGroupPic(String groupPic) {
        this.groupPic = groupPic;
    }
}
