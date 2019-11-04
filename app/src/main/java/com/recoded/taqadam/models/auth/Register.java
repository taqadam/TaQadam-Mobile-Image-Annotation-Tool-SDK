package com.recoded.taqadam.models.auth;

import com.google.gson.annotations.Expose;

public class Register {
    @Expose
    public String username;
    @Expose
    public String email;
    @Expose
    public String password;

    public Register(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
}
