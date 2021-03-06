package com.recoded.taqadam.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.textfield.TextInputLayout;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.recoded.taqadam.R;
import com.recoded.taqadam.models.Api.ApiError;
import com.recoded.taqadam.objects.User;
import com.recoded.taqadam.models.auth.UserAuthHandler;

public class SigninActivity extends BaseActivity {
    private static final String TAG = SigninActivity.class.getSimpleName();

    private TextInputLayout etEmailLayout, etPwLayout;
    private EditText etEmailField, etPwField;
    private LoginButton bFbLogin;

    private UserAuthHandler mAuth;
    private CallbackManager mCallbackManager;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(R.string.Signingin);
        progressDialog.setMessage(getString(R.string.Please_wait));
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        mAuth = UserAuthHandler.getInstance();
        if (!mAuth.shouldLogin()) {
            //Already signed in
            exit();
        }

        //setupFbAuth();

        etEmailLayout = findViewById(R.id.et_email);
        etPwLayout = findViewById(R.id.et_pw);

        etEmailField = etEmailLayout.getEditText();
        etPwField = etPwLayout.getEditText();

        findViewById(R.id.b_signin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleEmailAndPw();
            }
        });

        findViewById(R.id.b_signup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SigninActivity.this, RegisterActivity.class));
                finish();
            }
        });

    }

    private void handleEmailAndPw() {
        final String email = etEmailField.getText().toString();
        String pw = etPwField.getText().toString();
        if (email.isEmpty() || pw.isEmpty()) {
            indicateError(getString(R.string.please_enter_both_fields));
            return;
        }
        progressDialog.show();
        indicateError("");
        mAuth.login(email, pw)
                .addOnSuccessListener(this, new OnSuccessListener<User>() {
                    @Override
                    public void onSuccess(User user) {
                        progressDialog.dismiss();
                        exit();
                    }
                }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                String msg = e.getMessage();
                int status = ((ApiError) e).getStatusCode();
                if (status == 401) {
                    indicateError(msg);
                } else {
                    indicateError(msg);
                }
            }
        });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        progressDialog.show();
        mAuth.login(token).addOnSuccessListener(this, new OnSuccessListener<User>() {
            @Override
            public void onSuccess(User user) {
                progressDialog.dismiss();
                // Sign up success, update UI with the signed-in user's information
                exit();
            }
        }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                indicateError(e.getMessage());
            }
        });
    }

    private void setupFbAuth() {
        //Redundant
        if (AccessToken.getCurrentAccessToken() != null && !AccessToken.getCurrentAccessToken().isExpired()) {
            //handleFacebookAccessToken(AccessToken.getCurrentAccessToken());
            return;
        }
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void indicateError(String error) {
        //TODO-wisam set arabic
        ((TextView) findViewById(R.id.tv_error)).setText(error);
    }

    private void exit() {
        Intent i = new Intent(this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(i);
        finish();
    }
}
