package com.aolalabs.partywolf;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.victor.loading.rotate.RotateLoading;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

//import com.walnutlabs.android.ProgressHUD;

// Array of options --> ArrayAdapter --> ListView

//List view: {views: da_items.xml}

/**
 * Created by reecejackson on 8/18/15.
 *
 *
 *
 */

public class PostTableA extends Activity implements OnClickListener{
    private ListView eventList;
    private List<Event> events = new ArrayList<>();
    private List<Event> sortedEvents = new ArrayList<>();
    private List<ParseObject> parseEvents = new ArrayList<>();
    private List<Event> upvoteEvents = new ArrayList<>();
    private List<ParseObject> upvoteObjects = new ArrayList<>();
    private ImageButton newPostButton;
    private Button hypeButton;
    private Button dateButton;
    private ImageButton settingsButton;
    private boolean dateView = true;
    private ParseUser currentUser = null;
    private Dialog loadingDialog = null;
    private LocationManager locationManager;
    private Location userLocation = null;

    @Override
    protected void onCreate(Bundle savedInstance) {
        // Enable Local Datastore.
        // Parse.enableLocalDatastore(this);

        // Parse.initialize(this, "8IiZUWr2nVlthlX1VVrq3gDHXMfuhefW3EIbBdzE", "TNc5sg3go9lqQBcdizIZvgwa3wmXrDPo0D7txBTT");

        super.onCreate(savedInstance);
        setContentView(R.layout.posts_table);

        loadingDialog = showLoadingDialog();
        loadingDialog.show();

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        populateEventList();
        populateListView((ArrayList<Event>) events);
        registerClickCallback();

        newPostButton = (ImageButton) findViewById(R.id.newPostButton);
        eventList = (ListView) findViewById(R.id.main_list_view);
        hypeButton = (Button) findViewById(R.id.hypeOption);
        dateButton = (Button) findViewById(R.id.dateOption);
        settingsButton = (ImageButton) findViewById(R.id.settingsButton);

        newPostButton.setOnClickListener(this);
        hypeButton.setOnClickListener(this);
        dateButton.setOnClickListener(this);
        settingsButton.setOnClickListener(this);

        // Go to login screen if no user not currently logged in
        Intent login = new Intent(this, LoginA.class);

        try{
            currentUser = ParseUser.getCurrentUser();
            if(currentUser == null) {
                startActivity(login);
            } else {
                getLocation();
            }
        } catch (Exception e) {
            startActivity(login);
        }

        // Load the posts the user has updated
//        System.out.println("Confirmed: " + currentUser.getBoolean("confirmed"));

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUpvoteData();
        System.out.println("Resuming activity");
    }

    //9722610049

    // Handle the user clicking the upvote/unupvote button
    public void upvoteHandler(View v) {
        RelativeLayout vwParentRow = (RelativeLayout)v.getParent();

        TextView hype = (TextView) vwParentRow.findViewById(R.id.event_hype_number);
        Button hypeButton = (Button) vwParentRow.findViewById(R.id.event_hype_button);

        // If the button is in voting mode
        if(hypeButton.getText().equals("vote")) {

            hypeButton.setBackgroundResource(R.drawable.bluevote);
            hype.setTextColor(Color.rgb(0, 169, 255));

            // Get the new hype number and set it in the view
            final Integer hypeNumber = Integer.valueOf(hype.getText().toString()) + 1;
            hype.setText(hypeNumber.toString());

            // The corresponding object that the row describes
            ParseObject object = (ParseObject) hypeButton.getTag();

            upvoteEvent(object);

            hypeButton.setText("unvote");

        // If the button is in unvote mode
        } else {

            // Set the button and text back to black
            hypeButton.setBackgroundResource(R.drawable.vote);
            hype.setTextColor(Color.rgb(0, 0, 0));

            // Update the number in the view
            final Integer hypeNumber = Integer.valueOf(hype.getText().toString()) - 1;
            hype.setText(hypeNumber.toString());

            // Get the corresponding object from the row
            ParseObject object = (ParseObject) hypeButton.getTag();

            unvoteEvent(object);

            hypeButton.setText("vote");

        }
    }

    private void populateEventList() {

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Posts");

        Date today = new Date();
        GregorianCalendar calendar = new GregorianCalendar();
        Date startOfDay = calendar.getTime();
        startOfDay.setHours(0);
        startOfDay.setMinutes(0);
        startOfDay.setSeconds(0);

        Date oneWeek = new Date(today.getTime()+604800000);

        /*SimpleDateFormat sdf = new SimpleDateFormat("EEEE");;
        final String weekDay =  sdf.format(calendar.getTime());*/

        try {
            //ParseObject university = currentUser.getParseObject("university");
            //ParseObject realUniversity = ParseObject.createWithoutData("Universities", university.getObjectId());
            //query.whereEqualTo("university", realUniversity);
            query.whereEqualTo("approved", true);
            query.whereGreaterThanOrEqualTo("date", startOfDay);
            query.whereLessThanOrEqualTo("date", oneWeek);
            query.include("university");
        } catch (Exception e) {
            query.whereEqualTo("nothing", false);
        }

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> postList, ParseException e) {
                try {
                    if (e == null) {
                        for (ParseObject object : postList) {
                            if((eventInArea(object) && !object.getBoolean("onCampus"))
                                    || (currentUser.get("university").equals(object.get("university")))) {
                                events.add(new Event(object));
                                parseEvents.add(object);
                            } else {
                                System.out.println("Event not in local area");
                            }
                        }
                    } else {
                        e.printStackTrace();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    sortByHype();
                    populateListView((ArrayList<Event>) events);
                }
            }
        });

    }

    private void populateListView(ArrayList<Event> events) {
        // Build Adapter
        ArrayAdapter<Event> adapter = new MyListAdapter(events);

        // Configure the list view
        ListView list = (ListView) findViewById(R.id.main_list_view);
        list.setAdapter(adapter);
    }

    private void registerClickCallback() {
        ListView list = (ListView) findViewById(R.id.main_list_view);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked, int position, long id) {
                Event clickedEvent;
                if (dateView) {
                    clickedEvent = events.get(position);
                } else {
                    clickedEvent = sortedEvents.get(position);
                }
                Intent i = new Intent(PostTableA.this, EventDetailA.class);
                i.putExtra("event", clickedEvent);
                startActivity(i);
            }
        });
    }

    private class MyListAdapter extends ArrayAdapter<Event> {
        private ArrayList<Event> events;

        public MyListAdapter(ArrayList<Event> listEvents) {
            super(PostTableA.this, R.layout.event, listEvents);
            events = listEvents;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            // Make sure we have a view to work with (may have been given null)
            if (dateView) {
                events = (ArrayList<Event>) PostTableA.this.events;
            } else {
                events = (ArrayList<Event>) PostTableA.this.sortedEvents;
            }

            View itemView = convertView;
            if (itemView == null) {
                itemView = getLayoutInflater().inflate(R.layout.event, parent, false);
            }

            Event currentEvent = events.get(position);

            // fill the view
            RelativeLayout daySection = (RelativeLayout) itemView.findViewById(R.id.event_section);
            TextView weekday = (TextView) itemView.findViewById(R.id.event_weekday);
            TextView date = (TextView) itemView.findViewById(R.id.event_date);
            TextView title = (TextView) itemView.findViewById(R.id.event_title);
            TextView description = (TextView) itemView.findViewById(R.id.event_description);
            TextView timeAndPlace = (TextView) itemView.findViewById(R.id.event_time_and_place);
            TextView emoji = (TextView) itemView.findViewById(R.id.event_emoji);
            final TextView upvotes = (TextView) itemView.findViewById(R.id.event_hype_number);
            final Button button = (Button) itemView.findViewById(R.id.event_hype_button);

            button.setTag(parseObjectForEvent(currentEvent));

            int height = daySection.getLayoutParams().height;
            Event previousEvent = new Event();

            if(position != 0) {
                previousEvent = events.get(position - 1);
            }

            if (currentEvent.getDateOfEvent().equals(previousEvent.getDateOfEvent())) {
                daySection.setVisibility(View.GONE);
            } else {
                daySection.setVisibility(View.VISIBLE);
                weekday.setText(currentEvent.getWeekDay());
                date.setText(currentEvent.getDateOfEvent());
            }

            title.setText(currentEvent.getTitle());
            description.setText(currentEvent.getDesc());
            emoji.setText(currentEvent.getEmoji());
            upvotes.setText(String.valueOf(currentEvent.getNumUpvotes()));

            SimpleDateFormat sdf = new SimpleDateFormat("h:mma");
            String formattedTime = sdf.format(currentEvent.getDate())+" • "+"by "+currentEvent.getHost()+" "+currentEvent.getFee();
            timeAndPlace.setText(formattedTime);

            // Default case
            upvotes.setTextColor(Color.rgb(0, 0, 0));

            if(userUpvoted(currentEvent) != null) {
                upvotes.setTextColor(Color.rgb(0, 169, 255));
                button.setBackgroundResource(R.drawable.bluevote);
                button.setText("unvote");
            } else {
                button.setTextColor(Color.rgb(0, 0, 0));
                button.setBackgroundResource(R.drawable.vote);
                button.setText("vote");
            }

            loadingDialog.dismiss();

            return itemView;

        }
    }

    @Override
    public void onClick(View v) {
        Intent i;
        switch(v.getId()) {
            case R.id.newPostButton:
                i = new Intent(this, Add.class);
                startActivity(i);
                break;
            case R.id.dateOption:
                v.setBackgroundResource(R.drawable.selection_bg);
                findViewById(R.id.hypeOption).setBackgroundResource(R.drawable.selection_blank_bg);
                dateView = true;
                populateListView((ArrayList<Event>) events);
                registerClickCallback();
                break;
            case R.id.hypeOption:
                v.setBackgroundResource(R.drawable.selection_bg);
                findViewById(R.id.dateOption).setBackgroundResource(R.drawable.selection_blank_bg);
                dateView = false;
                sortByHype();
                populateListView((ArrayList<Event>) sortedEvents);
                registerClickCallback();
                break;
            case R.id.settingsButton:
                i = new Intent(this, Settings.class);
                startActivity(i);
                break;
            default:
                i = new Intent(this, NewPostA.class);
                startActivity(i);
        }
    }

    public void sortByHype() {
        sortedEvents.removeAll(sortedEvents);
        sortedEvents.addAll(events);
        Collections.sort(sortedEvents);
    }


    public void loadUpvoteData() {
        ParseRelation upvoteRelation = currentUser.getRelation("upvote_data");
        ParseQuery<ParseObject> upvoteQuery = upvoteRelation.getQuery();

        try {
            upvoteQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> list, ParseException e) {
                    if(list != null) {
                        for (ParseObject object : list) {
                            upvoteEvents.add(new Event(object));
                            upvoteObjects.add(object);
                        }
                    } else {
                        System.err.println("Error connecting to the network");
                        Toast networkToast = Toast.makeText(PostTableA.this, "Error connecting to the network", Toast.LENGTH_LONG);
                        networkToast.show();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ParseObject parseObjectForEvent(Event e) {
        for(ParseObject object : parseEvents) {
            if(object.getObjectId().equals(e.getObjectID())) {
                return object;
            }
        }
        return null;
    }

    public Event userUpvoted(Event event) {
        for(Event upvoted : upvoteEvents) {
            if(event.getObjectID().equals(upvoted.getObjectID())){
                return upvoted;
            }
        }
        return null;
    }

    public void unvoteEvent(ParseObject object) {

        try {
            object.increment("upvotes", -1);

            Event upvotedEvent = null;

            for (Event event : events) {
                if ((upvotedEvent = userUpvoted(event)) != null) {
                    event.unvote();
                    upvoteEvents.remove(upvotedEvent);
                }
            }

            upvoteObjects.remove(object);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            currentUser.put("upvote_data", upvoteObjects);
            currentUser.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    System.out.println("User updated");
                }
            });

            object.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    System.out.println("Post updated");
                }
            });
        }
    }

    public void upvoteEvent(ParseObject object) {

        try {
            object.increment("upvotes", 1);

            // Get the coresponding event in the local array and add to the upvoted list
            for (Event event : events) {
                if (event.getObjectID().equals(object.getObjectId())) {
                    upvoteEvents.add(event);
                    event.upvote();
                }
            }

            upvoteObjects.add(object);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            currentUser.put("upvote_data", upvoteObjects);
            currentUser.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    System.out.println("User updated");
                }
            });

            object.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    System.out.println("Post updated");
                }
            });
        }


    }

    public Dialog showLoadingDialog() {

        Dialog loadingDialog = new Dialog(this);
        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loadingDialog.setContentView(R.layout.loading_dialog);
        RotateLoading rotateLoading = (RotateLoading) loadingDialog.findViewById(R.id.rotateloading);
        rotateLoading.start();

        final Window window = loadingDialog.getWindow();
        window.setLayout(500, 500);
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        return loadingDialog;
    }



    public void getLocation() {

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                userLocation = location;
                loadUpvoteData();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

    }

    public boolean eventInArea(ParseObject object) {

        ParseGeoPoint eventLocation = object.getParseGeoPoint("postLocation");
        try {
            System.out.println("Latitude: " + this.userLocation.getLatitude() + " Longitude: " + this.userLocation.getLongitude());
        } catch (Exception e) {
            this.userLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            System.out.println("Used last known location");
        }

        ParseGeoPoint userLocation = new ParseGeoPoint(this.userLocation.getLatitude(), this.userLocation.getLongitude());

        Double distance = eventLocation.distanceInMilesTo(userLocation);
        System.out.println(this.userLocation);
        System.out.println(distance);

        if (distance < 10) {
            return (true);
        } else {
            return (false);
        }

    }


}
