package com.recoded.taqadam;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.ColorInt;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.recoded.taqadam.models.AppVersion;
import com.recoded.taqadam.models.User;
import com.recoded.taqadam.models.auth.UserAuthHandler;

import java.util.Map;

public class SplashActivity extends AppCompatActivity {

    private Runnable mBackgroundColorAnimation = new Runnable() {
        @Override
        public void run() {
            TypedValue typedValue = new TypedValue();
            Resources.Theme theme = getTheme();
            theme.resolveAttribute(R.attr.SigninActivityBGInv, typedValue, true);
            @ColorInt int colorFrom = typedValue.data;
            theme.resolveAttribute(R.attr.SigninActivityBG, typedValue, true);
            @ColorInt int colorTo = typedValue.data;

            ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
            colorAnimation.setDuration(1500); // milliseconds
            colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator animator) {
                    mContentView.setBackgroundColor((int) animator.getAnimatedValue());
                }

            });
            colorAnimation.start();
        }
    };
    private Runnable mLogoAlphaAnimation = new Runnable() {
        @Override
        public void run() {
            mBackgroundColorAnimation.run();
            mLogo.animate().alpha(1f)
                    .setDuration(500)
                    .setStartDelay(1200)
                    .setInterpolator(new DecelerateInterpolator())
                    .withEndAction(mLogoTranslationAnimation)
                    .start();
        }
    };

    private Runnable mLogoTranslationAnimation = new Runnable() {
        @Override
        public void run() {
            mLogo.animate().yBy(-450f)
                    .setDuration(500)
                    .setStartDelay(200)
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            mPbMain.animate().alpha(1f)
                                    .setDuration(100)
                                    .withEndAction(new Runnable() {
                                        @Override
                                        public void run() {
                                            checkVersion();
                                        }
                                    })
                                    .start();
                        }
                    });
        }
    };

    private View mContentView;
    private ImageView mLogo;
    private ProgressBar mPbMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_splash);
        mContentView = findViewById(R.id.content_layout);
        mLogo = findViewById(R.id.iv_logo);
        mPbMain = findViewById(R.id.pb_main);
        mLogoAlphaAnimation.run();
    }

    private void startApp() {
        UserAuthHandler.getInstance().getInitTask().addOnSuccessListener(this, new OnSuccessListener<User>() {
            @Override
            public void onSuccess(User user) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1000);
                            if (firstRun()) {
                                gotoIntro();
                            } else {
                                start();
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1500) {
            finish();
        }
    }

    private void checkVersion() {
        if (!isConnected()) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setCancelable(false);
            dialog.setTitle(R.string.No_internet);
            dialog.setMessage(R.string.no_internet);
            dialog.setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    checkVersion();
                }
            });
            dialog.setNegativeButton(R.string.quit, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            dialog.create().show();
        } else {
            if (BuildConfig.DEBUG) {
                FirebaseCrash.setCrashCollectionEnabled(false);
                startApp();
                return;
            }
            DatabaseReference updates = FirebaseDatabase.getInstance().getReference()
                    .child("App").child("Updates");
            updates.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() instanceof Map) {
                        AppVersion latestVersion = new AppVersion();
                        latestVersion.versionCode = BuildConfig.VERSION_CODE;
                        latestVersion.required = false;

                        Map<String, Object> value = (Map<String, Object>) dataSnapshot.getValue();
                        for (String verName : value.keySet()) {
                            AppVersion v = new AppVersion();
                            v.versionName = verName.replace('-', '.');
                            Map<String, Object> data = ((Map<String, Object>) value.get(verName));
                            v.required = (Boolean) data.get("required");
                            v.versionCode = ((Long) data.get("code")).intValue();
                            if (v.versionCode > latestVersion.versionCode) {
                                latestVersion.versionCode = v.versionCode;
                                latestVersion.versionName = v.versionName;
                            }
                            if (v.versionCode > BuildConfig.VERSION_CODE && v.required) {
                                latestVersion.required = true;
                            }
                        }

                        if (latestVersion.versionCode > BuildConfig.VERSION_CODE) {
                            showUpdateDialog(latestVersion);
                        } else {
                            startApp();
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d("Splash", "error while getting app versions" + databaseError.getMessage());
                    startApp();
                }
            });
        }
    }

    private void showUpdateDialog(AppVersion appVersion) {
        if (appVersion.required) {
            AlertDialog.Builder b = new AlertDialog.Builder(SplashActivity.this);
            b.setTitle("Update Required");
            b.setCancelable(false);
            b.setMessage("New app version " + appVersion.versionName + " is available. You need to update to continue");
            b.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                    }
                    finish();
                }
            });
            b.create().show();
        } else {
            AlertDialog.Builder b = new AlertDialog.Builder(SplashActivity.this);
            b.setTitle("Update Available");
            b.setCancelable(false);
            b.setMessage("New app version " + appVersion.versionName + " is available. do you want to update?");
            b.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                    }
                    finish();
                }
            });
            b.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    startApp();
                }
            });
            b.create().show();
        }
    }


    private void gotoIntro() {
        startActivityForResult(new Intent(this, IntroActivity.class), 1500);
    }

    private void start() {
        if (UserAuthHandler.getInstance().getCurrentUser() == null) {
            Intent i = new Intent(this, SigninActivity.class);
            startActivity(i);
        } else if (!UserAuthHandler.getInstance().getCurrentUser().isCompleteProfile()) {
            Intent i = new Intent(SplashActivity.this, ConfirmProfileActivity.class);
            startActivity(i);
            finish();
        } else {
            Intent i = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(i);
            finish();
        }
    }

    private boolean firstRun() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isFirstRun = preferences.getBoolean("firstRun", true);
        int themeId = Integer.parseInt(preferences.getString("theme", "1"));
        Lang.setLanguage(preferences.getString("language", ""));
        Theme.setTheme(themeId);
        return isFirstRun;
    }

    private boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }
}
