package com.aolalabs.partywolf;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

/**
 * Created by reecejackson on 9/1/15.
 */
public class ClassOfA extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.class_of);

        Spinner spinner = (Spinner) findViewById(R.id.iAmPrompt);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.classof, R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    public void continueButton(View v) {
        Intent i = new Intent(ClassOfA.this, ClassOfA.class);
        startActivity(i);
    }
}
