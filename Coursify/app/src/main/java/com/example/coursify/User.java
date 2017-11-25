package com.example.coursify;

import java.util.List;
import java.util.PriorityQueue;

/**
 * Created by LucyZhao on 2017/11/11.
 */

public class User {
    private String major;
    private String name;
    private String gradDate;
    private String facebookID;
    private List<String> facebookFriends;
    private String interest;
    //having a PriorityQueue is better for LRU
    private PriorityQueue<String> searchHistory;
    private List<String> coursesTaken;
    private List<String> coursesGoing;
    private List<String> coursesInterested;
    private List<String> bookmarks;
    public List<Note> notes;

    public User(String name, String major, String gradDate, String facebookID, List<String> facebookFriends, String interest, PriorityQueue<String> searchHistory,
                List<String> coursesTaken, List<String> coursesGoing, List<String> bookmarks, List<Note> notes){
        this.major = major;
        this.name = name;
        this.gradDate = gradDate;
        this.facebookID = facebookID;
        this.facebookFriends = facebookFriends;
        this.interest = interest;
        this.searchHistory = searchHistory;
        this.coursesTaken = coursesTaken;
        this.coursesGoing = coursesGoing;
        this.bookmarks = bookmarks;
        this.notes = notes;
    }

    //why do we need this? Is it okay to remove afterwards? lol
    public User(String name, String major) {
        this.name = name;
        this.major = major;
    }

    //From profile settings
    public User(String name, String major, String gradDate, String facebookID){
        this.name = name;
        this.major = major;
        this.gradDate = gradDate;
        this.facebookID = facebookID;
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
