package com.barmej.blueseacaptain.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import com.barmej.blueseacaptain.R;
import com.barmej.blueseacaptain.domain.entity.Trip;
import com.barmej.blueseacaptain.fragments.CurrentTripFragment;
import com.barmej.blueseacaptain.fragments.TripDetailsFragment;
import com.barmej.blueseacaptain.fragments.TripsListFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeActivity extends AppCompatActivity {
    private static final String SAVED_FRAGMENT = "fragment";
    private TripsListFragment tripsListFragment;
    private static final String TRIP_REF_PATH = "trips";
    private static final String ON_TRIP = "ON_TRIP";
    private static final String STATUS = "status";
    private Trip trip;
    private static FrameLayout currentTripFragmentFrameLayout;
    private DatabaseReference mDatabase;
    FragmentTransaction fragmentTransaction;
    FirebaseUser firebaseUser;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, HomeActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        currentTripFragmentFrameLayout = findViewById(R.id.fragment_current_trip);
        tripsListFragment = new TripsListFragment();
        if (savedInstanceState == null) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.main_fragment, tripsListFragment, SAVED_FRAGMENT).commit();
        } else {
            tripsListFragment = (TripsListFragment) getSupportFragmentManager().findFragmentByTag(SAVED_FRAGMENT);
        }
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        updateCurrentFragment();
    }

    public static void showCurrentLayout(boolean showCurrentLayout) {
        if (showCurrentLayout) {
            currentTripFragmentFrameLayout.setVisibility(View.VISIBLE);
        } else {
            currentTripFragmentFrameLayout.setVisibility(View.INVISIBLE);
        }
    }

    private void updateCurrentFragment() {
        mDatabase = FirebaseDatabase.getInstance().getReference(TRIP_REF_PATH);
        mDatabase.orderByChild(STATUS).equalTo(ON_TRIP).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    trip = ds.getValue(Trip.class);
                    if (trip != null && trip.getCaptainId().equals(firebaseUser.getUid())) {
                        if (!isFinishing()) {
                            fragmentTransaction = getSupportFragmentManager().beginTransaction();
                            CurrentTripFragment currentTripInfoFragment = new CurrentTripFragment();
                            fragmentTransaction.replace(R.id.fragment_current_trip, currentTripInfoFragment).commitAllowingStateLoss();
                        }
                        currentTripFragmentFrameLayout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                TripDetailsFragment tripDetailsFragment = new TripDetailsFragment();
                                Bundle bundle = new Bundle();
                                bundle.putSerializable(TripDetailsFragment.TRIP_DATA, trip);
                                tripDetailsFragment.setArguments(bundle);
                                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                                fragmentTransaction.replace(R.id.main_fragment, tripDetailsFragment).addToBackStack(null).commit();
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}