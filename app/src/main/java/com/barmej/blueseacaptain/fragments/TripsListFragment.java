package com.barmej.blueseacaptain.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.barmej.blueseacaptain.R;
import com.barmej.blueseacaptain.adapter.TripsListAdapter;
import com.barmej.blueseacaptain.activities.AddNewTripActivity;
import com.barmej.blueseacaptain.callback.CallBack;
import com.barmej.blueseacaptain.domain.TripManager;
import com.barmej.blueseacaptain.domain.entity.Trip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.barmej.blueseacaptain.activities.HomeActivity.showCurrentLayout;


public class TripsListFragment extends Fragment implements TripsListAdapter.OnTripClickListener {
    private Parcelable savedRecyclerLayoutState;
    private static final String BUNDLE_RECYCLER_LAYOUT = "recycler_layout";
    private static final String TRIP_REF_PATH = "trips";
    private static final String CAPTAIN_ID = "captainId";
    private RecyclerView mRecycleViewTrips;
    private TripsListAdapter mTripsListAdapter;
    private Button mAddTripButton;
    private ArrayList<Trip> mTrips;
    private LinearLayoutManager mLinearLayoutManager;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTrips = new ArrayList<>();

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        Query query = mDatabase.child(TRIP_REF_PATH)
                .orderByChild(CAPTAIN_ID).equalTo(firebaseUser.getUid());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mTrips.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    mTrips.add(ds.getValue(Trip.class));
                    if (mTripsListAdapter != null) {
                        mTripsListAdapter.notifyDataSetChanged();
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), R.string.error, Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_trips_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecycleViewTrips = view.findViewById(R.id.recycler_view_trip);
        mAddTripButton = view.findViewById(R.id.start_button);
        mAddTripButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), AddNewTripActivity.class));
            }
        });
        mLinearLayoutManager = new LinearLayoutManager(getContext());
        mRecycleViewTrips.setLayoutManager(mLinearLayoutManager);

        mTripsListAdapter = new TripsListAdapter(mTrips, TripsListFragment.this);
        mRecycleViewTrips.setAdapter(mTripsListAdapter);
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        updateCurrentFragment(firebaseUser.getUid());
        mTripsListAdapter.notifyDataSetChanged();
        if (savedRecyclerLayoutState != null) {
            mLinearLayoutManager.onRestoreInstanceState(savedRecyclerLayoutState);

        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mLinearLayoutManager != null) {
            outState.putParcelable(BUNDLE_RECYCLER_LAYOUT,
                    mLinearLayoutManager.onSaveInstanceState());
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (savedRecyclerLayoutState != null) {
            mLinearLayoutManager.onRestoreInstanceState(savedRecyclerLayoutState);
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            savedRecyclerLayoutState = savedInstanceState.getParcelable(BUNDLE_RECYCLER_LAYOUT);
        }
    }

    private void updateCurrentFragment(String captainId) {
        TripManager.getInstance().updateCurrentTripLayout(captainId, new CallBack() {
            @Override
            public void onComplete(boolean isSuccessful) {
                if (isSuccessful) {
                    showCurrentLayout(true);
                } else {
                    showCurrentLayout(false);
                }
            }
        });
    }

    @Override
    public void onTripClick(Trip trip) {
        TripDetailsFragment tripDetailsFragment = new TripDetailsFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(TripDetailsFragment.TRIP_DATA, trip);
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        tripDetailsFragment.setArguments(bundle);
        fragmentTransaction.replace(R.id.main_fragment, tripDetailsFragment).addToBackStack(null).commit();
    }
}
