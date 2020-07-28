package com.barmej.blueseacaptain.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import static java.text.DateFormat.getDateInstance;

public class HomeActivity extends AppCompatActivity {
    private CurrentTripFragment currentTripInfoFragment;
    private TripsListFragment tripsListFragment;
    private static final String TRIP_REF_PATH = "trips";
    private static final String FORMATTED_DATE = "formattedDate";
    private Trip trip;
    private FrameLayout currentTripFragmentFrameLayout;
    private DatabaseReference mDatabase;
    private Query query;
    FragmentTransaction fragmentTransaction;
    FirebaseUser firebaseUser;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, HomeActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        currentTripInfoFragment = new CurrentTripFragment();
        currentTripFragmentFrameLayout = findViewById(R.id.fragment_current_trip);

        tripsListFragment = (TripsListFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_trip_list);

        registerReceiver(m_timeChangedReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
        registerReceiver(m_timeChangedReceiver, new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED));
        registerReceiver(m_timeChangedReceiver, new IntentFilter(Intent.ACTION_TIME_CHANGED));

        final Calendar calendar = Calendar.getInstance();
        final Date currentDate = calendar.getTime();
        updateCurrentFragment(currentDate);

    }

    private final BroadcastReceiver m_timeChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Date currentDate = new Date();
            if (action.equals(Intent.ACTION_TIME_CHANGED) ||
                    action.equals(Intent.ACTION_TIMEZONE_CHANGED) ||
                    action.equals(Intent.ACTION_TIME_TICK)) {
                updateCurrentFragment(currentDate);
            }
        }
    };

    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(m_timeChangedReceiver);
    }

    private void updateCurrentFragment(Date currentDate) {
        currentTripFragmentFrameLayout.setVisibility(View.GONE);
        String stringDate = getDateInstance(DateFormat.MEDIUM).format(currentDate);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        query = mDatabase.child(TRIP_REF_PATH).orderByChild(FORMATTED_DATE)
                .equalTo(stringDate);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    trip = ds.getValue(Trip.class);
                    if (trip != null && trip.getCaptainId().equals(firebaseUser.getUid())) {
                        currentTripFragmentFrameLayout.setVisibility(View.VISIBLE);
                        if (!isFinishing()) {
                            fragmentTransaction = getSupportFragmentManager().beginTransaction();
                            fragmentTransaction.replace(R.id.fragment_current_trip, currentTripInfoFragment).commitAllowingStateLoss();
                        }
                        currentTripFragmentFrameLayout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                TripDetailsFragment tripDetailsFragment = new TripDetailsFragment();
                                Bundle bundle = new Bundle();
                                bundle.putSerializable(TripDetailsFragment.TRIP_DATA, trip);
                                tripDetailsFragment.setArguments(bundle);
                                fragmentTransaction = getSupportFragmentManager().beginTransaction();
                                fragmentTransaction.replace(R.id.main_layout, tripDetailsFragment).addToBackStack(null).commit();
                            }
                        });
                    } else {
                        currentTripFragmentFrameLayout.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(HomeActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
