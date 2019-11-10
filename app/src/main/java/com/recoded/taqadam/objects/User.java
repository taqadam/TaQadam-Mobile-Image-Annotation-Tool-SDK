
package com.recoded.taqadam.objects;

import android.util.Log;

import com.google.gson.annotations.Expose;
import com.recoded.taqadam.models.Channel;
import com.recoded.taqadam.models.FacebookUser;
import com.recoded.taqadam.models.Model;
import com.recoded.taqadam.models.Profile;
import com.recoded.taqadam.models.Team;

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
    private String email;
    @Expose
    private String username;
    @Expose
    private Profile profile;

    public String getEmail() {
        return email;
    }

    public String getName() {
        return username;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String name) {
        this.username = name;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public static User fromFacebookUser(FacebookUser fbUser) {
        User u = new User();

        u.email = fbUser.getEmail();
        u.username = fbUser.getName();

        Profile p = new Profile();

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
