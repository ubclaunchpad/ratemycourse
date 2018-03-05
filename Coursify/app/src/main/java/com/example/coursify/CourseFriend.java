package com.example.coursify;

/**
 * Created by sveloso on 2018-02-12.
 */

public class CourseFriend {

    private String name;
    private String preference;
    private String email;

    public CourseFriend (String name, String preference, String email) {
        this.name = name;
        this.preference = preference;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPreference() {
        return preference;
    }
}
