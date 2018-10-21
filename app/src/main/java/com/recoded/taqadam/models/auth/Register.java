package com.recoded.taqadam.models.auth;

import com.google.gson.annotations.Expose;

public class Register {
    @Expose
    public String name;
    @Expose
    public String email;
    @Expose
    public String password;

    public Register(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }
}
