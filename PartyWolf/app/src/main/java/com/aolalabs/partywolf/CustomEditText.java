package com.aolalabs.partywolf;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

/**
 * Created by reecejackson on 9/4/15.
 */
public class CustomEditText extends EditText{
    private KeyboardCloseListener listener;

    public CustomEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

    }

    public CustomEditText(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public CustomEditText(Context context) {
        super(context);

    }

    public KeyboardCloseListener getBackListener() {
        return listener;
    }

    public void setBackListener(KeyboardCloseListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            dispatchKeyEvent(event);
            //System.out.println("I'm on to something");
            if(listener != null){
                listener.onKeyboardClosed();
            }
            return false;
        }
        return super.onKeyPreIme(keyCode, event);
    }

    public interface KeyboardCloseListener {
        void onKeyboardClosed();
    }

}
