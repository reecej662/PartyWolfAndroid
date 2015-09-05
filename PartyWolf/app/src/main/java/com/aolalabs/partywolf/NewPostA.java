package com.aolalabs.partywolf;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

/**
 * Created by reecejackson on 8/18/15.
 */
public class NewPostA extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_post);
    }

    public void back(View v) {
        finish();
    }

}
