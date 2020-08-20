package com.beamotivator.beam.models;

public class ModelSuggestions {
    String name,uid,uDp,sugId,suggestion;

    public ModelSuggestions(){

    }

    public ModelSuggestions(String name, String uid, String uDp, String sugId, String suggestion) {
        this.name = name;
        this.uid = uid;
        this.uDp = uDp;
        this.sugId = sugId;
        this.suggestion = suggestion;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getuDp() {
        return uDp;
    }

    public void setuDp(String uDp) {
        this.uDp = uDp;
    }

    public String getSugId() {
        return sugId;
    }

    public void setSugId(String sugId) {
        this.sugId = sugId;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }
}
