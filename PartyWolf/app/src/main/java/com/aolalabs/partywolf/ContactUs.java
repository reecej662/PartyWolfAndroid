package com.aolalabs.partywolf;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;


public class ContactUs extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);
    }

    public void back(View v){
        finish();
    }

}
