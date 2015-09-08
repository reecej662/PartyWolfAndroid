package com.aolalabs.partywolf;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.List;
import java.util.Locale;

public class Settings extends Activity {

    private Boolean initialFirstCheck;
    private Boolean initialSecondCheck;
    private String userId;
    private Boolean onNew;
    private Boolean onStatus;
    private Integer onHype;
    private ParseUser currentUser = null;
    private ImageView profilePicture = null;
    private Location userLocation = null;
    private String userCity = null;

    NumberPicker noPicker = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        currentUser = ParseUser.getCurrentUser();

        userLocation = (Location) getIntent().getExtras().get("location");
        System.out.println(userLocation);
        if(userLocation != null) {
            userCity = getCity(userLocation);
        } else {
            try{
                userCity = currentUser.getString("currentCity");
            } catch (Exception e) {
                e.printStackTrace();
                userCity = "Unable to find city";
            }
        }

        noPicker = (NumberPicker) findViewById(R.id.on_number);
        noPicker.setMaxValue(100);
        noPicker.setMinValue(0);
        noPicker.setWrapSelectorWheel(false);
        noPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        userId = currentUser.getObjectId();

        Button doneButton = (Button) findViewById(R.id.doneButton);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        currentUser.saveInBackground();
    }

    protected void onStart(){
        super.onStart();

        //To automatically set on the first and second switches if necessary
        onNew = currentUser.getBoolean("onNew");
        onStatus = currentUser.getBoolean("onStatus");
        onHype = currentUser.getInt("onHype");


        Switch firstSwitch = (Switch) findViewById(R.id.first_notification);
        Switch secondSwitch = (Switch) findViewById(R.id.second_notification);
        NumberPicker onNewHype = (NumberPicker) findViewById(R.id.on_number);
        profilePicture = (ImageView) findViewById(R.id.userProfilePic);


        if (onNew) {
            firstSwitch.setChecked(true);
        } else {
            firstSwitch.setChecked(false);
        }

        if (onStatus) {
            secondSwitch.setChecked(true);
        } else {
            secondSwitch.setChecked(false);
        }

        onNewHype.setValue(onHype);

        /*ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("User");
        query.getInBackground(userId, new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    String hello = (String) object.get("hello");
                    Toast.makeText(Settings.this, "hello" + hello,Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(Settings.this, "Wrong "+ e, Toast.LENGTH_LONG).show();// something went wrong
                }
            }
        });


        boolean initialFirstBoolean = currentUser.getBoolean("onNew");
        onHype = ParseUser.getCurrentUser().getInt("onHype");

        noPicker.setValue(onHype);

        boolean initialSecondBoolean = currentUser.getBoolean("onNew");


        if (initialFirstBoolean) {
            Switch firstSwitch = (Switch) findViewById(R.id.first_notification);
            firstSwitch.setChecked(true);
        } else {
            Switch firstSwitch = (Switch) findViewById(R.id.first_notification);
            firstSwitch.setChecked(false);
        }

        if (initialSecondBoolean) {
            Switch secondSwitch = (Switch) findViewById(R.id.second_notification);
            secondSwitch.setChecked(true);
        } else {
            Switch secondSwitch = (Switch) findViewById(R.id.second_notification);
            secondSwitch.setChecked(false);
        }*/


        //Check status of switches upon activity start up
        initialFirstCheck = firstSwitch.isChecked();
        initialSecondCheck = secondSwitch.isChecked();

        setContent();
    }

    private void setContent() {
        // Set the profile picture
        try {
            ParseFile profilePictureFile = (ParseFile) currentUser.get("profile_pic");

            profilePictureFile.getDataInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] data, ParseException e) {

                    if (e == null)
                    {
                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                        RoundImage roundImage = new RoundImage(bmp);
                        profilePicture.setImageBitmap(bmp);
                    }
                    else
                    {
                        // Set a default profile picture
                    }

                }
            });
        } catch (Exception e) {
            System.err.println(e);
        }

        TextView location = (TextView) findViewById(R.id.userLocation);
        final TextView school = (TextView) findViewById(R.id.schoolName);
        TextView classYear = (TextView) findViewById(R.id.userClass);
        TextView name = (TextView) findViewById(R.id.username);

        final ParseObject universityPointer = (ParseObject) currentUser.get("university");

        universityPointer.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                school.setText(universityPointer.getString("name"));
            }
        });

        location.setText(userCity);
        classYear.setText("Class of " + currentUser.getNumber("classOf").toString()) ;

        // Set the name
        name.setText(currentUser.getString("first_name") + " " + currentUser.getString("last_name"));
    }

    public void openHype(View view){
        Intent intent = new Intent(this, Hype.class);
        startActivity(intent);
    }

    public void openDate(View view){
        Intent intent = new Intent(this, Date.class);
        startActivity(intent);
    }

    public void openAdd(View view){
        Intent intent = new Intent(this, Add.class);
        startActivity(intent);
    }

    public void openSettings(View view){
        Intent intent = new Intent(this, Settings.class);
        startActivity(intent);
    }

    public void openPrivacy(View view){
        Intent intent = new Intent(this, Privacy.class);
        startActivity(intent);
    }

    public void openTermsOfService(View view){
        Intent intent = new Intent(this, TermsOfService.class);
        startActivity(intent);
    }

    public void openContactUs(View view){
        Intent intent = new Intent(this, ContactUs.class);
        startActivity(intent);
    }

    public void logOut (View view) {
        ParseUser.logOut();
        Intent intent = new Intent(this, LoginA.class);
        setResult(2);
        finish();
        startActivity(intent);
    }

    public String getCity(Location userLocation) {
        double lat = userLocation.getLatitude();
        double lng = userLocation.getLongitude();

        try {
            userCity = currentUser.getString("currentCity");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (userCity == null) {
            Geocoder geoCoder = new Geocoder(Settings.this, Locale.getDefault());
            try {
                List<Address> address = geoCoder.getFromLocation(lat, lng, 1);
                String city = address.get(0).getLocality() + ", " + address.get(0).getAdminArea();

                currentUser.put("currentCity", city);
                userCity = city;
                System.out.println("I'm doing this");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return userCity;
    }

    @Override
    protected void onStop() {
        setResult(2);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        setResult(2);
        super.onDestroy();
    }

    /*https://parse.com/questions/updating-a-field-without-retrieving-the-object-first
    @Override
    public void onStop(){
        super.onStop();

        //Check status of switches upon activity close
        Switch oneSwitch = (Switch) findViewById(R.id.first_notification);
        final Boolean finalFirstCheck = (Boolean) oneSwitch.isChecked();

        Switch twoSwitch = (Switch) findViewById(R.id.second_notification);
        final Boolean finalSecondCheck = (Boolean) twoSwitch.isChecked();

        if (initialFirstCheck != finalFirstCheck) {
            StringBuilder success = new StringBuilder(getResources().getString(R.string.success));
            Toast.makeText(Settings.this, success.toString(), Toast.LENGTH_LONG).show();
            String currentUser = new String(String.valueOf(ParseUser.getCurrentUser()));

            ParseQuery<ParseObject> querySetting1 = ParseQuery.getQuery("User");
            querySetting1.whereEqualTo("user", currentUser);
            querySetting1.findInBackground(new FindCallback<ParseObject>() {
                public void done(List<ParseObject> setting1, ParseException e) {
                    if (e == null) {
                        for (ParseObject userObject : setting1) {
                            String userID = userObject.getObjectId();
                            ParseObject point = ParseObject.createWithoutData("User", userID);
                            point.put("onStatus", finalFirstCheck);
                        }
                    } else {
                    }
                }
            });
        }
    }*/
}