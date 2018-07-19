package com.recoded.taqadam.models.auth;

public class Register {
    public String name, email, password;

    public Register(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }
}
