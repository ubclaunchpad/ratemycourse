package com.example.coursify;

import java.util.List;
import java.util.PriorityQueue;

/**
 * Created by LucyZhao on 2017/11/11.
 */

public class User {
    private String major;
    private String name;

    public String getMajor() {
        return major;
    }

    public String getName() {
        return name;
    }

    public String getInterest() { return interest; }

    public String getGradDate() {
        return gradDate;
    }

    public String getFacebookID() {
        return facebookID;
    }

    public List<String> getFacebookFriends() {
        return facebookFriends;
    }

    public PriorityQueue<String> getSearchHistory() {
        return searchHistory;
    }

    public List<String> getCoursesTaken() {
        return coursesTaken;
    }

    public List<String> getCoursesGoing() {
        return coursesGoing;
    }

    public List<String> getCoursesInterested() {
        return coursesInterested;
    }

    public List<String> getBookmarks() {
        return bookmarks;
    }

    public List<Note> getNotes() {
        return notes;
    }

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
    private List<Note> notes;

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
        this.interest = interest;
    }

    //why do we need this? Is it okay to remove afterwards? lol
    public User(String name, String major) {
        this.name = name;
        this.major = major;
    }

    //From profile settings
    public User(String name, String major, String gradDate, String interest){
        this.name = name;
        this.major = major;
        this.gradDate = gradDate;
        this.interest = interest;
    }

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }
}
