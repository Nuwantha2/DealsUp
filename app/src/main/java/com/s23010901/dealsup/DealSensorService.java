package com.s23010901.dealsup;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class DealSensorService extends Service {

    private static final String TAG = "DealSensorService";
    private static final float DEAL_RADIUS_METERS = 5000f; // Radius to check for nearby deals

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created and running...");
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        createNotificationChannel();
        checkNearbyDeals();          // Begin location-based check
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started.");
        return START_STICKY;
    }

    private void checkNearbyDeals() {
        FusedLocationProviderClient locationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Location permission not granted.");
            return;
        }

        locationClient.getLastLocation().addOnSuccessListener(userLocation -> {
            if (userLocation == null) {
                Log.w(TAG, "User location is null");
                return;
            }

            String uid = mAuth.getCurrentUser().getUid();

            db.collection("users").document(uid).get().addOnSuccessListener(userSnapshot -> {
                List<String> userBanks = (List<String>) userSnapshot.get("creditCards");

                db.collection("deals").get().addOnSuccessListener(dealSnapshot -> {
                    for (QueryDocumentSnapshot doc : dealSnapshot) {
                        String bank = doc.getString("bank");
                        Double lat = doc.getDouble("latitude");
                        Double lon = doc.getDouble("longitude");

                        if (lat == null || lon == null || bank == null) continue;

                        if (userBanks != null && userBanks.contains(bank)) {
                            float[] result = new float[1];
                            Location.distanceBetween(
                                    userLocation.getLatitude(), userLocation.getLongitude(),
                                    lat, lon,
                                    result
                            );

                            if (result[0] <= DEAL_RADIUS_METERS) {
                                String dealTitle = doc.getString("title");

                                Log.d(TAG, "Nearby deal found: " + dealTitle);

                                sendNotification(dealTitle, "You're near a deal at " + bank + "!");

                                Map<String, Object> notif = new HashMap<>();
                                notif.put("title", "Nearby Deal Alert");
                                notif.put("message", dealTitle);
                                notif.put("timestamp", FieldValue.serverTimestamp());

                                db.collection("notifications")
                                        .document(uid)
                                        .collection("user_notifications")
                                        .add(notif);
                            }
                        }
                    }
                });
            });

        }).addOnFailureListener(e -> Log.e(TAG, "Failed to get user location", e));
    }

    private void sendNotification(String title, String message) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "POST_NOTIFICATIONS permission not granted.");
            return;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "deal_channel")
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        manager.notify((int) System.currentTimeMillis(), builder.build());
    }

    //send notifications
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "deal_channel",
                    "Deal Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // Not bound service
    }
}
