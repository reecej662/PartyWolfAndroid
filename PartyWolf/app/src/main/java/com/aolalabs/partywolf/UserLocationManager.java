package com.aolalabs.partywolf;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

import java.util.List;
import java.util.Locale;

/**
 * Created by reecejackson on 9/8/15.
 *
 */

public class UserLocationManager {
    private Location userLocation;
    private ParseUser currentUser;
    private UserLocationListener listener;
    private Context context;
    private boolean locationAvailable = false;
    private boolean firstLoad = true;

    // Constructor

    public UserLocationManager(Context context) {
        this.context = context;
        this.currentUser = ParseUser.getCurrentUser();
    }

    // Set up location

    public void getLocation() {

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        try {
            userLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            currentUser.put("currentLocation", getParseLocation());
            currentUser.put("currentCity", getCity(userLocation));
            locationAvailable = true;

            if(firstLoad) {
                listener.locationFound();
                firstLoad = false;
            } else {
                listener.locationUpdated();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.d("Location Manager", "Error getting last location");
            ParseGeoPoint parseLocation = currentUser.getParseGeoPoint("currentLocation");
        }

        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                double lat = location.getLatitude();
                double lng = location.getLongitude();

                userLocation = location;
                currentUser.put("currentLocation", new ParseGeoPoint(lat, lng));
                currentUser.put("currentCity", getCity(location));
                locationAvailable = true;

                if(firstLoad) {
                    listener.locationFound();
                    firstLoad = false;
                } else {
                    listener.locationUpdated();
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

    }

    // User interface and setter

    public interface UserLocationListener {
        void locationFound();
        void locationUpdated();
    }

    public void setListener(UserLocationListener listener) {
        this.listener = listener;
    }

    public String getCity(Location userLocation) {

        if(userLocation != null) {
            Geocoder geoCoder = new Geocoder(context, Locale.getDefault());
            try {
                List<Address> address = geoCoder.getFromLocation(userLocation.getLatitude(), userLocation.getLongitude(), 1);
                String city = address.get(0).getLocality() + ", " + address.get(0).getAdminArea();

                return city;

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                return currentUser.getString("currentCity");
            } catch (Exception e) {
                e.printStackTrace();
                return("Unable to determine city");
            }
        }

        return "";
    }

    public String getCurrentCity() {
        return getCity(this.userLocation);
    }

    // Location getters

    public Location getUserLocation() {
        return userLocation;
    }

    public ParseGeoPoint getParseLocation() {
        if(userLocation != null) {
            return new ParseGeoPoint(userLocation.getLatitude(), userLocation.getLongitude());
        } else {
            return currentUser.getParseGeoPoint("currentLocation");
        }
    }

    // Location available check

    public boolean locationAvailable() {
        return this.locationAvailable;
    }
}