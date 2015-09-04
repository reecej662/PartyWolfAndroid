package com.aolalabs.partywolf;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class LoginA extends Activity implements OnClickListener {
    ParseUser currentUser = ParseUser.getCurrentUser();

    List<String> permissions = Arrays.asList("public_profile", "user_friends", "user_location", "email");
    CallbackManager callbackManager;

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        return super.onCreateView(parent, name, context, attrs);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

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
                LoginManager.getInstance().logOut();
                break;
            case R.id.fbLoginButton:
                logInFacebook();
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

        ParseFacebookUtils.logInWithReadPermissionsInBackground(this, permissions, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                System.out.println(user);
                if (user == null) {
                    Log.d("MyApp", "Uh oh. The user cancelled the Facebook login.");
                } else if (user.isNew()) {
                    Log.d("MyApp", "User signed up and logged in through Facebook!");
                    try {
                        newUserSignUp();
                    } catch (Exception ex) {
                        System.err.println(ex);
                    } finally {
                        System.out.println("Now login A user " + ParseUser.getCurrentUser());
                        Intent i = new Intent(LoginA.this, AddEmailA.class);
                        startActivity(i);
                    }
                } else {
                    Log.d("MyApp", "User logged in through Facebook!");
                    try {
                        userLoggedIn(AccessToken.getCurrentAccessToken());
                    } catch (Exception ex) {
                        System.err.println(ex);
                    } finally {
                        Boolean emailConfirmed = ParseUser.getCurrentUser().getBoolean("emailVerified");
                        if(emailConfirmed) {
                            Intent i = new Intent(LoginA.this, PostTableA.class);
                            startActivity(i);
                        } else {
                            showConfirmEmailDialog(LoginA.this).show();
                        }
                    }
                }
            }
        });

    }

    private void newUserSignUp() {

        GraphRequest request = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject user, GraphResponse response) {

                        Log.d("JSONObject user", user.toString());
                        try {
                            Log.d("fbId", user.getString("id").toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        System.out.println(currentUser);

                        String fbID = "";

                        if (currentUser == null) {
                            currentUser = ParseUser.getCurrentUser();
                        }

                        if(currentUser == null) {
                            System.out.println("current user is still null");
                            currentUser = new ParseUser();
                        }

                        try {
                            System.out.println("User recieved from JSON" + user);

                            fbID = user.getString("id");
                            Log.d("fbID", fbID);
                            currentUser.put("fbLinked", true);
                            currentUser.put("fbID", fbID);

                            String firstName = user.getString("first_name");
                            Log.d("firstName", firstName);
                            currentUser.put("first_name", firstName);

                            String lastName = user.getString("last_name");
                            currentUser.put("last_name", lastName);

                            String gender = user.getString("gender");
                            currentUser.put("gender", gender);

                            JSONObject location = (JSONObject) user.get("location");
                            String hometown = location.getString("name");
                            currentUser.put("hometown", hometown);

                            currentUser.put("banned", "false");
                            currentUser.put("confirmed", false);
                            currentUser.put("score", 0);
                            currentUser.put("leader", false);

                            currentUser.put("onStatus", true);
                            currentUser.put("onNew", true);
                            currentUser.put("onHype", -1);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        // Get profile picture

                        String facebookProfileUrlLarge = "http://graph.facebook.com/"+fbID+"/picture?type=large";
                        String facebookProfileUrlThumb = "http://graph.facebook.com/"+fbID+"/picture?type=small";

                        System.out.println(facebookProfileUrlLarge);

                        String[] pics = {facebookProfileUrlLarge};
                        String pic = pics[0];
                        URL url = null;

                        try {
                            url = new URL(pic);
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }

                        new FetchProfilePic().doInBackground(url);

                        ParseUser.getCurrentUser().saveInBackground();
                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,first_name,last_name,gender,link,location,email");
        request.setParameters(parameters);
        request.executeAsync();
        System.out.println("We're getting here");

    }

    public void userLoggedIn(AccessToken token) {

        currentUser = ParseUser.getCurrentUser();

        GraphRequest request = GraphRequest.newMeRequest(
                token,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject user, GraphResponse response) {

                        System.out.println(user);
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
                            currentUser.put("classOf", 2018);

                        } catch (Exception e) {
                            System.err.println(e);
                        }

                        // Get profile picture

                        ParseUser.getCurrentUser().saveInBackground();

                        String facebookProfileUrlLarge = "http://graph.facebook.com/"+fbID+"/picture?type=large";
                        String facebookProfileUrlThumb = "http://graph.facebook.com/"+fbID+"/picture?type=small";

                        System.out.println(facebookProfileUrlLarge);

                        String[] pics = {facebookProfileUrlLarge};
                        String pic = pics[0];
                        URL url = null;

                        try {
                            url = new URL(pic);
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }

                        new FetchProfilePic().doInBackground(url);

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

    private class FetchProfilePic extends AsyncTask<URL, Void, Void> {

        @Override
        protected Void doInBackground(URL... urls) {

            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet(urls[0].toString());
            HttpResponse response;
            Bitmap img;

            try {
                response = (HttpResponse)client.execute(request);
                HttpEntity entity = response.getEntity();
                BufferedHttpEntity bufferedEntity = new BufferedHttpEntity(entity);
                InputStream inputStream = bufferedEntity.getContent();
                img = BitmapFactory.decodeStream(inputStream);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                stream.reset();

                img.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] image = stream.toByteArray();
                System.out.println(image);

                // Create the ParseFile
                ParseFile file = new ParseFile("file", image);
                // Upload the image into Parse Cloud
                file.saveInBackground();

                currentUser.put("profile_pic", file);

            } catch (Exception e) {
                e.printStackTrace();
            }


            return null;
        }

    }

    public Dialog showConfirmEmailDialog(Context context) {
        System.out.println("We're getting here");

        final Dialog loadingDialog = new Dialog(context);
        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loadingDialog.setContentView(R.layout.confirm_email_dialog);

        final Window window = loadingDialog.getWindow();
        window.setLayout(600, 700);
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        Button okButton = (Button) loadingDialog.findViewById(R.id.okButton);
        Button reenterButton = (Button) loadingDialog.findViewById(R.id.reenterButton);

        okButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingDialog.dismiss();
            }
        });

        reenterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingDialog.dismiss();
                Intent i = new Intent (LoginA.this, AddEmailA.class);
                startActivity(i);
            }
        });

        return loadingDialog;
    }


}
