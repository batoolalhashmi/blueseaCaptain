package com.barmej.blueseacaptain.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.barmej.blueseacaptain.R;
import com.barmej.blueseacaptain.callback.CallBack;
import com.barmej.blueseacaptain.domain.TripManager;
import com.barmej.blueseacaptain.domain.entity.Captain;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class LoginActivity extends AppCompatActivity {
    private TextInputEditText emailEt;
    private TextInputEditText passwordEt;
    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;
    private Button loginBt;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        passwordEt = findViewById(R.id.password_text_input_edit_text);
        emailEt = findViewById(R.id.email_text_input_edit_text);
        loginBt = findViewById(R.id.start_button);
        progressBar = findViewById(R.id.progressBar);
        emailLayout = findViewById(R.id.email_layout);
        passwordLayout = findViewById(R.id.password_layout);

        loginBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginClicked();
            }
        });
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            fetchUserProfileAndLogin(firebaseUser.getUid());
        }
    }

    private void loginClicked() {
        if (!isValidEmail(emailEt.getText())) {
            emailEt.setError(getString(R.string.invalid_email));
            return;
        }
        if (passwordEt.getText().length() < 6) {
            passwordEt.setError(getString(R.string.invalid_password_length));
            return;
        }
        hideForm(true);
        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference mDbRef = mDatabase.getReference("captains");
        final String email = emailEt.getText().toString();
        FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(email, passwordEt.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            final String captainId = task.getResult().getUser().getUid();
                            final Captain captain = new Captain();
                            captain.setEmail(email);
                            captain.setId(captainId);
                            captain.setStatus(Captain.Status.FREE.name());
                            mDbRef.child(captainId).setValue(captain).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        fetchUserProfileAndLogin(captainId);
                                    } else {
                                        Toast.makeText(LoginActivity.this, R.string.login_error, Toast.LENGTH_LONG).show();
                                        hideForm(false);
                                    }
                                }
                            });
                        } else {
                            hideForm(false);
                            Toast.makeText(LoginActivity.this, R.string.login_error, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void fetchUserProfileAndLogin(String captainId) {
        TripManager.getInstance().getCaptainProfile(captainId, new CallBack() {
            @Override
            public void onComplete(boolean isSuccessful) {
                if (isSuccessful) {
                    startActivity(HomeActivity.getStartIntent(LoginActivity.this));
                    finish();
                } else {
                    hideForm(false);
                    Toast.makeText(LoginActivity.this, R.string.login_error, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void hideForm(boolean hide) {
        if (hide) {
            progressBar.setVisibility(View.VISIBLE);

            passwordLayout.setVisibility(View.INVISIBLE);
            emailLayout.setVisibility(View.INVISIBLE);
            loginBt.setVisibility(View.INVISIBLE);
        } else {
            progressBar.setVisibility(View.INVISIBLE);
            passwordLayout.setVisibility(View.VISIBLE);
            emailLayout.setVisibility(View.VISIBLE);
            loginBt.setVisibility(View.VISIBLE);
        }
    }

    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());

    }
}
