package com.aolalabs.partywolf;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

/**
 * Created by reecejackson on 8/30/15.
 *
 */

public class AddEmailA extends Activity {
    private ParseUser currentUser;
    private EditText email;
    private String schoolEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.add_email);

        currentUser = ParseUser.getCurrentUser();
        System.out.println("Current user " + currentUser);
        email = (EditText) findViewById(R.id.emailText);

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);


        // Checks whether a hardware keyboard is available
        if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO) {
            Button confirmationButton = (Button) findViewById(R.id.confirmationButton);
            confirmationButton.setVisibility(View.VISIBLE);
            System.out.println("Keyboard was shown");
        }
    }

    public void emailSubmitted(View v) {
        Editable email =  this.email.getText();
        schoolEmail = email.toString();

        if(email.length() > 7) {
            String ending = email.subSequence(email.length() - 3, email.length()).toString();
            if (!ending.equals("edu")) {
                Toast.makeText(this, "Error please enter a valid edu email address", Toast.LENGTH_LONG).show();
                Log.d("Email ending", ending);
            } else {
                currentUser.put("email", email.toString());
                Log.d("Put email", "success putting user's email");

                try{
                    connectPack();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    currentUser.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            Intent i = new Intent(AddEmailA.this, PostTableA.class);
                            startActivity(i);
                        }
                    });
                }
            }
        } else {
            Toast.makeText(this, "Error please enter a valid edu email address", Toast.LENGTH_LONG).show();
        }

    }

    public void connectPack() {
        int atIndex = schoolEmail.indexOf("@");
        final String realSchoolEmail = schoolEmail.substring(atIndex, schoolEmail.length());
        Log.d("School email suffix", realSchoolEmail);

        ParseQuery<ParseObject> universityQuery = ParseQuery.getQuery("Universities");
        universityQuery.whereEqualTo("email_ending", realSchoolEmail);
        universityQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if(e == null) {
                    ParseObject realUniversity = list.get(0);
                    currentUser.put("university", realUniversity);
                    currentUser.put("pack", realUniversity.get("name"));
                }
            }
        });
    }

    public void back(View v) {
        finish();
    }


}
