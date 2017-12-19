package com.recoded.taqadam;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.ProviderQueryResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = RegisterActivity.class.getSimpleName();
    private static final String EMAIL_REGEX = "^[a-zA-Z]+[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.{1}[a-zA-Z0-9-.]{2,}(?<!\\.)$";
    private static final String PASSWORD_REGEX_MED = "^(?=.*[A-Za-z])(?=.*[0-9]).{6,}$";
    private static final String PASSWORD_REGEX_FAIR = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[a-z]).{7,}$";
    private static final String PASSWORD_REGEX_STRONG = "^(?=.*[A-Z])(?=.*[!@#$&*])(?=.*[0-9])(?=.*[a-z]).{8,}$";


    private CallbackManager mCallbackManager;
    private FirebaseAuth mAuth;
    private boolean agreementSigned = true; //TODO-wisam: Implement proper check
    private EditText etEmailField, etPwField;
    private TextInputLayout etEmailLayout, etPwLayout;
    private TextView tvPwMeter;
    private LoginButton bFbLogin;
    private Button bSignin;

    private OnCompleteListener<AuthResult> mAuthCompleteListener;
    private OnCompleteListener<ProviderQueryResult> mAuthProblemResolverCompletionListener;

    private String mSubmittedEmail;

    ProgressDialog mCreatingAccountProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        //TODO-wisam: add a reloader for user on applictaion start
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(this, ConfirmProfileActivity.class));
            finish();
        }
        setContentView(R.layout.activity_signup);

        setupForm();
        setupFbAuth();
        bSignin = findViewById(R.id.b_signin);

        mCreatingAccountProgressDialog = new ProgressDialog(this);
        mCreatingAccountProgressDialog.setCancelable(false);
        mCreatingAccountProgressDialog.setTitle("Creating Account");
        mCreatingAccountProgressDialog.setCanceledOnTouchOutside(false);

        setupAuthListener();

        setupAuthResolverListener();

    }

    private void setupAuthResolverListener() {
        mAuthProblemResolverCompletionListener = new OnCompleteListener<ProviderQueryResult>() {
            @Override
            public void onComplete(@NonNull Task<ProviderQueryResult> task) {
                mCreatingAccountProgressDialog.dismiss();

                if (task.isSuccessful() && task.getResult().getProviders() != null) {
                    //Start building the alert dialog. TODO: Extract strings
                    final AlertDialog.Builder db = new AlertDialog.Builder(RegisterActivity.this);
                    db.setIcon(getResources().getDrawable(R.drawable.ic_error));
                    db.setTitle("Error");
                    db.setCancelable(false);

                    //if this email is associated with both password and fb, he can sign in with fb or reset password
                    if (task.getResult().getProviders().contains(FacebookAuthProvider.PROVIDER_ID)
                            && task.getResult().getProviders().contains(EmailAuthProvider.PROVIDER_ID)) {
                        db.setMessage("This email is already registered and connected to a Facebook account.");
                        db.setPositiveButton("Sign in with Facebook", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                bFbLogin.performClick();
                            }
                        });
                        db.setNeutralButton("Forgot Password", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //TODO: implement the intent to pw reset activity
                                dialog.dismiss();
                                resetPassword(mSubmittedEmail);
                            }
                        });
                        db.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                        db.create().show();

                        //else if associated with only password means he already signed up. show him only reset pw
                    } else if (task.getResult().getProviders().contains(EmailAuthProvider.PROVIDER_ID)) {
                        db.setMessage("This email is already registered. Do you want to sign in?");
                        db.setPositiveButton("Sign in", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                bSignin.performClick();
                            }
                        });
                        db.setNeutralButton("Forgot Password", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //TODO: implement the intent to pw reset activity
                                dialog.dismiss();
                                resetPassword(mSubmittedEmail);
                            }
                        });
                        db.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                        db.create().show();

                        //else if associated with fb only, offer him to signin with fb
                    } else if (task.getResult().getProviders().contains(FacebookAuthProvider.PROVIDER_ID)) {
                        db.setMessage("This email is associated with a registered Facebook account. Do you want to sign in with Facebook instead?");
                        db.setPositiveButton("Sign in with Facebook", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                bFbLogin.performClick();
                            }
                        });

                        db.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                        db.create().show();
                    }
                }
            }
        };
    }

    private void setupAuthListener() {
        mAuthCompleteListener = new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    mCreatingAccountProgressDialog.dismiss();
                    //TODO: Send the user to profile editor activity to confirm email and phone number
                    //TODO: IF using Facebook check if he's already confirmed so no need for profile editor
                    // Sign in/up success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success");
                    startActivity(new Intent(RegisterActivity.this, ConfirmProfileActivity.class));
                    finish();
                } else {
                    mCreatingAccountProgressDialog.setMessage("Resolving problems...");
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.getException());
                    if (task.getException() instanceof FirebaseAuthUserCollisionException) {

                        //We'll check for the provider. If it's facebook then we will ask the user to connect them.
                        FirebaseAuth.getInstance().fetchProvidersForEmail(mSubmittedEmail).addOnCompleteListener(RegisterActivity.this, mAuthProblemResolverCompletionListener);
                    }
                }

                // ...
            }
        };
    }

    private void resetPassword(String mSubmittedEmail) {
        //TODO IMPLEMENT A SOLID PASSWORD RESET STRATEGY WITH ACTION CODE SETTINGS. SEE: https://firebase.google.com/docs/auth/android/passing-state-in-email-actions
        //TODO DON"T FORGET TO USE FirebaseAuth.setLanguageCode() for arabic preferences
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void validateAndRegister() {
        if (!agreementSigned) {
            showAgreement();
            return;
        }

        String validationMsgs[] = new String[5];
        //TODO-wisam: Change to resources
        validationMsgs[0] = "E-Mail is too short!";
        validationMsgs[1] = "E-Mail address is invalid. Should be in the format: john@example.com";
        validationMsgs[2] = "Password is too short! Should be more than 5 characters";
        validationMsgs[3] = "Password Must be 6 characters long and should contain at least one letter and one number";
        validationMsgs[4] = "Write your E-Mail address!";

        String emailAddr = etEmailField.getText().toString();
        String password = etPwField.getText().toString();
        if (emailAddr.length() == 0) {
            etEmailLayout.setError(validationMsgs[4]);
        } else if (emailAddr.length() < 5 && emailAddr.length() > 0) {
            etEmailLayout.setError(validationMsgs[0]);
        } else if (!emailAddr.matches(EMAIL_REGEX)) {
            etEmailLayout.setError(validationMsgs[1]);
        } else if (password.length() < 6) {
            etEmailLayout.setError("");
            etPwLayout.setError(validationMsgs[2]);
        } else if (!password.matches(PASSWORD_REGEX_MED)) {
            etEmailLayout.setError("");
            etPwLayout.setError(validationMsgs[3]);
        } else {
            etEmailLayout.setError("");
            etPwLayout.setError("");
            handleEmailAndPassword(emailAddr, password);
        }
    }

    private void handleEmailAndPassword(String email, String pw) {
        mCreatingAccountProgressDialog.setMessage("Please wait");
        mCreatingAccountProgressDialog.show();

        Log.d(TAG, "handleEmailAndPassword:" + email);
        mSubmittedEmail = email;
        mAuth.createUserWithEmailAndPassword(email, pw).addOnCompleteListener(this, mAuthCompleteListener);
    }

    private void handleFacebookAccessToken(AccessToken token) {
        mCreatingAccountProgressDialog.setMessage("Please wait");
        mCreatingAccountProgressDialog.show();

        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());

        mAuth.signInWithCredential(credential).addOnCompleteListener(this, mAuthCompleteListener);
    }

    //Agreement parts for later
    private void showAgreement() {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar_MinWidth);
        builder.setMessage(getAgreement())
                .setTitle("Workers Agreement")
                .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Toast.makeText(RegisterActivity.this, "Accepted", Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Decline", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Toast.makeText(RegisterActivity.this, "Declined", Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    }
                })
                .setCancelable(false)
                .setIcon(R.drawable.ic_assignment_black_24dp);
        // Create the AlertDialog object and return it
        builder.create().show();
    }

    private CharSequence getAgreement() {
        String txt = "";
        BufferedReader br;
        try {
            br = new BufferedReader(new InputStreamReader(getAssets().open("Worker_Agreement")));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
            }
            txt = sb.toString();
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return txt;
    }

    private void setupFbAuth() {
        mCallbackManager = CallbackManager.Factory.create();
        bFbLogin = findViewById(R.id.b_fb);
        bFbLogin.setReadPermissions("email", "public_profile", "user_birthday");
        bFbLogin.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                // ...
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                // ...
            }
        });
    }


    private void setupForm() {
        etEmailLayout = findViewById(R.id.et_email);
        etPwLayout = findViewById(R.id.et_pw);
        etEmailField = etEmailLayout.getEditText();
        etPwField = etPwLayout.getEditText();
        tvPwMeter = findViewById(R.id.tv_pw_strength);

        attachKeyboardListeners(); //Work-around to make tvPwMeter Visible

        etEmailField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                //TODO-wisam: Use relative to support arabic
                boolean drawableAdded = etEmailField.getCompoundDrawables()[2] != null;
                if (s.length() == 0 && drawableAdded) {
                    etEmailField.setCompoundDrawablePadding(0);
                    etEmailField.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                    return;
                }
                if (s.length() < 5) {
                    etEmailField.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.ic_cross_circle), null);
                } else if (s.toString().matches(EMAIL_REGEX)) {
                    etEmailField.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.ic_check_circle), null);
                } else {
                    etEmailField.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.ic_cross_circle), null);
                }
            }
        });

        etPwField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                ((LinearLayout) tvPwMeter.getParent()).setVisibility(View.VISIBLE);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                //TODO-wisam: change to resources
                String pw = s.toString();
                if (pw.matches(PASSWORD_REGEX_STRONG)) {
                    tvPwMeter.setText("STRONG");
                    tvPwMeter.setTextColor(getResources().getColor(R.color.colorPwStrong));
                    return;
                } else if (pw.matches(PASSWORD_REGEX_FAIR)) {
                    tvPwMeter.setText("FAIR");
                    tvPwMeter.setTextColor(getResources().getColor(R.color.colorPwFair));
                    return;
                } else if (pw.matches(PASSWORD_REGEX_MED)) {
                    tvPwMeter.setText("MEDIUM");
                    tvPwMeter.setTextColor(getResources().getColor(R.color.colorPwMedium));
                    return;
                } else if (pw.length() > 5) {
                    tvPwMeter.setText("WEAK");
                    tvPwMeter.setTextColor(getResources().getColor(R.color.colorPwWeak));
                } else {
                    tvPwMeter.setText("SHORT");
                    tvPwMeter.setTextColor(getResources().getColor(R.color.colorMaroon));
                }
            }
        });

        findViewById(R.id.b_signup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAndRegister();
            }
        });

    }

    //A work-around to solve some issues with softInput hiding required views
    private boolean keyboardListenersAttached = false;
    private ViewGroup rootLayout;

    protected void attachKeyboardListeners() {
        if (keyboardListenersAttached) {
            return;
        }
        rootLayout = findViewById(R.id.root_layout);
        rootLayout.getViewTreeObserver().addOnGlobalLayoutListener(keyboardLayoutListener);

        keyboardListenersAttached = true;
    }

    private ViewTreeObserver.OnGlobalLayoutListener keyboardLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            Rect r = new Rect(); //Visible container rectangle
            int xy[] = {0, 0}; //The required view coords

            rootLayout.getWindowVisibleDisplayFrame(r);
            tvPwMeter.getLocationOnScreen(xy);

            if (r.height() < xy[1]) { //The view is obscured and not within the rectangle so scroll to make it visible
                rootLayout.scrollBy(0, xy[1] - r.height());
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (keyboardListenersAttached) {
            rootLayout.getViewTreeObserver().removeOnGlobalLayoutListener(keyboardLayoutListener);
        }
    }
}
