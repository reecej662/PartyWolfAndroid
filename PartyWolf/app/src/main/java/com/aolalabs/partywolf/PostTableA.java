package com.aolalabs.partywolf;

import android.animation.ValueAnimator;
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
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.swipe.SwipeLayout;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.victor.loading.rotate.RotateLoading;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import in.srain.cube.views.ptr.PtrClassicDefaultHeader;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrUIHandler;
import in.srain.cube.views.ptr.header.StoreHouseHeader;
import in.srain.cube.views.ptr.indicator.PtrIndicator;

//import com.walnutlabs.android.ProgressHUD;

// Array of options --> ArrayAdapter --> ListView

//List view: {views: da_items.xml}

// Fix bug that makes the buttons blue more consistently

/**
 * Created by reecejackson on 8/18/15.
 *
 *
 *
 */

public class PostTableA extends Activity implements OnClickListener{
    private ListView eventList;

    private PostDataManager dataManager;

    private ImageButton newPostButton;
    private Button hypeButton;
    private Button dateButton;
    private ImageButton settingsButton;
    private boolean dateView = true;
    private ParseUser currentUser = null;
    private Dialog loadingDialog = null;
    private LocationManager locationManager;
    private Location userLocation = null;
    private boolean firstLoad = true;

    @Override
    protected void onCreate(Bundle savedInstance) {

        super.onCreate(savedInstance);
        setContentView(R.layout.posts_table);

        loadingDialog = showLoadingDialog();
        loadingDialog.show();

        //final SwipeRefreshLayout pullToRefresh = (SwipeRefreshLayout) findViewById(R.id.pullToRefresh);

        dataManager = new PostDataManager(this);
        dataManager.setDataListener(new PostDataManager.DataListener() {
            @Override
            public void onDataLoaded() {
                Log.d("dataManager", "Data loaded");
                if(firstLoad)
                    populateListView(dataManager.events);
                firstLoad = false;
                registerClickCallback();
                loadingDialog.dismiss();
                //pullToRefresh.setRefreshing(false);
            }
        });

        dataManager.getData();

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        populateListView(dataManager.events);
        registerClickCallback();

        newPostButton = (ImageButton) findViewById(R.id.newPostButton);
        eventList = (ListView) findViewById(R.id.main_list_view);
        hypeButton = (Button) findViewById(R.id.hypeOption);
        dateButton = (Button) findViewById(R.id.dateOption);
        settingsButton = (ImageButton) findViewById(R.id.settingsButton);



        //pullToRefresh.setColorSchemeResources(R.color.ColorPrimary);

        setUpPullToRefresh();

        newPostButton.setOnClickListener(this);
        hypeButton.setOnClickListener(this);
        dateButton.setOnClickListener(this);
        settingsButton.setOnClickListener(this);

//        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                pullToRefresh.setRefreshing(true);
//                System.out.println("Refreshed");
//                Log.d("Swipe", "Refreshing Number");
//                (new Handler()).postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        dataManager.refresh();
//                        System.out.println("Now we're done refreshing");
//                    }
//                }, 2000);
//            }
//        });


        // Load the posts the user has updated
//        System.out.println("Confirmed: " + currentUser.getBoolean("confirmed"));

        Log.d("On create:", "Finished on create");

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

            dataManager.upvoteEvent(object);

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

            dataManager.unvoteEvent(object);

            hypeButton.setText("vote");

        }
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
                System.out.println(viewClicked.getId());
                Event clickedEvent;
                if (dateView) {
                    clickedEvent = dataManager.getEventAtIndex(position);
                } else {
                    clickedEvent = dataManager.getSortedEventAtIndex(position);
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
            super(PostTableA.this, R.layout.swipe_event, listEvents);
            events = listEvents;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            // Make sure we have a view to work with (may have been given null)
            if (dateView) {
                events = dataManager.events;
            } else {
                events = dataManager.sortedEvents;
            }

            View itemView = convertView;
            if (itemView == null) {
                itemView = getLayoutInflater().inflate(R.layout.swipe_event, parent, false);
            }

            final Event currentEvent = events.get(position);

            // fill the view
            LinearLayout daySection = (LinearLayout) itemView.findViewById(R.id.event_section);
            TextView weekday = (TextView) itemView.findViewById(R.id.event_weekday);
            TextView date = (TextView) itemView.findViewById(R.id.event_date);
            TextView title = (TextView) itemView.findViewById(R.id.event_title);
            TextView description = (TextView) itemView.findViewById(R.id.event_description);
            TextView timeAndPlace = (TextView) itemView.findViewById(R.id.event_time_and_place);
            TextView emoji = (TextView) itemView.findViewById(R.id.event_emoji);
            final TextView upvotes = (TextView) itemView.findViewById(R.id.event_hype_number);
            final Button button = (Button) itemView.findViewById(R.id.event_hype_button);
            SwipeLayout swipeLayout =  (SwipeLayout) itemView.findViewById(R.id.swipeLayout);
            final Button reportButton = (Button) itemView.findViewById(R.id.report_button);

            button.setTag(dataManager.parseObjectForEvent(currentEvent));

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

            if(dataManager.userUpvoted(currentEvent) != null) {
                upvotes.setTextColor(Color.rgb(0, 169, 255));
                button.setBackgroundResource(R.drawable.bluevote);
                button.setText("unvote");
            } else {
                button.setTextColor(Color.rgb(0, 0, 0));
                button.setBackgroundResource(R.drawable.vote);
                button.setText("vote");
            }

            // Handle swipe layout

            final int elementPosition = position;
            final OnClickListener clickListener = new OnClickListener() {
                @Override
                public void onClick(View v) {
                    System.out.println(v.getId());
                    Event clickedEvent;
                    if (dateView) {
                        clickedEvent = events.get(elementPosition);
                    } else {
                        clickedEvent = dataManager.getSortedEventAtIndex(elementPosition);
                    }
                    Intent i = new Intent(PostTableA.this, EventDetailA.class);
                    i.putExtra("event", clickedEvent);
                    startActivity(i);
                }
            };

            //set show mode.
            swipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown);
            swipeLayout.setOnClickListener(clickListener);

            //add drag edge.(If the BottomView has 'layout_gravity' attribute, this line is unnecessary)
//            swipeLayout.addDrag(SwipeLayout.DragEdge.Right, findViewById(R.id.bottom_wrapper));

            swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
                @Override
                public void onClose(SwipeLayout layout) {
                    //when the SurfaceView totally cover the BottomView.
                    layout.setOnClickListener(clickListener);
                }

                @Override
                public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {
                    //you are swiping.
                    layout.setOnClickListener(null);
                }

                @Override
                public void onStartOpen(SwipeLayout layout) {

                }

                @Override
                public void onOpen(SwipeLayout layout) {
                    //when the BottomView totally show.
                }

                @Override
                public void onStartClose(SwipeLayout layout) {

                }

                @Override
                public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {
                    //when user's hand released.
                }
            });

            reportButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast reportToast = Toast.makeText(PostTableA.this, "Event reported. Thanks!", Toast.LENGTH_LONG);
                    reportToast.show();
                    Report newReport = new Report("Problem with event", dataManager.parseObjectForEvent(currentEvent), PostTableA.this);
                    newReport.submit();
                }
            });

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
                populateListView(dataManager.getEvents());
                registerClickCallback();
                break;
            case R.id.hypeOption:
                v.setBackgroundResource(R.drawable.selection_bg);
                findViewById(R.id.dateOption).setBackgroundResource(R.drawable.selection_blank_bg);
                dateView = false;
                //sortByHype();
                populateListView(dataManager.getSortedEvents());
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
                //loadUpvoteData();
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


    public void setUpPullToRefresh() {
        PtrFrameLayout pullToRefresh = (PtrFrameLayout) findViewById(R.id.pullToRefresh);
        PtrClassicDefaultHeader headerView = (PtrClassicDefaultHeader) pullToRefresh.getHeaderView();
        final StoreHouseHeader newHeader = new StoreHouseHeader(pullToRefresh.getContext());
        newHeader.setBackgroundResource(R.color.colorPrimary);
        headerView.setBackgroundResource(R.color.colorPrimary);

        newHeader.setDropHeight(200);
        //newHeader.setMinimumHeight(300);

        ImageView wolfSpinner = new ImageView(this);
        wolfSpinner.setBackgroundResource(R.drawable.wolf_spinner);
        wolfSpinner.setLayoutParams(new ViewGroup.LayoutParams(100, 100));
        wolfSpinner.setScaleType(ImageView.ScaleType.FIT_XY);

        ImageView glassesSpinner = new ImageView(this);
        glassesSpinner.setBackgroundResource(R.drawable.glasses_spinner);
        glassesSpinner.setLayoutParams(new ViewGroup.LayoutParams(100, 100));
        glassesSpinner.setScaleType(ImageView.ScaleType.FIT_XY);

        final ImageView wolf = (ImageView) findViewById(R.id.wolfSpinner);
        final ImageView glasses = (ImageView) findViewById(R.id.glassesSpinner);

        wolf.setVisibility(View.GONE);
        glasses.setVisibility(View.GONE);

        wolf.setX(50);
        glasses.setX(getWindowManager().getDefaultDisplay().getWidth() - 260);

        //newHeader.addView(wolfSpinner);
        //newHeader.add
        newHeader.setPadding(0, 25, 0, 25);
        newHeader.setBackgroundColor(Color.argb(0, 255, 255, 255));

        final ValueAnimator colorAnimation = ValueAnimator.ofInt(0, 255*5);
        colorAnimation.setDuration(2000);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {

                int value = (Integer) animator.getAnimatedValue() % 510;
                int alpha = 0;
                if(value < 255) {
                    alpha = value;
                } else if (value > 255){
                    alpha = 510 - value;
                }
                System.out.println("Alpha: " + alpha);

                newHeader.setBackgroundColor(Color.argb(alpha, 0, 169, 255));
            }

        });
        //colorAnimation.setDuration(500);

        newHeader.initWithString("Work dammit");
        headerView.addView(glassesSpinner);

        pullToRefresh.setHeaderView(newHeader);

        pullToRefresh.setPtrHandler(new PtrDefaultHandler() {
            @Override
            public void onRefreshBegin(final PtrFrameLayout frame) {
                frame.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dataManager.refresh();
                        frame.refreshComplete();
                        Log.d("Refresh", "Refreshing complete");
                    }
                }, 1800);

            }
        });

        pullToRefresh.addPtrUIHandler(new PtrUIHandler() {
            @Override
            public void onUIReset(PtrFrameLayout frameLayout) {
                newHeader.setBackgroundColor(Color.argb(0, 255, 255, 255));
                wolf.setX(50);
                glasses.setX(getWindowManager().getDefaultDisplay().getWidth() - 260);
            }

            @Override
            public void onUIRefreshPrepare(PtrFrameLayout frameLayout) {
                wolf.setVisibility(View.VISIBLE);
                glasses.setVisibility(View.VISIBLE);
            }

            @Override
            public void onUIRefreshBegin(PtrFrameLayout frameLayout) {
                colorAnimation.start();
            }

            @Override
            public void onUIRefreshComplete(PtrFrameLayout frameLayout) {
                colorAnimation.end();
            }

            @Override
            public void onUIPositionChange(PtrFrameLayout frameLayout, boolean b, byte b1, PtrIndicator ptrIndicator) {
                //System.out.println(eventList.getY());
                float yOffSet = eventList.getY();

                
                float wolfX = wolf.getX() + yOffSet/12;
                float glassesX = glasses.getX() - yOffSet/12;
                float wolfMaxOffset = (getWindowManager().getDefaultDisplay().getWidth() - wolf.getLayoutParams().width)/2;
                float glassesMaxOffset = (getWindowManager().getDefaultDisplay().getWidth() - glasses.getLayoutParams().width)/2;

                wolf.setX(min(wolfX, wolfMaxOffset));
                glasses.setX(max(glassesX, glassesMaxOffset));
            }
        });



        // Go to login screen if no user not currently logged in
        Intent login = new Intent(this, LoginA.class);

        try{
            currentUser = ParseUser.getCurrentUser();
            if(currentUser == null) {
                startActivity(login);
            } else {
                currentUser.fetchInBackground();
                getLocation();
            }
        } catch (Exception e) {
            startActivity(login);
        }
    }

    public float min(float f1, float f2) {
        if(f1 < f2)
            return(f1);
        else
            return(f2);
    }

    public float max(float f1, float f2) {
        if(f1 > f2)
            return(f1);
        else
            return(f2);
    }

}
