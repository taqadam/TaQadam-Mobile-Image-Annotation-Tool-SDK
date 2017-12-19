package com.recoded.taqadam.models.auth;

/**
 * Created by wisam on Dec 16 17.
 */

public class UserAuthHandler {
    /*
    private static final String TAG = UserAuthHandler.class.getSimpleName();
    private String mUid;
    private User mCurrentUser;
    private static FirebaseAuth mAuth;

    private static UserAuthHandler authHandler;

    public enum UserState{
        NOT_EXIST, //goto sign in activity
        EXIST_NO_PROFILE, //Goto confirm activity
        EXIST_PROFILE_OK, //Goto main activity
        DISABLED //goto contact us
    }


    //TODO-wisam : Do this !

    public static UserAuthHandler getInstance() {
        if(authHandler == null){
            authHandler = new UserAuthHandler();
        }
        return authHandler;
    }

    public Task<UserState> signIn(String email, String pw){
        final TaskCompletionSource<UserState> authTask = new TaskCompletionSource<>();
        mAuth.signInWithEmailAndPassword(email, pw).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(final AuthResult authResult) {
                try {
                    initAuth().addOnSuccessListener(new OnSuccessListener<UserState>() {
                        @Override
                        public void onSuccess(UserState userState) {
                            authTask.setResult(userState);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                authTask.setException(e);
            }
        });
        return authTask.getTask();
    }

    public Task<UserState> signIn(AccessToken fbToken){
        final TaskCompletionSource<UserState> authTask = new TaskCompletionSource<>();

        AuthCredential credential = FacebookAuthProvider.getCredential(fbToken.getToken());
        mAuth.signInWithCredential(credential).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                try {
                    initAuth().addOnSuccessListener(new OnSuccessListener<UserState>() {
                        @Override
                        public void onSuccess(UserState userState) {
                            authTask.setResult(userState);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                authTask.setException(e);
            }
        });

        return authTask.getTask();
    }



    public static Task<UserState> initAuth() throws Exception {
        if (authHandler.getCurrentUser() != null) {
            throw new Exception("Already initialized");
        }
        if(mAuth == null) mAuth = FirebaseAuth.getInstance();
        final TaskCompletionSource<UserState> authTask = new TaskCompletionSource<>();

        if (mAuth.getCurrentUser() == null) {
            authTask.setResult(UserState.NOT_EXIST);
        } else {
            mAuth.getCurrentUser().getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                @Override
                public void onComplete(@NonNull Task<GetTokenResult> task) {
                    if(!task.isSuccessful()){
                        if(task.getException() != null){
                            if(((FirebaseAuthInvalidUserException) task.getException()).getErrorCode().contains("disabled")){
                                authTask.setResult(UserState.DISABLED);
                            } else {
                                authTask.setResult(UserState.NOT_EXIST);
                            }
                        } else {
                            authTask.setResult(UserState.NOT_EXIST);
                        }
                    } else { //User exists and legit
                        authHandler.mUid = mAuth.getUid();
                        UserDbHandler.getInstance().fetchUserNode().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                            @Override
                            public void onSuccess(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists()){
                                    authTask.setResult(UserState.EXIST_PROFILE_OK);
                                    authHandler.mCurrentUser = User.fromMap((HashMap) dataSnapshot.getValue());
                                } else {
                                    authTask.setResult(UserState.EXIST_NO_PROFILE);
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                authTask.setException(e);
                                Log.d(TAG, "Error while fetching user node from db: " + e.getMessage());
                            }
                        });
                    }
                }
            });
        }

        return authTask.getTask();
    }

    public String getUid() {
        return mUid;
    }

    private UserAuthHandler() {
    }

    public User getCurrentUser(){
        return mCurrentUser;
    }
    */
}
