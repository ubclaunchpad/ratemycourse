package com.example.coursify;

/**
 * Created by LucyZhao on 2017/11/11.
 */

public class User {
    private String major;
    private String name;
    private String gradDate;
    private String facebookID;

    public User(String name, String major, String gradDate, String facebookID){
        this.major = major;
        this.name = name;
        this.gradDate = gradDate;
        this.facebookID = facebookID;
    }

    public User(String name, String major) {
        this.name = name;
        this.major = major;
    }

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public String getMajor() {
        return major;
    }

    public String getName() {
        return name;
    }

    public String getGradDate() {
        return gradDate;
    }

    public String getFacebookID() {
        return facebookID;
    }

}
