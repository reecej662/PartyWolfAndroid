package com.aolalabs.partywolf;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;


public class TermsOfService extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_of_service);
    }

    public void openSettings(View view){
        Intent intent = new Intent(this, Settings.class);
        startActivity(intent);
    }
}
