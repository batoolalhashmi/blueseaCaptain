package com.barmej.blueseacaptain.fragments;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.barmej.blueseacaptain.R;
import com.barmej.blueseacaptain.callback.PermissionFailListener;
import com.barmej.blueseacaptain.callback.StatusCallBack;
import com.barmej.blueseacaptain.domain.TripManager;
import com.barmej.blueseacaptain.domain.entity.FullStatus;
import com.barmej.blueseacaptain.domain.entity.Trip;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TripDetailsFragment extends Fragment implements OnMapReadyCallback {
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private static final String TRIP_REF_PATH = "trips";
    public static final String TRIP_DATA = "trip_data";
    private PermissionFailListener permissionFailListener = getPermissionFailListener();

    private MapView mMapView;
    private Trip trip;
    private GoogleMap mMap;
    private Marker pickUpMarker;
    private Marker destinationMarker;
    private Trip tripBundle;
    DatabaseReference mDatabase;
    private String id;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient locationProviderClient;
    private StatusCallBack statusCallBack = getStatusCallBack();
    private Button startButton;
    private Button arrivedButton;
    private LatLng pickUpLatLng;
    private LatLng destinationLatLng;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_trip_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final TextView tripPickUpPortTextView = view.findViewById(R.id.pick_up_port_text_view);
        final TextView tripDestinationPortTextView = view.findViewById(R.id.destination_port_text_view);
        final TextView tripDateTextView = view.findViewById(R.id.date);
        final TextView tripAvailableSeats = view.findViewById(R.id.available_seats);
        final TextView tripBookedUpSeats = view.findViewById(R.id.booked_up_seats);
        arrivedButton = view.findViewById(R.id.arrived_button);
        startButton = view.findViewById(R.id.start_button);
        mMapView = view.findViewById(R.id.map_view);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);

        Bundle bundle = this.getArguments();
        tripBundle = (Trip) bundle.getSerializable(TRIP_DATA);
        if (tripBundle != null) {
            id = tripBundle.getId();
            mDatabase = FirebaseDatabase.getInstance().getReference(TRIP_REF_PATH);

            mDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    trip = dataSnapshot.child(id).getValue(Trip.class);

                    tripPickUpPortTextView.setText(trip.getPickUpPort());
                    tripDestinationPortTextView.setText(trip.getDestinationPort());
                    tripDateTextView.setText(trip.getFormattedDate());
                    tripAvailableSeats.setText(trip.getAvailableSeats());
                    tripBookedUpSeats.setText(trip.getBookedUpSeats());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }
        arrivedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TripManager.getInstance().updateTripToArrivedToDestination(trip);
            }
        });
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TripManager.getInstance().startTrip(trip);
            }
        });

    }

    private StatusCallBack getStatusCallBack() {
        return new StatusCallBack() {
            @Override
            public void onUpdate(FullStatus fullStatus) {
                String tripStatus = fullStatus.getTrip().getStatus();
                if (tripStatus.equals(Trip.Status.MOVING_SOON.name())) {
                    updateWithStatus(fullStatus);
                } else if (tripStatus.equals(Trip.Status.ON_TRIP.name())) {
                    updateWithStatus(fullStatus);
                    trackAndSendLocationUpdates();
                } else if (tripStatus.equals(Trip.Status.ARRIVED.name())) {
                    updateWithStatus(fullStatus);
                }
            }
        };
    }

    private void trackAndSendLocationUpdates() {
        if (locationCallback == null) {
            locationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    Location lastLocation = locationResult.getLastLocation();
                    TripManager.getInstance().updateCurrentLocation(lastLocation.getLatitude(), lastLocation.getLongitude());

                }

            };
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationProviderClient.requestLocationUpdates(new LocationRequest(), locationCallback, null);
            }
        }
    }

    private void checkLocationPermissionAndSetUpLocation() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            setUpUserLocation();
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        checkLocationPermissionAndSetUpLocation();
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Trip trip = dataSnapshot.child(id).getValue(Trip.class);
                mMap = googleMap;
                updateMarkers(trip);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void setPickUpMarker(LatLng target) {
        if (mMap == null) return;
        if (pickUpMarker == null) {
            BitmapDescriptor descriptor = BitmapDescriptorFactory.fromResource(R.drawable.position);
            MarkerOptions options = new MarkerOptions();
            options.icon(descriptor);
            options.position(target);

            pickUpMarker = mMap.addMarker(options);
        } else {
            pickUpMarker.setPosition(target);
        }

    }

    private void setDestinationMarker(LatLng target) {
        if (mMap == null) return;
        if (destinationMarker == null) {
            BitmapDescriptor descriptor = BitmapDescriptorFactory.fromResource(R.drawable.destination);
            MarkerOptions options = new MarkerOptions();
            options.icon(descriptor);
            options.position(target);

            destinationMarker = mMap.addMarker(options);
        } else {
            destinationMarker.setPosition(target);
        }
    }

    private void updateMarkers(Trip trip) {
        if (trip != null) {
            pickUpLatLng = new LatLng(trip.getPickUpLat(), trip.getPickUpLng());
            destinationLatLng = new LatLng(trip.getDestinationLat(), trip.getDestinationLng());

            setPickUpMarker(pickUpLatLng);
            setDestinationMarker(destinationLatLng);
        }
    }

    private PermissionFailListener getPermissionFailListener() {
        return new PermissionFailListener() {
            @Override
            public void onPermissionFail() {
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(R.string.location_permission_needed);

                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
            }
        };
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (permissions.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setUpUserLocation();
            } else {
                if (permissionFailListener != null) {
                    permissionFailListener.onPermissionFail();
                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void setUpUserLocation() {
        if (mMap == null)
            return;
        mMap.setMyLocationEnabled(true);

        FusedLocationProviderClient locationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        locationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    CameraUpdate update = CameraUpdateFactory.newLatLngZoom(currentLatLng, 16f);
                    mMap.moveCamera(update);
                }
            }
        });
    }

    @Override
    public void onStop() {
        TripManager.getInstance().stopListeningToStatus();
        stopLocationUpdates();
        mMapView.onStop();
        super.onStop();
    }

    @Override
    public void onResume() {
        mMapView.onResume();
        TripManager.getInstance().startListeningForStatus(statusCallBack, id);
        super.onResume();
    }

    private void stopLocationUpdates() {
        if (locationProviderClient != null && locationCallback != null) {
            locationProviderClient.removeLocationUpdates(locationCallback);
            locationCallback = null;
        }
    }

    private void updateWithStatus(FullStatus fullStatus) {

        String tripStatus = fullStatus.getTrip().getStatus();

        if (tripStatus.equals(Trip.Status.MOVING_SOON.name())) {
            arrivedButton.setVisibility(View.GONE);
            startButton.setVisibility(View.VISIBLE);

        } else if (tripStatus.equals(Trip.Status.ON_TRIP.name())) {
            arrivedButton.setVisibility(View.VISIBLE);
            startButton.setVisibility(View.GONE);

        } else if (tripStatus.equals(Trip.Status.ARRIVED.name())) {
            arrivedButton.setVisibility(View.GONE);
            startButton.setVisibility(View.GONE);
        }
    }

}