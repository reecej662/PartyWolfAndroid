package com.aolalabs.partywolf;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;


public class ContactUs extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);
    }

    public void openSettings(View view){
        Intent intent = new Intent(this, Settings.class);
        startActivity(intent);
    }
}
