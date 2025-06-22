package com.s23010901.dealsup;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class add extends AppCompatActivity {

    private EditText etTitle, etDescription, etImageUrl;
    private Spinner spinnerBank, spinnerCategory;
    private TextView tvLocation;
    private Button btnPickLocation, btnSubmit;

    private double selectedLat = 0.0;
    private double selectedLng = 0.0;

    private FirebaseFirestore db;

    private static final int LOCATION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        db = FirebaseFirestore.getInstance();

        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etImageUrl = findViewById(R.id.etImageUrl);
        spinnerBank = findViewById(R.id.spinnerBank);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        tvLocation = findViewById(R.id.tvLocation);
        btnPickLocation = findViewById(R.id.btnPickLocation);
        btnSubmit = findViewById(R.id.btnSubmit);

        setupSpinners();
        setupLocationPicker();
        setupBottomNav();

        btnSubmit.setOnClickListener(v -> submitDeal());
    }

    //dropdowns to choose bank and category
    private void setupSpinners() {
        ArrayAdapter<String> bankAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                Arrays.asList("Select Bank", "HNB", "BOC", "Sampath", "NDB", "Commercial"));
        bankAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBank.setAdapter(bankAdapter);

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                Arrays.asList("Select Category", "Food", "Clothing", "Electronics", "Services"));
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);
    }

    //location selector
    private void setupLocationPicker() {
        btnPickLocation.setOnClickListener(v -> {
            Intent intent = new Intent(add.this, locationPicker.class);
            startActivityForResult(intent, LOCATION_REQUEST_CODE);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LOCATION_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            selectedLat = data.getDoubleExtra("latitude", 0.0);
            selectedLng = data.getDoubleExtra("longitude", 0.0);

            tvLocation.setText("Location: " + selectedLat + ", " + selectedLng);
        }
    }

    private void submitDeal() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String imageUrl = etImageUrl.getText().toString().trim();
        String bank = spinnerBank.getSelectedItem().toString();
        String category = spinnerCategory.getSelectedItem().toString();

        if (title.isEmpty() || description.isEmpty() || imageUrl.isEmpty() ||
                bank.equals("Select Bank") || category.equals("Select Category") ||
                selectedLat == 0.0 || selectedLng == 0.0) {
            Toast.makeText(this, "Please fill all fields and select location", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> deal = new HashMap<>();
        deal.put("title", title);
        deal.put("description", description);
        deal.put("imageUrl", imageUrl);
        deal.put("bank", bank);
        deal.put("category", category);
        deal.put("latitude", selectedLat);
        deal.put("longitude", selectedLng);

        db.collection("deals")
                .add(deal)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Deal added successfully!", Toast.LENGTH_SHORT).show();
                    clearForm();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to add deal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void clearForm() {
        etTitle.setText("");
        etDescription.setText("");
        etImageUrl.setText("");
        spinnerBank.setSelection(0);
        spinnerCategory.setSelection(0);
        tvLocation.setText("No location selected");
        selectedLat = 0.0;
        selectedLng = 0.0;
    }


    //bottom navigation
    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_add);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, Dashboard.class));
                return true;
            } else if (id == R.id.nav_location) {
                startActivity(new Intent(this, map.class));
                return true;
            } else if (id == R.id.nav_add) {
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, profile.class));
                return true;
            }
            return false;
        });
    }
}
