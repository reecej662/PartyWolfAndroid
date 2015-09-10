package com.aolalabs.partywolf;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

/**
 * Created by reecejackson on 9/1/15.
 */
public class ClassOfA extends Activity {
    Spinner iAmPrompt;
    Spinner classOfPrompt;
    Integer classOf;
    ParseUser currentUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.class_of);

        currentUser = ParseUser.getCurrentUser();
        currentUser.fetchIfNeededInBackground();

        iAmPrompt = (Spinner) findViewById(R.id.iAmPrompt);
        classOfPrompt = (Spinner) findViewById(R.id.classNumber);

        setUpSpinner(iAmPrompt);
        setUpSpinner(classOfPrompt);
    }

    public void continueButton(View v) {
        System.out.println("User is class of " + classOf);
        currentUser.put("classOf", classOf);
        currentUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                Intent i = new Intent(ClassOfA.this, PostTableA.class);
                startActivity(i);
            }
        });
    }

    public void setUpSpinner(final Spinner spinner) {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> arg0, View view,
                                       int position, long id) {

                TextView tmpView = (TextView) spinner.getSelectedView().findViewById(android.R.id.text1);
                tmpView.setTextColor(Color.WHITE);
                tmpView.setTextSize(25);
                Log.d("Spinner text: ", tmpView.getText().toString());
                if(spinner.equals(classOfPrompt)){
                    tmpView.setGravity(Gravity.LEFT);
                    String tmpClassOf = tmpView.getText().toString();
                    classOf = Integer.valueOf(tmpClassOf);
                } else {
                    tmpView.setGravity(Gravity.RIGHT);
                }

            }

            public void onNothingSelected(AdapterView<?> arg0) {
                // do stuff

            }
        });
    }
}
