package com.aolalabs.partywolf;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;


public class Date extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date);
    }

    public void openHype(View view){
        Intent intent = new Intent(this, Hype.class);
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
}