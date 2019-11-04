package com.recoded.taqadam.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.WebView;
import android.webkit.WebViewClient;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.recoded.taqadam.R;
import com.recoded.taqadam.models.Api.ApiError;
import com.recoded.taqadam.models.Api.InvalidException;
import com.recoded.taqadam.models.Error;
import com.recoded.taqadam.models.User;
import com.recoded.taqadam.models.auth.UserAuthHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.HttpException;

public class RegisterActivity extends BaseActivity {
    private static final String TAG = RegisterActivity.class.getSimpleName();
    private static final String EMAIL_REGEX = "^[a-zA-Z]+[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]{2,}(?<!\\.)$";
    private static final String PASSWORD_REGEX_MED = "^(?=.*[A-Za-z])(?=.*[0-9]).{8,}$";
    private static final String PASSWORD_REGEX_FAIR = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[a-z]).{8,}$";
    private static final String PASSWORD_REGEX_STRONG = "^(?=.*[A-Z])(?=.*[!@#$&*])(?=.*[0-9])(?=.*[a-z]).{8,}$";


    private CallbackManager mCallbackManager;
    private UserAuthHandler mAuth;
    private boolean agreementSigned = false;
    private EditText etNameField, etEmailField, etPwField;
    private TextInputLayout etNameLayout, etEmailLayout, etPwLayout;
    private TextView tvPwMeter;
    private LoginButton bFbLogin;
    private Button bSignin;

    private AlertDialog agreementDialog;

    ProgressDialog mCreatingAccountProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = UserAuthHandler.getInstance();
        //TODO-wisam: add a reloader for user on applictaion start
        if (mAuth.getCurrentUser() != null) {
            if (mAuth.getCurrentUser().getProfile() == null) {
                startActivity(new Intent(this, ConfirmProfileActivity.class));
            } else {
                Intent i = new Intent(this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
            }
            finish();
        }

        setContentView(R.layout.activity_signup);

        showAgreement();
        setupForm();
        setupFbAuth();
        bSignin = findViewById(R.id.b_signin);
        bSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, SigninActivity.class));
                finish();
            }
        });

        mCreatingAccountProgressDialog = new ProgressDialog(this);
        mCreatingAccountProgressDialog.setCancelable(false);
        mCreatingAccountProgressDialog.setTitle(R.string.creatingAccount);
        mCreatingAccountProgressDialog.setCanceledOnTouchOutside(false);
        mCreatingAccountProgressDialog.setMessage(getString(R.string.Please_wait));

    }

    private void resetPassword(String mSubmittedEmail) {
        //todo implement this!
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

        String validationMsgs[] = new String[7];
        validationMsgs[0] = getString(R.string.short_email_errorm);
        validationMsgs[1] = getString(R.string.email_format_errorm);
        validationMsgs[2] = getString(R.string.short_pass_errorm);
        validationMsgs[3] = getString(R.string.pass_instruction_errorm);
        validationMsgs[4] = getString(R.string.write_email_errorm);
        validationMsgs[5] = getString(R.string.short_name_errorm);
        validationMsgs[6] = getString(R.string.long_name_errom);

        String name = etNameField.getText().toString();
        String emailAddr = etEmailField.getText().toString();
        String password = etPwField.getText().toString();
        if (name.length() == 0) {
            etNameLayout.setError(validationMsgs[5]);
        } else if (name.length() > 20) {
            etNameLayout.setError(validationMsgs[6]);
        } else if (emailAddr.length() == 0) {
            etEmailLayout.setError(validationMsgs[4]);
        } else if (emailAddr.length() < 5) {
            etEmailLayout.setError(validationMsgs[0]);
        } else if (!emailAddr.matches(EMAIL_REGEX)) {
            etEmailLayout.setError(validationMsgs[1]);
        } else if (password.length() < 8) {
            etEmailLayout.setError("");
            etPwLayout.setError(validationMsgs[2]);
        } else if (!password.matches(PASSWORD_REGEX_MED)) {
            etEmailLayout.setError("");
            etPwLayout.setError(validationMsgs[3]);
        } else {
            etEmailLayout.setError("");
            etPwLayout.setError("");
            handleEmailAndPassword(name, emailAddr, password);
        }
    }

    private void handleEmailAndPassword(String name, final String email, String pw) {
        mCreatingAccountProgressDialog.show();

        mAuth.register(name, email, pw).addOnSuccessListener(this, new OnSuccessListener<User>() {
            @Override
            public void onSuccess(User user) {
                mCreatingAccountProgressDialog.dismiss();
                // Sign up success, update UI with the signed-in user's information
                if (user.getProfile() == null) {
                    startActivity(new Intent(RegisterActivity.this, ConfirmProfileActivity.class));
                } else {
                    Intent i = new Intent(RegisterActivity.this, MainActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(i);
                }
                finish();
            }
        }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mCreatingAccountProgressDialog.dismiss();
                if (e instanceof InvalidException) {
                    //Start building the alert dialog. TODO: Extract strings
                    final AlertDialog.Builder db = new AlertDialog.Builder(RegisterActivity.this);
                    db.setIcon(getResources().getDrawable(R.drawable.ic_error_black));
                    db.setTitle(R.string.error);
                    db.setCancelable(false);
                    Map<String, List<Error>> errorsMap = ((InvalidException) e).getErrors();
                    List<Error> errors = errorsMap.containsKey("email") ? errorsMap.get("email") : new ArrayList<Error>();
                    for (Error error : errors) {
                        if (error.getKey().equals("email") && error.getMessage().contains("User email is already registered")) {
                            db.setMessage(R.string.already_registed_signin_errorm);
                            db.setPositiveButton(R.string.sign_in, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    bSignin.performClick();
                                }
                            });
                            db.setNeutralButton(R.string.forgot_password_action, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //TODO: implement the intent to pw reset activity
                                    dialog.dismiss();
                                    resetPassword(email);
                                }
                            });
                            db.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });

                            db.create().show();
                            break;
                        }
                    }

                    indicateErrors((ApiError) e);
                    /*
                    if (ex.getErrors() == AuthSignUpException.EMAIL_ASSOC_FB_PW_WRONG) {
                        //if this email is associated with both password and fb, he can sign in with fb or reset password
                        db.setMessage(R.string.allready_registed_email_errorm);
                        db.setPositiveButton(R.string.sign_in_with_facebook, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                bFbLogin.performClick();
                            }
                        });
                        db.setNeutralButton(R.string.forgot_password_action, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //TODO: implement the intent to pw reset activity
                                dialog.dismiss();
                                resetPassword(email);
                            }
                        });
                        db.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                        db.create().show();

                    } else if (ex.getErrorCode() == AuthSignUpException.PW_WRONG) {
                        //else if associated with only password means he already signed up. show him only reset pw
                        db.setMessage(R.string.already_registed_signin_errorm);
                        db.setPositiveButton(R.string.sign_in, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                bSignin.performClick();
                            }
                        });
                        db.setNeutralButton(R.string.forgot_password_action, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //TODO: implement the intent to pw reset activity
                                dialog.dismiss();
                                resetPassword(email);
                            }
                        });
                        db.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                        db.create().show();

                    } else if (ex.getErrorCode() == AuthSignUpException.EMAIL_ASSOC_FB) {
                        //else if associated with fb only, offer him to signin with fb
                        db.setMessage(R.string.signin_facebook_instead_errorm);
                        db.setPositiveButton(R.string.signin_facebook, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                bFbLogin.performClick();
                            }
                        });

                        db.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                        db.create().show();
                    }
                    */

                } else if (e instanceof ApiError){
                    Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                } else if (e instanceof HttpException) {
                    Toast.makeText(RegisterActivity.this, R.string.network_error, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(RegisterActivity.this, R.string.resolving_errorm, Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    private void indicateErrors(ApiError e) {
    }

    private void handleFacebookAccessToken(AccessToken token) {
        if (!agreementSigned) {
            showAgreement();
            return;
        }
        mCreatingAccountProgressDialog.show();
        mAuth.login(token).addOnSuccessListener(this, new OnSuccessListener<User>() {
            @Override
            public void onSuccess(User user) {
                mCreatingAccountProgressDialog.dismiss();
                // Sign up success, update UI with the signed-in user's information
                if (user.getProfile() == null) {
                    startActivity(new Intent(RegisterActivity.this, ConfirmProfileActivity.class));
                } else {
                    Intent i = new Intent(RegisterActivity.this, MainActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(i);
                }
                finish();
            }
        }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mCreatingAccountProgressDialog.dismiss();
                Toast.makeText(RegisterActivity.this, R.string.error_signin_with_fb_errorm, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showAgreement() {
        if (agreementDialog == null) {
            LinearLayout root = new LinearLayout(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            root.setLayoutParams(lp);
            WebView wv = new WebView(this);
            WebViewClient wvc = new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    agreementDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    agreementDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(true);
                }
            };
            wv.setWebViewClient(wvc);
            wv.loadUrl("https://taqadam.io/docs/TrainerAgreement");
            root.addView(wv);

            AlertDialog.Builder b = new AlertDialog.Builder(this);
            b.setTitle("Trainer Agreement");
            b.setView(root);
            b.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    agreementSigned = true;
                    dialog.dismiss();
                }
            });
            b.setNegativeButton(R.string.decline, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Toast.makeText(RegisterActivity.this, "You need to accept the agreement to continue", Toast.LENGTH_LONG).show();
                    finish();
                    dialog.dismiss();
                }
            });
            b.setCancelable(false);
            b.setIcon(R.drawable.ic_agreement_purple_48dp);

            agreementDialog = b.create();
        }
        agreementDialog.show();
        agreementDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
        agreementDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(false);
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
        etNameLayout = findViewById(R.id.et_name);
        etEmailLayout = findViewById(R.id.et_email);
        etPwLayout = findViewById(R.id.et_pw);
        etNameField = etNameLayout.getEditText();
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
                    etEmailField.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.ic_cross_maroon), null);
                } else if (s.toString().matches(EMAIL_REGEX)) {
                    etEmailField.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.ic_check_green), null);
                } else {
                    etEmailField.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.ic_cross_maroon), null);
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
                    tvPwMeter.setText(R.string.pas_strong);
                    tvPwMeter.setTextColor(getResources().getColor(R.color.colorPwStrong));
                    return;
                } else if (pw.matches(PASSWORD_REGEX_FAIR)) {
                    tvPwMeter.setText(R.string.pas_fair);
                    tvPwMeter.setTextColor(getResources().getColor(R.color.colorPwFair));
                    return;
                } else if (pw.matches(PASSWORD_REGEX_MED)) {
                    tvPwMeter.setText(R.string.pas_medium);
                    tvPwMeter.setTextColor(getResources().getColor(R.color.colorPwMedium));
                    return;
                } else if (pw.length() > 5) {
                    tvPwMeter.setText(R.string.pas_weak);
                    tvPwMeter.setTextColor(getResources().getColor(R.color.colorPwWeak));
                } else {
                    tvPwMeter.setText(R.string.pas_short);
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
        if (agreementDialog != null && agreementDialog.isShowing()) {
            agreementDialog.dismiss();
        }
        super.onDestroy();

        if (keyboardListenersAttached) {
            rootLayout.getViewTreeObserver().removeOnGlobalLayoutListener(keyboardLayoutListener);
        }
    }
}
