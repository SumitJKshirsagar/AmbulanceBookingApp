package com.example.ambulanceapp;

import static com.google.firebase.firestore.core.CompositeFilter.Operator.OR;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.ambulanceapp.BookingRequest;
import com.example.ambulanceapp.DriverMapsActivity;
import com.example.ambulanceapp.R;
import com.example.ambulanceapp.User;
import com.example.ambulanceapp.Utils;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class BottomsheetDriver extends Fragment {

    private DatabaseReference usersRef;
    private DatabaseReference driversRef;
    private DatabaseReference rideRequestsRef;
    private BookingRequest rideRequest; // Declare rideRequest as a class variable
    boolean AmbulanceIsBusy = false;
    MediaPlayer buzzer;
    private DriverMapsActivity mapActivity;

    public BottomsheetDriver() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true); // Retain fragment instance across configuration changes
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bottomsheet_driver, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rideRequestsRef = FirebaseDatabase.getInstance().getReference("rideRequests");
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        driversRef = FirebaseDatabase.getInstance().getReference("drivers");
        mapActivity = (DriverMapsActivity) getActivity();
        // Add a listener to fetch pending requests
        listenForPendingRequests(view);

        // Add a button or trigger to accept a request
        Button acceptButton = view.findViewById(R.id.accept_button);
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button call = view.findViewById(R.id.CallPatient);
                call.setVisibility(View.VISIBLE);
                Button accept_button = view.findViewById(R.id.accept_button);
                accept_button.setVisibility(View.GONE);
                Button reject_button = view.findViewById(R.id.reject_button);
                reject_button.setVisibility(View.GONE);
                TextView status = view.findViewById(R.id.RequestText);
                status.setText("Patients Details");
                mapActivity.retrieveAndDisplayUserLocations();
                stopBuzzer();
                AmbulanceIsBusy = true;
                // Check if rideRequest is not null before using it
                if (rideRequest != null && rideRequest.getRequestId() != null) {
                    // Implement logic to accept the request
                    listenForRequestStatusChanges(rideRequest.getRequestId(), getView());
                    acceptRequest(rideRequest.getRequestId());
                } else {
                    // Handle the case where rideRequest is null or its ID is null
                    Log.e("BottomsheetDriver", "rideRequest is null or its ID is null");
                }
            }
        });

        Button rejectButton = view.findViewById(R.id.reject_button);
        rejectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button accept_button = view.findViewById(R.id.accept_button);
                accept_button.setVisibility(View.GONE);
                Button reject_button = view.findViewById(R.id.reject_button);
                reject_button.setVisibility(View.GONE);
                RelativeLayout customerDetailsLayout = view.findViewById(R.id.CustomerDetails);
                customerDetailsLayout.setVisibility(View.GONE);
                TextView status = view.findViewById(R.id.RequestText);
                status.setText("No Requests Available");
                stopBuzzer();
                // Check if rideRequest is not null before using it
                if (rideRequest != null && rideRequest.getRequestId() != null) {
                    // Implement logic to accept the request
                    rejectRequest(rideRequest.getRequestId());
                } else {
                    // Handle the case where rideRequest is null or its ID is null
                    Log.e("BottomsheetDriver", "rideRequest is null or its ID is null");
                }
            }
        });

        Button call = view.findViewById(R.id.CallPatient);
        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startCallDialog();
            }
        });
        Button rideComplete = view.findViewById(R.id.RideComplete);
        rideComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                TextView textView = view.findViewById(R.id.RequestText);
                textView.setText("Ride Completed, Wait for New Request");
                RelativeLayout customerDetailsLayout = view.findViewById(R.id.CustomerDetails);
                customerDetailsLayout.setVisibility(View.GONE);
                Button accept_button = view.findViewById(R.id.accept_button);
                accept_button.setVisibility(View.GONE);
                Button reject_button = view.findViewById(R.id.reject_button);
                reject_button.setVisibility(View.GONE);
                call.setVisibility(View.GONE);
                rideComplete.setVisibility(view.GONE);

                // Check if rideRequest is not null before using it
                if (rideRequest != null && rideRequest.getRequestId() != null) {
                    // Implement logic to accept the request
                   rideComplete(rideRequest.getRequestId());
                } else {
                    // Handle the case where rideRequest is null or its ID is null
                    Log.e("BottomsheetDriver", "rideRequest is null or its ID is null");
                }
            }
        });

    }


    public void acceptRequest(String requestId) {
        rideRequestsRef.child(requestId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                BookingRequest request = dataSnapshot.getValue(BookingRequest.class);
                if (request != null) {
                    request.setStatus("accepted");
                    rideRequestsRef.child(requestId).setValue(request);

                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (currentUser != null) {
                        String driverId = currentUser.getUid();
                        driversRef.child(driverId).child("status").setValue("busy");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    public void rejectRequest(String requestId) {
        rideRequestsRef.child(requestId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                BookingRequest request = dataSnapshot.getValue(BookingRequest.class);
                if (request != null) {
                    request.setStatus("rejected");
                    rideRequestsRef.child(requestId).setValue(request);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }
    public void rideComplete(String requestId) {
        rideRequestsRef.child(requestId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                BookingRequest request = dataSnapshot.getValue(BookingRequest.class);
                if (request != null) {
                    request.setStatus("Completed");
                    rideRequestsRef.child(requestId).setValue(request);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    private void listenForPendingRequests(View view) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String driverId = currentUser.getUid();

            // Listen for changes in the rideRequests node
            rideRequestsRef.orderByChild("status").equalTo("pending")
                    .addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                            rideRequest = dataSnapshot.getValue(BookingRequest.class);

                            if (rideRequest != null) {
                                TextView textView = view.findViewById(R.id.RequestText);
                                textView.setText("Patient Requested!");
                                mapActivity.showNotification(" Request!", "You have a request from patient");
                                RelativeLayout customerDetailsLayout = view.findViewById(R.id.CustomerDetails);
                                customerDetailsLayout.setVisibility(View.VISIBLE);
                                Button accept_button = view.findViewById(R.id.accept_button);
                                accept_button.setVisibility(View.VISIBLE);
                                Button reject_button = view.findViewById(R.id.reject_button);
                                reject_button.setVisibility(View.VISIBLE);
                                Buzzer();

                                // Check if the request is for the current driver
                                if (rideRequest.getDriverId().equals(driverId)) {
                                    // Display request details
                                    fetchSenderDetails(rideRequest.getUserId(), getView());
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

    private void fetchSenderDetails(String userId, View view) {
        // Query the "users" node to get details of the sender
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Fetch sender details
                User sender = dataSnapshot.getValue(User.class);

                if (sender != null) {
                    // Display sender details on your UI (e.g., TextViews)
                    displaySenderDetails(sender, view);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
            }

        });
    }

    private void displaySenderDetails(User sender, View view) {
        // Display the details on your UI (e.g., TextViews)
        TextView senderNameTextView = view.findViewById(R.id.name);
        TextView senderPhoneTextView = view.findViewById(R.id.mobile);
        TextView senderLocationTextView = view.findViewById(R.id.location);
        senderNameTextView.setText(sender.getFirstName());
        senderPhoneTextView.setText(sender.getPhoneNumber());
        LatLng senderLatLng = new LatLng(sender.getLatitude(), sender.getLongitude());
        String senderAddress = Utils.getAddressFromLatLng(senderLatLng,requireContext());
        senderLocationTextView.setText(senderAddress);
    }

    public void Buzzer() {
        if (buzzer == null) {
            buzzer = MediaPlayer.create(requireContext(), R.raw.ambulance_siren);
            buzzer.start();
            buzzer.setLooping(true);
        }
    }

    public void stopBuzzer() {
        if (buzzer != null) {
            buzzer.release();
            buzzer= null;
        }
    }

    private void listenForRequestStatusChanges(String requestId,View view) {
        if (requestId != null) {
            rideRequestsRef.child(requestId).child("status").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String newStatus = dataSnapshot.getValue(String.class);
                    if ("Canceled".equals(newStatus)) {
                        TextView status = view.findViewById(R.id.RequestText);
                        status.setText("Request Cancelled By Patient");
                        Button call = view.findViewById(R.id.CallPatient);
                        call.setVisibility(View.GONE);
                        RelativeLayout customerDetailsLayout = view.findViewById(R.id.CustomerDetails);
                        customerDetailsLayout.setVisibility(View.GONE);
                    } else if ("arrived".equals(newStatus)) {
                        TextView status = view.findViewById(R.id.RequestText);
                        status.setText("You arrived at Patients Location ");
                        Button call = view.findViewById(R.id.CallPatient);
                        Button rideComplete =view.findViewById(R.id.RideComplete);
                        rideComplete.setVisibility(View.VISIBLE);
                        call.setVisibility(View.VISIBLE);
                        RelativeLayout customerDetailsLayout = view.findViewById(R.id.CustomerDetails);
                        customerDetailsLayout.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle error
                }
            });
        }
    }


    private void startCallDialog() {
        String userId = rideRequest.getUserId();
        if (userId != null && !userId.isEmpty()) {
            // Query the "users" node to get details of the sender
            usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // Fetch sender details
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        String phoneNumber = user.getPhoneNumber();
                        if (phoneNumber != null && !phoneNumber.isEmpty()) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                            builder.setTitle("Call Confirmation")
                                    .setMessage("Do you want to call this number?\n" + phoneNumber)
                                    .setPositiveButton("Call", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            // Call the phone number
                                            Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
                                            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                                                startActivity(callIntent);
                                            } else {
                                                // Permission not granted, request it
                                                ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CALL_PHONE}, 1);
                                            }
                                        }
                                    })
                                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            // Cancel the dialog
                                            dialog.dismiss();
                                        }
                                    })
                                    .create()
                                    .show();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle error
                    Toast.makeText(requireContext(), "Failed to fetch user details: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }}
