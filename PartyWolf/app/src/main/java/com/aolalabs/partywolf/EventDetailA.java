package com.aolalabs.partywolf;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.utils.PercentFormatter;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.rey.material.widget.ProgressView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by reecejackson on 8/18/15.
 */
public class EventDetailA extends Activity {
    private Event event;
    private PieChart genderChart;
    private PieChart classChart;
    private LinearLayout friendView;
    private ArrayList<ParseUser> upvoteData = new ArrayList<>();
    private ParseRelation<ParseObject> eventRelation;
    private Dialog loadingDialog = null;

    private float[] genderNumbers = {0, 0, 0};
    private String[] genderNames = {"Male", "Female", "Unknown"};

    private float[] classNumbers = {0, 0, 0, 0, 0};
    private String[] classNames = {"Freshman", "Sophomore", "Junior", "Senior", "Unknown"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_detail);

        loadingDialog = showLoadingDialog();
        loadingDialog.show();

        event = (Event) getIntent().getSerializableExtra("event");

        System.out.println(event);

        TextView title = (TextView) findViewById(R.id.event_detail_title);
        TextView host = (TextView) findViewById(R.id.event_detail_host);
        TextView location = (TextView) findViewById(R.id.event_detail_location);
        TextView emoji = (TextView) findViewById(R.id.event_detail_emoji);
        TextView date = (TextView) findViewById(R.id.event_detail_date);
        friendView = (LinearLayout) findViewById(R.id.event_detail_friend_container);

        /*

        1. Get chart from the view (onCreate)
        2. Get relation to list of users that have upvoted (getUserRelation)
        3. Add users form relation into upvote data array (loadUpvoteData)
        4. Add/Increment yData array based on gender in upvoteData array (getGenderData)
        5. Set up chart based on new data (setUpChart)
        6. Animate the chart (animate)

         */

        // Chart step 1
        genderChart = (PieChart) findViewById(R.id.event_detail_graphs_gender);
        classChart = (PieChart) findViewById(R.id.event_detail_graphs_class);
        System.out.println("We finished step 1");

        title.setText(event.getTitle());
        host.setText("by " + event.getHost() + " " + event.getFee());
        location.setText(event.getDesc());
        emoji.setText(event.getEmoji());

        // Chart step 2
        getUserRelation();

        SimpleDateFormat dateString = new SimpleDateFormat("MMM d, yyyy (EEE) • h:ma");
        String formattedTime = dateString.format(event.getDate());
        date.setText(formattedTime);

    }

    public void getUserRelation() {

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Posts");
        query.whereEqualTo("objectId", event.getObjectID());
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                try {
                    for(ParseObject object : list) {
                        eventRelation = object.getRelation("upvote_data");
                    }
                } catch (Exception ex) {
                    System.err.println("Line 93: " + ex);
                } finally {
                    System.out.println("We finished step 2");

                    // Start step 3
                    getUpvoteData();
                }
            }
        });
    }

    public void getUpvoteData() {

        ParseRelation<ParseObject> relation = eventRelation;

        if(relation != null) {
            ParseQuery<ParseObject> query = relation.getQuery();

            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> list, ParseException e) {
                    try {
                        if (list.isEmpty()) {
                            ParseUser currentUser = ParseUser.getCurrentUser();
                            EventDetailA.this.upvoteData.add(currentUser);
                            System.out.println("The upvote list is empty");
                        }
                        for (ParseObject user : list) {
                            EventDetailA.this.upvoteData.add((ParseUser) user);
                        }
                    } catch (Exception ex) {
                        System.err.println("Line 164:" + ex);
                    } finally {
                        System.out.println("We finished step 3");

                        // Start step 4
                        getGenderBreakdown();
                        getClassBreakdown();

                        loadFacebookFriends();
                    }
                }
            });
        }

    }

    public void getGenderBreakdown() {

        try {
            for (ParseUser user : upvoteData) {

                String gender = user.getString("gender");

                if (gender != null) {
                    switch (gender) {
                        case "male":
                            genderNumbers[0] += 1;
                            break;
                        case "female":
                            genderNumbers[1] += 1;
                            break;
                        default:
                            genderNumbers[2] += 1;
                    }
                } else {
                    genderNumbers[2] += 1;
                }

            }
        } catch (Exception e) {
            System.err.println("Line 170: " + e);
        } finally {
            System.out.println("We finished step 4");

            // Start step 5
            setUpChart(genderChart);
        }
    }

    public void getClassBreakdown() {

        try {
            for(ParseUser user: upvoteData) {

                Number userYear = user.getNumber("classOf");
                int userClass = userYear.intValue();

                if(!userYear.equals(null)) {
                    switch (userClass) {
                        case 2019:
                            classNumbers[0] += 1;   // Freshman
                            break;
                        case 2018:
                            classNumbers[1] += 1;   // Sophomore
                            break;
                        case 2017:
                            classNumbers[2] += 1;   // Junior
                            break;
                        case 2016:
                            classNumbers[3] += 1;   // Senior
                            break;
                        default:
                            break;
                    }
                }

            }
        } catch (Exception e) {
            System.err.println("Line 202: " + e);
        } finally {
            setUpChart(classChart);
        }

    }

    public void setUpChart(PieChart chart) {
        chart.setUsePercentValues(true);
        chart.setHoleRadius(30);

        chart.setDrawHoleEnabled(true);
        chart.setHoleColorTransparent(true);
        chart.setTransparentCircleRadius(0);

        chart.setRotationAngle(0);
        chart.setRotationEnabled(true);

        chart.getLegend().setEnabled(false);
        chart.setDescription("");

        System.out.println("We finished step 5");

        // Start step 6
        addData(chart);
    }

    public void addData(PieChart chart) {

        // Set up the gender graph
        ArrayList<Entry> graphYData;
        ArrayList<String> graphXData;
        ArrayList<Integer> colors = new ArrayList<>();

        if(chart.equals(findViewById(R.id.event_detail_graphs_gender))) {

            ArrayList<Entry> genderNumsData = new ArrayList<>();

            for (int i = 0; i < genderNumbers.length; i++) {
                if (genderNumbers[i] != 0) {
                    genderNumsData.add(new Entry(genderNumbers[i], i));
                }
            }

            ArrayList<String> genderNamesData = new ArrayList<>();

            for (int i = 0; i < genderNames.length; i++) {
                if (genderNumbers[i] != 0) {
                    genderNamesData.add(genderNames[i]);
                }
            }

            graphYData = genderNumsData;
            graphXData = genderNamesData;

            colors.add(Color.rgb(70, 105, 145)); //Dark blue
            colors.add(Color.rgb(245, 90, 104)); //Pinkish
            colors.add(Color.rgb(72, 79, 97)); //Blue-grey

            for(int i = 0; i < colors.size(); i++) {
                if(genderNumbers[i] == 0) {
                    colors.remove(i);
                }
            }

        } else {

            ArrayList<Entry> classNumsData = new ArrayList<>();

            for (int i = 0; i < classNumbers.length; i++) {
                if (classNumbers[i] != 0) {
                    classNumsData.add(new Entry(classNumbers[i], i));
                }
            }

            ArrayList<String> classNamesData = new ArrayList<>();

            for (int i = 0; i < classNames.length; i++) {
                if (classNumbers[i] != 0) {
                    classNamesData.add(classNames[i]);
                }
            }

            graphYData = classNumsData;
            graphXData = classNamesData;

            colors.add(Color.rgb(70, 105, 145)); //Dark blue
            colors.add(Color.rgb(245, 90, 104)); //Pinkish
            colors.add(Color.rgb(72, 79, 97)); //Blue-grey
            colors.add(Color.rgb(70, 105, 145)); //Dark blue
            colors.add(Color.rgb(245, 90, 104)); //Pinkish

            for(int i = 0; i < colors.size(); i++) {
                if(classNumbers[i] == 0) {
                    colors.remove(i);
                }
            }

        }

        PieDataSet dataSet = new PieDataSet(graphYData, "Gender Distribution");
        dataSet.setSliceSpace(3);
        dataSet.setSelectionShift(5);

        dataSet.setColors(colors);

        // instantiate pie data object
        PieData data = new PieData(graphXData, dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);

        chart.setData(data);

        // undo all highlights
        chart.highlightValues(null);

        // update pie chart
        System.out.println("We finished step 6");
        chart.animateXY(1000, 1000);

        loadingDialog.dismiss();
    }

    public void loadFacebookFriends() {

        TextView numFriends = (TextView) findViewById(R.id.event_detail_number_friends);
        String message = " friends hyped this event";

        numFriends.setText(String.valueOf(upvoteData.size()) + message);

        for(ParseUser user : upvoteData) {
            FacebookFriendsView newFriend = new FacebookFriendsView(this, user);
            try {
                if(!user.getString("first_name").equals(""))
                    friendView.addView(newFriend);
            } catch (Exception e) {
                System.err.println(e);
            }
        }

    }

    public Dialog showLoadingDialog() {

        Dialog loadingDialog = new Dialog(this);
        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loadingDialog.setContentView(R.layout.event_detail_loading);
        ProgressView rotateLoading = (ProgressView) loadingDialog.findViewById(R.id.progress_wheel);
        rotateLoading.start();

        final Window window = loadingDialog.getWindow();
        window.setLayout(400, 250);
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        WindowManager.LayoutParams params = window.getAttributes();
        params.y = 213;
        window.setAttributes(params);


        return loadingDialog;
    }

}
