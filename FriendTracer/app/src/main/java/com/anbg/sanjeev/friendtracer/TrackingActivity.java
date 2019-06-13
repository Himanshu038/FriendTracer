package com.anbg.sanjeev.friendtracer;

import android.*;
import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.anbg.sanjeev.friendtracer.model.Tracking;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;

public class TrackingActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleMap mMap;
    private String email;

    DatabaseReference onlineRef, currentUserref, counterRef, locationsDatabase,locations;

    private static final int MY_PREFERENCE_REQUEST_CODE = 7171;
    private static final int PLAY_SERVICE_REQUEST_CODE = 7172;
    private LocationRequest locationRequest;
    private GoogleApiClient googleApiClient;
    private Location mlocation;

    private static int UPDATE_INTERVAL = 5000;

    private static int FASTEST_INTERVAL = 3000;
    private static int DISTANCE = 10;
    Double lat, lng;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        locations = FirebaseDatabase.getInstance().getReference("Locations");
        if (getIntent()!=null)
        {
            email = getIntent().getStringExtra("email");
            lat = getIntent().getDoubleExtra("lat",0);
            lng = getIntent().getDoubleExtra("lng",0);
        }
        init();
    }

    private double distance(Location current_user, Location friendLoc) {
        double theta = current_user.getLongitude() - friendLoc.getLongitude();
        double dist =  Math.sin(deg2rad(current_user.getLatitude()))
                *Math.sin(deg2rad(friendLoc.getLatitude()))
                *Math.cos(deg2rad(friendLoc.getLatitude()))
                *Math.cos(deg2rad(friendLoc.getLatitude()))
                *Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 *1.1515;
        return dist;
    }

    private double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

    private double deg2rad(double latitude) {
        return (latitude * Math.PI / 180.0);
    }

    private void loadLocationForThisUser(String email) {
//        Query user_location = locations.orderByChild("email").equalTo(email);
        Query user_location = locations.orderByChild("email");
        user_location.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren())
                {
                    Log.d("DataSnapshot", String.valueOf(postSnapshot.getChildren()));
                    Tracking tracking = postSnapshot.getValue(Tracking.class);
                    //Add marker for friend location
                    LatLng friend_latLng = new LatLng(Double.parseDouble(tracking.getLat()),
                            Double.parseDouble(tracking.getLng()));
                    //Create Location from user coordinates
                    Location current_user =  new Location("");
                    current_user.setLatitude(lat);
                    current_user.setLongitude(lng);
                    //for friend
                    Location friendLoc =  new Location("");
                    friendLoc.setLatitude(Double.parseDouble(tracking.getLat()));
                    friendLoc.setLongitude(Double.parseDouble(tracking.getLng()));
                    // Clear All old Marker
                    mMap.clear();
                    // Add friend marker on map
                    mMap.addMarker(new MarkerOptions()
                            .position(friend_latLng)
                            .title(tracking.getEmail())
                            .snippet("Distance " + new DecimalFormat("#.#").format( current_user.distanceTo(friendLoc) / 1000)+" km")
//                    .snippet("Distance " + new DecimalFormat("#.#").format( distance(current_user,friendLoc)))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat,lng),12.0f));
                }
                // Create Marker for current user
                LatLng current =  new LatLng(lat,lng);
                mMap.addMarker(new MarkerOptions().position(current)
                        .title(FirebaseAuth.getInstance().getCurrentUser().getEmail())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat,lng),12.0f));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
//        if (!TextUtils.isEmpty(email))
//        {
////            loadLocationForThisUser(email);
//            Query user_location = locations.orderByChild("email");
//            user_location.addValueEventListener(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren())
//                    {
//                        Log.d("DataSnapshot", String.valueOf(postSnapshot.getChildren()));
//                        Tracking tracking = postSnapshot.getValue(Tracking.class);
//
//                        //Add marker for friend location
//                        LatLng friend_latLng = new LatLng(Double.parseDouble(tracking.getLat()),
//                                Double.parseDouble(tracking.getLng()));
//                        //Create Location from user coordinates
//                        Location current_user =  new Location("");
//                        current_user.setLatitude(lat);
//                        current_user.setLongitude(lng);
//                        //for friend
//                        Location friendLoc =  new Location("");
//                        friendLoc.setLatitude(Double.parseDouble(tracking.getLat()));
//                        friendLoc.setLongitude(Double.parseDouble(tracking.getLng()));
//                        // Clear All old Marker
//                        mMap.clear();
//                        // Add friend marker on map
//                        mMap.addMarker(new MarkerOptions()
//                                .position(friend_latLng)
//                                .title(tracking.getEmail())
//                                .snippet("Distance " + new DecimalFormat("#.#").format( current_user.distanceTo(friendLoc) / 1000)+" km")
////                    .snippet("Distance " + new DecimalFormat("#.#").format( distance(current_user,friendLoc)))
//                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
//                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(tracking.getLat()),
//                                Double.parseDouble(tracking.getLng())),12.0f));
//                    }
//                    // Create Marker for current user
//                    LatLng current =  new LatLng(lat,lng);
//                    mMap.addMarker(new MarkerOptions().position(current)
//                            .title(FirebaseAuth.getInstance().getCurrentUser().getEmail())
//                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
//                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat,lng),12.0f));
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//
//                }
//            });
//        }
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Locations");
        Query query = reference.orderByChild("email");
//        query.addListenerForSingleValueEvent(new ValueEventListener() {
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // dataSnapshot is the "issue" node with all children with id 0
                    for (DataSnapshot mDataSnapshot : dataSnapshot.getChildren()) {
                        // do something with the individual "issues"
                        Tracking tracking = mDataSnapshot.getValue(Tracking.class);

                        LatLng current =  new LatLng(Double.parseDouble(tracking.getLat()),Double.parseDouble(tracking.getLng()));
//                        mMap.clear();
                        mMap.addMarker(new MarkerOptions().position(current)
                                .title(tracking.getEmail())
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(tracking.getLat()),
                                Double.parseDouble(tracking.getLng())),12.0f));

                    }
                }
                // Create Marker for current user
                LatLng current =  new LatLng(lat,lng);
                mMap.addMarker(new MarkerOptions().position(current)
                        .title(FirebaseAuth.getInstance().getCurrentUser().getEmail())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat,lng),12.0f));
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    public void init() {
        locationsDatabase = FirebaseDatabase.getInstance().getReference("Locations");
        onlineRef = FirebaseDatabase.getInstance().getReference().child(".info/connected");
        counterRef = FirebaseDatabase.getInstance().getReference("lastOnline");
        currentUserref = FirebaseDatabase.getInstance().getReference("lastOnline")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, MY_PREFERENCE_REQUEST_CODE);
        } else {
            if (checkPlayServices()) {
                buildGoogleApiClient();
                createLocationRequest();
                displayLocation();
            }
        }
    }
    private void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        locationRequest.setSmallestDisplacement(DISTANCE);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
    private boolean checkPlayServices() {
        int requestCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (requestCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(requestCode)) {
                GooglePlayServicesUtil.getErrorDialog(requestCode, this, PLAY_SERVICE_REQUEST_CODE).show();
            } else {
                Toast.makeText(this, "This device is not supported", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }
    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mlocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (mlocation != null) {

            locationsDatabase.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .setValue(new Tracking(FirebaseAuth.getInstance().getCurrentUser().getEmail(),
                            FirebaseAuth.getInstance().getCurrentUser().getUid(),
                            String.valueOf(mlocation.getLatitude()),
                            String.valueOf(mlocation.getLongitude())));
        } else {
//            Toast.makeText(this, "Couldn't get the location", Toast.LENGTH_SHORT).show();
            Log.d("Log","Couldn't get the location");
        }
    }
    private void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();
    }
    private void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED ){
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        googleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mlocation = location;
        displayLocation();
    }
    @Override
    protected void onStart() {
        super.onStart();
        if (googleApiClient != null)
            googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        if (googleApiClient != null) {
            googleApiClient.disconnect();
            super.onStop();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();

    }
}
