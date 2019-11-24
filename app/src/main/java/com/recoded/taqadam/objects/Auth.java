
package com.recoded.taqadam.objects;

import com.google.gson.annotations.Expose;
import com.recoded.taqadam.objects.User;

import javax.annotation.Generated;

@Generated("net.hexar.json2pojo")
@SuppressWarnings("unused")
public class Auth {
    @Expose
    private String error;
    @Expose
    private User user;
    @Expose
    private String token;


    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
