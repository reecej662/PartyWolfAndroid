package com.aolalabs.partywolf;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.victor.loading.rotate.RotateLoading;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import in.srain.cube.views.ptr.PtrClassicDefaultHeader;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrUIHandler;
import in.srain.cube.views.ptr.header.StoreHouseHeader;
import in.srain.cube.views.ptr.indicator.PtrIndicator;

/**
 * Created by reecejackson on 8/18/15.
 *
 *
 *
 */

public class PostTableA extends Activity implements OnClickListener{
    private ListView eventList;
    private PostDataManager dataManager;
    private ParseUser currentUser = null;
    private ArrayAdapter<Event> adapter;
    private Dialog loadingDialog = null;
    private boolean firstLoad = true;
    private boolean dateView = true;

    @Override
    protected void onCreate(Bundle savedInstance) {

        super.onCreate(savedInstance);
        setContentView(R.layout.posts_table);

        // Go to login screen if no user not currently logged in
        Intent login = new Intent(this, LoginA.class);

        try{
            currentUser = ParseUser.getCurrentUser();
            if(currentUser == null) {
                finish();
                startActivity(login);
            } else if(currentUser.getNumber("classOf") == null) {
                Intent i = new Intent(PostTableA.this, ClassOfA.class);
                startActivity(i);
            } else {
                loadingDialog = getLoadingDialog();
                loadingDialog.show();
                checkInstallation();

                currentUser.fetchInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject parseObject, ParseException e) {
                        //getLocation()
                        setUpActivity();
                    }
                });
            }
        } catch (Exception e) {
            startActivity(login);
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        saveUser();
    }

    public void setUpActivity(){

        // Set up data manager
        dataManager = new PostDataManager(this);

        dataManager.setDataListener(new PostDataManager.DataListener() {
            @Override
            public void onDataLoaded() {
                if(firstLoad)
                    populateListView(dataManager.events);
                else{
                    if (adapter.getClass().equals(MyListAdapter.class)) {
                        MyListAdapter tmpAdapter = (MyListAdapter) adapter;
                        if(tmpAdapter.getEvents().size() != dataManager.getEvents().size()) {
                            if(dateView) {
                                populateListView(dataManager.getEvents());
                                registerClickCallback();
                            } else {
                                populateListView(dataManager.getSortedEvents());
                                registerClickCallback();
                            }
                        }
                    }
                }
                firstLoad = false;
                registerClickCallback();

                findViewById(R.id.wolfSpinner).setVisibility(View.VISIBLE);
                findViewById(R.id.glassesSpinner).setVisibility(View.VISIBLE);
                Log.d("Post Table", "Data loading");
            }
        });

        dataManager.setUp();
        registerClickCallback();

        eventList = (ListView) findViewById(R.id.main_list_view);
        ImageButton newPostButton = (ImageButton) findViewById(R.id.newPostButton);
        Button hypeButton = (Button) findViewById(R.id.hypeOption);
        Button dateButton = (Button) findViewById(R.id.dateOption);
        ImageButton settingsButton = (ImageButton) findViewById(R.id.settingsButton);

        setUpPullToRefresh();

        newPostButton.setOnClickListener(this);
        hypeButton.setOnClickListener(this);
        dateButton.setOnClickListener(this);
        settingsButton.setOnClickListener(this);
    }

    //9722610049
    private void populateListView(ArrayList<Event> events) {

        findViewById(R.id.emptyTextView).setVisibility(View.GONE);
        adapter = new MyListAdapter(events);

        Log.d("Populate List view", "Events size: " + events.size());

        if(events.size() == 0) {
            findViewById(R.id.emptyTextView).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.emptyTextView).setVisibility(View.GONE);
            // Configure the list view
        }

        ListView list = (ListView) findViewById(R.id.main_list_view);
        list.setDivider(null);
        list.setAdapter(adapter);

        // Use this to refresh:
        // adapter.notifyDataSetChanged();

        if(loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }

    }

    private class MyListAdapter extends ArrayAdapter<Event> {
        private ArrayList<Event> events;

        public ArrayList<Event> getEvents() {
            return events;
        }

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

            return itemView;
        }
    }

    public void noEventsFound() {

//        One of these is gonna work I swear..

//        Log.d("No events", "I should be doing something");
//        findViewById(R.id.main_list_view).setVisibility(View.INVISIBLE);
//        View emptyLayout = getLayoutInflater().inflate(R.layout.empty_posts_table, (ListView) findViewById(R.id.main_list_view), false);
//        PtrFrameLayout pullToRefresh = (PtrFrameLayout) findViewById(R.id.pullToRefresh);
//        TextView emptyTextView = (TextView) emptyLayout.findViewById(R.id.emptyTextView);
//        LinearLayout parent = (LinearLayout) emptyTextView.getParent();
//        parent.removeAllViews();
//        pullToRefresh.addView(emptyTextView);

//        PtrFrameLayout pullToRefresh = (PtrFrameLayout) findViewById(R.id.pullToRefresh);
//        TextView emptyText = (TextView) findViewById(R.id.emptyTextView);
//        RelativeLayout parent = (RelativeLayout) emptyText.getParent();
//        parent.removeView(emptyText);
//        pullToRefresh.addView(emptyText);

//        ListView mainListView = (ListView) findViewById(R.id.main_list_view);
//        mainListView.setVisibility(View.GONE);
//        TextView emptyTextView = new TextView(this, null);
//        emptyTextView.setHeight(800);
//        emptyTextView.setWidth(500);
//        emptyTextView.setGravity(Gravity.CENTER);
//        emptyTextView.setPadding(10, 10, 10, 10);
//        emptyTextView.setText("No events to display, click the + to add one!");
//        emptyTextView.setTextColor(Color.argb(0, 0, 0, 0));
//        emptyTextView.setTextSize(22);
//        PtrFrameLayout pullToRefresh = (PtrFrameLayout) findViewById(R.id.pullToRefresh);
//        pullToRefresh.addView(emptyTextView);

        findViewById(R.id.emptyTextView).setVisibility(View.VISIBLE);

    }

    private void registerClickCallback() {
        ListView list = (ListView) findViewById(R.id.main_list_view);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked, int position, long id) {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == 2) {
            PostTableA.this.finish();
        }
    }

    @Override
    public void onClick(View v) {
        Intent i;
        switch(v.getId()) {
            case R.id.newPostButton:
                i = new Intent(this, Add.class);
                i.putExtra("location", dataManager.getUserLocation());
                startActivityForResult(i, 1);
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
                populateListView(dataManager.getSortedEvents());
                registerClickCallback();
                break;
            case R.id.settingsButton:
                i = new Intent(this, Settings.class);
                i.putExtra("city", dataManager.getCity());
                startActivity(i);
                break;
            default:
                i = new Intent(this, NewPostA.class);
                startActivity(i);
        }
    }

    public Dialog getLoadingDialog() {

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


    public void setUpPullToRefresh() {
        PtrFrameLayout pullToRefresh = (PtrFrameLayout) findViewById(R.id.pullToRefresh);
        PtrClassicDefaultHeader headerView = (PtrClassicDefaultHeader) pullToRefresh.getHeaderView();
        final StoreHouseHeader newHeader = new StoreHouseHeader(pullToRefresh.getContext());
        newHeader.setBackgroundResource(R.color.colorPrimary);
        headerView.setBackgroundResource(R.color.colorPrimary);

        newHeader.setDropHeight(200);

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

        newHeader.setPadding(0, 25, 0, 25);
        newHeader.setBackgroundColor(Color.argb(0, 255, 255, 255));

        final ValueAnimator colorAnimation = ValueAnimator.ofInt(0, 200*5);
        colorAnimation.setDuration(2000);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {

                int value = (Integer) animator.getAnimatedValue() % 400;
                int alpha = 0;
                if(value < 200) {
                    alpha = value;
                } else if (value > 200){
                    alpha = 200 - value;
                }

                newHeader.setBackgroundColor(Color.argb(alpha, 0, 169, 255));
            }

        });

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
            float prevYOffset = 0;
            float wolfInitialX = 50;
            float glassesInitialX = getWindowManager().getDefaultDisplay().getWidth() - 260;
            float wolfMaxOffset = (getWindowManager().getDefaultDisplay().getWidth() - wolf.getLayoutParams().width)/2;
            float glassesMaxOffset = (getWindowManager().getDefaultDisplay().getWidth() - glasses.getLayoutParams().width)/2;

            @Override
            public void onUIReset(PtrFrameLayout frameLayout) {
                prevYOffset = 0;
                newHeader.setBackgroundColor(Color.argb(0, 255, 255, 255));
                wolf.setX(wolfInitialX);
                glasses.setX(glassesInitialX);
                wolf.setVisibility(View.GONE);
                glasses.setVisibility(View.GONE);
            }

            @Override
            public void onUIRefreshPrepare(PtrFrameLayout frameLayout) {
                wolf.setVisibility(View.VISIBLE);
                glasses.setVisibility(View.VISIBLE);
                findViewById(R.id.emptyTextView).setVisibility(View.GONE);
            }

            @Override
            public void onUIRefreshBegin(PtrFrameLayout frameLayout) {
                colorAnimation.start();
                wolf.setX(wolfMaxOffset);
                glasses.setX(glassesMaxOffset);
            }

            @Override
            public void onUIRefreshComplete(PtrFrameLayout frameLayout) {
                colorAnimation.end();
            }

            @Override
            public void onUIPositionChange(PtrFrameLayout frameLayout, boolean b, byte b1, PtrIndicator ptrIndicator) {
                float yOffSet = eventList.getY();

                if(prevYOffset > yOffSet) {
                    yOffSet = -yOffSet;
                } else {
                    prevYOffset = yOffSet;
                }

                if(wolf.getX() == wolfMaxOffset && glasses.getX() == glassesMaxOffset) {
                    yOffSet = 0;
                }

                float wolfX = wolf.getX() + yOffSet/12;
                float glassesX = glasses.getX() - yOffSet/12;

                wolf.setX(max(min(wolfX, wolfMaxOffset), wolfInitialX));
                glasses.setX(min(max(glassesX, glassesMaxOffset), glassesInitialX));
            }
        });

    }

    public void saveUser(){
        try {
            currentUser.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    Log.d("saveUser", "user saved");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void checkInstallation() {
        final ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        if(currentUser != null) {
            ParseUser userInstallation = (ParseUser) installation.getParseObject("user");
            if (userInstallation != null) {
                // All good, no need to fetch stuff
            } else {
                currentUser.fetchInBackground();

                installation.put("user", currentUser);

                try{
                    installation.put("onStatus", currentUser.getBoolean("onStatus"));
                } catch (Exception e) {
                    installation.put("onStatus", true);
                    currentUser.put("onStatus", true);
                }

                try{
                    installation.put("onNew", currentUser.getBoolean("onNew"));
                } catch (Exception e) {
                    installation.put("onNew", true);
                    currentUser.put("onNew", true);
                }

                try {
                    installation.put("onHype", currentUser.getInt("onHype"));
                } catch (Exception e) {
                    installation.put("onHype", -1);
                    currentUser.put("onHype", -1);
                }

                currentUser.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        Log.d("Check installation", "user saved");
                        ParseObject university;
                        try {
                            university = currentUser.getParseObject("university");
                            installation.addUnique("channels", university.getObjectId());
                            installation.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    Log.d("Save installation", "installation is saved");
                                }
                            });
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });

            }
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
