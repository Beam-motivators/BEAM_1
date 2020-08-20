package com.beamotivator.beam.models;

public class ModelUser {

    //use same name as in firebase database
    String name, email, search, phone, image ,cover, uid, onlineStatus, typingTo,myPoints, weeklyPoints,likePoints, postCount, totalLikes;
    boolean isBlocked = false;

    public ModelUser(){

    }

    public ModelUser(String name, String email, String search, String phone, String image, String cover, String uid, String onlineStatus, String typingTo, String myPoints, String weeklyPoints, String likePoints, String postCount, String totalLikes, boolean isBlocked) {
        this.name = name;
        this.email = email;
        this.search = search;
        this.phone = phone;
        this.image = image;
        this.cover = cover;
        this.uid = uid;
        this.onlineStatus = onlineStatus;
        this.typingTo = typingTo;
        this.myPoints = myPoints;
        this.weeklyPoints = weeklyPoints;
        this.likePoints = likePoints;
        this.postCount = postCount;
        this.totalLikes = totalLikes;
        this.isBlocked = isBlocked;
    }

    public String getTotalLikes() {
        return totalLikes;
    }

    public void setTotalLikes(String totalLikes) {
        this.totalLikes = totalLikes;
    }

    public String getPostCount() {
        return postCount;
    }

    public void setPostCount(String postCount) {
        this.postCount = postCount;
    }

    public String getLikePoints() {
        return likePoints;
    }

    public void setLikePoints(String likePoints) {
        this.likePoints = likePoints;
    }

    public String getWeeklyPoints() {
        return weeklyPoints;
    }

    public void setWeeklyPoints(String weeklyPoints) {
        this.weeklyPoints = weeklyPoints;
    }

    public String getMyPoints() {
        return myPoints;
    }

    public void setMyPoints(String myPoints) {
        this.myPoints = myPoints;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getOnlineStatus() {
        return onlineStatus;
    }

    public void setOnlineStatus(String onlineStatus) {
        this.onlineStatus = onlineStatus;
    }

    public String getTypingTo() {
        return typingTo;
    }

    public void setTypingTo(String typingTo) {
        this.typingTo = typingTo;
    }
}
