package com.example.sunrise;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import androidx.annotation.NonNull;

import android.content.res.Configuration;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sunrise.BeachListActivity.BeachList;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth auth;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        // firebase auth instance
        auth = FirebaseAuth.getInstance();

        // set onclick listeners
        findViewById(R.id.signinButton).setOnClickListener(this);
        findViewById(R.id.signupButton).setOnClickListener(this);
        findViewById(R.id.buttonReturn).setOnClickListener(this);

        // change background image if landscape
        int orientation = getResources().getConfiguration().orientation;
        if(orientation == Configuration.ORIENTATION_LANDSCAPE) {
            findViewById(R.id.rootView).setBackgroundResource (R.drawable.login_background_land);
        }

        if (savedInstanceState != null){
            ((TextView) findViewById(R.id.emailEditText)).setText(savedInstanceState.getString("email"));
            ((TextView) findViewById(R.id.passEditText)).setText(savedInstanceState.getString("password"));
        }
    }

    @Override
    public void onClick(final View v) {
        if (v.getId() == R.id.signinButton){
            // get input values
            String email = ((EditText) findViewById(R.id.emailEditText)).getText().toString();
            String password = ((EditText) findViewById(R.id.passEditText)).getText().toString();

            if (!email.isEmpty() && !password.isEmpty()) {
                // login
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this,
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // success
                                if (task.isSuccessful()) {

                                    FirebaseUser user = auth.getCurrentUser();

                                    // check if user is verified
                                    if (user.isEmailVerified())
                                        finish();

                                    else{
                                        auth.signOut();
                                        Toast.makeText(v.getContext(), getString(R.string.emailVerification), Toast.LENGTH_LONG).show();
                                    }
                                }

                                // failed
                                else {
                                    Log.w("Auth error", task.getException());
                                    Toast.makeText(Login.this, R.string.authFail,
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }

            else{
                Toast.makeText(Login.this, R.string.emptyFields,
                        Toast.LENGTH_LONG).show();
            }
        }

        else if (v.getId() == R.id.signupButton){
            Intent intent = new Intent(Login.this, Register.class);
            startActivity(intent);
        }

        else if (v.getId() == R.id.buttonReturn){
            Intent intent = new Intent(Login.this, BeachList.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("email", ((TextView) findViewById(R.id.emailEditText)).getText().toString());
        outState.putString("password", ((TextView) findViewById(R.id.passEditText)).getText().toString());
    }
}
