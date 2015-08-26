package com.aolalabs.partywolf;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;


public class Hype extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hype);

        /*ParseQueryAdapter mainAdapter = new ParseQueryAdapter<ParseObject>(this,"Event");
        mainAdapter.setTextKey("title");
        mainAdapter.loadObjects();

        ParseQuery<ParseObject> queryEvent = ParseQuery.getQuery("Event");
        queryEvent.orderByDescending("upvotes");
        queryEvent.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> events, ParseException e) {
                if (e == null) {


                    }

                    LinearLayout parent = (LinearLayout) (R.id.second_layout);
                    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    for(int i = 0; i <events.size(); i++) {
                        View hypeCustom = inflater.inflate(R.layout.hype_custom, null);
                        TextView upvoteText = (TextView) findViewById(R.id.black_upvotes);
                        TextView titleText = (TextView) findViewById(R.id.title_text);
                        TextView descriptionText = (TextView) findViewById(R.id.description_text);
                        TextView dateText = (TextView) findViewById(R.id.date_text);
                        TextView emojiText = (TextView) findViewById(R.id.emoji_text);

                        upvoteText.append(events.get(i).getString("upvotes"));
                        titleText.append(events.get(i).getString("title"));
                        descriptionText.append(events.get(i).getString("description"));
                        dateText.append(events.get(i).getString("date"));
                        emojiText.append(events.get(i).getString("emoji"));

                        parent.addView(hypeCustom);
                    }
                    setContentView(R.layout.activity_hype);

                else {
                    StringBuilder badMessage = new StringBuilder(getResources().getString(R.string.failure));
                    Toast.makeText(Hype.this, badMessage.toString(), Toast.LENGTH_LONG).show();
                }
            }
        });*/
    }


    public void openAdd(View view){
        Intent intent = new Intent(this, Add.class);
        startActivity(intent);
    }

    public void openSettings(View view){
        Intent intent = new Intent(this, Settings.class);
        startActivity(intent);
    }

    public void openDate(View view){
        Intent intent = new Intent(this, Date.class);
        startActivity(intent);
    }
}