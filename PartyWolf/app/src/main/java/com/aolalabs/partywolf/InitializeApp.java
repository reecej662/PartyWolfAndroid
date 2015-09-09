package com.aolalabs.partywolf;

import android.app.Application;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.facebook.FacebookSdk;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import io.fabric.sdk.android.Fabric;


/**
 * Created by DanielMiller on 6/30/15.
 */
public class InitializeApp extends Application {

    public void onCreate(){
        super.onCreate();
        //Crashlytics crashlytics = new Crashlytics.Builder().disabled(BuildConfig.DEBUG).build();
        //Fabric.with(this, crashlytics);
        Fabric.with(this, new Crashlytics());

        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "8IiZUWr2nVlthlX1VVrq3gDHXMfuhefW3EIbBdzE", "TNc5sg3go9lqQBcdizIZvgwa3wmXrDPo0D7txBTT");

        FacebookSdk.sdkInitialize(this);

        ParseFacebookUtils.initialize(this);
        ParseUser.enableRevocableSessionInBackground();

        ParseObject testObject = new ParseObject("TestObject");
        testObject.put("foo", "bar");
        testObject.saveInBackground();

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
