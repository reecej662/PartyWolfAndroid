package com.aolalabs.partywolf;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by reecejackson on 8/28/15.
 *
 * A class to handle all the data in the Post Table Activity
 */

public class PostDataManager {
    private ParseUser currentUser = ParseUser.getCurrentUser();
    protected ArrayList<Event> events = new ArrayList<>();
    protected ArrayList<Event> sortedEvents = new ArrayList<>();
    protected List<ParseObject> parseEvents = new ArrayList<>();
    protected List<Event> upvoteEvents = new ArrayList<>();
    protected List<ParseObject> upvoteObjects = new ArrayList<>();
    private Activity context;
    private DataListener listener;
    private UserLocationManager userLocationManager;
    private boolean eventsLoaded = false;
    private boolean upvotesLoaded = false;

    // Set up and basic utility methods

    public PostDataManager(Activity context){
        this.context = context;
        this.listener = null;
        this.currentUser = ParseUser.getCurrentUser();
    }

    public void setUp() {
        if(currentUser == null) {
            ParseUser.getCurrentUser().fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject parseObject, ParseException e) {
                    setUpLocation();
                }
            });
        } else {
            setUpLocation();
        }
    }

    public void setUpLocation() {
        this.userLocationManager = new UserLocationManager(context);

        userLocationManager.setListener(new UserLocationManager.UserLocationListener() {
            @Override
            public void locationFound() {
                // Returns when the user's location is not null
                // DON'T USE LOCATION UNTIL THIS RETURNS


                // gonna want to get the data here
                // only runs on the first time
                getData();
            }

            @Override
            public void locationUpdated() {

                // gonna want to update the data here
                // But this runs A LOT

            }
        });

        userLocationManager.getLocation();
    }

    private void getData() {
        try {
            loadUpvoteData();
            loadEventData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void callListenerLoaded() {
        if(upvotesLoaded && eventsLoaded) {
            listener.onDataLoaded();
        }
    }

    // Figure out a more efficient way to refresh -- wasting queries
    public void refresh() {
        try {
            loadUpvoteData();
            loadEventData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clearData() {
        events.clear();
        sortedEvents.clear();
        parseEvents.clear();
        upvoteEvents.clear();
        upvoteObjects.clear();
    }

    public void sortEvents() {
        sortedEvents.clear();
        sortedEvents.addAll(events);
        Collections.sort(sortedEvents);
    }

    // Getters

    public ParseObject parseObjectForEvent(Event e) {
        for(ParseObject object : parseEvents) {
            if(object.getObjectId().equals(e.getObjectID())) {
                return object;
            }
        }
        return null;
    }

    public Event getEventAtIndex(int index) {
        return events.get(index);
    }

    public Event getSortedEventAtIndex(int index) {
        return sortedEvents.get(index);
    }

    public ArrayList<Event> getEvents() {
        return this.events;
    }

    public ArrayList<Event> getSortedEvents() {
        if(this.events.size() != this.sortedEvents.size()) {
            sortEvents();
            return this.sortedEvents;
        } else {
            return this.sortedEvents;
        }
    }

    public ArrayList<ParseObject> getParseEvents() {
        return (ArrayList<ParseObject>) parseEvents;
    }


    public Context getContext() {
        return this.context;
    }

    // Methods to load data into class

    public void loadEventData() {

        clearData();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Posts");
        eventsLoaded = false;

        java.util.Date today = new java.util.Date();
        GregorianCalendar calendar = new GregorianCalendar();
        java.util.Date startOfDay = calendar.getTime();
        startOfDay.setHours(0);
        startOfDay.setMinutes(0);
        startOfDay.setSeconds(0);

        java.util.Date oneWeek = new java.util.Date(today.getTime()+604800000);

        try {
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
                            if((eventInArea(object) && !object.getBoolean("onCampus")) || (currentUser.get("university").equals(object.get("university")))) {
                                events.add(new Event(object));
                                parseEvents.add(object);
                            }
                        }
                    } else {
                        e.printStackTrace();
                        Log.d("Event loading", "Error connecting to the network");
                        Toast networkToast = Toast.makeText(PostDataManager.this.context, "Error connecting to the network", Toast.LENGTH_LONG);
                        networkToast.show();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    sortEvents();
                    Log.d("Data Manager", "Events Loaded");
                    eventsLoaded = true;
                    callListenerLoaded();
                }
            }
        });

    }

    public void loadUpvoteData() {

        ParseRelation upvoteRelation = currentUser.getRelation("upvote_data");
        ParseQuery<ParseObject> upvoteQuery = upvoteRelation.getQuery();
        upvotesLoaded = false;

        try {
            upvoteQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> list, ParseException e) {
                    if (list != null) {
                        for (ParseObject object : list) {
                            upvoteEvents.add(new Event(object));
                            upvoteObjects.add(object);
                        }
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Log.d("Data Manager", "Upvotes Loaded");
            upvotesLoaded = true;
            callListenerLoaded();
        }
    }

    // Methods to handle the voting/unvoting of events

    public void upvoteEvent(final ParseObject object) {

        // Get the coresponding event in the local array and add to the upvoted list
        for (Event event : events) {
            if (event.getObjectID().equals(object.getObjectId())) {
                upvoteEvents.add(event);
                event.upvote();
            }
        }

        upvoteObjects.add(object);

        // Add the current user to the object's upvote relation and increment the count
        try {
            object.increment("upvotes", 1);
            object.getRelation("upvote_data").add(ParseUser.getCurrentUser());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            object.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null)
                        Log.d("Object saving error", e.toString());
                    Log.d("Upvote event", "Event updated");
                }
            });
        }

        // Remove the object from the current user's upvoted objects
        try {
            currentUser.getRelation("upvote_data").add(object);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            currentUser.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null)
                        Log.d("User saving error", e.toString());
                    Log.d("Upvote event", "User updated");
                }
            });
        }
    }

    public void unvoteEvent(ParseObject object) {

        // Change the values for the locally stored Event
        for (Event event : events) {
            if (event.getObjectID().equals(object.getObjectId())) {
                Event upvotedEvent = userUpvoted(event);
                event.unvote();
                upvoteEvents.remove(upvotedEvent);
            }
        }

        upvoteObjects.remove(object);

        // Add the new user to the object upvote relation and increment number
        try {
            // Change the values for the parse object
            object.increment("upvotes", -1);
            object.getRelation("upvote_data").remove(currentUser);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            object.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null)
                        Log.d("Object saving error", e.toString());
                }
            });
        }

        // Add the new post to the user's list of upvoted posts
        try {
            ParseUser.getCurrentUser().getRelation("upvote_data").remove(object);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            currentUser.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null)
                        Log.d("User saving error", e.toString());
                }
            });
        }

    }

    public Event userUpvoted(Event event) {
        for(Event upvoted : upvoteEvents) {
            if(event.getObjectID().equals(upvoted.getObjectID())){
                return upvoted;
            }
        }
        return null;
    }

    // Interface and method to set up DataListener

    public interface DataListener {
        void onDataLoaded();
    }

    public void setDataListener(DataListener listener) {
        this.listener = listener;
    }

    // Methods to deal with location

    public boolean eventInArea(ParseObject object) {

        ParseGeoPoint eventLocation = object.getParseGeoPoint("postLocation");

        if(userLocationManager.locationAvailable()) {
            ParseGeoPoint userLocation;

            try {
                userLocation = userLocationManager.getParseLocation();
            } catch (Exception e) {
                e.printStackTrace();
                userLocation = currentUser.getParseGeoPoint("currentLocation");
            }

            Double distance = eventLocation.distanceInMilesTo(userLocation);

            if (distance < 20) {
                return (true);
            } else {
                return (false);
            }
        } else {
            Log.d("Event in Area: ", "location not avaliable");
            return false;
        }

    }

    public Location getUserLocation() {
        return userLocationManager.getUserLocation();
    }

    public String getCity() {
        return userLocationManager.getCurrentCity();
    }

}
