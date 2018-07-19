package com.recoded.taqadam.models;

import java.io.Serializable;

;

/**
 * Created by wisam on Dec 10 17.
 */

public class UserOld implements Serializable {
    /*
    private static final String TAG = User.class.getSimpleName();
    private String name, email;
    private int id;
    private Boolean isApproved = false, isEmailVerified = true;
    private Profile profile;
    private Channel channel;
    private List<Team> teams;
    private transient Wallet wallet;

    public String getEmailString() {
        String ret = email.concat(" ");
        if (isEmailVerified) {
            ret += "(Verified)";
        } else {
            ret += "(Not Verified)";
        }
        return ret;
    }

    public static User parseJson(String response) {
        Gson gson = new GsonBuilder()
                .setDateFormat(Api.DATE_FORMAT)
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();

        return gson.fromJson(response, User.class);
    }

    public static User fromFacebookUser(FacebookUser fbUser) {
        User u = new User();

        u.email = fbUser.getEmail();
        u.name = fbUser.getName();

        Profile p = new Profile();
        p.firstName = fbUser.getFirstName();
        p.lastName = fbUser.getFirstName();

        String g = fbUser.getGenderEnum();
        p.gender = g.equals("female")? Profile.Gender.FEMALE:(g.equals("male")?Profile.Gender.MALE:Profile.Gender.NOT_SPECIFIED);


        //Facebook may send only MM/DD or only YYYY, depending on user privacy settings.
        //So to watch-out for future bugs we check if it is without a year and append the current year.
        //If it was send with only YYYY then ignore and ask user for it!
        //Later we'll ask for the year if it's == current year.
        if (fbUser.getBirthday() != null && fbUser.getBirthday().length() != 4) {
            String bday = fbUser.getBirthday();
            if (bday.length() == 5) {
                bday += "/" + Calendar.getInstance().get(Calendar.YEAR);
            }
            try {
                p.birthDate = new SimpleDateFormat("MM/dd/yyyy", Locale.US).parse(bday);
            } catch (ParseException e) {
                Log.d(TAG, "Error while parsing facebook birthday string: " + bday);
                e.printStackTrace();
            }
        }


        if (fbUser.getPictureUrl() != null) {
            p.avatar = Uri.parse(fbUser.getPictureUrl());
        }
        u.profile = p;
        return u;
    }

    //CONSTRUCTORS
    public User() {
    }
    */
}
