package com.recoded.taqadam.models.auth;

import com.google.gson.annotations.Expose;

public class Login {
    @Expose
    public String username;
    @Expose
    public String password;

    public Login(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
