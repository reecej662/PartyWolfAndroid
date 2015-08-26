package com.aolalabs.partywolf;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

/**
 * Created by reecejackson on 8/21/15.
 */
public class FacebookFriendsView extends RelativeLayout {
    private ParseUser friend;
    private ImageView profilePicture;
    private TextView name;

    public FacebookFriendsView(Context context, ParseUser friend){
        super(context);
        initViews();
        this.friend = friend;
        setContent();
    }

    public FacebookFriendsView(Context context, AttributeSet attrs, int defStyle, ParseUser friend) {
        super(context, attrs, defStyle);
        initViews();
        setContent();
    }


    private void initViews() {
        inflate(getContext(), R.layout.facebook_friend, this);
        this.profilePicture = (ImageView) findViewById(R.id.facebook_friend_profile_picture);
        this.name = (TextView) findViewById(R.id.facebook_friend_name);
    }

    private void setContent() {
        // Set the profile picture
        try {
            ParseFile profilePictureFile = (ParseFile) friend.get("profile_pic");

            profilePictureFile.getDataInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] data, ParseException e) {

                    if (e == null)
                    {
                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                        RoundImage roundImage = new RoundImage(bmp);
                        profilePicture.setImageDrawable(roundImage);
                    }
                    else
                    {
                        // Set a default profile picture
                    }

                }
            });
        } catch (Exception e) {
            System.err.println(e);
        }

        // Set the name
        name.setText(friend.getString("first_name"));
    }

    public ParseUser getFriend() {
        return friend;
    }

    public ImageView getProfilePicture() {
        return profilePicture;
    }

    public TextView getName() {
        return name;
    }

    public void setFriend(ParseUser friend) {
        this.friend = friend;
    }

    public void setProfilePicture(ImageView profilePicture) {
        this.profilePicture = profilePicture;
    }

    public void setName(TextView name) {
        this.name = name;
    }
}
