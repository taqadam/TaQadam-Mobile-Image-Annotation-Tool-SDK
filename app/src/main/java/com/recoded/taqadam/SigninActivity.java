package com.recoded.taqadam;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;

public class SigninActivity extends AppCompatActivity {
    private static final String TAG = SigninActivity.class.getSimpleName();

    private TextInputLayout etEmailLayout, etPwLayout;
    private EditText etEmailField, etPwField;
    private LoginButton bFbLogin;

    private FirebaseAuth mAuth;
    private CallbackManager mCallbackManager;
    private OnCompleteListener<AuthResult> mAuthCompleteListener;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            //Already signed in
            exit();
        }

        setupAuthListener();
        setupFbAuth();

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

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Signing in");
        progressDialog.setMessage("Please wait..");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

    }

    private void handleEmailAndPw() {
        String email = etEmailField.getText().toString();
        String pw = etPwField.getText().toString();
        if (email.isEmpty() || pw.isEmpty()) {
            indicateError("Please enter both fields");
            return;
        }
        progressDialog.show();
        indicateError("");
        mAuth.signInWithEmailAndPassword(email, pw)
                .addOnCompleteListener(mAuthCompleteListener);
    }

    private void setupAuthListener() {
        mAuthCompleteListener = new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    exit();
                } else {
                    progressDialog.dismiss();
                    indicateError(task.getException().getMessage());
                }

                // ...
            }
        };
    }

    private void handleFacebookAccessToken(AccessToken token) {
        progressDialog.show();
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());

        mAuth.signInWithCredential(credential).addOnCompleteListener(this, mAuthCompleteListener);
    }

    private void setupFbAuth() {
        if (AccessToken.getCurrentAccessToken() != null && !AccessToken.getCurrentAccessToken().isExpired()) {
            handleFacebookAccessToken(AccessToken.getCurrentAccessToken());
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
        Intent i = new Intent(this, ConfirmProfileActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(i);
        finish();
    }
}
