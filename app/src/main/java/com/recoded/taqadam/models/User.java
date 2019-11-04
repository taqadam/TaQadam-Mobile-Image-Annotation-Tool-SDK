
package com.recoded.taqadam.models;

import android.util.Log;

import com.google.gson.annotations.Expose;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.annotation.Generated;

@Generated("net.hexar.json2pojo")
@SuppressWarnings("unused")
public class User extends Model {

    private static final String TAG = User.class.getSimpleName();

    @Expose
    private Channel channel;
    @Expose
    private String email;
    @Expose
    private Boolean isApproved = false;
    @Expose
    private Boolean isEmailVerified = true;
    @Expose
    private String username;
    @Expose
    private List<Team> teams;
    @Expose
    private Profile profile;

    public Channel getChannel() {
        return channel;
    }

    public String getEmail() {
        return email;
    }

    public String getEmailString() {
        String ret = email.concat(" ");
        if (isEmailVerified) {
            ret += "(Verified)";
        } else {
            ret += "(Not Verified)";
        }
        return ret;
    }

    public Boolean getIsApproved() {
        return isApproved;
    }

    public Boolean getIsEmailVerified() {
        return isEmailVerified;
    }

    public String getName() {
        return username;
    }

    public List<Team> getTeams() {
        return teams;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setApproved(Boolean approved) {
        isApproved = approved;
    }

    public void setEmailVerified(Boolean emailVerified) {
        isEmailVerified = emailVerified;
    }

    public void setName(String name) {
        this.username = name;
    }

    public void setTeams(List<Team> teams) {
        this.teams = teams;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public static User fromFacebookUser(FacebookUser fbUser) {
        User u = new User();

        u.email = fbUser.getEmail();
        u.username = fbUser.getName();

        Profile p = new Profile();
        p.setFirstName(fbUser.getFirstName());
        p.setLastName(fbUser.getFirstName());

        String g = fbUser.getGender();
        p.setGender(g);


        //Facebook may send only MM/DD or only YYYY, depending on user privacy settings.
        //So to watch-out for future bugs we check if it is without a year and append the current year.
        //If it was send with only YYYY then ignore and ask user for it
        //Later we'll ask for the year if it's == current year.
        if (fbUser.getBirthday() != null && fbUser.getBirthday().length() != 4) {
            String bday = fbUser.getBirthday();
            if (bday.length() == 5) {
                bday += "/" + Calendar.getInstance().get(Calendar.YEAR);
            }
            try {
                Date birthDate = new SimpleDateFormat("MM/dd/yyyy", Locale.US).parse(bday);
                p.setBirthDate(new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(birthDate));
            } catch (ParseException e) {
                Log.d(TAG, "Error while parsing facebook birthday string: " + bday);
                e.printStackTrace();
            }
        }


        if (fbUser.getPictureUrl() != null) {
            p.setAvatar(fbUser.getPictureUrl());
        }
        u.profile = p;
        return u;
    }
}
