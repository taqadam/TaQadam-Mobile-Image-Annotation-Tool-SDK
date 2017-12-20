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
    private static User currentUser;
    private String mFName, mLName, mDName, mEMailAddr;
    private String mPhoneNumber;
    private String mUserAddress;
    private Uri mPicturePath;
    private Date mDateOfBirth;
    private Gender mGender;
    private boolean mAccountApproved = false, mEmailVerified = false, mPhoneNumberVerified = false, mCompleteProfile = false;
    private transient Wallet mWallet;

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User currentUser) {
        User.currentUser = currentUser;
    }

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

    public String getDisplayName() {
        return mDName;
    }

    public void setDisplayName(String mDName) {
        this.mDName = mDName;
    }

    public String getEmailAddress() {
        return mEMailAddr;
    }

    public void setEmailAddress(String mEMailAddr) {
        this.mEMailAddr = mEMailAddr;
    }

    public String getPhoneNumber() {
        return mPhoneNumber;
    }

    public void setPhoneNumber(String mPhoneNumber) {
        this.mPhoneNumber = mPhoneNumber;
    }

    public String getUserAddress() {
        return mUserAddress;
    }

    public void setUserAddress(String mUserAddress) {
        this.mUserAddress = mUserAddress;
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

    public static User fromMap(HashMap map) {
        User u = new User();
        u.mDName = (String) map.get(UserDbHandler.DISPLAY_NAME);
        u.mEMailAddr = (String) map.get(UserDbHandler.EMAIL_ADDRESS);
        u.mFName = (String) map.get(UserDbHandler.FIRST_NAME);
        u.mLName = (String) map.get(UserDbHandler.LAST_NAME);
        u.mPhoneNumber = (String) map.get(UserDbHandler.PHONE_NUMBER);
        u.mPicturePath = Uri.parse((String) map.get(UserDbHandler.DISPLAY_IMAGE));
        u.mDateOfBirth = new Date((long) map.get(UserDbHandler.USER_DOB_TS));
        u.mUserAddress = (String) map.get(UserDbHandler.USER_ADDRESS);
        u.mPhoneNumberVerified = (boolean) map.get(UserDbHandler.PHONE_CONFIRMED);
        u.mEmailVerified = (boolean) map.get(UserDbHandler.EMAIL_CONFIRMED);
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
