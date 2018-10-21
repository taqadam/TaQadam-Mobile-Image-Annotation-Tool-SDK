package com.recoded.taqadam.models.auth;

import com.google.gson.annotations.Expose;

public class Login {
    @Expose
    public String email;
    @Expose
    public String password;

    public Login(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
