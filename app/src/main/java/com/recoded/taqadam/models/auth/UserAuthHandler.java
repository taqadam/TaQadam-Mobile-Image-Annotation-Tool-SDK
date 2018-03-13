package com.recoded.taqadam.models.auth;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.ProviderQueryResult;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.recoded.taqadam.models.FacebookUser;
import com.recoded.taqadam.models.User;
import com.recoded.taqadam.models.db.UserDbHandler;

import java.util.HashMap;

/**
 * Created by wisam on Dec 21 17.
 */

public class UserAuthHandler {
    private static final String TAG = UserAuthHandler.class.getSimpleName();
    private static UserAuthHandler instance;

    private FirebaseAuth mAuth;
    private String mUid;
    private User currentUser;
    private Task<User> initTask;

    public static UserAuthHandler getInstance() {
        if (instance == null) {
            instance = new UserAuthHandler();
            instance.initializeAuth();
        }
        return instance;
    }

    private UserAuthHandler() {
        mAuth = FirebaseAuth.getInstance();
    }

    public String getUid() {
        return mUid;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public Task<User> getInitTask() {
        return initTask;
    }

    private void initializeAuth() {
        final TaskCompletionSource<User> initTask = new TaskCompletionSource<>();
        if (mAuth.getCurrentUser() == null) {
            initTask.setResult(null);
        } else {
            mUid = mAuth.getUid();
            UserDbHandler.getInstance().fetchUserNode().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                @Override
                public void onSuccess(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 1) {
                        currentUser = User.fromMap((HashMap) dataSnapshot.getValue());
                        if (!currentUser.isEmailVerified() && mAuth.getCurrentUser().isEmailVerified()) {
                            currentUser.setEmailVerified(mAuth.getCurrentUser().isEmailVerified());
                        }
                        currentUser.setCompleteProfile(true);
                        initTask.setResult(currentUser);
                    } else {
                        currentUser = new User(mAuth.getCurrentUser());
                        initTask.setResult(currentUser);
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    initTask.setException(e);
                    Log.d(TAG, "Error initializing auth - fetchUserNode Failed: " + e.getMessage());
                }
            });
        }
        this.initTask = initTask.getTask();
    }

    public Task<User> signIn(String userName, String pw) {
        final TaskCompletionSource<User> signInTask = new TaskCompletionSource<>();
        if (currentUser != null) {
            signInTask.setResult(currentUser);
        } else {
            mAuth.signInWithEmailAndPassword(userName, pw).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(final AuthResult authResult) {
                    mUid = mAuth.getUid();
                    UserDbHandler.getInstance().fetchUserNode().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                        @Override
                        public void onSuccess(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                currentUser = User.fromMap((HashMap) dataSnapshot.getValue());
                                currentUser.setCompleteProfile(true);
                                signInTask.setResult(currentUser);
                            } else {
                                currentUser = new User(authResult.getUser());
                                signInTask.setResult(currentUser);
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            signInTask.setException(e);
                            Log.d(TAG, "Error signing in user with pw - fetchUserNode Failed: " + e.getMessage());
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    signInTask.setException(e);
                    Log.d(TAG, "Error signing in user with pw: " + e.getMessage());
                }
            });
        }

        return signInTask.getTask();
    }

    public Task<User> signIn(final AccessToken accessToken) {
        final TaskCompletionSource<User> signInTask = new TaskCompletionSource<>();
        if (currentUser != null) {
            signInTask.setResult(currentUser);
        } else {
            AuthCredential creds = FacebookAuthProvider.getCredential(accessToken.getToken());
            mAuth.signInWithCredential(creds).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(final AuthResult authResult) {
                    mUid = mAuth.getUid();
                    UserDbHandler.getInstance().fetchUserNode().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                        @Override
                        public void onSuccess(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                currentUser = User.fromMap((HashMap) dataSnapshot.getValue());
                                currentUser.setCompleteProfile(true);
                                signInTask.setResult(currentUser);
                            } else {
                                fetchFbData(accessToken).addOnSuccessListener(new OnSuccessListener<User>() {
                                    @Override
                                    public void onSuccess(User user) {
                                        currentUser = user;
                                        signInTask.setResult(currentUser);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        currentUser = new User(authResult.getUser());
                                        signInTask.setResult(currentUser);
                                        Log.d(TAG, "Error signing in user with fb - fetchFbData Failed: " + e.getMessage());
                                    }
                                });
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            signInTask.setException(e);
                            Log.d(TAG, "Error signing in user with fb - fetchUserNode Failed: " + e.getMessage());
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    signInTask.setException(e);
                    Log.d(TAG, "Error signing in user with fb: " + e.getMessage());
                }
            });
        }
        return signInTask.getTask();
    }

    public Task<User> signUp(final String userName, String pw) {
        final TaskCompletionSource<User> signUpTask = new TaskCompletionSource<>();
        mAuth.createUserWithEmailAndPassword(userName, pw).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                mUid = mAuth.getUid();
                currentUser = new User(authResult.getUser());
                authResult.getUser().sendEmailVerification();
                signUpTask.setResult(currentUser);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mAuth.fetchProvidersForEmail(userName).addOnSuccessListener(new OnSuccessListener<ProviderQueryResult>() {
                    @Override
                    public void onSuccess(ProviderQueryResult res) {
                        if (res.getProviders() != null && !res.getProviders().isEmpty()) {
                            if (res.getProviders().contains(FacebookAuthProvider.PROVIDER_ID)
                                    && res.getProviders().contains(EmailAuthProvider.PROVIDER_ID)) {
                                signUpTask.setException(new AuthSignUpException(AuthSignUpException.EMAIL_ASSOC_FB_PW_WRONG));
                            } else if (res.getProviders().contains(EmailAuthProvider.PROVIDER_ID)) {
                                signUpTask.setException(new AuthSignUpException(AuthSignUpException.PW_WRONG));
                            } else if (res.getProviders().contains(FacebookAuthProvider.PROVIDER_ID)) {
                                signUpTask.setException(new AuthSignUpException(AuthSignUpException.EMAIL_ASSOC_FB));
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        signUpTask.setException(e);
                        Log.d(TAG, "Error signing up user user with pw - fetchProvidersForEmail Failed: " + e.getMessage());
                    }
                });
            }
        });
        return signUpTask.getTask();
    }

    @NonNull
    private Task<User> fetchFbData(AccessToken token) {
        final TaskCompletionSource<User> fbTask = new TaskCompletionSource<>();
        GraphRequest request = GraphRequest.newGraphPathRequest(
                token,
                "/me/",
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        if (response.getError() == null && response.getJSONObject() != null) {
                            User user = User.fromFacebookUser(new FacebookUser(response.getJSONObject()));
                            fbTask.setResult(user);
                        } else {
                            fbTask.setException(response.getError().getException());
                        }
                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "first_name,last_name,name,gender,picture.width(2000).height(2000){width,height,url,is_silhouette},verified,birthday,email,id");
        request.setParameters(parameters);
        request.executeAsync();

        return fbTask.getTask();
    }

    public void signOut() {
        UserDbHandler.getInstance().release();
        currentUser = null;
        mUid = null;
        if (AccessToken.getCurrentAccessToken() != null) {
            LoginManager.getInstance().logOut();
        }
        mAuth.signOut();
    }

    public void sendEmailVerification(final String email) {
        final FirebaseUser u = mAuth.getCurrentUser();
        if (u != null && u.getProviders() != null && u.getProviders().contains(FacebookAuthProvider.PROVIDER_ID)) {
            if (u.getEmail() == null || u.getEmail().isEmpty()) {
                u.updateEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        sendEmailVerification(email);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Crashlytics.log(Log.ERROR, TAG, "Error updating FirebaseUser " + email + ": " + e);
                    }
                });
            } else if (!u.isEmailVerified()) {
                u.sendEmailVerification().addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof FirebaseAuthRecentLoginRequiredException) {
                            AuthCredential a = FacebookAuthProvider.getCredential(AccessToken.getCurrentAccessToken().getToken());
                            u.reauthenticate(a).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    u.sendEmailVerification();
                                }
                            });
                        }
                    }
                });
            }
        }
    }

    public void updateUserProfile(User user) {
        if (mAuth.getCurrentUser() != null) {
            UserProfileChangeRequest b = new UserProfileChangeRequest.Builder()
                    .setDisplayName(user.getDisplayName())
                    .build();
            mAuth.getCurrentUser().updateProfile(b);
        }
    }

    public static class AuthSignUpException extends Exception {
        public static final int PW_WRONG = 2001;
        public static final int EMAIL_ASSOC_FB = 2000;
        public static final int EMAIL_ASSOC_FB_PW_WRONG = 2002;
        private int errorCode;

        public AuthSignUpException(int errorCode) {
            this.errorCode = errorCode;
        }

        public int getErrorCode() {
            return errorCode;
        }

    }

    public void updateCurrentUser(User user) {
        if (user.isCompleteProfile()) {
            currentUser = user;
        }
    }
}
