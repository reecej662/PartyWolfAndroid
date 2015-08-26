package com.aolalabs.partywolf;

import android.app.Activity;
import android.os.Bundle;

import com.victor.loading.rotate.RotateLoading;

/**
 * Created by reecejackson on 8/24/15.
 */
public class LoadingA extends Activity{
    private RotateLoading rotateLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.loading_dialog);

        rotateLoading = (RotateLoading) findViewById(R.id.rotateloading);
        rotateLoading.start();

    }
}
