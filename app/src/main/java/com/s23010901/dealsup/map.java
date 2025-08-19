package com.s23010901.dealsup;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.*;

public class map extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Location userLocation;
    private List<String> userBanks = new ArrayList<>();

    private final Map<Marker, Deal> markerDealMap = new HashMap<>();

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final double RADIUS_KM = 10.0;

    @Override
    //create map
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        requestLocationPermission();
        setupBottomNav();
    }

    //request location permission
    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            fetchLocation();
        }
    }


    //get location
    private void fetchLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                userLocation = location;

                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.mapNearby);
                if (mapFragment != null) {
                    mapFragment.getMapAsync(this);
                }

                loadUserBanksAndDeals();
            } else {
                Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //load deals
    private void loadUserBanksAndDeals() {
        String uid = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (uid == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<String> cards = (List<String>) snapshot.get("creditCards");
                    if (cards != null) {
                        userBanks = cards;
                        loadNearbyDeals();
                    }
                });
    }

    //check and load nearby deals
    private void loadNearbyDeals() {
        if (userLocation == null) return;

        db.collection("deals")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Deal deal = doc.toObject(Deal.class);

                        if (deal.getLatitude() == null || deal.getLongitude() == null) continue;
                        if (!userBanks.contains(deal.getBank())) continue;

                        Location dealLocation = new Location("");
                        dealLocation.setLatitude(deal.getLatitude());
                        dealLocation.setLongitude(deal.getLongitude());

                        float distance = userLocation.distanceTo(dealLocation) / 1000f;

                        if (distance <= RADIUS_KM) {
                            LatLng latLng = new LatLng(deal.getLatitude(), deal.getLongitude());
                            Marker marker = mMap.addMarker(new MarkerOptions()
                                    .position(latLng)
                                    .title(deal.getTitle()));
                            if (marker != null) {
                                markerDealMap.put(marker, deal);
                            }
                        }
                    }

                    LatLng userLatLng = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 13));
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load deals", Toast.LENGTH_SHORT).show()
                );
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        mMap.setOnInfoWindowClickListener(marker -> {
            Deal selectedDeal = markerDealMap.get(marker);
            if (selectedDeal != null) {
                Intent intent = new Intent(this, dealMap.class);
                intent.putExtra("deal", selectedDeal);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Deal not found", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //bottom navigation
    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_location);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, Dashboard.class));
                return true;
            } else if (id == R.id.nav_location) {
                return true;
            } else if (id == R.id.nav_add) {
                startActivity(new Intent(this, add.class));
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, profile.class));
                return true;
            }
            return false;
        });
    }

    //get user location
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchLocation();
        } else {
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
        }
    }
}
