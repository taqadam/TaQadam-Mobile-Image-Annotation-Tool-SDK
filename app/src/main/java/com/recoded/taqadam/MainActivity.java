package com.recoded.taqadam;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                boolean isFirstTime = preferences.getBoolean("first", true);

                if (isFirstTime) {
                    startActivity(new Intent(MainActivity.this, IntroActivity.class));
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("first", false);
                    editor.apply();
                }
            }
        });
        thread.start();


        findViewById(R.id.sign_up).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, RegisterActivity.class));
            }
        });
        findViewById(R.id.task).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, TaskActivity.class));
            }
        });

        findViewById(R.id.profile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, SigninActivity.class));
            }
        });

        findViewById(R.id.payment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, ConfirmProfileActivity.class);
                i.putExtra("EDIT_MODE", true);
                startActivity(i);
            }
        });
    }
}
