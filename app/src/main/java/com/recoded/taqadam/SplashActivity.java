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
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.recoded.taqadam.models.Api.Api;
import com.recoded.taqadam.models.Api.ApiError;
import com.recoded.taqadam.models.AppVersion;
import com.recoded.taqadam.models.User;
import com.recoded.taqadam.models.auth.UserAuthHandler;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

//import com.google.firebase.crash.FirebaseCrash;

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
            mLogo.animate().yBy(-400f)
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
                                            //isConnected();
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
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UserAuthHandler.init(this);

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
                SplashActivity.this.user = user;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(200);
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
        }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                ApiError ex = (ApiError) e;
                Toast.makeText(SplashActivity.this, ex.getStatusCode() + ": " + ex.getMessage(), Toast.LENGTH_LONG).show();
                finish();
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
                //FirebaseCrash.setCrashCollectionEnabled(false);
                //Crashlytics.getInstance().crash();
                startApp();
                return;
            }
            Call<List<AppVersion>> call = Api.getInstance().endpoints.getLatestVersions((long) BuildConfig.VERSION_CODE);
            call.enqueue(new Callback<List<AppVersion>>() {
                @Override
                public void onResponse(Call<List<AppVersion>> call, Response<List<AppVersion>> response) {
                    List<AppVersion> list = response.body();
                    if (list != null) {
                        AppVersion latest = new AppVersion();
                        latest.code = (long) BuildConfig.VERSION_CODE;
                        latest.required = false;
                        for (AppVersion version : list) {
                            if (version.code > latest.code) {
                                latest.code = version.code;
                                latest.version = version.version;
                            }
                            if (version.code > BuildConfig.VERSION_CODE && version.required) {
                                latest.required = true;
                            }
                        }

                        if (latest.code > BuildConfig.VERSION_CODE) {
                            showUpdateDialog(latest);
                        } else {
                            startApp();
                        }
                    } else {
                        Crashlytics.log("Null list from api app versions");
                        startApp();
                    }
                }

                @Override
                public void onFailure(Call<List<AppVersion>> call, Throwable t) {
                    //we have some thing wrong with api
                    Log.d("Version Check", t.getMessage(), t);
                    Crashlytics.logException(t);
                    startApp();
                }
            });
        }
    }

    private void showUpdateDialog(AppVersion appVersion) {
        AlertDialog.Builder b = new AlertDialog.Builder(SplashActivity.this);
        b.setTitle(appVersion.required ? "Update Required" : "Update Available");
        b.setCancelable(false);
        String msg = "New app version " + appVersion.version + " is available.";
        msg += appVersion.required? "You have to update to continue":"Would you like to update?";
        b.setMessage(msg);
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
        if (!appVersion.required) {
            b.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    startApp();
                }
            });
        }
        b.create().show();
    }


    private void gotoIntro() {
        startActivityForResult(new Intent(this, IntroActivity.class), 1500);
    }

    private void start() {
        if (this.user == null) {
            Intent i = new Intent(this, SigninActivity.class);
            startActivity(i);
            finish();
        } else if (this.user.getProfile() == null) {
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
        SharedPreferences preferences = this.getSharedPreferences("config", MODE_PRIVATE);
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
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                return true;
            }
        }
        /*AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setCancelable(false);
        dialog.setTitle(R.string.No_internet);
        dialog.setMessage(R.string.no_internet);
        dialog.setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                isConnected();
            }
        });
        dialog.setNegativeButton(R.string.quit, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        dialog.create().show();*/
        return false;
    }
}
