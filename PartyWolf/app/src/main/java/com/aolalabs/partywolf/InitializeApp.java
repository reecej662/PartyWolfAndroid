package com.aolalabs.partywolf;

import android.app.Application;
import android.util.Log;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParsePush;
import com.parse.SaveCallback;


/**
 * Created by DanielMiller on 6/30/15.
 */
public class InitializeApp extends Application {

    public void onCreate(){
        super.onCreate();

        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "pFNop5SHw3AT5myWYsoHI09BrfWyfwfZn5g943Xq", "B6v9CxV1qp7zErCcZZTcYWAPNgfEsJxoAzULg2Jz");


        ParsePush.subscribeInBackground("", new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.d("com.parse.push", "successfully subscribed to the broadcast channel.");
                } else {
                    Log.e("com.parse.push", "failed to subscribe for push", e);
                }
            }
        });
    }
}
