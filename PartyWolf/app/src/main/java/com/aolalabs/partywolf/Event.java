package com.aolalabs.partywolf;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by reecejackson on 8/18/15.
 */
public class Event implements Serializable, Comparable<Event> {
    private String objectID = "";
    private String title = "No title";
    private String desc = "No description";
    private String emoji = "\uD83D\uDC33";
    private int numUpvotes = 0;
    private String voteStatus = "none";
    private Date date = new Date();
    private String weekDay = "default";
    private boolean upvoted = false;

    public boolean isUpvoted() {
        return upvoted;
    }

    public void setUpvoted(boolean upvoted) {
        this.upvoted = upvoted;
    }

    public boolean currentUserUpvoted(ParseObject object) {
        ParseRelation<ParseUser> upvoteRelation = (ParseRelation<ParseUser>) object.get("upvote_data");
        ParseQuery upvoteQuery = upvoteRelation.getQuery();

        upvoteQuery.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> list, ParseException e) {
                if(list.contains(ParseUser.getCurrentUser())) {
                    System.out.println("User has upvoted this thing");
                    Event.this.upvoted = true;
                } else {
                    Event.this.upvoted = false;
                    System.out.println("User hasn't upvoted this thing");
                }
            }
        });
        return this.upvoted;
    }

    public String getWeekDay() {
        return weekDay;
    }

    public void setWeekDay(String weekDay) {
        this.weekDay = weekDay;
    }

    public Event(String objectID, String title, String desc, String emoji, int numUpvotes, String voteStatus,
                 String host, String fee, String dateOfEvent, String dayOfWeek) {
        this.objectID = objectID;
        this.title = title;
        this.desc = desc;
        this.emoji = emoji;
        this.numUpvotes = numUpvotes;
        this.voteStatus = voteStatus;
        this.host = host;
        if(!fee.equals("$0.00")) {
            this.fee = fee;
        }
        this.dateOfEvent = dateOfEvent;
        this.dayOfWeek = dayOfWeek;
    }

    private String host = "(Test host)";
    private String fee = "(Free)";
    private String dateOfEvent = "";
    private String dayOfWeek = "";

    public Event() {}

    public Event(String title, String emoji, int numUpvotes, String host, String fee) {
        this.title = title;
        this.emoji = emoji;
        this.numUpvotes = numUpvotes;
        this.host = host;
        this.fee = fee;
    }

    public Event(ParseObject object) {
        this.objectID = object.getObjectId();
        this.title = object.getString("title");
        this.desc = object.getString("description");
        this.emoji = object.getString("emoji");
        this.numUpvotes = object.getInt("upvotes");
        this.voteStatus = "none";
        this.host = object.getString("host");
        if(!object.getString("fee").equals("$0.00")) {
            this.fee = object.getString("fee");
        }
        this.date = object.getDate("date");
        System.out.println(this.date);
        SimpleDateFormat sdf1 = new SimpleDateFormat("LLLL, d" +
                "");
        this.dateOfEvent = sdf1.format(this.date);
        System.out.println(this.dateOfEvent);

        SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
        this.weekDay =  sdf.format(this.date);
        System.out.println(this.weekDay);
    }

    public Date getDate(){
        return this.date;
    }

    public void upvote(){
        this.numUpvotes += 1;
        this.upvoted = true;
    }

    public void unvote() {
        this.numUpvotes -= 1;
        this.upvoted = false;
    }

    public void setObjectID(String objectID) {
        this.objectID = objectID;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }

    public void setNumUpvotes(int numUpvotes) {
        this.numUpvotes = numUpvotes;
    }

    public void setVoteStatus(String voteStatus) {
        this.voteStatus = voteStatus;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setFee(String fee) {
        this.fee = fee;
    }

    public void setDateOfEvent(String dateOfEvent) {
        this.dateOfEvent = dateOfEvent;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getObjectID() {

        return objectID;
    }

    public String getTitle() {
        return title;
    }

    public String getDesc() {
        return desc;
    }

    public String getEmoji() {
        return emoji;
    }

    public int getNumUpvotes() {
        return numUpvotes;
    }

    public String getVoteStatus() {
        return voteStatus;
    }

    public String getHost() {
        return host;
    }

    public String getFee() {
        return fee;
    }

    public String getDateOfEvent() {
        return dateOfEvent;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    @Override
    public String toString() {
        return "Event{" +
                "objectID='" + objectID + '\'' +
                ", title='" + title + '\'' +
                ", desc='" + desc + '\'' +
                ", emoji='" + emoji + '\'' +
                ", numUpvotes=" + numUpvotes +
                ", voteStatus='" + voteStatus + '\'' +
                ", host='" + host + '\'' +
                ", fee='" + fee + '\'' +
                ", dateOfEvent=" + dateOfEvent +
                ", dayOfWeek='" + dayOfWeek + '\'' +
                '}';
    }

    public int compareTo(Event other) {
        if (this.numUpvotes > other.getNumUpvotes()) {
            return -1;
        } else if (this.numUpvotes < other.getNumUpvotes()) {
            return 1;
        } else {
            return 0;
        }
    }
}
