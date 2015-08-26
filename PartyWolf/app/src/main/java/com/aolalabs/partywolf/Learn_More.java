package com.aolalabs.partywolf;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ViewFlipper;

public class Learn_More extends Activity implements
        GestureDetector.OnGestureListener {

    private GestureDetectorCompat mDetector;

    Button buttonPrev, buttonNext;
    ViewFlipper viewFlipper;

    Animation slide_in_left, slide_out_right;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn_more);

        mDetector = new GestureDetectorCompat(this,this);

        viewFlipper = (ViewFlipper) findViewById(R.id.viewflipper);

        slide_in_left = AnimationUtils.loadAnimation(this,
                android.R.anim.slide_in_left);
        slide_out_right = AnimationUtils.loadAnimation(this,
                android.R.anim.slide_out_right);


        //viewFlipper.setInAnimation(slide_in_left);
        //viewFlipper.setOutAnimation(slide_out_right);

        ;

    }

    public void login(View view){
        Intent intent = new Intent(this, Login.class);
        startActivity(new Intent(intent));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.mDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent arg0) {
        return false;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                           float velocityY) {
        float sensitvity = 50;

        if((e1.getX() - e2.getX()) > sensitvity){

        }

        else if((e2.getX() - e1.getX()) > sensitvity){
            viewFlipper.setInAnimation(slide_in_left);
            viewFlipper.setOutAnimation(slide_out_right);
            viewFlipper.showNext();
        }

        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                            float distanceY) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

}