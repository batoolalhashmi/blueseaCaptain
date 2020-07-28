
package com.barmej.blueseacaptain.domain;

import androidx.annotation.NonNull;

import com.barmej.blueseacaptain.callback.CallBack;
import com.barmej.blueseacaptain.callback.StatusCallBack;
import com.barmej.blueseacaptain.domain.entity.Captain;
import com.barmej.blueseacaptain.domain.entity.FullStatus;
import com.barmej.blueseacaptain.domain.entity.Trip;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class TripManager {
    private static final String TRIP_REF_PATH = "trips";
    private static final String CAPTAIN_REF_PATH = "captains";

    private static TripManager instance;
    private FirebaseDatabase database;

    private Trip trip;
    private Captain captain;

    private ValueEventListener tripStatusListener;
    private StatusCallBack statusCallBack;

    public TripManager() {
        database = FirebaseDatabase.getInstance();
    }

    public static TripManager getInstance() {
        if (instance == null) {
            instance = new TripManager();
        }
        return instance;
    }

    public void getCaptainProfile(final String captainId, final CallBack callback) {
        database.getReference(CAPTAIN_REF_PATH).child(captainId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                captain = dataSnapshot.getValue(Captain.class);
                if (captain != null) {
                    callback.onComplete(true);
                } else {
                    callback.onComplete(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void startListeningForStatus(StatusCallBack statusCallBack, final String tripId) {
        this.statusCallBack = statusCallBack;
        tripStatusListener = database.getReference(TRIP_REF_PATH).child(tripId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                trip = dataSnapshot.getValue(Trip.class);
                if (trip != null) {
                    if (trip.getStatus().equals(Trip.Status.ON_TRIP.name())) {
                        getCaptainAndNotifyStatus(tripId);
                    } else {
                        FullStatus fullStatus = new FullStatus();
                        fullStatus.setTrip(trip);
                        notifyListener(fullStatus);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getCaptainAndNotifyStatus(String tripId) {
        database.getReference(TRIP_REF_PATH).child(tripId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final Trip trip = dataSnapshot.getValue(Trip.class);
                database.getReference(CAPTAIN_REF_PATH).child(trip.getCaptainId())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                captain = dataSnapshot.getValue(Captain.class);
                                if (captain != null) {
                                    FullStatus fullStatus = new FullStatus();
                                    fullStatus.setCaptain(captain);
                                    fullStatus.setTrip(trip);
                                    notifyListener(fullStatus);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void notifyListener(FullStatus fullStatus) {
        if (statusCallBack != null) {
            statusCallBack.onUpdate(fullStatus);
        }
    }

    public void updateCurrentLocation(double lat, double lng) {
        trip.setCurrentLat(lat);
        trip.setCurrentLng(lng);
        database.getReference(TRIP_REF_PATH).child(trip.getId()).setValue(trip);

        FullStatus fullStatus = new FullStatus();
        fullStatus.setTrip(trip);
        fullStatus.setCaptain(captain);
        notifyListener(fullStatus);

    }

    public void updateTripToArrivedToDestination(Trip trip) {
        trip.setStatus(Trip.Status.ARRIVED.name());
        database.getReference(TRIP_REF_PATH).child(trip.getId()).setValue(trip);

        captain.setStatus(Captain.Status.FREE.name());
        database.getReference(CAPTAIN_REF_PATH).child(captain.getId()).setValue(captain);

        FullStatus fullStatus = new FullStatus();
        fullStatus.setCaptain(captain);
        fullStatus.setTrip(trip);
        notifyListener(fullStatus);
    }

    public void startTrip(Trip trip) {
        trip.setStatus(Trip.Status.ON_TRIP.name());
        database.getReference(TRIP_REF_PATH).child(trip.getId()).setValue(trip);

        captain.setStatus(Captain.Status.ON_TRIP.name());
        database.getReference(CAPTAIN_REF_PATH).child(captain.getId()).setValue(captain);

        FullStatus fullStatus = new FullStatus();
        fullStatus.setCaptain(captain);
        fullStatus.setTrip(trip);
        notifyListener(fullStatus);

    }

    public void stopListeningToStatus() {
        if (tripStatusListener != null) {
            database.getReference().child(captain.getId()).removeEventListener(tripStatusListener);
        }
        statusCallBack = null;
    }
}