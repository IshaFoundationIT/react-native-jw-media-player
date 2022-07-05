package com.appgoalz.rnjwplayer;


import android.content.Context;
import android.content.res.Configuration;
import android.view.KeyEvent;

import android.view.View;

import com.jwplayer.pub.view.JWPlayerView;
import android.util.Log;

import android.view.ViewGroup;
import android.widget.ImageView;
import com.jwplayer.ui.views.CueMarkerSeekbar;


public class RNJWPlayer extends JWPlayerView {
    public Boolean fullScreenOnLandscape = false;
    public Boolean exitFullScreenOnPortrait = false;

    // Changed constructor to accept boolean for hideSeekbarAndForwardConrol
    public RNJWPlayer(Context var1, Boolean hideSeekbarAndForwardConrol) {
        super(var1);
        Log.d("RNJWPlayer", "hideSeekbarAndForwardConrol " + hideSeekbarAndForwardConrol);
        if(hideSeekbarAndForwardConrol){
            hideSeekbarAndForwardControl();
        }
    }

    private void hideSeekbarAndForwardControl() {
        try{
            ImageView forwardCenterControl = findViewById(R.id.center_forward_btn);
            /*  Grab forward control by id and change its width and height 
                Since 0dp wasn't hiding the image as per docs instead rendering a stretched image,
                changing width/height to negative value fixed it.
                
                Also setting visibility to INVISIBLE didn't work as somehow visibility was getting reset
            */
            forwardCenterControl.getLayoutParams().height = -1000;
            forwardCenterControl.getLayoutParams().width = -1000;
            forwardCenterControl.setImageDrawable(null);

            // Remove seekbar from parent view group to hide it.
            CueMarkerSeekbar seekBar = findViewById(R.id.controlbar_seekbar);
            ViewGroup parent = (ViewGroup) seekBar.getParent();
            if (parent != null) {
                Log.i("RNJWPlayer", "Removing seekbar view");
                parent.removeView(seekBar);
            }
        }
        catch(Exception e){
            Log.d("RNJWPlayer constructor", "Exception: " + e.getMessage());
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // Exit fullscreen or perform the action requested
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && this.getPlayer().getFullscreen()) {
            if (event.getAction() == KeyEvent.ACTION_UP) {
                this.getPlayer().setFullscreen(false,false);
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void requestLayout() {
        super.requestLayout();

        // The spinner relies on a measure + layout pass happening after it calls requestLayout().
        // Without this, the widget never actually changes the selection and doesn't call the
        // appropriate listeners. Since we override onLayout in our ViewGroups, a layout pass never
        // happens after a call to requestLayout, so we simulate one here.
        post(measureAndLayout);  
    }


    private final Runnable measureAndLayout = new Runnable() {
        @Override
        public void run() {
            measure(
                    MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.EXACTLY));
            layout(getLeft(), getTop(), getRight(), getBottom());
        }
    };

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (fullScreenOnLandscape) {
                this.getPlayer().setFullscreen(true,true);
            }
        } else if (newConfig.orientation==Configuration.ORIENTATION_PORTRAIT) {
            if (exitFullScreenOnPortrait) {
                this.getPlayer().setFullscreen(false,false);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && this.getPlayer().getFullscreen()) {
            this.getPlayer().setFullscreen(false,false);
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}