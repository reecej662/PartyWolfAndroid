package com.aolalabs.partywolf;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by reecejackson on 9/3/15.
 */
public class resizeTest extends Activity {
    private TextView text1;
    private CustomEditText text2;
    private EditText text3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.resize_test);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        text1 = (TextView) findViewById(R.id.text1);
        text2 = (CustomEditText) findViewById(R.id.text2);
        text3 = (EditText) findViewById(R.id.text3);

        System.out.println("Text1 is selected: " + text1.isSelected());
        System.out.println("Text2 is selected: " + text2.isActivated());
        System.out.println("Text3 is selected: " + text3.isActivated());

        text2.setBackListener(new CustomEditText.KeyboardCloseListener() {
            @Override
            public void onKeyboardClosed() {
                text1.setText("Keyboard close");
            }
        });

        text2.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                System.out.println("Has focus: " + hasFocus);
                System.out.println(resizeTest.this.getWindow().getDecorView().getHeight());
                if(hasFocus) {
                    ViewGroup.LayoutParams params = v.getLayoutParams();
                    params.height = 100;
                    v.setLayoutParams(params);
                } else {
                    ViewGroup.LayoutParams params = v.getLayoutParams();
                    params.height = 450;
                    v.setLayoutParams(params);
                    text1.setText("Text 1");
                }
            }
        });


    }

    public void testButton(View v) {
        Toast.makeText(this, "Button pressed", Toast.LENGTH_LONG).show();
    }

}
