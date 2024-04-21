package com.example.ambulanceapp;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import androidx.lifecycle.ViewModelProvider;

import org.w3c.dom.Text;

public class BottomSheet extends Fragment {

    private boolean isRideBooked = false;
    private double lat;
    private double lng;
    private DatabaseReference rideRequestsRef;
    private DatabaseReference driversRef;

    private String driverMobile;

    private BottomSheetViewModel viewModel;

    private UserMapsActivity mapUserActivity;
    private SharedPreferences sharedPreferences;
    private ImageButton showLocation;
    private LatLng driverLocationLatLng;


    public BottomSheet() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = requireContext().getSharedPreferences("booking_prefs", Context.MODE_PRIVATE);
        viewModel = new ViewModelProvider(this).get(BottomSheetViewModel.class);
        setRetainInstance(true); // Retain fragment instance across configuration changes
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mapUserActivity = (UserMapsActivity) getActivity();
        driversRef = FirebaseDatabase.getInstance().getReference("drivers");
        Button bookRideButton = view.findViewById(R.id.requestButton);
        rideRequestsRef = FirebaseDatabase.getInstance().getReference("rideRequests");
        showLocation = view.findViewById(R.id.showLocationButton);
        showLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (driverLocationLatLng != null) {
                    // Call the method to show the driver's location on the map
                    showDriverLocationOnMap(driverLocationLatLng.latitude, driverLocationLatLng.longitude);
                } else {
                    Toast.makeText(requireContext(), "Driver's location is not available", Toast.LENGTH_SHORT).show();
                }
            }
        });
        ImageButton callButton = view.findViewById(R.id.callButton2);
        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCallDialog(driverMobile);
            }
        });

        Button cancelButton = view.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView textViewStatus = view.findViewById(R.id.statusBar);
                textViewStatus.setText("Find Ambulance...");
                Button bookRideButton = view.findViewById(R.id.requestButton);
                bookRideButton.setText("Request");
                bookRideButton.setVisibility(View.VISIBLE);
                ImageView Image = view.findViewById(R.id.driverImage);
                Image.setVisibility(View.GONE);
                TextView textViewName = view.findViewById(R.id.driverName);
                textViewName.setVisibility(View.GONE);
                TextView textViewNumber = view.findViewById(R.id.driverNumber);
                textViewNumber.setVisibility(View.GONE);
                TextView textViewLocation = view.findViewById(R.id.driverLocation);
                textViewLocation.setVisibility(View.GONE);
                ImageButton callButton = view.findViewById(R.id.callButton2);
                callButton.setVisibility(View.GONE);
                Button cancelButton = view.findViewById(R.id.cancelButton);
                cancelButton.setVisibility(View.GONE);
                ImageButton showLocationButton = view.findViewById(R.id.showLocationButton);
                showLocationButton.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "You Cancelled Request ", Toast.LENGTH_SHORT).show();
                listenForCancelingRequests(view);
                isRideBooked = false;
            }
        });

        FusedLocationProviderClient locationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        lat = location.getLatitude();
                        lng = location.getLongitude();
                    } else {
                        Toast.makeText(requireContext(), "Unable to determine your location", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        bookRideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRideBooked) {
                    if (lat != 0 && lng != 0) {
                        TextView textViewStatus = view.findViewById(R.id.statusBar);
                        textViewStatus.setText("Finding Ambulance...");
                        ProgressBar progressBar = view.findViewById(R.id.progressBar);
                        progressBar.setVisibility(View.VISIBLE);
                        fetchNearbyDrivers(view);
                    } else {
                        Toast.makeText(requireContext(), "Unable to determine your location", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(requireContext(), "Ride is already booked", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void fetchNearbyDrivers(View view) {
        TextView DriversName = view.findViewById(R.id.driverName);
        TextView DriverMobile = view.findViewById(R.id.driverNumber);
        TextView DriverLocation = view.findViewById(R.id.driverLocation);

        // Inflate the layout of the other activity
        View otherActivityLayout = getLayoutInflater().inflate(R.layout.activity_user_maps, null);

        // Find the RelativeLayout in the other activity layout
        RelativeLayout timeLayout = otherActivityLayout.findViewById(R.id.time0);
        TextView estimatedtime = otherActivityLayout.findViewById(R.id.time);

        final double radius = 1.0;
        LatLng userLatLng = new LatLng(lat, lng);

        Query nearbyDriversQuery = driversRef.orderByChild("latitude")
                .startAt(lat - radius / 111.12)
                .endAt(lat + radius / 111.12)
                .limitToFirst(1); // Limit the query to fetch only one driver

        nearbyDriversQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String driverId = snapshot.getKey();
                    String driverName = snapshot.child("fullName").getValue(String.class);
                    String driverPhone = snapshot.child("mobile").getValue(String.class);
                    Double driverLat = snapshot.child("latitude").getValue(Double.class);
                    Double driverLng = snapshot.child("longitude").getValue(Double.class);

                    if (driverId != null && driverLat != null && driverLng != null) {
                        // Set driver's name and phone number
                        DriversName.setText(driverName);
                        DriverMobile.setText(driverPhone);
                        // Set driverMobile variable
                        driverMobile = driverPhone;
                        sendBookingRequestToDriver(driverId, view, driverName, driverPhone);
                        // Calculate estimated time and display it
                        float[] results = new float[1];
                        Location.distanceBetween(lat, lng, driverLat, driverLng, results);
                        float distance = results[0];
                        int estimatedTime = calculateEstimatedTime(distance);
                        // Inside fetchNearbyDrivers() method
                        estimatedtime.setText("Estimated Time: " + estimatedTime + " minutes");
                        timeLayout.setVisibility(View.VISIBLE);

                    } else {
                        TextView textViewStatus = view.findViewById(R.id.statusBar);
                        textViewStatus.setText("Sorry, Ambulance Not Found...");
                        // Hide the timeLayout if driver information is not available
                        // timeLayout.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "fetchNearbyDrivers:onCancelled", databaseError.toException());
                TextView textViewStatus = view.findViewById(R.id.statusBar);
                textViewStatus.setText("Error: Unable to fetch nearby drivers.");
            }
        });
    }

    private void sendBookingRequestToDriver(String driverId, View view, String driverName, String driverPhone) {

        if (isRideBooked) {
            Toast.makeText(requireContext(), "Ride is already booked", Toast.LENGTH_SHORT).show();
        }

        // Check if the driver is busy
        DatabaseReference driversRef = FirebaseDatabase.getInstance().getReference("drivers").child(driverId);
        driversRef.child("status").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String driverStatus = dataSnapshot.getValue(String.class);
                if (driverStatus != null && driverStatus.equals("busy")) {
                    Toast.makeText(requireContext(), "Driver is busy, please try again later", Toast.LENGTH_SHORT).show();
                } else {
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (currentUser != null) {
                        String userId = currentUser.getUid();
                        String requestId = rideRequestsRef.push().getKey();
                        BookingRequest bookingRequest = new BookingRequest(requestId, userId, driverId, "pending");

                        rideRequestsRef.child(requestId).setValue(bookingRequest);

                        rideRequestsRef.child(requestId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                BookingRequest response = dataSnapshot.getValue(BookingRequest.class);
                                if (response != null) {
                                    String status = response.getStatus();
                                    if ("accepted".equals(status)) {
                                        requireActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (response.getUserId().equals(userId)) {
                                                    fetchDriverDetails(driverId, view); // Pass the correct driverId here
                                                }
                                                mapUserActivity.showNotification("Ambulance Found!","Request Accepted By Driver");
                                                updateUI(view);
                                                Toast.makeText(requireContext(), "Booking accepted by the driver", Toast.LENGTH_SHORT).show();
                                                mapUserActivity.checkDriverArrival();
                                                isRideBooked = true;
                                            }
                                        });
                                    } else if ("rejected".equals(status)) {
                                        requireActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                mapUserActivity.showNotification("Ambulance Not Found!","Request Rejected By Driver");
                                                TextView textViewStatus = view.findViewById(R.id.statusBar);
                                                textViewStatus.setText("Sorry For The Inconvenience...");
                                                ProgressBar progressBar = view.findViewById(R.id.progressBar);
                                                progressBar.setVisibility(View.GONE);
                                                Button bookRideButton = view.findViewById(R.id.requestButton);
                                                bookRideButton.setText("Retry");
                                                Toast.makeText(requireContext(), "Booking rejected by the driver", Toast.LENGTH_SHORT).show();
                                                isRideBooked = false;
                                            }
                                        });
                                    } else if ("arrived".equals(status)) {
                                        requireActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                TextView textViewStatus = view.findViewById(R.id.statusBar);
                                                textViewStatus.setText("The Ambulance has Arrived");
                                                ProgressBar progressBar = view.findViewById(R.id.progressBar);
                                                progressBar.setVisibility(View.GONE);
                                                Button bookRideButton = view.findViewById(R.id.requestButton);
                                                bookRideButton.setVisibility(View.GONE);
                                                Button cancelButton = view.findViewById(R.id.cancelButton);
                                                cancelButton.setVisibility(View.GONE);
                                                isRideBooked = true;
                                            }
                                        });

                                    } else if ("Completed".equals(status)) {
                                        requireActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                TextView textViewStatus = view.findViewById(R.id.statusBar);
                                                textViewStatus.setText("You Reach at Hospital");
                                                ProgressBar progressBar = view.findViewById(R.id.progressBar);
                                                progressBar.setVisibility(View.GONE);
                                                Button bookRideButton = view.findViewById(R.id.requestButton);
                                                bookRideButton.setVisibility(View.VISIBLE);
                                                Button cancelButton = view.findViewById(R.id.cancelButton);
                                                cancelButton.setVisibility(View.GONE);
                                                ImageView Image = view.findViewById(R.id.driverImage);
                                                Image.setVisibility(View.GONE);
                                                TextView textViewName = view.findViewById(R.id.driverName);
                                                textViewName.setVisibility(View.GONE);
                                                TextView textViewNumber = view.findViewById(R.id.driverNumber);
                                                textViewNumber.setVisibility(View.GONE);
                                                TextView textViewLocation = view.findViewById(R.id.driverLocation);
                                                textViewLocation.setVisibility(View.GONE);
                                                isRideBooked = false;
                                            }
                                        });
                                    }
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {  // Handle errors
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void fetchDriverDetails(String driverId, View view) { // Update method signature to accept driverId
        driversRef.child(driverId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Driver2 driver = dataSnapshot.getValue(Driver2.class);

                if (driver != null) {
                    displayDriversDetails(driver, view);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void displayDriversDetails(Driver2 driver, View view) {
        TextView driverNameTextView = view.findViewById(R.id.driverName);
        TextView driverPhoneTextView = view.findViewById(R.id.driverNumber);
        TextView driverLocationTextView = view.findViewById(R.id.driverLocation);
        driverNameTextView.setText(driver.getFullName());
        driverPhoneTextView.setText(driver.getMobile());
        LatLng driverLatLng = new LatLng(driver.getLatitude(), driver.getLongitude());
        String driverAddress = Utils.getAddressFromLatLng(driverLatLng, requireContext());
        driverLocationTextView.setText(driverAddress);

        double driverLat = driver.getLatitude();
        double driverLng = driver.getLongitude();
        driverLocationLatLng = new LatLng(driverLat, driverLng);
    }

    private void updateUI(View view) {
        TextView textViewStatus = view.findViewById(R.id.statusBar);
        textViewStatus.setText("Ambulance is On The Way...");
        ProgressBar progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        Button bookRideButton = view.findViewById(R.id.requestButton);
        bookRideButton.setVisibility(View.GONE);
        ImageView Image = view.findViewById(R.id.driverImage);
        Image.setVisibility(View.VISIBLE);
        TextView textViewName = view.findViewById(R.id.driverName);
        textViewName.setVisibility(View.VISIBLE);
        TextView textViewNumber = view.findViewById(R.id.driverNumber);
        textViewNumber.setVisibility(View.VISIBLE);
        TextView textViewLocation = view.findViewById(R.id.driverLocation);
        textViewLocation.setVisibility(View.VISIBLE);
        ImageButton callButton = view.findViewById(R.id.callButton2);
        callButton.setVisibility(View.VISIBLE);
        Button cancelButton = view.findViewById(R.id.cancelButton);
        cancelButton.setVisibility(View.VISIBLE);
        ImageButton showLocationButton = view.findViewById(R.id.showLocationButton);
        showLocationButton.setVisibility(View.VISIBLE);
    }

    private void startCallDialog(String phoneNumber) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Call Ambulance");
        builder.setMessage("Do you want to call the ambulance?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber));
                startActivity(intent);
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void listenForCancelingRequests(View view) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference requestsRef = FirebaseDatabase.getInstance().getReference("rideRequests");
            requestsRef.orderByChild("userId").equalTo(userId).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    BookingRequest request = dataSnapshot.getValue(BookingRequest.class);
                    if (request != null && "pending".equals(request.getStatus())) {
                        dataSnapshot.getRef().removeValue(); // Remove the request from the database
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }
    }

    private void showDriverLocationOnMap(double latitude, double longitude) {
        String uri = "http://maps.google.com/maps?q=loc:" + latitude + "," + longitude + " (" + "Ambulance Location" + ")";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("com.google.android.apps.maps");
        startActivity(intent);
    }

    private int calculateEstimatedTime(float distance) {
        // Assuming average speed of 40 km/h
        int averageSpeed = 40;
        // Converting distance from meters to kilometers
        float distanceInKm = distance / 1000;
        // Calculating estimated time in minutes
        return (int) (distanceInKm / averageSpeed * 60);
    }
}