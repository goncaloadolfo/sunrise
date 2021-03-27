package com.example.sunrise;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sunrise.Utils.BaseActivity;
import com.example.sunrise.Utils.FirebaseCom;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Register extends AppCompatActivity implements View.OnClickListener {

    private final String SUCCESS = "success";

    // register data
    private String email;
    private String firstName;
    private String lastName;
    private String homeTown;
    private String password;
    private String passwordConfirmation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_register);
        findViewById(R.id.signUpBtn).setOnClickListener(this);

        // change background image if landscape
        int orientation = getResources().getConfiguration().orientation;
        if(orientation == Configuration.ORIENTATION_LANDSCAPE) {
            findViewById(R.id.rootView).setBackgroundResource (R.drawable.login_background_land);
        }

        if (savedInstanceState != null){
            ((EditText) findViewById(R.id.firstNameEditText)).setText(savedInstanceState.getString("firstName"));
            ((EditText) findViewById(R.id.lastNameEditText)).setText(savedInstanceState.getString("lastName"));
            ((EditText) findViewById(R.id.homeTownEditText)).setText(savedInstanceState.getString("hometown"));
            ((EditText) findViewById(R.id.emailEditText)).setText(savedInstanceState.getString("email"));
            ((EditText) findViewById(R.id.passwordEditText)).setText(savedInstanceState.getString("password"));
            ((EditText) findViewById(R.id.confirmEditText)).setText(savedInstanceState.getString("passwordConfirmation"));
        }
    }

    @Override
    public void onClick(View v){
        if (v.getId() == R.id.signUpBtn){

            // get input values
            firstName = ((EditText) findViewById(R.id.firstNameEditText)).getText().toString();
            lastName = ((EditText) findViewById(R.id.lastNameEditText)).getText().toString();
            homeTown = ((EditText) findViewById(R.id.homeTownEditText)).getText().toString();
            email = ((EditText) findViewById(R.id.emailEditText)).getText().toString();
            password = ((EditText) findViewById(R.id.passwordEditText)).getText().toString();
            passwordConfirmation = ((EditText) findViewById(R.id.confirmEditText)).getText().toString();

            // validate input values
            String validationStr = checkUserInputs();

            // register user
            if (validationStr.equals(SUCCESS)){
                registerUser();
            }

            // print validation error
            else {
                Toast.makeText(Register.this, validationStr, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void registerUser(){
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener
                (this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                // user creation success
                if (task.isSuccessful()){
                    // send email verification
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    user.sendEmailVerification();
                    FirebaseAuth.getInstance().signOut();

                    // intent to login activity
                    Intent intent = new Intent(Register.this, Login.class);
                    startActivity(intent);

                    // insert user data to db
                    insertUserToDB();
                }

                // otherwise
                else {
                    Toast.makeText(Register.this, R.string.registrationMsg, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private String checkUserInputs(){
        // there are empty inputs
        boolean emptyFields = firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() ||
                homeTown.isEmpty() || password.isEmpty() || passwordConfirmation.isEmpty();

        if (!emptyFields) {
            boolean passwordsValidation = samePassword();

            // passwords are not matching
            if (!passwordsValidation){
                return getString(R.string.passwordsValidation);
            }

            // inputs are ok
            else {
                return SUCCESS;
            }
        }

        else {
            return getString(R.string.emptyFields);
        }
    }

    private boolean samePassword(){
        // passwords string comparator
        return password.equals(passwordConfirmation);
    }

    private void insertUserToDB(){
        FirebaseCom.addUser(email, firstName, lastName, homeTown);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("firstName", ((EditText) findViewById(R.id.firstNameEditText)).getText().toString());
        outState.putString("lastName", ((EditText) findViewById(R.id.lastNameEditText)).getText().toString());
        outState.putString("hometown", ((EditText) findViewById(R.id.homeTownEditText)).getText().toString());
        outState.putString("email", ((EditText) findViewById(R.id.emailEditText)).getText().toString());
        outState.putString("password", ((EditText) findViewById(R.id.passwordEditText)).getText().toString());
        outState.putString("passwordConfirmation", ((EditText) findViewById(R.id.confirmEditText)).getText().toString());
    }
}
