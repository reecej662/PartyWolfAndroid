package com.aolalabs.partywolf;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.victor.loading.rotate.RotateLoading;

/**
 * Created by reecejackson on 8/28/15.
 */
public class Report {
    private String message = "";
    private ParseObject post;
    private ParseUser user;
    private Activity context;
    private Dialog completionDialog;

    public Report(String message, ParseObject post, Activity context) {
        this.message = message;
        this.post = post;
        this.user = ParseUser.getCurrentUser();
        this.context = context;
    }

    public void submit(){

        if(this.message.equals("") || this.post == null || this.user == null) {
            Log.d("Report error", "More information needed");
            Toast errorMessage = Toast.makeText(this.context, "Error in report: more information needed", Toast.LENGTH_LONG);
            errorMessage.show();
            return;
        }

        completionDialog = getCompletionDialogue();
        completionDialog.show();

        ParseObject report = new ParseObject("Reports");
        report.put("post", this.post);
        report.put("type", this.message);
        report.put("user", this.user);

        report.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e != null)
                    Log.d("Report saving error", e.toString());
                Log.d("Report", "Report submitted");
                completionDialog.dismiss();
            }
        });

    }

    public Dialog getCompletionDialogue(){
        Dialog completionDialog = new Dialog(this.context);
        completionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        completionDialog.setContentView(R.layout.loading_dialog);
        RotateLoading rotateLoading = (RotateLoading) completionDialog.findViewById(R.id.rotateloading);
        rotateLoading.start();

        final Window window = completionDialog.getWindow();
        window.setLayout(500, 500);
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        completionDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        return completionDialog;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ParseObject getPost() {
        return post;
    }

    public void setPost(ParseObject post) {
        this.post = post;
    }

    public ParseUser getUser() {
        return user;
    }

    public void setUser(ParseUser user) {
        this.user = user;
    }

    public Activity getContext() {
        return context;
    }

    public void setContext(Activity context) {
        this.context = context;
    }

    public Dialog getCompletionDialog() {
        return completionDialog;
    }

    public void setCompletionDialog(Dialog completionDialog) {
        this.completionDialog = completionDialog;
    }
}
