package com.recoded.taqadam.models;

import android.net.Uri;
import android.util.Log;

import com.google.firebase.auth.FirebaseUser;
import com.recoded.taqadam.models.db.UserDbHandler;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

;

/**
 * Created by wisam on Dec 10 17.
 */

public class User implements Serializable {
    private static final String TAG = User.class.getSimpleName();
    private String mFName, mLName, mDName, mEMailAddr;
    private String mPhoneNumber;
    private City mUserCity;
    private Uri mPicturePath;
    private Date mDateOfBirth;
    private Gender mGender;
    private boolean mAccountApproved = false, mEmailVerified = false, mPhoneNumberVerified = false, mCompleteProfile = false;
    private transient Wallet mWallet;

    public String getFirstName() {
        return mFName;
    }

    public void setFirstName(String mFName) {
        this.mFName = mFName;
    }

    public String getLastName() {
        return mLName;
    }

    public void setLastName(String mLName) {
        this.mLName = mLName;
    }

    public String getFullName() {
        return getFirstName() + " " + getLastName();
    }

    public String getDisplayName() {
        return mDName;
    }

    public void setDisplayName(String mDName) {
        this.mDName = mDName;
    }

    public String getEmailAddress() {
        return mEMailAddr;
    }

    public String getEmailString() {
        String ret = getEmailAddress().concat(" ");
        if (isEmailVerified()) {
            ret += "(Verified)";
        } else {
            ret += "(Not Verified)";
        }
        return ret;
    }

    public void setEmailAddress(String mEMailAddr) {
        this.mEMailAddr = mEMailAddr;
    }

    public String getPhoneNumber() {
        return mPhoneNumber;
    }

    public String getPhoneNumberString() {
        String ret = getPhoneNumber().concat(" ");
        if (isPhoneNumberVerified()) {
            ret += "(Verified)";
        } else {
            ret += "(Not Verified)";
        }
        return ret;
    }

    public void setPhoneNumber(String mPhoneNumber) {
        this.mPhoneNumber = mPhoneNumber;
    }

    public City getUserCity() {
        return mUserCity;
    }

    public void setUserCity(City mUserAddress) {
        this.mUserCity = mUserAddress;
    }

    public Uri getPicturePath() {
        return mPicturePath;
    }

    public void setPicturePath(Uri mPicturePath) {
        this.mPicturePath = mPicturePath;
    }

    public Date getDateOfBirth() {
        return mDateOfBirth;
    }

    public String getAgeString() {
        String ageString;

        Calendar dob = Calendar.getInstance();
        Calendar today = Calendar.getInstance();

        dob.setTime(getDateOfBirth());
        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }

        String format = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
        ageString = sdf.format(dob.getTime()) + " (" + age + " years)";

        return ageString;
    }

    public void setDateOfBirth(Date mDateOfBirth) {
        this.mDateOfBirth = mDateOfBirth;
    }

    public Gender getGender() {
        return mGender;
    }

    public void setGender(Gender mGender) {
        this.mGender = mGender;
    }

    public boolean isAccountApproved() {
        return mAccountApproved;
    }

    public boolean isEmailVerified() {
        return mEmailVerified;
    }

    public void setEmailVerified(boolean mEmailVerified) {
        this.mEmailVerified = mEmailVerified;
    }

    public boolean isPhoneNumberVerified() {
        return mPhoneNumberVerified;
    }

    public void setPhoneNumberVerified(boolean mPhoneNumberVerified) {
        this.mPhoneNumberVerified = mPhoneNumberVerified;
    }

    public void setCompleteProfile(boolean completeProfile) {
        this.mCompleteProfile = completeProfile;
    }

    public boolean isCompleteProfile() {
        return mCompleteProfile;
    }

    public Wallet getWallet() {
        return mWallet;
    }

    public void setWallet(Wallet mWallet) {
        this.mWallet = mWallet;
    }

    public enum Gender {
        MALE("Male"),
        FEMALE("Female"),
        NOT_SPECIFIED("Not Specified");
        private final String gender;

        Gender(String friendlyName) {
            this.gender = friendlyName;
        }

        public String getGenderName() {
            return gender;
        }
    }

    public enum City {
        //These ids should be the same as the order of cities
        // in the 'cities_lebanon' array or else it won't work
        //We will set the selected spinner item as the userCity.getId()
        ALEY("Aley", 1),
        BAALBEK("Baalbek", 2),
        BATROUN("Batroun", 3),
        BEIRUT("Beirut", 4),
        BYBLOS("Byblos", 5),
        JOUNIEH("Jounieh", 6),
        NABATIEH("Nabatieh", 7),
        SIDON("Sidon", 8),
        TRIPOLI("Tripoli", 9),
        TYRE("Tyre", 10),
        ZAHLE("Zahle", 11),
        ZGHARTA("Zgharta", 12);

        private final int positionId;
        private final String name;

        City(String name, int id) {
            this.name = name;
            this.positionId = id;
        }

        public String getName() {
            return name;
        }

        public int getId() {
            return positionId;
        }
    }

    public static User fromMap(HashMap map) {
        User u = new User();
        u.mDName = (String) map.get(UserDbHandler.DISPLAY_NAME);
        u.mEMailAddr = (String) map.get(UserDbHandler.EMAIL_ADDRESS);
        u.mFName = (String) map.get(UserDbHandler.FIRST_NAME);
        u.mLName = (String) map.get(UserDbHandler.LAST_NAME);
        u.mPhoneNumber = (String) map.get(UserDbHandler.PHONE_NUMBER);
        u.mPicturePath = Uri.parse((String) map.get(UserDbHandler.DISPLAY_IMAGE));
        u.mDateOfBirth = new Date((long) map.get(UserDbHandler.USER_DOB_TS));
        u.mPhoneNumberVerified = (boolean) map.get(UserDbHandler.PHONE_CONFIRMED);
        u.mEmailVerified = (boolean) map.get(UserDbHandler.EMAIL_CONFIRMED);
        u.mAccountApproved = (boolean) map.get(UserDbHandler.IS_APPROVED);
        switch ((String) map.get(UserDbHandler.USER_GENDER)) {
            case "Male":
                u.mGender = Gender.MALE;
                break;
            case "Female":
                u.mGender = Gender.FEMALE;
                break;
            default:
                u.mGender = Gender.NOT_SPECIFIED;
                break;
        }

        for (City city : City.values()) {
            if (city.getName().equals(map.get(UserDbHandler.USER_ADDRESS))) {
                u.mUserCity = city;
            }
        }
        return u;
    }

    public static User fromFacebookUser(FacebookUser fbUser) {
        User u = new User();
        u.mEMailAddr = fbUser.getEmail();
        u.mDName = fbUser.getName();
        u.mFName = fbUser.getFirstName();
        u.mLName = fbUser.getLastName();

        switch (fbUser.getGender()) {
            case "female":
                u.mGender = Gender.FEMALE;
                break;
            case "male":
                u.mGender = Gender.MALE;
                break;
            default:
                u.mGender = Gender.NOT_SPECIFIED;
        }

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
                u.mDateOfBirth = new SimpleDateFormat("MM/dd/yyyy", Locale.US).parse(bday);
            } catch (ParseException e) {
                Log.d(TAG, "Error while parsing facebook birthday string: " + bday);
                e.printStackTrace();
            }
        }


        if (fbUser.getPictureUrl() != null) {
            u.mPicturePath = Uri.parse(fbUser.getPictureUrl());
        }

        return u;
    }

    //CONSTRUCTORS
    public User() {
    }

    public User(FirebaseUser u) {
        mEMailAddr = u.getEmail();

    }
}
