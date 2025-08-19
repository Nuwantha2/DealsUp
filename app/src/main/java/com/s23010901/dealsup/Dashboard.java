package com.s23010901.dealsup;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class Dashboard extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private TextView username;
    private RecyclerView recyclerView;
    private Spinner spinnerBank, spinnerCategory, spinnerLocation;
    private ImageView notificationIcon;

    private DealAdapter dealAdapter;
    private final List<Deal> allDeals = new ArrayList<>();

    private List<String> userBanks = new ArrayList<>();
    private String selectedBank = "Choose bank";
    private String selectedCategory = "Choose category";
    private String selectedLocation = "Choose location";

    // 🔹 Static cache to hold deals in memory
    private static List<Deal> cachedDeals = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // get username,deals and category,bank and location data
        username = findViewById(R.id.username);
        recyclerView = findViewById(R.id.recyclerDeals);
        spinnerBank = findViewById(R.id.spinnerCardType);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerLocation = findViewById(R.id.spinnerLocation);
        notificationIcon = findViewById(R.id.notificationIcon);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        dealAdapter = new DealAdapter(this, allDeals);
        recyclerView.setAdapter(dealAdapter);

        // Open Notifications screen
        notificationIcon.setOnClickListener(v -> startActivity(new Intent(this, notifications.class)));

        // Ask for notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        setupStaticSpinners();
        fetchUserData();

        // Bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) return true;
            else if (itemId == R.id.nav_location) startActivity(new Intent(this, map.class));
            else if (itemId == R.id.nav_add) startActivity(new Intent(this, add.class));
            else if (itemId == R.id.nav_profile) startActivity(new Intent(this, profile.class));
            return true;
        });
    }

    // Setup filters
    private void setupStaticSpinners() {
        List<String> categories = new ArrayList<>();
        categories.add("Choose category");
        categories.add("Food");
        categories.add("Clothing");
        categories.add("Electronics");

        List<String> locations = new ArrayList<>();
        locations.add("Choose location");
        locations.add("Colombo");
        locations.add("Kandy");
        locations.add("Galle");

        setupSpinner(spinnerCategory, categories);
        setupSpinner(spinnerLocation, locations);

        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                selectedCategory = spinnerCategory.getSelectedItem().toString();
                selectedLocation = spinnerLocation.getSelectedItem().toString();
                applyFilters(); // 🔹 Filter cached deals instantly
                refreshDeals(); // 🔹 Update from Firestore in background
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        spinnerCategory.setOnItemSelectedListener(listener);
        spinnerLocation.setOnItemSelectedListener(listener);
    }

    private void setupSpinner(Spinner spinner, List<String> data) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, data);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //  Show cached deals first (for fast loading)
        if (!cachedDeals.isEmpty()) {
            allDeals.clear();
            allDeals.addAll(cachedDeals);
            dealAdapter.notifyDataSetChanged();
        }

        // Then fetch new deals from Firebase in background
        refreshDeals();
    }

    // Display username + banks
    private void fetchUserData() {
        String uid = mAuth.getCurrentUser().getUid();

        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        username.setText((name != null ? name : "User") + "!");

                        List<String> creditCards = (List<String>) documentSnapshot.get("creditCards");
                        if (creditCards != null) {
                            userBanks = creditCards;

                            List<String> bankOptions = new ArrayList<>();
                            bankOptions.add("Choose bank");
                            bankOptions.addAll(userBanks);

                            setupSpinner(spinnerBank, bankOptions);

                            spinnerBank.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                                    selectedBank = spinnerBank.getSelectedItem().toString();
                                    applyFilters();
                                    refreshDeals();
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {}
                            });

                            // Load deals initially
                            onResume();

                            // Start DealSensorService
                            Intent serviceIntent = new Intent(Dashboard.this, DealSensorService.class);
                            startService(serviceIntent);
                        }
                    } else {
                        username.setText("User!");
                        Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    username.setText("User!");
                    Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                });
    }

    // fillters
    private void applyFilters() {
        allDeals.clear();
        for (Deal deal : cachedDeals) {
            boolean matchesBank = selectedBank.equals("Choose bank") || deal.getBank().equalsIgnoreCase(selectedBank);
            boolean matchesCategory = selectedCategory.equals("Choose category") || deal.getCategory().equalsIgnoreCase(selectedCategory);
            boolean matchesLocation = selectedLocation.equals("Choose location") || deal.getLocation().equalsIgnoreCase(selectedLocation);

            if (userBanks.contains(deal.getBank()) && matchesBank && matchesCategory && matchesLocation) {
                allDeals.add(deal);
            }
        }
        dealAdapter.notifyDataSetChanged();
    }

    // get latest deals from firebase
    private void refreshDeals() {
        db.collection("deals").get().addOnSuccessListener(querySnapshot -> {
            List<Deal> freshDeals = new ArrayList<>();
            for (QueryDocumentSnapshot doc : querySnapshot) {
                Deal deal = doc.toObject(Deal.class);
                freshDeals.add(deal);
            }


            cachedDeals.clear();
            cachedDeals.addAll(freshDeals);

            applyFilters();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to load deals", Toast.LENGTH_SHORT).show();
        });
    }
}
