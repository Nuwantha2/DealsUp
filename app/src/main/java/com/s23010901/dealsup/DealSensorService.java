package com.s23010901.dealsup;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DealSensorService extends Service implements SensorEventListener {

    private static final String TAG = "DealSensorService";
    private static final float DEAL_RADIUS_METERS = 5000f;
    private static final long NOTIFY_COOLDOWN_MS = 30 * 60 * 1000; // 30 minutes

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private SensorManager sensorManager;
    private Sensor accelerometer;

    // Cache to store last notification time per deal
    private final Map<String, Long> dealNotificationCache = new ConcurrentHashMap<>();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created and running...");

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        //check accelerometer sensor available or not
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (accelerometer != null) {
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
                Log.d(TAG, "Accelerometer listener registered.");
            } else {
                Log.w(TAG, "Accelerometer not available.");
            }
        }

        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started.");
        return START_STICKY;
    }

    //detect motions using accelerometer sensor
    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        if (Math.abs(x) > 2 || Math.abs(y) > 2 || Math.abs(z) > 2) {
            Log.d(TAG, "Motion detected: x=" + x + " y=" + y + " z=" + z);
            checkNearbyDeals();
        }
    }

    //check nearby deals
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

            String uid = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
            if (uid == null) return;

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

                            //nearby deal found
                            if (result[0] <= DEAL_RADIUS_METERS) {
                                String dealId = doc.getId();
                                long now = System.currentTimeMillis();
                                long lastNotified = dealNotificationCache.getOrDefault(dealId, 0L);

                                if (now - lastNotified >= NOTIFY_COOLDOWN_MS) {
                                    String dealTitle = doc.getString("title");
                                    Log.d(TAG, "Nearby deal found: " + dealTitle);

                                    sendNotification(dealTitle, "You're near a deal at " + bank + "!");
                                    dealNotificationCache.put(dealId, now);

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
                    }
                });
            });

        }).addOnFailureListener(e -> Log.e(TAG, "Failed to get user location", e));
    }


    //send notifications
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
    public void onDestroy() {
        super.onDestroy();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
            Log.d(TAG, "Sensor listener unregistered.");
        }
    }


    //
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No action required
    }
}
