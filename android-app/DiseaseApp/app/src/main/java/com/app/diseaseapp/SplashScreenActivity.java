package com.app.diseaseapp;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashScreenActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 2500; // 2.5 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LanguageManager.applyAppLocale(this);
        setContentView(R.layout.activity_splash_screen);
        new Handler().postDelayed(() -> {
            SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
            boolean hasSeenIntro = prefs.getBoolean("intro_shown", false);

            Class<?> next = hasSeenIntro ? LoginActivity.class : IntroSliderActivity.class;
            startActivity(new Intent(SplashScreenActivity.this, next));
            finish();
        }, 2000); // 2-second delay
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);
//
//        setContentView(R.layout.activity_splash_screen);
//
//        ImageView logo = findViewById(R.id.logo);
//        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
//        logo.startAnimation(fadeIn);
//
//        new Handler().postDelayed(() -> {
//            Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
//            startActivity(intent);
//            finish();
//        }, SPLASH_DURATION);
    }
}
