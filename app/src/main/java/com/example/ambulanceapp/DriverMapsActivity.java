package com.example.ambulanceapp;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;



public class DriverMapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationManager locationManager2;
    private LocationListener locationListener2;
    static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private DrawerLayout drawerLayout2;
    private NavigationView navigationView;
    private DatabaseReference driverLocationRef;
    private DatabaseReference userLocationRef;

    private double lat2;
    private double lng2;

    private List<Marker> userMarkers = new ArrayList<>();

    private DatabaseReference driverAvailabilityRef;


    private FirebaseAuth mAuth;





    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_maps);
        FirebaseApp.initializeApp(this);


        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();


        drawerLayout2 = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

        mAuth = FirebaseAuth.getInstance();

        userLocationRef = database.getReference("users");
        driverAvailabilityRef = database.getReference("drivers");

        SwitchMaterial driverAvailabilitySwitch = findViewById(R.id.driverAvailabilitySwitch);



        // Set initial text based on the initial state of the switch
        updateDriverStatusText(driverAvailabilitySwitch.isChecked());

        // Set listener for switch state changes
        driverAvailabilitySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Update text when switch is toggled
                updateDriverStatusText(isChecked);
                updateDriverAvailability(isChecked);
            }
        });


        Toolbar toolbar = findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setTitle("Your Current Location");



        }

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout2, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout2.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // Handle navigation item clicks here
                int id = item.getItemId();

                if (id == R.id.nav_home2) {
                    Intent intent = new Intent(getApplicationContext(), DriverMapsActivity.class);
                    startActivity(intent);

                } else if (id == R.id.nav_profile2) {
                    Intent intent = new Intent(getApplicationContext(), Profile2.class);
                    startActivity(intent);
                    // Handle History click
                    // Add your code to open history activity
                } else if (id == R.id.nav_about2) {
                    Intent intent = new Intent(getApplicationContext(), AboutPage2.class);
                    startActivity(intent);
                    // Handle About click
                    // Add your code to open about activity
                } else if (id == R.id.nav_logout2) {
                    mAuth.signOut();
                    Intent intent = new Intent(getApplicationContext(), user_driver.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    FlashScreen sharedPreferencesActivity = new FlashScreen();
                    sharedPreferencesActivity.clearSharedPreferences(getApplicationContext());
                    startActivity(intent);
                    finish();

                } else if (id == R.id.nav_help2) {

                Intent intent = new Intent(getApplicationContext(), HelpPageActivity.class);
                startActivity(intent);
            }

                drawerLayout2.closeDrawer(GravityCompat.START);
                return true;
            }
        });
        FloatingActionButton btnHospitals = findViewById(R.id.BtnHospital);
        btnHospitals.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call the function to find nearby hospitals
                Intent intent = new Intent(getApplicationContext(), HospitalsActivity.class);
                startActivity(intent);
            }
        });


        BottomsheetDriver yourFragment = new BottomsheetDriver();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, yourFragment)
                .commit();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map2);
        mapFragment.getMapAsync(this);

        driverLocationRef = FirebaseDatabase.getInstance().getReference("drivers");

        locationManager2 = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener2 = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                lat2 = location.getLatitude();
                lng2 = location.getLongitude();
                LatLng driverLocation = new LatLng(lat2, lng2);
                updateMarker(driverLocation);

                FirebaseUser currentDriver = FirebaseAuth.getInstance().getCurrentUser();
                if (currentDriver != null) {
                    String userId = currentDriver.getUid();

                    // Fetch the current driver's data
                    driverLocationRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                // Retrieve the existing driver data
                                Driver2 updatedDriver = dataSnapshot.getValue(Driver2.class);

                                // Update only the latitude and longitude fields
                                updatedDriver.setLatitude(lat2);
                                updatedDriver.setLongitude(lng2);
                                updatedDriver.setFcmToken(updatedDriver.getFcmToken());
                                updateFcmToken();
                                // Update the database with the modified driver data
                                driverLocationRef.child(userId).setValue(updatedDriver);

                            }
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

    @SuppressLint("NewApi")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeLocation();
            } else {
                // Handle location permission denied
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Call the drawRoute method with your origin and destination coordinates

        mMap = googleMap;

        try {
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            DriverMapsActivity.this, R.raw.map_style));
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

            locationManager2.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000, // minimum time interval between updates (in milliseconds)
                    1,    // minimum distance between updates (in meters)
                    locationListener2
            );

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

    BitmapDescriptor DriversMarkerIcon;
    boolean cameraMovedToUserLocation = false;

    private void updateMarker(LatLng DriverLocation) {
        DriversMarkerIcon = BitmapDescriptorFactory.fromResource(R.drawable.ambulance_1048341);
        if (mMap != null) {
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(DriverLocation).title("Your Location").icon(DriversMarkerIcon));
            if (!cameraMovedToUserLocation) {
                mMap.moveCamera(CameraUpdateFactory.newLatLng(DriverLocation));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(15.0f));
                cameraMovedToUserLocation = true;
            }
            CircleOptions circleOptions = new CircleOptions()
                    .center(DriverLocation) // Set the center of the circle
                    .radius(1000) // Set the radius of the circle
                    .strokeColor(Color.RED) // Set the stroke color
                    .strokeWidth(2) // Set the stroke width
                    .fillColor(Color.argb(50, 255, 0, 0)) // Set the fill color
                    .clickable(false); // Set the circle to be non-clickable

// Add the circle to the map
            Circle circle = mMap.addCircle(circleOptions);

        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationManager2 != null) {
            locationManager2.removeUpdates(locationListener2);
        }
        cameraMovedToUserLocation = false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {

            if (drawerLayout2.isDrawerOpen(GravityCompat.START)) {
                drawerLayout2.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout2.openDrawer(GravityCompat.START);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void updateDriverStatusText(boolean isAvailable) {
        String statusText = isAvailable ? "\t\tAvailable" : "\t\tUnavailable";

        TextView statusTextView = findViewById(R.id.statusTextView);
        statusTextView.setText(statusText);
    }

    private void updateDriverAvailability(boolean isAvailable) {
        FirebaseUser currentDriver = FirebaseAuth.getInstance().getCurrentUser();
        if (currentDriver != null) {
            String userId = currentDriver.getUid();

            DatabaseReference specificDriverRef = driverAvailabilityRef.child(userId);

            // Assuming "status" is the key under which the availability status is stored
            specificDriverRef.child("driversAvaibility").setValue(isAvailable);

            // Show a toast message indicating the status update
            Toast.makeText(DriverMapsActivity.this, isAvailable ? "Driver is now available" : "Driver is now unavailable", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateFcmToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String token = task.getResult();
                        FirebaseUser currentDriver = FirebaseAuth.getInstance().getCurrentUser();
                        if (currentDriver != null) {
                            String userId = currentDriver.getUid();
                            DatabaseReference specificDriverRef = driverAvailabilityRef.child(userId);

                            // Assuming "fcmToken" is the key under which the FCM token is stored
                            specificDriverRef.child("fcmToken").setValue(token);
                        }
                    } else {
                        Log.e("FCMTokenError", "Unable to retrieve FCM token");
                    }
                });
    }


    BitmapDescriptor userMarkerIcon;

    public void retrieveAndDisplayUserLocations() {
        userLocationRef = FirebaseDatabase.getInstance().getReference("users");
        userMarkerIcon = BitmapDescriptorFactory.fromResource(R.drawable.l1a);
        userLocationRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (Marker marker : userMarkers) {
                    marker.remove();
                }
                userMarkers.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String userId = snapshot.getKey();
                    Double userLat = snapshot.child("latitude").getValue(Double.class);
                    Double userLng = snapshot.child("longitude").getValue(Double.class);

                    if (userId != null && userLat != null && userLng != null) {
                        LatLng patientLocation = new LatLng(userLat, userLng);
                        Marker driverMarker = mMap.addMarker(new MarkerOptions()
                                .position(patientLocation)
                                .title("Patient Location")
                                .icon(userMarkerIcon));
                        userMarkers.add(driverMarker);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
            }
        });

    }

    public void showNotification(String title, String message) {
        // Create an intent to open the app when notification is tapped
        Intent intent = new Intent(this, DriverMapsActivity.class);
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
                .setSmallIcon(R.drawable.baseline_notifications_active_24)
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



}














