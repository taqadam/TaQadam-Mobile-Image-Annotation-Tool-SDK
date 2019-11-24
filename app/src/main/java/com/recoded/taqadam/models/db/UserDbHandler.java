package com.recoded.taqadam.models.db;

/**
 * Created by wisam on Dec 13 17.
 */

public class UserDbHandler {
    /*
    private static final String TAG = UserDbHandler.class.getSimpleName();
    private static final String EXCEPTION_MSG = "Used not signed in";
    public static final String
            FIRST_NAME = "first_n",
            LAST_NAME = "last_n",
            DISPLAY_NAME = "display_n",
            EMAIL_ADDRESS = "email_addr",
            EMAIL_CONFIRMED = "is_email_confirmed",
            PHONE_NUMBER = "phone_number",
            PHONE_CONFIRMED = "is_phone_confirmed",
            USER_ADDRESS = "user_address",
            USER_DOB_TS = "dob_ts",
            USER_GENDER = "gender",
            DISPLAY_IMAGE = "display_img",
            GOVT_ID = "govt_id_img",
            IS_APPROVED = "is_approved", //IS THE USER ACCOUNT APPROVED BY TAQADAM, THIS SHOULD ALWAYS BE SET TO 0 BY THE APP AND TAQADAM'S ADMIN WILL CHANGE IT TO (TRUE)
            LAST_ACTIVE_TS = "last_active_ts",
            REG_ON_TS = "registered_on_ts";

    //TODO-wisam: MOVE THIS TO ITS OWN CLASS
    private static final String
            IS_ACTIVE = "is_active", //TAQADAM'S ADMIN CAN SUSPEND THE WORKERS WALLET SO NO TRANSACTION IS ALLOWED
            MONEY_AMOUNT = "money_amount";

    private static UserDbHandler handler;

    private DatabaseReference mDbReference;

    private ChildEventListener userListener;

    public static UserDbHandler getInstance() {
        if (handler == null) {
            handler = new UserDbHandler();
        }
        return handler;
    }

    private UserDbHandler() {
        String mUid = UserAuthHandler.getInstance().getUid();
        this.mDbReference = FirebaseDatabase.getInstance().getReference().child("Users").child(mUid);
        setupUserListener();
    }

    public Task<Void> writeNewUser(User user) {
        Map<String, Object> keyValues = new HashMap<>();
        keyValues.put(FIRST_NAME, user.getFirstName());
        keyValues.put(LAST_NAME, user.getLastName());
        keyValues.put(DISPLAY_NAME, user.getDisplayName());
        keyValues.put(EMAIL_ADDRESS, user.getEmailAddress());
        keyValues.put(EMAIL_CONFIRMED, false);
        keyValues.put(PHONE_NUMBER, user.getPhoneNumber());
        keyValues.put(PHONE_CONFIRMED, false);
        keyValues.put(USER_ADDRESS, user.getUserCity().getName());
        keyValues.put(USER_DOB_TS, user.getDateOfBirth().getTime());
        keyValues.put(USER_GENDER, user.getGenderEnum().getGenderName());
        keyValues.put(DISPLAY_IMAGE, user.getPicturePath().toString());
        keyValues.put(GOVT_ID, "null");
        keyValues.put(IS_APPROVED, false);
        keyValues.put(REG_ON_TS, System.currentTimeMillis());

        return mDbReference.setValue(keyValues);
    }

    public Task<Void> updateUser(User user) {
        Map<String, Object> keyValues = new HashMap<>();
        keyValues.put(FIRST_NAME, user.getFirstName());
        keyValues.put(LAST_NAME, user.getLastName());
        keyValues.put(DISPLAY_NAME, user.getDisplayName());
        //keyValues.put(EMAIL_ADDRESS, user.getEmailAddress());
        keyValues.put(PHONE_NUMBER, user.getPhoneNumber());
        keyValues.put(USER_ADDRESS, user.getUserCity().getName());
        keyValues.put(USER_DOB_TS, user.getDateOfBirth().getTime());
        keyValues.put(USER_GENDER, user.getGenderEnum().getGenderName());
        keyValues.put(DISPLAY_IMAGE, user.getPicturePath().toString());
        return mDbReference.updateChildren(keyValues);
    }

    public Task<Void> updateUserImg(Uri newPicturePath) {
        Map<String, Object> keyValues = new HashMap<>();
        keyValues.put(DISPLAY_IMAGE, newPicturePath.toString());
        return mDbReference.updateChildren(keyValues);
    }

    public Task<String> fetchUserPicture(String uid) {
        final TaskCompletionSource<String> src = new TaskCompletionSource<>();
        mDbReference.getParent().child(uid).child(DISPLAY_IMAGE).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                src.setResult((String) dataSnapshot.getValue());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                src.setException(databaseError.toException());
            }
        });
        return src.getTask();
    }

    public Task<DataSnapshot> fetchUserNode() {
        final TaskCompletionSource<DataSnapshot> source = new TaskCompletionSource<>();
        mDbReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                source.setResult(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                source.setException(databaseError.toException());
            }
        });

        return source.getTask();
    }

    private void setupUserListener() {
        this.userListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                updateCurrentUser(dataSnapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mDbReference.addChildEventListener(this.userListener);
    }

    public void release() {
        mDbReference.removeEventListener(this.userListener);
        userListener = null;
        mDbReference = null;
        handler = null;
    }

    private void updateCurrentUser(DataSnapshot data) {
        String key = data.getKey();
        switch (key) {
            case FIRST_NAME:
                UserAuthHandler.getInstance().getCurrentUser().setFirstName((String) data.getValue());
                break;
            case LAST_NAME:
                UserAuthHandler.getInstance().getCurrentUser().setLastName((String) data.getValue());
                break;
            case DISPLAY_NAME:
                UserAuthHandler.getInstance().getCurrentUser().setDisplayName((String) data.getValue());
                break;
            case EMAIL_ADDRESS:
                UserAuthHandler.getInstance().getCurrentUser().setEmailAddress((String) data.getValue());
                break;
            case EMAIL_CONFIRMED:
                UserAuthHandler.getInstance().getCurrentUser().setEmailVerified((boolean) data.getValue());
                break;
            case PHONE_NUMBER:
                UserAuthHandler.getInstance().getCurrentUser().setPhoneNumber((String) data.getValue());
                break;
            case PHONE_CONFIRMED:
                UserAuthHandler.getInstance().getCurrentUser().setPhoneNumberVerified((boolean) data.getValue());
                break;
            case USER_ADDRESS:
                for (User.City city : User.City.values()) {
                    if (city.getName().equals(data.getValue())) {
                        UserAuthHandler
                                .getInstance()
                                .getCurrentUser()
                                .setUserCity(city);
                    }
                }
                break;
            case USER_DOB_TS:
                UserAuthHandler.getInstance().getCurrentUser().setDateOfBirth(new Date((long) data.getValue()));
                break;
            case DISPLAY_IMAGE:
                UserAuthHandler.getInstance().getCurrentUser().setPicturePath(Uri.parse((String) data.getValue()));
                break;
            case USER_GENDER:
                String gender = (String) data.getValue();
                switch (gender) {
                    case "Male":
                        UserAuthHandler.getInstance().getCurrentUser().setGender(User.Gender.MALE);
                        break;
                    case "Female":
                        UserAuthHandler.getInstance().getCurrentUser().setGender(User.Gender.FEMALE);
                        break;
                    default:
                        UserAuthHandler.getInstance().getCurrentUser().setGender(User.Gender.NOT_SPECIFIED);
                        break;
                }
                break;
        }
    }
    */
}
