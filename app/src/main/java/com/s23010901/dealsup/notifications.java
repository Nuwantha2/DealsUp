package com.s23010901.dealsup;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class notifications extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<NotificationItem> notifications;
    private FirebaseFirestore db;
    private String userId;

    //display notifications
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        recyclerView = findViewById(R.id.recyclerNotifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        notifications = new ArrayList<>();
        adapter = new NotificationAdapter(notifications);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadNotifications();
    }

    //load notifications
    private void loadNotifications() {
        db.collection("notifications")
                .document(userId)
                .collection("user_notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    notifications.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        NotificationItem item = doc.toObject(NotificationItem.class);
                        notifications.add(item);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load notifications: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
