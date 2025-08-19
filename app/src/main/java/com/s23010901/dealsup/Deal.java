package com.s23010901.dealsup;

import android.os.Parcel;
import android.os.Parcelable;

public class Deal implements Parcelable {
    private String title;
    private String description;
    private String bank;
    private String category;
    private String location;
    private String imageUrl;
    private Double latitude;
    private Double longitude;

    public Deal() {
        // Firestore needs empty constructor
    }




    //get deals data
    public Deal(String title, String description, String bank, String category, String location,
                String imageUrl, double latitude, double longitude) {
        this.title = title;
        this.description = description;
        this.bank = bank;
        this.category = category;
        this.location = location;
        this.imageUrl = imageUrl;
        this.latitude = latitude;
        this.longitude = longitude;
    }


    protected Deal(Parcel in) {
        title = in.readString();
        description = in.readString();
        bank = in.readString();
        category = in.readString();
        location = in.readString();
        imageUrl = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
    }

    public static final Creator<Deal> CREATOR = new Creator<Deal>() {
        @Override
        public Deal createFromParcel(Parcel in) {
            return new Deal(in);
        }

        @Override
        public Deal[] newArray(int size) {
            return new Deal[size];
        }
    };

    // get data from firebase
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getBank() { return bank; }
    public String getCategory() { return category; }
    public String getLocation() { return location; }
    public String getImageUrl() { return imageUrl; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(title);
        parcel.writeString(description);
        parcel.writeString(bank);
        parcel.writeString(category);
        parcel.writeString(location);
        parcel.writeString(imageUrl);
        parcel.writeDouble(latitude);
        parcel.writeDouble(longitude);
    }
}
