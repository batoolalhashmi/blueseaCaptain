package com.barmej.blueseacaptain.activities;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.barmej.blueseacaptain.R;
import com.barmej.blueseacaptain.domain.entity.Trip;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
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

public class AddNewTripActivity extends AppCompatActivity implements DatePicker.OnDateChangedListener {

    private static final String TRIP_REF_PATH = "trips";
    private static final String FORMATTED_DATE = "formattedDate";
    private ConstraintLayout mConstraintLayout;
    private TextInputLayout mTripPickUpPortTextInputLayout;
    private TextInputLayout mTripDestinationPortTextInputLayout;
    private TextInputLayout mAvailableSeatsTextInputLayout;
    private TextInputEditText mTripPickUpPortTextInputEditText;
    private TextInputEditText mTripDestinationPortTextInputEditText;
    private TextInputEditText mAvailableSeatsTextInputEditText;
    private Date mTripDate;
    private Button addTripButton;
    private Trip checkTrip;
    private String stringDate;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_trip);
        mConstraintLayout = findViewById(R.id.add_trip_constraint_layout);
        mTripPickUpPortTextInputLayout = findViewById(R.id.password_layout);
        mTripDestinationPortTextInputLayout = findViewById(R.id.email_layout);
        mAvailableSeatsTextInputLayout = findViewById(R.id.available_seats_layout);
        mTripPickUpPortTextInputEditText = findViewById(R.id.pick_up_port);
        mTripDestinationPortTextInputEditText = findViewById(R.id.destination_port);
        mAvailableSeatsTextInputEditText = findViewById(R.id.available_seats);
        addTripButton = findViewById(R.id.start_button);
        DatePicker datePicker = findViewById(R.id.date_picker);

        addTripButton.setVisibility(View.VISIBLE);

        datePicker.setMinDate(new Date().getTime());
        datePicker.setOnDateChangedListener(this);

        addTripButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                mTripPickUpPortTextInputLayout.setError(null);
                mTripDestinationPortTextInputLayout.setError(null);
                mAvailableSeatsTextInputLayout.setError(null);
                if (TextUtils.isEmpty(mTripPickUpPortTextInputEditText.getText())) {
                    mTripPickUpPortTextInputLayout.setError(getString(R.string.error_msg_pick_up));
                } else if (TextUtils.isEmpty(mTripDestinationPortTextInputEditText.getText())) {
                    mTripDestinationPortTextInputLayout.setError(getString(R.string.error_msg_destination));
                } else if (TextUtils.isEmpty(mAvailableSeatsTextInputEditText.getText())) {
                    mAvailableSeatsTextInputLayout.setError(getString(R.string.error_msg_available_seat));
                } else {
                    addTripButton.setVisibility(View.GONE);
                    addTripToFirebase();
                }
            }
        });
    }


    private void addTripToFirebase() {
        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference mDbRef = mDatabase.getReference(TRIP_REF_PATH);
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        final Trip trip = new Trip();

        final String captainId = firebaseUser.getUid();

        trip.setPickUpPort(mTripPickUpPortTextInputEditText.getText().toString());
        trip.setDestinationPort(mTripDestinationPortTextInputEditText.getText().toString());
        trip.setAvailableSeats(mAvailableSeatsTextInputEditText.getText().toString());
        trip.setCaptainId(captainId);
        trip.setStatus(Trip.Status.MOVING_SOON.name());

        if (mTripDate != null) {
            trip.setDate(mTripDate.getTime());
            stringDate = getDateInstance(DateFormat.MEDIUM).format(mTripDate);

        } else {
            Calendar calendar = Calendar.getInstance();
            stringDate = getDateInstance(DateFormat.MEDIUM).format(calendar.getTime());
            trip.setDate(calendar.getTime().getTime());

        }
        Query query = mDbRef.orderByChild(FORMATTED_DATE).equalTo(stringDate);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    checkTrip = ds.getValue(Trip.class);
                }
                if (checkTrip != null && checkTrip.getCaptainId().contains(captainId)) {
                    Toast.makeText(AddNewTripActivity.this, R.string.can_not_add_trip_in_same_date, Toast.LENGTH_LONG).show();
                    addTripButton.setVisibility(View.VISIBLE);
                } else {
                    trip.setBookedUpSeats("0");
                    final String tripId = mDbRef.push().getKey();
                    trip.setId(tripId);

                    mDbRef.child(tripId).setValue(trip).

                            addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Snackbar.make(mConstraintLayout, R.string.add_trip_success, Snackbar.LENGTH_SHORT).addCallback(new Snackbar.Callback() {
                                            @Override
                                            public void onDismissed(Snackbar transientBottomBar, int event) {
                                                super.onDismissed(transientBottomBar, event);
                                                finish();
                                            }
                                        }).show();
                                    } else {
                                        Snackbar.make(mConstraintLayout, R.string.add_trip_failed, Snackbar.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Snackbar.make(mConstraintLayout, R.string.add_trip_failed, Snackbar.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    public void onDateChanged(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
        Calendar newCalender = Calendar.getInstance();
        newCalender.set(year, monthOfYear, dayOfMonth);
        mTripDate = newCalender.getTime();

    }
}
