package com.aolalabs.partywolf;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class LoginA extends Activity implements OnClickListener {

    List<String> permissions = Arrays.asList("public_profile", "user_friends", "user_location", "email");
    CallbackManager callbackManager;

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        return super.onCreateView(parent, name, context, attrs);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this);
        setContentView(R.layout.activity_login);
        //Parse.enableLocalDatastore(this);
        //Parse.initialize(this, "8IiZUWr2nVlthlX1VVrq3gDHXMfuhefW3EIbBdzE", "TNc5sg3go9lqQBcdizIZvgwa3wmXrDPo0D7txBTT");
        ParseFacebookUtils.initialize(this);
        ParseUser.enableRevocableSessionInBackground();

        Button learnMoreButton = (Button) findViewById(R.id.learnMoreButton);
        ImageButton fbLoginButton = (ImageButton) findViewById(R.id.fbLoginButton);

        callbackManager = CallbackManager.Factory.create();

        learnMoreButton.setOnClickListener(this);
        fbLoginButton.setOnClickListener(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.learnMoreButton:
                System.out.println("They wanna learn more!11!!1");
                LoginManager.getInstance().logOut();
                //System.out.println(AccessToken.getCurrentAccessToken().getPermissions());
                break;
            case R.id.fbLoginButton:
                System.out.println("They wanna log in...");

                logInFacebook();
                //System.out.println(LoginManager.getInstance().a);
                break;

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void logInFacebook() {
        //login

        //final AccessToken myToken = token;

        ParseFacebookUtils.logInWithReadPermissionsInBackground(this, permissions, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (user == null) {
                    Log.d("MyApp", "Uh oh. The user cancelled the Facebook login.");
                } else if (user.isNew()) {
                    Log.d("MyApp", "User signed up and logged in through Facebook!");
                    try {
                        newUserSignUp();
                    } catch (Exception ex) {
                        System.err.println(ex);
                    } finally {
                        Intent i = new Intent(LoginA.this, PostTableA.class);
                        startActivity(i);
                    }
                } else {
                    Log.d("MyApp", "User logged in through Facebook!");
                    try {
                        userLoggedIn(AccessToken.getCurrentAccessToken());
                    } catch (Exception ex) {
                        System.err.println(ex);
                    } finally {
                        Intent i = new Intent(LoginA.this, PostTableA.class);
                        startActivity(i);
                    }
                }
            }
        });

        ParseUser currentUser = ParseUser.getCurrentUser();
        String fbID = "";

        try {
            wait(1000);
        } catch (Exception e) {
            System.err.println(e);
        }

        try {
            fbID = currentUser.getString("fbID");
            if(!fbID.equals("")) {
                System.out.println(fbID);
                System.out.println(AccessToken.getCurrentAccessToken().getPermissions().toString());
                userLoggedIn(AccessToken.getCurrentAccessToken());
            } else {
                newUserSignUp();
            }
        } catch (Exception e) {
            System.err.println(e);
            newUserSignUp();
        }

    }

    private void newUserSignUp() {

        GraphRequest request = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject user, GraphResponse response) {

                        System.out.println(user);
                        ParseUser currentUser = ParseUser.getCurrentUser();
                        //System.out.println("Parse user: " + currentUser.getString("first_name"));

                        String fbID = "";

                        try {
                            System.out.println(user);

                            fbID = user.getString("fbID");
                            currentUser.put("fbLinked", true);
                            currentUser.put("fbID", fbID);

                            String firstName = user.getString("first_name");
                            currentUser.put("first_name", firstName);

                            String lastName = user.getString("last_name");
                            currentUser.put("last_name", lastName);

                            String gender = user.getString("gender");
                            currentUser.put("gender", gender);

                            String location = user.getString("location");
                            currentUser.put("hometown", location);

                            currentUser.put("banned", false);
                            currentUser.put("confirmed", false);
                            currentUser.put("score", 0);
                            currentUser.put("leader", false);

                            currentUser.put("onStatus", true);
                            currentUser.put("onNew", true);
                            currentUser.put("onHype", -1);

                        } catch (Exception e) {
                            System.err.println(e);
                        }

                        // Get profile picture

                        String facebookProfileUrlLarge = "http://graph.facebook.com/"+fbID+"/picture?type=large";
                        String facebookProfileUrlThumb = "http://graph.facebook.com/"+fbID+"/picture?type=small";

                        String[] pics = {facebookProfileUrlLarge};

                        for(String pic : pics) {

                            try {

                                URL url = new URL(pic);
                                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                                InputStream is = connection.getInputStream();
                                Bitmap img = BitmapFactory.decodeStream(is);

                                if(pic.equals(pics[0])) {
                                    currentUser.put("profile_pic", img);
                                } else {
                                    currentUser.put("profile_thumb", img);
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }

                        try {
                            currentUser.saveInBackground();
                        } catch (Exception e) {
                            System.err.println(e);
                        }

                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,first_name,last_name,gender,link,location,email");
        request.setParameters(parameters);
        request.executeAsync();

    }

    public void userLoggedIn(AccessToken token) {

        ParseUser currentUser = ParseUser.getCurrentUser();

        GraphRequest request = GraphRequest.newMeRequest(
                token,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject user, GraphResponse response) {

                        System.out.println(user);
                        ParseUser currentUser = ParseUser.getCurrentUser();
                        currentUser.fetchInBackground();

                        System.out.println(currentUser);

                        String fbID = "";

                        try {
                            fbID = user.getString("id");
                            currentUser.put("fbLinked", true);
                            currentUser.put("fbID", fbID);

                            try {
                                String firstName = user.getString("first_name");
                                currentUser.put("first_name", firstName);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            try {
                                String lastName = user.getString("last_name");
                                currentUser.put("last_name", lastName);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            try {
                                String gender = user.getString("gender");
                                currentUser.put("gender", gender);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            try {
                                String location = user.getString("location");
                                currentUser.put("hometown", location);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            currentUser.put("banned", false);
                            currentUser.put("confirmed", false);
                            currentUser.put("score", 0);
                            currentUser.put("leader", false);

                            currentUser.put("onStatus", true);
                            currentUser.put("onNew", true);
                            currentUser.put("onHype", -1);

                        } catch (Exception e) {
                            System.err.println(e);
                        }

                        // Get profile picture

                        String facebookProfileUrlLarge = "http://graph.facebook.com/"+fbID+"/picture?type=large";
                        String facebookProfileUrlThumb = "http://graph.facebook.com/"+fbID+"/picture?type=small";

                        System.out.println(facebookProfileUrlLarge);

                        String[] pics = {facebookProfileUrlLarge};

                        for(String pic : pics) {

                            try {

                                URL url = new URL(pic);
                                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                                InputStream is = connection.getInputStream();
                                Bitmap img = BitmapFactory.decodeStream(is);

                                if(pic.equals(pics[0])) {
                                    currentUser.put("profile_pic", img);
                                } else {
                                    currentUser.put("profile_thumb", img);
                                }

                            } catch (Exception e) {
                                System.err.println(e);
                            }

                        }

                        ParseUser.getCurrentUser().saveInBackground();

                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,first_name,last_name,gender,link,location,email");
        request.setParameters(parameters);
        request.executeAsync();

        boolean isVerified = false;
        String isBanned = "false";
        String wolfPack = "";

        try {
            isVerified = currentUser.getBoolean("emailVerified");
        } catch (Exception e) {
            isVerified = false;
        }

        try {
            isBanned = currentUser.getString("banned");
        } catch (Exception e) {
            System.err.println(e);
        }

        try{
            wolfPack = currentUser.getString("wolfPack");
        } catch (Exception e) {
            System.err.println(e);
        }

        boolean isLeader = false;
        try {
            isLeader = currentUser.getBoolean("leader");
        } catch (Exception e) {
            currentUser.put("leader", isLeader);
            currentUser.saveInBackground();
        }

        boolean onNew = false;
        try {
            onNew = currentUser.getBoolean("onNew");
            System.out.println("onNew");
        } catch (Exception e) {
            currentUser.put("onNew", onNew);
            currentUser.saveInBackground();

        }

        boolean onStatus = false;
        try {
            onStatus = currentUser.getBoolean("onStatus");
        } catch (Exception e) {
            currentUser.put("onStatus", false);
            currentUser.saveInBackground();
        }

        System.out.println("We're getting here!");

        if(isVerified && isBanned.equals(false) && !wolfPack.equals("None")) {

            try {
                ParseObject university = currentUser.getParseObject("university");
                ParseObject realUniversity = ParseObject.createWithoutData("Universities", university.getObjectId());
                ParseQuery uniQuery = ParseQuery.getQuery("Universities");
                uniQuery.whereEqualTo("objectId", university.getObjectId());

                uniQuery.findInBackground(new FindCallback<ParseObject>() {
                    ParseUser currentUser = ParseUser.getCurrentUser();

                    @Override
                    public void done(List<ParseObject> objects, ParseException e) {
                        for(ParseObject object : objects) {
                            String name = object.getString("name");
                            if(!name.equals(currentUser.getString("pack"))) {
                                currentUser.put("confirmed", true);
                                currentUser.put("pack", object);
                                currentUser.saveInBackground();
                                System.out.println("We're getting here!");
                            }
                        }
                    }
                });

            } catch(Exception e){
                currentUser.put("pack", "None");
                currentUser.signUpInBackground();
            }

        } else if (isBanned.equals("true")) {
            //kick to banned
        } else if (wolfPack.equals("None")) {
            //schoolLockedAlert
        } else {
            // confirmEmailAlert();
        }
    }

}
