package com.example.coursify;

/**
 * Created by sveloso on 2018-02-12.
 */

public class CourseFriend {

    private String name;
    private String preference;

    public CourseFriend (String name, String preference) {
        this.name = name;
        this.preference = preference;
    }

    public String getName() {
        return name;
    }

    public String getPreference() {
        return preference;
    }
}
