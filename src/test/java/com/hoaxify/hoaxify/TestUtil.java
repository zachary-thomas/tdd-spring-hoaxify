package com.hoaxify.hoaxify;

import com.hoaxify.hoaxify.user.User;

public class TestUtil {

    public static User createValidUser(){
        return User.builder()
                .displayName("test-display")
                .username("test-user")
                .password("P4ssword")
                .build();
    }
}
