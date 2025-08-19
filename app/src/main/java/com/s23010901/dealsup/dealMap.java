package com.s23010901.dealsup;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

public class dealMap extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private double latitude, longitude;
    private String title, description, imageUrl;

    private TextView tvTitle, tvDescription;
    private ImageView imgLogo;
    private Button btnNavigate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deal_map);

        tvTitle = findViewById(R.id.tvMapTitle);
        tvDescription = findViewById(R.id.tvMapDescription);
        imgLogo = findViewById(R.id.imgMapLogo);
        btnNavigate = findViewById(R.id.btnNavigate);

        // Get deal data
        title = getIntent().getStringExtra("title");
        description = getIntent().getStringExtra("description");
        imageUrl = getIntent().getStringExtra("imageUrl");
        latitude = getIntent().getDoubleExtra("latitude", 0.0);
        longitude = getIntent().getDoubleExtra("longitude", 0.0);

        tvTitle.setText(title);
        tvDescription.setText(description);
        Glide.with(this).load(imageUrl).placeholder(R.drawable.placeholder).into(imgLogo);

        // Map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapView);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        //navigation button
        btnNavigate.setOnClickListener(v -> {
            String uri = "google.navigation:q=" + latitude + "," + longitude;
            Intent navIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            navIntent.setPackage("com.google.android.apps.maps");
            startActivity(navIntent);
        });
    }

    //view on google maps
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        LatLng shopLocation = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(shopLocation).title(title));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(shopLocation, 16));
    }
}
