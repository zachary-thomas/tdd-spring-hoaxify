package com.hoaxify.hoaxify.user;

// Another solution to DTO in repo
// not the most ideal.
public interface UserProjection {

    long getId();

    String getUsername();

    String getDisplayName();

    String getImage();
}
