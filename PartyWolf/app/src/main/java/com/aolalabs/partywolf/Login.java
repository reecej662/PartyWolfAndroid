package com.aolalabs.partywolf;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;


public class Login extends Activity {

    private EditText usernameText;
    private EditText passwordText;
    private EditText passwordAgainText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ParseAnalytics.trackAppOpenedInBackground(getIntent());

        //Determine whether or not a user is already logged in
        if (ParseUser.getCurrentUser() != null) {
            Intent intent = new Intent(Login.this, Date.class);
            startActivity(new Intent(intent));
        }

        //Set up sign in form
        //usernameText = (EditText) findViewById(R.id.username);
        //passwordText = (EditText) findViewById(R.id.password);
        //passwordAgainText = (EditText) findViewById(R.id.password_again);
    }

    public void learnMore(View view){
        Intent intent = new Intent(this, Learn_More.class);
        startActivity(new Intent(intent));
    }

    public void makeVisible (View view){
        LinearLayout sLayout = (LinearLayout) findViewById(R.id.second_layout);
        sLayout.setVisibility(View.VISIBLE);
    }

    public void signIn(View view) {
        boolean validationErrorSignIn = false;
        StringBuilder validationErrorMessageSignIn = new StringBuilder(getResources().getString(R.string.please));

        if (usernameText.getText().toString().trim().length() == 0) {
            validationErrorSignIn = true;
            validationErrorMessageSignIn.append(getResources().getString(R.string.enter_a_username));
        }

        if (passwordText.getText().toString().trim().length() == 0) {
            if (validationErrorSignIn) {
                validationErrorMessageSignIn.append(getResources().getString(R.string.and));
            }
            validationErrorSignIn = true;
            validationErrorMessageSignIn.append(getResources().getString(R.string.enter_a_password));
        }
        validationErrorMessageSignIn.append(getResources().getString(R.string.period));

        if (validationErrorSignIn) {
            Toast.makeText(Login.this, validationErrorMessageSignIn.toString(), Toast.LENGTH_LONG).show();
            return;
        }

        //Progress Dialog
        final ProgressDialog dlg = new ProgressDialog(Login.this);
        dlg.setTitle("Please wait.");
        dlg.setMessage("Signing in.");
        dlg.show();

        //Parse login method
        ParseUser.logInInBackground(usernameText.getText().toString(), passwordText.getText().toString(), new LogInCallback() {
            public void done(ParseUser user, ParseException e) {
                dlg.dismiss();
                if (e != null) {
                    Toast.makeText(Login.this, e.getMessage(), Toast.LENGTH_LONG).show();
                } else {
                    Intent intent = new Intent(Login.this, Date.class);
                    startActivity(new Intent(intent));
                }
            }
        });
    };

    public void signUp1 (View view) {
        //Validate the first Sign Up button
        boolean validationErrorSignUp = false;
        StringBuilder validationErrorMessageSignUp = new StringBuilder(getResources().getString(R.string.please));

        if (usernameText.getText().toString().trim().length() == 0) {
            validationErrorSignUp = true;
            validationErrorMessageSignUp.append(getResources().getString(R.string.enter_a_username));
        }

        if (passwordText.getText().toString().trim().length() == 0) {
            if (validationErrorSignUp){
                validationErrorMessageSignUp.append(getResources().getString(R.string.and));
            }
            validationErrorSignUp = true;
            validationErrorMessageSignUp.append(getResources().getString(R.string.enter_a_password));
        }
        validationErrorMessageSignUp.append(getResources().getString(R.string.period));

        //If there is an error, display message
        if (validationErrorSignUp){
            Toast.makeText(Login.this, validationErrorMessageSignUp.toString(), Toast.LENGTH_LONG).show();
            return;
        }

        else {
            //Make visible the re-enter password edit text
            /*passwordAgainText.setVisibility(View.VISIBLE);

            //Make invisible the original Sign Up button
            Button aButton = (Button) findViewById(R.id.sign_up_button);
            aButton.setVisibility(View.GONE);

            //Make visible the second Sign Up button
            Button bButton = (Button) findViewById(R.id.sign_up_button2);
            bButton.setVisibility(View.VISIBLE);

            //Make invisible the Sign In button
            Button cButton = (Button) findViewById(R.id.sign_in_button);
            cButton.setVisibility(View.GONE);*/
        }
    }

    //This is the actual sign up process
    public void signUp2 (View view){
        boolean validationErrorSignUp2 = false;
        StringBuilder validationErrorMessageSignUp2 = new StringBuilder(getResources().getString(R.string.please));


        if (usernameText.getText().toString().trim().length() == 0) {
            validationErrorSignUp2 = true;
            validationErrorMessageSignUp2.append(getResources().getString(R.string.enter_a_username));
        }

        if (passwordText.getText().toString().trim().length() == 0) {
            if (validationErrorSignUp2){
                validationErrorMessageSignUp2.append(getResources().getString(R.string.and));
            }
            validationErrorSignUp2 = true;
            validationErrorMessageSignUp2.append(getResources().getString(R.string.enter_a_password));
        }
        if (!isSame(passwordText, passwordAgainText)) {
            if (validationErrorSignUp2) {
                validationErrorMessageSignUp2.append(getResources().getString(R.string.and));
            }
            validationErrorSignUp2 = true;
            validationErrorMessageSignUp2.append(getResources().getString(R.string.enter_same_password));
        }
        validationErrorMessageSignUp2.append(getResources().getString(R.string.period));

        //If there is an error, display message
        if (validationErrorSignUp2){
            Toast.makeText(Login.this, validationErrorMessageSignUp2.toString(), Toast.LENGTH_LONG).show();
            return;
        }

        //Progress Dialog
        final ProgressDialog dlg = new ProgressDialog(Login.this);
        dlg.setTitle("Please wait.");
        dlg.setMessage("Signing up.");
        dlg.show();

        //Add new user to database
        ParseUser user = new ParseUser();
        user.setUsername(usernameText.getText().toString());
        user.setPassword(passwordText.getText().toString());
        user.signUpInBackground(new SignUpCallback() {

            @Override
            public void done(ParseException e) {
                dlg.dismiss();
                if (e != null) {
                    //Error
                    Toast.makeText(Login.this, e.getMessage(), Toast.LENGTH_LONG).show();
                } else {
                    //Direct to app
                    Intent intent = new Intent(Login.this, Date.class);
                    startActivity(new Intent(intent));
                }
            }
        });
    }

        private boolean isSame(EditText etText1, EditText etText2) {
            if (etText1.getText().toString().equals(etText2.getText().toString())){
                return true;
            } else {
                return false;
            }
        }

}