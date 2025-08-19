package com.s23010901.dealsup;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class profile extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    Button updateProfile, updateCards, notificationPref, contactAdmin, deleteAccount, signOut;
    TextView usernameText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        // connect UI
        updateProfile = findViewById(R.id.updateProfile);
        updateCards = findViewById(R.id.UpdateCards);
        notificationPref = findViewById(R.id.notificationPref);
        contactAdmin = findViewById(R.id.contactAdmin);
        deleteAccount = findViewById(R.id.deleteAccount);
        signOut = findViewById(R.id.signOut);
        usernameText = findViewById(R.id.username);

        loadUserName(); // Load username

        // Setup navigation
        updateProfile.setOnClickListener(v -> startActivity(new Intent(this, updateProfile.class)));
        updateCards.setOnClickListener(v -> startActivity(new Intent(this, chooseCards.class)));
        notificationPref.setOnClickListener(v -> startActivity(new Intent(this, notificationPreference.class)));

        //contact admin
        contactAdmin.setOnClickListener(v -> {
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("message/rfc822");
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"nuwanthadanajayabandara@gmail.com"});
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "DealsUp App Support");
            emailIntent.putExtra(Intent.EXTRA_TEXT, "Hi Admin,\n\nI need help with...");

            try {
                startActivity(Intent.createChooser(emailIntent, "Send Email to Admin..."));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(this, "No email clients installed.", Toast.LENGTH_SHORT).show();
            }
        });

        //signout
        signOut.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(this, "Signed out", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        //delete account
        deleteAccount.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Confirm Delete")
                    .setMessage("Are you sure you want to permanently delete your account?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        if (currentUser != null) {
                            String uid = currentUser.getUid();
                            db.collection("users").document(uid).delete()
                                    .addOnSuccessListener(unused -> currentUser.delete()
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(this, "Account deleted", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(this, Login.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(intent);
                                                finish();
                                            })
                                            .addOnFailureListener(e -> Toast.makeText(this, "Failed to delete account", Toast.LENGTH_SHORT).show()))
                                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to delete user data", Toast.LENGTH_SHORT).show());
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        setupBottomNavigation();
    }

    //load username
    private void loadUserName() {
        if (currentUser != null) {
            String uid = currentUser.getUid();
            db.collection("users").document(uid).get()
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot.exists()) {
                            String name = snapshot.getString("name");
                            if (name != null && !name.isEmpty()) {
                                usernameText.setText(name + " !");
                            }
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to load name", Toast.LENGTH_SHORT).show());
        }
    }

    //bottom navigations
    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_profile);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, Dashboard.class));
                return true;
            } else if (id == R.id.nav_location) {
                startActivity(new Intent(this, map.class));
                return true;
            } else if (id == R.id.nav_add) {
                startActivity(new Intent(this, add.class));
                return true;
            } else if (id == R.id.nav_profile) {
                return true;
            }
            return false;
        });
    }
}
