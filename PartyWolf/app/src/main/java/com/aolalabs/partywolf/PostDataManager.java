package com.aolalabs.partywolf;

import android.app.Activity;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.parse.FindCallback;
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
    private Location userLocation = null;

    public PostDataManager(Activity context){
        this.context = context;
        this.listener = null;
    }

    public void getData() {
        try {
            loadUpvoteData();
            loadEventData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Event getEventAtIndex(int index) {
        return events.get(index);
    }

    public Event getSortedEventAtIndex(int index) {
        return sortedEvents.get(index);
    }

    public ArrayList<Event> getEvents() {
        return (ArrayList<Event>) this.events;
    }

    public ArrayList<Event> getSortedEvents() {
        return (ArrayList<Event>) this.sortedEvents;
    }

    public ArrayList<ParseObject> getParseEvents() {
        return (ArrayList<ParseObject>) parseEvents;
    }

    public void sortEvents() {
        sortedEvents.clear();
        sortedEvents.addAll(events);
        Collections.sort(sortedEvents);
    }

    public void clearData() {
        events.clear();
        sortedEvents.clear();
        parseEvents.clear();
        upvoteEvents.clear();
        upvoteObjects.clear();
    }

    public void refresh() {
        loadUpvoteData();
        loadEventData();
        this.listener.onDataLoaded();
    }


    // Methods to load data into class

    public void loadEventData() {

        clearData();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Posts");

        java.util.Date today = new java.util.Date();
        GregorianCalendar calendar = new GregorianCalendar();
        java.util.Date startOfDay = calendar.getTime();
        startOfDay.setHours(0);
        startOfDay.setMinutes(0);
        startOfDay.setSeconds(0);

        java.util.Date oneWeek = new java.util.Date(today.getTime()+604800000);

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
                            if((eventInArea(object) && !object.getBoolean("onCampus")) || (currentUser.get("university").equals(object.get("university")))) {
                                events.add(new Event(object));
                                parseEvents.add(object);
                                System.out.println(object.getString("title"));
                            }
                        }
                    } else {
                        e.printStackTrace();
                        System.err.println("Error connecting to the network");
                        Toast networkToast = Toast.makeText(PostDataManager.this.context, "Error connecting to the network", Toast.LENGTH_LONG);
                        networkToast.show();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    sortEvents();
                    //populateListView((ArrayList<Event>) events);
                    Log.d("Event loading", "Finished");
                    if(PostDataManager.this.listener != null) {
                        listener.onDataLoaded();
                    }
                }
            }
        });

    }

    public void loadUpvoteData() {

        ParseRelation upvoteRelation = currentUser.getRelation("upvote_data");
        ParseQuery<ParseObject> upvoteQuery = upvoteRelation.getQuery();
        Log.d("Upvote relation: ", upvoteRelation.toString());

        try {
            upvoteQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> list, ParseException e) {
                    if(list != null) {
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
            Log.d("Upvote loading", "Finished");
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
                    System.out.println("New upvote count: " + object.getNumber("upvotes"));
                    System.out.println("Post updated");
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
                    System.out.println("User updated");
                }
            });
        }
    }

    public void unvoteEvent(ParseObject object) {

        // Change the values for the locally stored Event
        Event upvotedEvent = null;
        for (Event event : events) {
            if ((upvotedEvent = userUpvoted(event)) != null) {
                event.unvote();
                upvoteEvents.remove(upvotedEvent);
                System.out.println("User unvoted the event");
                System.out.println("upvoteEvents count: " + upvoteEvents.size());
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
                    System.out.println("Post updated");
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
                    System.out.println("User updated");
                }
            });
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

    public interface DataListener {
        void onDataLoaded();
    }

    public void setDataListener(DataListener listener) {
        this.listener = listener;
    }

    public boolean eventInArea(ParseObject object) {

        ParseGeoPoint eventLocation = object.getParseGeoPoint("postLocation");

        /*try {
            System.out.println("Latitude: " + this.userLocation.getLatitude() + " Longitude: " + this.userLocation.getLongitude());
        } catch (Exception e) {
            System.out.println("Location was null");
        }*/

        ParseGeoPoint userLocation = new ParseGeoPoint(this.userLocation.getLatitude(), this.userLocation.getLongitude());

        Double distance = eventLocation.distanceInMilesTo(userLocation);

        if (distance < 20) {
            return (true);
        } else {
            return (false);
        }

    }

    public Location getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(Location userLocation) {
        this.userLocation = userLocation;
    }
}
