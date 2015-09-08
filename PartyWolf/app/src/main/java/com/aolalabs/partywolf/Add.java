package com.aolalabs.partywolf;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
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

    private CustomEditText eventTitle;
    private CustomEditText eventDescription;
    private CustomEditText emoji;
    private CustomEditText host;
    private CustomEditText fee;
//    private int day,month,year, hour, minute;
    private Date eventDate;
    private ParseGeoPoint location;
    private TextView dateText;
    private int bottomHeight = 0;
    private int eventDescriptionHeight = 0;
    private boolean keyboardActive = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        Location userLocation = (Location) getIntent().getExtras().get("location");

        if(userLocation != null)
            location = new ParseGeoPoint(userLocation.getLatitude(), userLocation.getLongitude());

        eventTitle = (CustomEditText) findViewById(R.id.event_name_title);
        eventDescription = (CustomEditText) findViewById(R.id.event_description);
        emoji = (CustomEditText) findViewById(R.id.one_emoji);
        host = (CustomEditText) findViewById(R.id.presented_by);
        fee = (CustomEditText) findViewById(R.id.fee);
        dateText = (TextView) findViewById(R.id.date_text);
        dateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDate(v);
            }
        });

        setLayoutParameters();
        setListeners();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public void setDate (View view){
        final SimpleDateFormat mFormatter = new SimpleDateFormat("EEEE MMMM dd, h:mm aa");
        keyboardClosed();
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
        String fee = this.fee.getText().toString();
        if(fee.charAt(0) == '$') {
            event.put("fee", fee);
        } else {
            String feeAmount = "$" + fee;
            event.put("fee", feeAmount);
        }
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
    }

    public void setListeners() {

        //bottomHeight = 500;

        View.OnFocusChangeListener listener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus && eventDescription.getLayoutParams().height == eventDescriptionHeight) {
                    keyboardOpened();
                } else if(!hasFocus){
                    keyboardClosed();
                }
            }
        };

        CustomEditText.KeyboardCloseListener keyboardCloseListener = new CustomEditText.KeyboardCloseListener() {
            @Override
            public void onKeyboardClosed() {
                keyboardClosed();
            }
        };

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!keyboardActive) {
                    keyboardOpened();
                }
            }
        };

        eventTitle.setOnFocusChangeListener(listener);
        eventDescription.setOnFocusChangeListener(listener);
        emoji.setOnFocusChangeListener(listener);
        host.setOnFocusChangeListener(listener);
        fee.setOnFocusChangeListener(listener);

        eventTitle.setBackListener(keyboardCloseListener);
        eventDescription.setBackListener(keyboardCloseListener);
        emoji.setBackListener(keyboardCloseListener);
        host.setBackListener(keyboardCloseListener);
        fee.setBackListener(keyboardCloseListener);

        eventTitle.setOnClickListener(onClickListener);
        eventDescription.setOnClickListener(onClickListener);
        emoji.setOnClickListener(onClickListener);
        host.setOnClickListener(onClickListener);
        fee.setOnClickListener(onClickListener);

    }

    public void keyboardOpened() {
        System.out.println("I'm supposed to be doing something...");
        ViewGroup.LayoutParams eventDescriptionParams = eventDescription.getLayoutParams();
        eventDescriptionParams.height -= bottomHeight;
        System.out.println(bottomHeight);
        eventDescription.setLayoutParams(eventDescriptionParams);
        keyboardActive = true;
    }

    public void keyboardClosed() {
        ViewGroup.LayoutParams eventDescriptionParams = eventDescription.getLayoutParams();
        eventDescriptionParams.height = eventDescriptionHeight;
        System.out.println(bottomHeight);
        eventDescription.setLayoutParams(eventDescriptionParams);
        keyboardActive = false;
    }

    public void setLayoutParameters() {
        int windowHeight = getWindowManager().getDefaultDisplay().getHeight();
        int topBarHeight = findViewById(R.id.topBar).getLayoutParams().height;
        int eventNameTitleHeight = findViewById(R.id.event_name_title).getLayoutParams().height;
        int event_descriptionHeight = findViewById(R.id.event_description).getLayoutParams().height;

        eventDescriptionHeight = eventDescription.getLayoutParams().height;
        //bottomHeight = 176 + windowHeight - (topBarHeight+eventNameTitleHeight + event_descriptionHeight);
        bottomHeight = 325;

        Log.d("Window height: ", "" + windowHeight);
        Log.d("Bottom height: ", "" + bottomHeight);

    }

    public void openPrevious(View view){
        finish();
    }

}