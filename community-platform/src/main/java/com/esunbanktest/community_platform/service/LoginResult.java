package com.esunbanktest.community_platform.service;

import com.esunbanktest.community_platform.entity.UserEntity;

public class LoginResult {
    private final boolean success;
    private final String message;
    private final UserEntity user;

    public LoginResult(boolean success, String message, UserEntity user) {
        this.success = success;
        this.message = message;
        this.user = user;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public UserEntity getUser() {
        return user;
    }
}
