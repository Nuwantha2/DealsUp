package com.s23010901.dealsup;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class updateProfile extends AppCompatActivity {

    private EditText editUsername, editPassword;
    private Button updateBtn;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;

    private String currentName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        editUsername = findViewById(R.id.editusername);
        editPassword = findViewById(R.id.editpassword);
        updateBtn = findViewById(R.id.button);

        editPassword.setTransformationMethod(new PasswordTransformationMethod());

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        //load current username and password
        loadCurrentName();
        setupBottomNav();

        //update username and password
        updateBtn.setOnClickListener(v -> {
            String newName = editUsername.getText().toString().trim();
            String newPassword = editPassword.getText().toString().trim();

            if (TextUtils.isEmpty(newName)) {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newName.equals(currentName)) {
                updateName(newName);
            }

            if (!newPassword.equals("******") && newPassword.length() >= 6) {
                updatePassword(newPassword);
            } else if (!newPassword.equals("******")) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCurrentName() {
        if (currentUser == null) return;

        db.collection("users").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentName = documentSnapshot.getString("name");
                        if (currentName != null) {
                            editUsername.setText(currentName);
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load name", Toast.LENGTH_SHORT).show());
    }

    private void updateName(String newName) {
        if (currentUser == null) return;

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("name", newName);

        db.collection("users").document(currentUser.getUid())
                .update(updateData)
                .addOnSuccessListener(aVoid -> {
                    currentName = newName;
                    Toast.makeText(this, "Name updated successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to update name", Toast.LENGTH_SHORT).show());
    }

    private void updatePassword(String newPassword) {
        if (currentUser == null) return;

        currentUser.updatePassword(newPassword)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                    editPassword.setText("******");
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to update password", Toast.LENGTH_SHORT).show());
    }

    private void setupBottomNav() {
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
