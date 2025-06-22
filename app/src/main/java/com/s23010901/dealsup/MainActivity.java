package com.s23010901.dealsup;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;

public class MainActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private int progressStatus = 0;
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);

        // Create notification channel for deals
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "deal_channel",
                    "Deal Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for nearby credit card deals");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        progressBar = findViewById(R.id.splashProgress);

        new Thread(() -> {
            while (progressStatus < 100) {
                progressStatus += 1;

                handler.post(() -> progressBar.setProgress(progressStatus));

                try {
                    Thread.sleep(20); // Simulate loading
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            handler.post(() -> {
                // After progress complete, navigate to LoginActivity
                Intent intent = new Intent(MainActivity.this, Login.class);
                startActivity(intent);
                finish();
            });

        }).start();
    }
}
