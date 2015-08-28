package com.aolalabs.partywolf;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;

import com.parse.ParseObject;

import java.util.Calendar;


public class Add extends Activity {

    private EditText eventTitle;
    private EditText eventDescription;
    private EditText emoji;
    private EditText host;
    private EditText fee;
    private LinearLayout horizontal_layout;
    private int day,month,year, hour, minute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        eventTitle = (EditText) findViewById(R.id.event_name_title);
        eventDescription = (EditText) findViewById(R.id.event_description);
        emoji = (EditText) findViewById(R.id.one_emoji);
        host = (EditText) findViewById(R.id.presented_by);
        fee = (EditText) findViewById(R.id.fee);
        horizontal_layout = (LinearLayout) findViewById(R.id.horizontal_layout);

        DatePicker datePicker = (DatePicker) findViewById(R.id.eventDate);
        datePicker.setMinDate(System.currentTimeMillis() - 1000);
        datePicker.setMaxDate(System.currentTimeMillis() + 604800000);
    }

    public void setDate (View view){
        Button aButton = (Button) findViewById(R.id.new_date);
        aButton.setVisibility(View.GONE);
        Button bButton = (Button) findViewById(R.id.new_time);
        //bButton.setVisibility(View.VISIBLE);
        DatePicker datePicker = (DatePicker) findViewById(R.id.eventDate);
        datePicker.setVisibility(View.VISIBLE);
        eventTitle.setVisibility(View.GONE);
        eventDescription.setVisibility(View.GONE);
        emoji.setVisibility(View.GONE);
        host.setVisibility(View.GONE);
        fee.setVisibility(View.GONE);
        horizontal_layout.setVisibility(View.GONE);
        TimePicker timePicker = (TimePicker) findViewById(R.id.eventTime);
        timePicker.setVisibility(View.VISIBLE);
        Button cButton = (Button) findViewById(R.id.post);
        cButton.setVisibility(View.VISIBLE);
    }

    public void setTime (View view){
        Button bButton = (Button) findViewById(R.id.new_time);
        bButton.setVisibility(View.GONE);
        DatePicker datePicker = (DatePicker) findViewById(R.id.eventDate);
        datePicker.setVisibility(View.GONE);
        TimePicker timePicker = (TimePicker) findViewById(R.id.eventTime);
        timePicker.setVisibility(View.VISIBLE);
        Button cButton = (Button) findViewById(R.id.post);
        cButton.setVisibility(View.VISIBLE);
    }

    public void hideCalendar (View view){
        DatePicker datePicker = (DatePicker) findViewById(R.id.eventDate);
        datePicker.setVisibility(View.GONE);
    }

    public void newPost (View view) {
        //Grab date and time and prepare to send to parse as one string
        DatePicker datePicker = (DatePicker) findViewById(R.id.eventDate);
        TimePicker selectedTime = (TimePicker) findViewById(R.id.eventTime);
        selectedTime.clearFocus();
        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        hour = selectedTime.getCurrentHour();
        minute = selectedTime.getCurrentMinute();
        boolean minuteWrong = false;

        //minuteWrong is a little fix to an issue with minute values that are less than 10; as they
        //are recorded as "5", for example, instead of as "05"; the rest of the fix is just below
        if (minute < 10){
            minuteWrong = true;
        }


        String newYear = String.valueOf(year);
        String newMonth = String.valueOf(month);
        String newDay = String.valueOf(day);
        String newHour = String.valueOf(hour);
        String newMinute = String.valueOf(minute);

        StringBuilder dateAndTime = new StringBuilder(getResources().getString(R.string.empty));

        //Inefficient form of changing numeric months to letters
        if (newMonth.equals(getResources().getString(R.string.one))) {
            String lastMonth = (String) getResources().getString(R.string.jan);
            dateAndTime.append(lastMonth);
        }
        if (newMonth.equals(getResources().getString(R.string.two))) {
            String lastMonth = (String) getResources().getString(R.string.feb);
            dateAndTime.append(lastMonth);
        }
        if (newMonth.equals(getResources().getString(R.string.three))) {
            String lastMonth = (String) getResources().getString(R.string.mar);
            dateAndTime.append(lastMonth);
        }
        if (newMonth.equals(getResources().getString(R.string.four))) {
            String lastMonth = (String) getResources().getString(R.string.apr);
            dateAndTime.append(lastMonth);
        }
        if (newMonth.equals(getResources().getString(R.string.five))) {
            String lastMonth = (String) getResources().getString(R.string.may);
            dateAndTime.append(lastMonth);
        }
        if (newMonth.equals(getResources().getString(R.string.six))) {
            String lastMonth = (String) getResources().getString(R.string.jun);
            dateAndTime.append(lastMonth);
        }
        if (newMonth.equals(getResources().getString(R.string.seven))) {
            String lastMonth = (String) getResources().getString(R.string.jul);
            dateAndTime.append(lastMonth);
        }
        if (newMonth.equals(getResources().getString(R.string.eight))) {
            String lastMonth = (String) getResources().getString(R.string.aug);
            dateAndTime.append(lastMonth);
        }
        if (newMonth.equals(getResources().getString(R.string.nine))) {
            String lastMonth = (String) getResources().getString(R.string.sep);
            dateAndTime.append(lastMonth);
        }
        if (newMonth.equals(getResources().getString(R.string.ten))) {
            String lastMonth = (String) getResources().getString(R.string.oct);
            dateAndTime.append(lastMonth);
        }
        if (newMonth.equals(getResources().getString(R.string.eleven))) {
            String lastMonth = (String) getResources().getString(R.string.nov);
            dateAndTime.append(lastMonth);
        }
        if (newMonth.equals(getResources().getString(R.string.twelve))) {
            String lastMonth = (String) getResources().getString(R.string.dec);
            dateAndTime.append(lastMonth);
        }

        dateAndTime.append(getResources().getString(R.string.space));
        dateAndTime.append(newDay);
        dateAndTime.append(getResources().getString(R.string.comma));
        dateAndTime.append(newYear);
        dateAndTime.append(getResources().getString(R.string.comma));
        dateAndTime.append(newHour);
        dateAndTime.append(getResources().getString(R.string.colon));

        if (minuteWrong) {
            dateAndTime.append(getResources().getString(R.string.zero));
        }
        dateAndTime.append(newMinute);


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
        event.put("date", dateAndTime.toString());
        event.put("approved", false);
        event.put("fee", fee.getText().toString());
        event.put("upvotes", 0);
        event.put("onCampus", onCampus);
        event.saveInBackground();
        dlg.dismiss();

        StringBuilder thanksForNewEventMessage = new StringBuilder(getResources().getString(R.string.thanks_for_new_event));
        Toast.makeText(Add.this, thanksForNewEventMessage.toString(), Toast.LENGTH_LONG).show();
        finish();

//        Event newEvent = new Event(event);
//        System.out.println("New event: " + newEvent);
    }

    public void openPrevious(View view){
        finish();
    }
}