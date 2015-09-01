package com.aolalabs.partywolf;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.github.jjobes.slidedatetimepicker.SlideDateTimeListener;
import com.github.jjobes.slidedatetimepicker.SlideDateTimePicker;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.text.SimpleDateFormat;
import java.util.Date;


public class Add extends FragmentActivity {

    private EditText eventTitle;
    private EditText eventDescription;
    private EditText emoji;
    private EditText host;
    private EditText fee;
    private LinearLayout horizontal_layout;
//    private int day,month,year, hour, minute;
    private Date eventDate;
    private ParseGeoPoint location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        Location userLocation = (Location) getIntent().getExtras().get("location");
        System.out.println(userLocation);

        if(userLocation != null)
            location = new ParseGeoPoint(userLocation.getLatitude(), userLocation.getLongitude());

        eventTitle = (EditText) findViewById(R.id.event_name_title);
        eventDescription = (EditText) findViewById(R.id.event_description);
        emoji = (EditText) findViewById(R.id.one_emoji);
        host = (EditText) findViewById(R.id.presented_by);
        fee = (EditText) findViewById(R.id.fee);
        horizontal_layout = (LinearLayout) findViewById(R.id.horizontal_layout);
    }


    public void setDate (View view){
        final SimpleDateFormat mFormatter = new SimpleDateFormat("EEEE MMMM dd, h:mm aa");
        SlideDateTimeListener listener = new SlideDateTimeListener() {

            @Override
            public void onDateTimeSet(Date date)
            {
                Toast.makeText(Add.this, mFormatter.format(date), Toast.LENGTH_SHORT).show();
                TextView dateText = (TextView) findViewById(R.id.date_text);
                dateText.setVisibility(View.VISIBLE);
                dateText.setText(mFormatter.format(date));
                eventDate = date;
            }

            // Optional cancel listener
            @Override
            public void onDateTimeCancel()
            {
                Toast.makeText(Add.this, "Canceled", Toast.LENGTH_SHORT).show();
            }
        };

        new SlideDateTimePicker.Builder(getSupportFragmentManager())
                .setListener(listener)
                .setInitialDate(new Date())
                .setMaxDate(new Date(System.currentTimeMillis() + 604800000))
                .setMinDate(new Date())
                .setIndicatorColor(Color.rgb(0, 169, 255))
                .build()
                .show();
    }


    public void newPost (View view) {
//        String newYear = String.valueOf(year);
//        String newMonth = String.valueOf(month);
//        String newDay = String.valueOf(day);
//        String newHour = String.valueOf(hour);
//        String newMinute = String.valueOf(minute);
//
//        StringBuilder dateAndTime = new StringBuilder(getResources().getString(R.string.empty));
//
//        //Inefficient form of changing numeric months to letters
//        if (newMonth.equals(getResources().getString(R.string.one))) {
//            String lastMonth = (String) getResources().getString(R.string.jan);
//            dateAndTime.append(lastMonth);
//        }
//        if (newMonth.equals(getResources().getString(R.string.two))) {
//            String lastMonth = (String) getResources().getString(R.string.feb);
//            dateAndTime.append(lastMonth);
//        }
//        if (newMonth.equals(getResources().getString(R.string.three))) {
//            String lastMonth = (String) getResources().getString(R.string.mar);
//            dateAndTime.append(lastMonth);
//        }
//        if (newMonth.equals(getResources().getString(R.string.four))) {
//            String lastMonth = (String) getResources().getString(R.string.apr);
//            dateAndTime.append(lastMonth);
//        }
//        if (newMonth.equals(getResources().getString(R.string.five))) {
//            String lastMonth = (String) getResources().getString(R.string.may);
//            dateAndTime.append(lastMonth);
//        }
//        if (newMonth.equals(getResources().getString(R.string.six))) {
//            String lastMonth = (String) getResources().getString(R.string.jun);
//            dateAndTime.append(lastMonth);
//        }
//        if (newMonth.equals(getResources().getString(R.string.seven))) {
//            String lastMonth = (String) getResources().getString(R.string.jul);
//            dateAndTime.append(lastMonth);
//        }
//        if (newMonth.equals(getResources().getString(R.string.eight))) {
//            String lastMonth = (String) getResources().getString(R.string.aug);
//            dateAndTime.append(lastMonth);
//        }
//        if (newMonth.equals(getResources().getString(R.string.nine))) {
//            String lastMonth = (String) getResources().getString(R.string.sep);
//            dateAndTime.append(lastMonth);
//        }
//        if (newMonth.equals(getResources().getString(R.string.ten))) {
//            String lastMonth = (String) getResources().getString(R.string.oct);
//            dateAndTime.append(lastMonth);
//        }
//        if (newMonth.equals(getResources().getString(R.string.eleven))) {
//            String lastMonth = (String) getResources().getString(R.string.nov);
//            dateAndTime.append(lastMonth);
//        }
//        if (newMonth.equals(getResources().getString(R.string.twelve))) {
//            String lastMonth = (String) getResources().getString(R.string.dec);
//            dateAndTime.append(lastMonth);
//        }
//
//        dateAndTime.append(getResources().getString(R.string.space));
//        dateAndTime.append(newDay);
//        dateAndTime.append(getResources().getString(R.string.comma));
//        dateAndTime.append(newYear);
//        dateAndTime.append(getResources().getString(R.string.comma));
//        dateAndTime.append(newHour);
//        dateAndTime.append(getResources().getString(R.string.colon));
//
//        if (minuteWrong) {
//            dateAndTime.append(getResources().getString(R.string.zero));
//        }
//        dateAndTime.append(newMinute);
        final ProgressDialog dlg = new ProgressDialog(Add.this);
        dlg.setTitle("Please wait.");
        dlg.setMessage("Please wait.");
        dlg.show();


        boolean validationError = false;
        StringBuilder validationErrorMessage = new StringBuilder(getResources().getString(R.string.please));


        if (eventTitle.getText().toString().trim().length() < 3) {
            validationError = true;
            validationErrorMessage.append(getResources().getString(R.string.enter_a_title));
        }

        if (eventDescription.getText().toString().trim().length() < 3) {
            if (validationError) {
                validationErrorMessage.append(getResources().getString(R.string.and));
            }
            validationError = true;
            validationErrorMessage.append(getResources().getString(R.string.enter_a_description));
        }

        if (emoji.getText().toString().trim().length() < 1) {
            if (validationError) {
                validationErrorMessage.append(getResources().getString(R.string.and));
            }
            validationError = true;
            validationErrorMessage.append(getResources().getString(R.string.enter_an_emoji));
        }

        if (emoji.getText().toString().trim().length() > 2) {
            if (validationError) {
                validationErrorMessage.append(getResources().getString(R.string.and));
            }
            validationError = true;
            validationErrorMessage.append(getResources().getString(R.string.one_emoji_only));
        }

        if (host.getText().toString().trim().length() < 3) {
            if (validationError) {
                validationErrorMessage.append(getResources().getString(R.string.and));
            }
            validationError = true;
            validationErrorMessage.append(getResources().getString(R.string.enter_a_host));
        }

        if (fee.getText().toString().trim().length() < 5 ) {
            if (validationError) {
                validationErrorMessage.append(getResources().getString(R.string.and));
            }
            validationError = true;
            validationErrorMessage.append(getResources().getString(R.string.enter_a_fee));
        }

        validationErrorMessage.append(getResources().getString(R.string.period));

        if (validationError) {
            Toast.makeText(Add.this, validationErrorMessage.toString(), Toast.LENGTH_LONG).show();
            dlg.dismiss();
            return;
        }

        //Determine whether or not there is an emoji entered
        String emojiString = emoji.getText().toString();

        int RangeLow = 55357; // can get range from e.g. http://www.utf8-chartable.de/unicode-utf8-table.pl
        int RangeHigh = 55357;
        for(int iLetter = 0; iLetter < emojiString.length() ; iLetter++) {
            int cv = emojiString.codePointAt(iLetter);
            if (cv >= RangeLow && cv <= RangeHigh) {
                // Not emoji
                StringBuilder errorNoEmojiMessage = new StringBuilder(getResources().getString(R.string.enter_an_emoji2));
                Toast.makeText(Add.this, errorNoEmojiMessage.toString(), Toast.LENGTH_LONG).show();
                dlg.dismiss();
                return;
            }
        }
        // Else is emoji

        Switch onCampusSwitch = (Switch) findViewById(R.id.on_campus);
        Boolean onCampus = onCampusSwitch.isChecked();

        ParseObject event = new ParseObject("Posts");
        event.put("title", eventTitle.getText().toString());
        event.put("description", eventDescription.getText().toString());
        event.put("emoji", emoji.getText().toString());
        event.put("host", host.getText().toString());
        event.put("date", eventDate);
        event.put("approved", false);
        event.put("fee", fee.getText().toString());
        event.put("upvotes", 0);
        event.put("user", ParseUser.getCurrentUser());
        event.put("university", ParseUser.getCurrentUser().get("university"));
        event.put("onCampus", onCampus);
        event.put("postLocation", location);
        event.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e != null) {
                    Log.d("Error saving new event", e.toString());
                } else {
                    Log.d("New event", "Save complete!");
                }
            }
        });
        dlg.dismiss();

        StringBuilder thanksForNewEventMessage = new StringBuilder(getResources().getString(R.string.thanks_for_new_event));
        Toast.makeText(Add.this, thanksForNewEventMessage.toString(), Toast.LENGTH_LONG).show();
        finish();
//        Z
    }

    public void openPrevious(View view){
        finish();
    }
}