package com.example.ambulanceapp;


import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserMapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int PERMISSION_REQUEST_CALL_PHONE = 1  ;
    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private DatabaseReference userLocationRef;
    private DatabaseReference driversRef;

    private LatLng driverLocation;
    private LatLng userLocation;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private double lat;
    private double lng;

    private BookingRequest rideRequest;
    private List<Marker> driverMarkers = new ArrayList<>();
    private DatabaseReference rideRequestsRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_maps);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        Toolbar toolbar = findViewById(R.id.toolbar);
        mAuth = FirebaseAuth.getInstance();
        rideRequestsRef = FirebaseDatabase.getInstance().getReference("rideRequests");




        setSupportActionBar(toolbar);
        FirebaseApp.initializeApp(this);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setTitle("Your Current Location");


        }


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // Handle navigation item clicks here
                int id = item.getItemId();

                if (id == R.id.nav_home) {
                    Intent intent = new Intent(getApplicationContext(), UserMapsActivity.class);
                    startActivity(intent);

                } else if (id == R.id.nav_profile) {
                    // Handle History click
                    // Add your code to open history activity
                    Intent intent = new Intent(getApplicationContext(), Profile.class);
                    startActivity(intent);
                } else if (id == R.id.nav_about) {
                    // Handle About click
                    // Add your code to open about activity
                    Intent intent = new Intent(getApplicationContext(), AboutPage.class);
                    startActivity(intent);
                } else if (id == R.id.nav_logout) {

                    mAuth.signOut();
                    Intent intent = new Intent(getApplicationContext(), user_driver.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    FlashScreen sharedPreferencesActivity = new FlashScreen();
                    sharedPreferencesActivity.clearSharedPreferences(getApplicationContext());
                    startActivity(intent);
                    finish();

                } else if (id == R.id.nav_help) {

                Intent intent = new Intent(getApplicationContext(), HelpPageActivity.class);
                startActivity(intent);

                } else if (id == R.id.nav_Emergency) {

                  showEmergencyDialog();
            }

                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        BottomSheet yourFragmentCustomer = new BottomSheet();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container2, yourFragmentCustomer)
                .commit();


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        userLocationRef = FirebaseDatabase.getInstance().getReference("users");
        driversRef = FirebaseDatabase.getInstance().getReference("drivers");

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                lat = location.getLatitude();
                lng = location.getLongitude();
                userLocation = new LatLng(lat, lng);
                updateMarker(userLocation);

                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    String userId = currentUser.getUid();
                    userLocationRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                // Retrieve the existing driver data
                                User updatedUser = dataSnapshot.getValue(User.class);

                                // Update only the latitude and longitude fields
                                updatedUser.setLatitude(lat);
                                updatedUser.setLongitude(lng);

                                // Update the database with the modified driver data
                                userLocationRef.child(userId).setValue(updatedUser);
                            }
                            retrieveAndDisplayDriverLocations();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle database error, if needed
                        }
                    });

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
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        };

        checkLocationPermission();

        }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeLocation();
            } else {

            }
        }
        if (requestCode == PERMISSION_REQUEST_CALL_PHONE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                Toast.makeText(this, "Permission granted. Call again.", Toast.LENGTH_SHORT).show();
            } else {
                // Permission denied
                Toast.makeText(this, "Permission denied. Cannot make a call.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        try {
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            UserMapsActivity.this, R.raw.map_style));
            if (!success) {
                Log.e(TAG, "Failed to set map style.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Resource not found: " + e);
        }
        if (checkLocationPermission()) {
            initializeLocation();


        }
    }

    private void initializeLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
    }

    private boolean checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return false;
        } else {
            return true;
        }
    }


    BitmapDescriptor userMarkerIcon;
    boolean cameraMovedToUserLocation = false;
    private void updateMarker(LatLng userLocation) {
        userMarkerIcon = BitmapDescriptorFactory.fromResource(R.drawable.l1a);

        if (mMap != null) {
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(userLocation).title("Your Location").icon(userMarkerIcon));
            if (!cameraMovedToUserLocation) {
                mMap.moveCamera(CameraUpdateFactory.newLatLng(userLocation));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(15.0f));
                cameraMovedToUserLocation = true;
            }
            CircleOptions circleOptions = new CircleOptions()
                    .center(userLocation) // Set the center of the circle
                    .radius(1000) // Set the radius of the circle
                    .strokeColor(Color.RED) // Set the stroke color
                    .strokeWidth(2) // Set the stroke width
                    .fillColor(Color.argb(50, 255, 0, 0)) // Set the fill color
                    .clickable(false); // Set the circle to be non-clickable

// Add the circle to the map
            Circle circle = mMap.addCircle(circleOptions);


// Set a OnCameraIdleListener to detect when the camera has finished moving

        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
        cameraMovedToUserLocation = false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    BitmapDescriptor driverMarkerIcon;


    private void retrieveAndDisplayDriverLocations() {
        driverMarkerIcon = BitmapDescriptorFactory.fromResource(R.drawable.ambulance_1048341);
        driversRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (Marker marker : driverMarkers) {
                    marker.remove();
                }
                driverMarkers.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String userId = snapshot.getKey();
                    Double driverLat = snapshot.child("latitude").getValue(Double.class);
                    Double driverLng = snapshot.child("longitude").getValue(Double.class);

                    if (userId != null && driverLat != null && driverLng != null) {
                        driverLocation = new LatLng(driverLat, driverLng);
                        Marker driverMarker = mMap.addMarker(new MarkerOptions()
                                .position(driverLocation)
                                .title("Ambulance Location")
                                .icon(driverMarkerIcon));
                        driverMarkers.add(driverMarker);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
            }
        });

    }

    public void checkDriverArrival() {
        if (userLocation != null && driverLocation != null) {
        Location userLocationObj = new Location("");
        userLocationObj.setLatitude(userLocation.latitude);
        userLocationObj.setLongitude(userLocation.longitude);

        Location driverLocationObj = new Location("");
        driverLocationObj.setLatitude(driverLocation.latitude);
        driverLocationObj.setLongitude(driverLocation.longitude);

        // Calculate distance between user and driver locations
        float distance = userLocationObj.distanceTo(driverLocationObj); // in meters

            if (distance <= 10000) {
                // Driver has arrived within 100 meters radius
                handleDriverArrival();
            } else {
                // Driver is not yet within 100 meters radius
            }
        } else {
            // Location information not available yet
            if (userLocation == null) {
                Log.d("LocationCheck", "User location is null");
            }
            if (driverLocation == null) {
                Log.d("LocationCheck", "Driver location is null");
            }

        }
    }
    public GoogleMap getGoogleMap() {
        // Return the GoogleMap object here
        return mMap;
    }

    private void handleDriverArrival() {
        // Update UI or perform actions when the driver arrives
        // For example, show a notification to the user
        showNotification("Driver has arrived!", "Your ride is ready.");

        // Update driver status to "arrive" in Firebase Realtime Database

            listenForPendingRequests();

    }

    public void showNotification(String title, String message) {
        // Create an intent to open the app when notification is tapped
        Intent intent = new Intent(this, UserMapsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        // Create the notification channel (for Android 8.0 and higher)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "channel_name";
            String description = "channel_description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("channel_id", name, importance);
            channel.setDescription(description);

            // Register the channel with the system
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        // Create the notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "channel_id")
                .setSmallIcon(R.drawable.img_1)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        // Show the notification
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(0, notificationBuilder.build());
        }
    }

    private void updateDriverStatus(String requestId) {

        // Update driver status to "arrived" in Firebase Realtime Database
        rideRequestsRef.child(requestId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                BookingRequest request = dataSnapshot.getValue(BookingRequest.class);
                if (request != null) {
                    request.setStatus("arrived");
                    rideRequestsRef.child(requestId).setValue(request);


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });}
    private void listenForPendingRequests() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Listen for changes in the rideRequests node
            rideRequestsRef.orderByChild("status").equalTo("accepted")
                    .addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                            rideRequest = dataSnapshot.getValue(BookingRequest.class);

                            if (rideRequest != null) {

                                updateDriverStatus(rideRequest.getRequestId());
                                // Check if the request is for the current driver
                                if (rideRequest.getUserId().equals(userId)) {


                                }
                            }
                        }


                        @Override
                        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                            // Handle changes if needed
                        }

                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                            // Handle removal if needed
                        }

                        @Override
                        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                            // Handle movement if needed
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // Handle errors
                        }
                    });
        }


    }
    private void showEmergencyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Emergency Call");
        builder.setMessage("Select the emergency service you want to call:");
        builder.setPositiveButton("Call 108", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callEmergencyNumber("108");
            }
        });
        builder.setNegativeButton("Call Police", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callEmergencyNumber("100"); // You can replace "ambulance" with the actual ambulance number
            }
        });
        builder.setNeutralButton("Cancel", null);
        builder.create().show();
    }

    private void callEmergencyNumber(String number) {
        if (ContextCompat.checkSelfPermission(UserMapsActivity.this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(UserMapsActivity.this,
                    new String[]{android.Manifest.permission.CALL_PHONE},
                    PERMISSION_REQUEST_CALL_PHONE);
        } else {
            String phoneNumber;
            if (number.equals("108")) {
                phoneNumber = "tel:108";
            } else {
                // Replace this with the actual ambulance number
                phoneNumber = "tel:100";
            }
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse(phoneNumber));
            startActivity(callIntent);
        }
    }


}

















