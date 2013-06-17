package com.janustech.picalc;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.janustech.picalc.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class FullscreenActivity extends Activity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;
    
    private static final int CALC_PI = 1;
    private static final int TIMER_FREQ = 1000;
    
    private static final BigDecimal TWO = new BigDecimal("2");
    private static final BigDecimal FOUR = new BigDecimal("4");
    private static final BigDecimal FIVE = new BigDecimal("5");
    private static final BigDecimal TWO_THIRTY_NINE = new BigDecimal("239");
    
    private static BigDecimal arccot(BigDecimal x, int numDigits) {
    	 
        BigDecimal unity = BigDecimal.ONE.setScale(numDigits,
          RoundingMode.DOWN);
        BigDecimal sum = unity.divide(x, RoundingMode.DOWN);
        BigDecimal xpower = new BigDecimal(sum.toString());
        BigDecimal term = null;
     
        boolean add = false;
     
        for (BigDecimal n = new BigDecimal("3"); term == null ||
          term.compareTo(BigDecimal.ZERO) != 0; n = n.add(TWO)) {
     
          xpower = xpower.divide(x.pow(2), RoundingMode.DOWN);
          term = xpower.divide(n, RoundingMode.DOWN);
          sum = add ? sum.add(term) : sum.subtract(term);
          add = ! add;
        }
        return sum;
      }
    
    //Message handler class that lets us run a calculation on a timer
    static class MsgHandler extends Handler {
    	private int digit = 1;
    	private BigDecimal pi = new BigDecimal(0);
    	
    	//Pi calculating code cribbed from
    	//http://en.literateprograms.org/Pi_with_Machin's_formula_(Java)
		public static BigDecimal pi(int numDigits) {
		  
		  int calcDigits = numDigits + 10;
		        
		  return FOUR.multiply((FOUR.multiply(arccot(FIVE, calcDigits)))
		      .subtract(arccot(TWO_THIRTY_NINE, calcDigits)))
		      .setScale(numDigits, RoundingMode.DOWN);
		}
    	
    	@Override
        public void handleMessage(Message msg) {
        	Log.i("handleMessage", "called");
        	if(msg.what == CALC_PI) {
        		Log.i("handleMessage", "CALC_PIC message");
        		if(msg.arg1 == 1) {
        			//TODO - Do calculation here
        			pi = pi(digit);
        			Log.i("calcPi", pi.toString());
        			
        			digit++;
        			
        			Message msgNew = new Message();
        			msgNew.copyFrom(msg);
        			this.sendMessageDelayed(msgNew, TIMER_FREQ);
        		}else {
        			Log.i("handleMessage", "Not sending a new message");
        		}
        	}
        }
    }
    private MsgHandler mHandler = new MsgHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        final View contentView = findViewById(R.id.fullscreen_content);

        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider
                .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
                    // Cached values.
                    int mControlsHeight;
                    int mShortAnimTime;

                    @Override
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
                    public void onVisibilityChange(boolean visible) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                            // If the ViewPropertyAnimator API is available
                            // (Honeycomb MR2 and later), use it to animate the
                            // in-layout UI controls at the bottom of the
                            // screen.
                            if (mControlsHeight == 0) {
                                mControlsHeight = controlsView.getHeight();
                            }
                            if (mShortAnimTime == 0) {
                                mShortAnimTime = getResources().getInteger(
                                        android.R.integer.config_shortAnimTime);
                            }
                            controlsView.animate()
                                    .translationY(visible ? 0 : mControlsHeight)
                                    .setDuration(mShortAnimTime);
                        } else {
                            // If the ViewPropertyAnimator APIs aren't
                            // available, simply show or hide the in-layout UI
                            // controls.
                            controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
                        }

                        if (visible && AUTO_HIDE) {
                            // Schedule a hide().
                            delayedHide(AUTO_HIDE_DELAY_MILLIS);
                        }
                    }
                });

        // Set up the user interaction to manually show or hide the system UI.
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TOGGLE_ON_CLICK) {
                    mSystemUiHider.toggle();
                } else {
                    mSystemUiHider.show();
                }
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
        
        final Button button = (Button) findViewById(R.id.dummy_button);
        button.setText("Start");
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	//Toggle our button between Start/Stop
            	if(button.getText() == "Start") {
            		button.setText("Stop");
            		sendStartCalcMessage();
            	}else {
            		button.setText("Start");
            		sendStopCalcMessage();
            	}
            }
        });
    }
    
    private void sendStartCalcMessage() {
    	sendCalcMessage(1);
    }
    
    private void sendStopCalcMessage() {
    	sendCalcMessage(0);
    }
    
    private void sendCalcMessage(int arg1) {
        if(arg1 == 1) {
        	//We're starting so kick off the first delayed message
        	Message msg = new Message();
            msg.what = CALC_PI;
            msg.arg1 = arg1; //Started
            
        	mHandler.sendMessageDelayed(msg, TIMER_FREQ);
        }else {
        	//We're stopping so remove any existing messages in the queue
        	mHandler.removeMessages(CALC_PI);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }


    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}
