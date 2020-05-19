package com.allan.gcreationz;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class SplashScreenActivity extends AppCompatActivity {

    private ImageView logo;
    private SharedPreferences sp;
    private boolean status, isFirstTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        logo = findViewById(R.id.logo);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in_anim);
        logo.startAnimation(fadeIn);
        sp = getSharedPreferences("mypref", 0);
        isFirstTime = sp.getBoolean("isFirstTime", true);
        if (!isFirstTime) {
            Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            status = sp.getBoolean("isLoggedIn", false);
            if (status) {
                Intent intent = new Intent(SplashScreenActivity.this, DashActivity.class);
                startActivity(intent);
                finish();
            } else {
                Intent intent = new Intent(SplashScreenActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }
}
