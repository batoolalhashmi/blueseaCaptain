package com.barmej.blueseacaptain.activities;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;

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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Date;

public class AddNewTripActivity extends AppCompatActivity implements DatePicker.OnDateChangedListener {

    private static final String TRIP_REF_PATH = "trips";
    private ConstraintLayout mConstraintLayout;
    private TextInputLayout mTripPickUpPortTextInputLayout;
    private TextInputLayout mTripDestinationPortTextInputLayout;
    private TextInputLayout mAvailableSeatsTextInputLayout;
    private TextInputEditText mTripPickUpPortTextInputEditText;
    private TextInputEditText mTripDestinationPortTextInputEditText;
    private TextInputEditText mAvailableSeatsTextInputEditText;
    private DatePicker datePicker;
    private Date mTripDate;
    private Button addTripButton;

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
        datePicker = findViewById(R.id.date_picker);

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

        String captainId = firebaseUser.getUid();

        trip.setPickUpPort(mTripPickUpPortTextInputEditText.getText().toString());
        trip.setDestinationPort(mTripDestinationPortTextInputEditText.getText().toString());
        trip.setAvailableSeats(mAvailableSeatsTextInputEditText.getText().toString());
        trip.setCaptainId(captainId);
        trip.setStatus(Trip.Status.MOVING_SOON.name());

        if (mTripDate != null) {
            trip.setDate(mTripDate.getTime());
        } else {
            Calendar calendar = Calendar.getInstance();
            trip.setDate(calendar.getTime().getTime());
        }

        trip.setBookedUpSeats("0");

        final String tripId = mDbRef.push().getKey();
        trip.setId(tripId);

        mDbRef.child(tripId).setValue(trip).addOnCompleteListener(new OnCompleteListener<Void>() {
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

    @Override
    public void onDateChanged(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
        Calendar newCalender = Calendar.getInstance();
        newCalender.set(year, monthOfYear, dayOfMonth);
        mTripDate = newCalender.getTime();
    }
}
