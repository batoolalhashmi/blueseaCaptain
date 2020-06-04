package com.barmej.blueseacaptain.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.barmej.blueseacaptain.R;
import com.barmej.blueseacaptain.fragments.TripsListFragment;

public class HomeActivity extends AppCompatActivity {
    public static final String SAVED_FRAGMENT = "fragment";
    private TripsListFragment tripsListFragment;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, HomeActivity.class);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        tripsListFragment = new TripsListFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if (savedInstanceState == null) {
            fragmentTransaction.replace(R.id.main_layout, tripsListFragment, SAVED_FRAGMENT).commit();
        }
    }
}
