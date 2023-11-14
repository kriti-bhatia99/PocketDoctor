package com.example.pocketdoctor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    ImageView splashScreenImage;
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        splashScreenImage = findViewById(R.id.splashScreenImage);

        preferences = getSharedPreferences(getString(R.string.pref_title), MODE_PRIVATE);
        String currentUser = preferences.getString(getString(R.string.pref_phone), "");

        Thread splashThread = new Thread()
        {
            @Override
            public void run() {
                try
                {
                    sleep(3000);

                    if (currentUser.equals(""))
                    {
                        // No user is logged in
                        Intent in = new Intent(MainActivity.this, Login.class);
                        startActivity(in);
                    }
                    else
                    {
                        // Some user is logged in
                        Intent in = new Intent(MainActivity.this, Home.class);
                        startActivity(in);
                    }
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }

                finish();
                super.run();
            }
        };

        splashThread.start();
    }

    @Override
    protected void onStart() {
        super.onStart();
        splashScreenImage.animate().alpha(1).setDuration(2000);
    }
}