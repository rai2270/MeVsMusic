package mvm.flying;

import mvm.settings.GameFileIO;
import mvm.settings.HighScore;

import r.RActivity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;


import mvm.flying.R;

import com.un4seen.bass.BASS;

public class FlyingActivity extends RActivity implements SensorEventListener, OnTouchListener { 
	
	private final static int JOYSTICK_SIZE = 180;// dpad is 180; dpad2 is 128; dpad3 is 180
	private final static int JOYSTICK_SIZE_HALF = JOYSTICK_SIZE / 2;
	
	int mScreenSize_x;
	int mScreenSize_y;
	
	protected PowerManager.WakeLock wakeLock;
	
	private FlyingRenderer mRenderer;
	private boolean bUseSensor = true;
	private SensorManager mSensorManager;
	private Display mDisplay;
	private float mGravity[];
	private static final float ALPHA = 0.15f;
	private static final int SENSITIVITY = 10;
	//private TextView mFPSLabel;
	private TextView mScoreLabel;
	//private DecimalFormat mDecFormat;
	private Point mScreenSize;
	private ImageView mJoystick;
	
	String mTitle;
	String mFileName;
	boolean bAccelerometer;
	
	private ImageView mLoaderGraphic;
	
	LinearLayout mPleaseWaitLL;
	TextView mPleaseWaitLabel;
	ImageView mPleaseWaitImage;
	
	LinearLayout mShipLL;	
	LinearLayout rlShip;
	ImageView[] mShip;
	
	LinearLayout mGameLL;
	LinearLayout mCountDownLL;
	LinearLayout mScoreLL;
	LinearLayout mGameOverLL;
	TextView mCountDownLabel;
	TextView mTracknameLabel;
	TextView mTrackscoreLabel;
	TextView mGameoverLabel;
	//TextView mGameoverFreeLabel;
	//ImageButton buyButton2;
	String mSaveTitle;
	long mSaveScore;
	
	GameFileIO fileIO;
	
	float spCountDown;
	float spPleaseWait;
	float spGameOver;
	float spScore;
	float spTrackName;
	//float spBuy;
	
	
	TableLayout mGameOverTable;  
	TableRow mGameOverRowTitle;  
	TableRow rowFirstLabels;
	TableRow rowSecondLabel;
	TableRow rowThirdLabel;
	TextView mTop3title;
	TextView firstLabel;
	TextView name1Label;
	TextView name1ScoreLabel;
	TextView secondLabel;
	TextView name2Label;
	TextView name2ScoreLabel; 
	TextView thirdLabel;
	TextView name3Label;
	TextView name3ScoreLabel; 
	
	private static Handler mHandler = new Handler();
	
	//boolean bRenderStarted = false;
	
	//boolean bExit = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		//bStartRender = true;
		//http://stackoverflow.com/questions/5685107/android-system-kill-my-serversocket-in-service
        // http://developer.android.com/reference/android/os/PowerManager.html
        final PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "MeVsMusicFlyingActivity");
        //wakeLock.acquire();
        
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		// Initialize OpenGL compatibility layer to prevent context errors
		OpenGLCompat.init(this);
		
		mMultisamplingEnabled = false;
		super.onCreate(savedInstanceState);
		
		mTitle = getIntent().getExtras().getString("Title");
		mFileName = getIntent().getExtras().getString("FileName");
		if(getIntent().getExtras().getString("Accelerometer").equals("ON"))
		{
			bAccelerometer = true;
		}
		else
		{
			bAccelerometer = false;
		}
		mRenderer = new FlyingRenderer(this, mTitle, mFileName, bAccelerometer);
		mRenderer.setSurfaceView(mSurfaceView);
		mRenderer.setUsesCoverageAa(mUsesCoverageAa);
		//mRenderer.setFPSUpdateListener(this);
		super.setRenderer(mRenderer);

		mDisplay = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
		mScreenSize_x = mDisplay.getWidth();
		mScreenSize_y = mDisplay.getHeight();
		spCountDown = (float)(0.2*mScreenSize_x);
		spPleaseWait = (float)(0.07*mScreenSize_x);
		spGameOver = (float)(0.07*mScreenSize_x);
		spScore = (float)(0.05*mScreenSize_x);
		spTrackName = (float)(0.03*mScreenSize_x);
		//spBuy = (float)(0.06*mScreenSize_x);
		
		mGravity = new float[3];
		if(bUseSensor)
		{
			mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
			mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);
		}
		
		mShipLL = new LinearLayout(this);
		mShipLL.setOrientation(LinearLayout.VERTICAL);
		mShipLL.setGravity(Gravity.BOTTOM); // TOP
		rlShip = new LinearLayout(this);
		mShip = new ImageView[FlyingRenderer.MAX_NUM_SHIPS];
		for(int i=0;i<mShip.length;i++)
		{
			mShip[i] = new ImageView(this);
		}
		mShip[0].setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ship_lives1));
		mShip[1].setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ship_lives2));
		mShip[2].setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ship_lives3));
		mShip[3].setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ship_lives4));
		mShip[4].setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ship_lives5));
		
		mScoreLL = new LinearLayout(this);
		mScoreLL.setOrientation(LinearLayout.VERTICAL);
		mScoreLL.setGravity(Gravity.BOTTOM);
		mScoreLabel = new TextView(this);
		mScoreLabel.setTextColor(Color.parseColor("#9000bfff"));
		mScoreLabel.setGravity(Gravity.CENTER);
		mScoreLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, spScore);
		mScoreLabel.setHeight((int)(2f*spScore));
		mScoreLabel.setTypeface(null, Typeface.BOLD_ITALIC);
		mScoreLL.addView(mScoreLabel);
		
		mGameLL = new LinearLayout(this);
		mGameLL.setOrientation(LinearLayout.HORIZONTAL);
		mGameLL.setGravity(Gravity.BOTTOM);
		
		RelativeLayout rl = new RelativeLayout(this);
		mJoystick = new ImageView(this);
		mJoystick.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.dpad3));//.dpad2));//.dpad));
		mJoystick.setOnTouchListener(this);
		rl.addView(mJoystick);
		rl.setGravity(Gravity.LEFT);
		rl.setPadding(20, 0, 0, 10);
		mGameLL.addView(rl);

		//mFPSLabel = new TextView(this);
		//mFPSLabel.setTextSize(15);
		//mFPSLabel.setHeight(100);
		//mGameLL.addView(mFPSLabel);
		
		mCountDownLL = new LinearLayout(this);
		mCountDownLabel = new TextView(this);
		mCountDownLL.setOrientation(LinearLayout.VERTICAL);
		mCountDownLL.setGravity(Gravity.TOP);
		mCountDownLabel.setTextColor(Color.parseColor("#9000ffff"));
		mCountDownLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, spCountDown);
		mCountDownLabel.setGravity(Gravity.CENTER);
		mCountDownLabel.setHeight((int)(2f*spCountDown));
		mCountDownLabel.setTypeface(null, Typeface.BOLD_ITALIC);
		mCountDownLL.addView(mCountDownLabel);
		
		mSurfaceView.setOnTouchListener(this);
		
		Display display = getWindowManager().getDefaultDisplay();
		mScreenSize = new Point();
		mScreenSize.x = display.getWidth();
		mScreenSize.y = display.getHeight();
		
		//mDecFormat = new DecimalFormat( "#,###,###,##0.00" );
		
		fileIO = new GameFileIO(getExternalFilesDir(null));
				
		HighScore.load(fileIO);
		
		initLoader();
	}
	
	
	// Prevent screen rotation call onPause. works with android:configChanges="orientation|keyboardHidden" in manifest.
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);
	    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	}
	
	protected void initLoader() {
		mLoaderGraphic = new ImageView(this);
		
		mPleaseWaitLL = new LinearLayout(this);
		mPleaseWaitLabel = new TextView(this);
		mPleaseWaitImage = new ImageView(this);
		mPleaseWaitImage.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.loading));
		
		mGameOverLL = new LinearLayout(this);
		mTracknameLabel = new TextView(this);
		mTrackscoreLabel = new TextView(this);
		mGameoverLabel = new TextView(this);
		//mGameoverFreeLabel = new TextView(this);
		
		//buyButton2 = new ImageButton(this); 
		//buyButton2.setBackgroundResource(R.drawable.buy);
		//RelativeLayout.LayoutParams shareParams = new RelativeLayout.LayoutParams(
		//        LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		//buyButton2.setLayoutParams(shareParams);
		//buyButton2.setBackgroundColor(Color.TRANSPARENT);
				
		mGameOverTable = new TableLayout(this);  
	    mGameOverRowTitle = new TableRow(this);  
	    rowFirstLabels = new TableRow(this);
	    rowSecondLabel = new TableRow(this);  
	    rowThirdLabel = new TableRow(this);
	    mTop3title = new TextView(this);  
	    firstLabel = new TextView(this);
	    name1Label = new TextView(this);  
	    name1ScoreLabel = new TextView(this);
	    secondLabel = new TextView(this); 
	    name2Label = new TextView(this);  
	    name2ScoreLabel = new TextView(this);  
	    thirdLabel = new TextView(this);
	    name3Label = new TextView(this);  
	    name3ScoreLabel = new TextView(this); 
	    mPleaseWaitLL.setOrientation(LinearLayout.VERTICAL);
		mPleaseWaitLL.setGravity(Gravity.TOP);
		//mPleaseWaitLL.addView(mPleaseWaitImage);
		//mLayout.addView(mPleaseWaitLL);
	}
	
	/*public void hideLoader() {
		mLayout.post(new Runnable() {
			public void run() {
					mLayout.removeView(mPleaseWaitLL);
			}
		});
	}*/

	
	
	public void showLoader() {
		mLayout.post(new Runnable() {
			public void run() {
				try
				{
					mPleaseWaitLL.addView(mPleaseWaitImage);
					mLayout.addView(mPleaseWaitLL);
					
					//bStartRender = true;
					//mPleaseWaitLL.setOrientation(LinearLayout.VERTICAL);
					//mPleaseWaitLL.setGravity(Gravity.TOP);
					//mPleaseWaitLL.addView(mPleaseWaitImage);
					//mLayout.addView(mPleaseWaitLL);
					
					/*mLoaderGraphic.setId(1);
					mLoaderGraphic.setScaleType(ScaleType.CENTER);
					mLoaderGraphic.setImageResource(R.drawable.loader);
					mLayout.addView(mLoaderGraphic);
					
					AnimationSet animSet = new AnimationSet(false);
			
					RotateAnimation anim1 = new RotateAnimation(0, 360,
							Animation.RELATIVE_TO_SELF, .5f, Animation.RELATIVE_TO_SELF,
							.5f);
					anim1.setRepeatCount(Animation.INFINITE);
					anim1.setDuration(2000);
					animSet.addAnimation(anim1);
			
					AlphaAnimation anim2 = new AlphaAnimation(0, 1);
					anim2.setRepeatCount(0);
					anim2.setDuration(1000);
					animSet.addAnimation(anim2);
			
					mLoaderGraphic.setAnimation(animSet);*/
				}
				catch(Exception e)
				{
					
				}
			}
		});
	}

	public void hideLoader() {
		mLayout.post(new Runnable() {
			public void run() {
				try
				{
					mLayout.removeView(mPleaseWaitLL);
					mLayout.removeView(mLoaderGraphic);
					mLayout.addView(mGameLL);
					mLayout.addView(mCountDownLL);
					
					/*
					AlphaAnimation anim = new AlphaAnimation(1, 0);
					anim.setRepeatCount(0);
					anim.setDuration(500);
					anim.setAnimationListener(new AnimationListener() {
						public void onAnimationStart(Animation animation) {
						}
	
						public void onAnimationRepeat(Animation animation) {
						}
	
						public void onAnimationEnd(Animation animation) {
							mLoaderGraphic.setVisibility(View.INVISIBLE);
							mLayout.removeView(mLoaderGraphic);
							mLayout.addView(mGameLL);
							mLayout.addView(mCountDownLL);
						}
					});
					((AnimationSet) mLoaderGraphic.getAnimation())
							.addAnimation(anim);*/
				}
				catch(Exception e)
				{
					
				}
			}
		});
	}
	
	/*public void onFPSUpdate(final double fps) {
		mLayout.post(new Runnable() {
			public void run() {
				mFPSLabel.setText("FPS: " + mDecFormat.format(fps));
			}
		});
	}*/
	
	public void onStatusUpdate(final String msg, boolean countDown) {
		if(countDown)
		{
			mLayout.post(new Runnable() {
				public void run() {
					mCountDownLabel.setText(msg);
				}
			});
		}
		else
		{
			mLayout.post(new Runnable() {
				public void run() {
					mScoreLabel.setText(msg);
				}
			});
		}
	}
	
	public void onShipUpdate(final int shipCount) {
		mLayout.post(new Runnable() {
			public void run() {
				rlShip.removeAllViews();
				for(int i=0;i<shipCount;i++)
				{
					rlShip.addView(mShip[i]);
				}
			}
		});
	}
	
	public void removeCountDown() {
		mLayout.post(new Runnable() {
			public void run() {
				mCountDownLL.removeView(mCountDownLabel);
				mCountDownLL.setGravity(Gravity.BOTTOM);
				rlShip.setGravity(Gravity.RIGHT); // LEFT
				mShipLL.addView(rlShip);
				mLayout.addView(mShipLL);
				mLayout.addView(mScoreLL);
			}
		});
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {

	}

	@Override
    public boolean dispatchKeyEvent(KeyEvent event)
    {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			switch (event.getKeyCode()) {
		        case KeyEvent.KEYCODE_BACK:
		        	//Log.i("FlyingActivity", "dispatchKeyEvent - KEYCODE_BACK");
		        	finish();
		            return true;
		        /*case KeyEvent.KEYCODE_HOME:
		        	Log.i("FlyingActivity", "dispatchKeyEvent - KEYCODE_HOME");
		            return true;
		        case KeyEvent.KEYCODE_POWER:
		        	Log.i("FlyingActivity", "dispatchKeyEvent - KEYCODE_POWER");
		        	return true;*/
			}
		}
		return super.dispatchKeyEvent(event);
    }


	@Override
	public void onResume() {
		super.onResume();
		
		if (wakeLock != null && wakeLock.isHeld() == false)
    	{
        	wakeLock.acquire();
    	}
		
		if(bUseSensor)
		{
			mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);
		}
		
		//if(mRenderer!=null)
    	//	mRenderer.onResume();
	}
	
	@Override
	public void onPause() {
		if(mRenderer!=null)
    		mRenderer.onPause();
		
		BASS.BASS_RecordFree();
		
		//if(mRenderer!=null)
		//	mRenderer.onSurfaceDestroyed();
		
		//unbindDrawables(mLayout);
		
		//if(mRenderer!=null)
		//	mRenderer.onPause();
	
		//mLayout.removeAllViews();
	
		if (wakeLock != null && wakeLock.isHeld())
		{
			wakeLock.release();
		}
	
		if(bUseSensor)
		{
			mSensorManager.unregisterListener(this);
		}
		
		super.onPause();
		
		finish();
	}
	
	
	@Override
	public void onStop() {
		super.onStop();
				
		//if(bActivityStarted)
			//finish();
	
	}
	
	public boolean onTouch(View v, MotionEvent event) {
		if(v instanceof GLSurfaceView) {
			if(event.getX() > JOYSTICK_SIZE && event.getAction() == MotionEvent.ACTION_DOWN) 
			{
				//mRenderer.setTouch(true);
				mRenderer.setTouch(30f);//event.getX() / mScreenSize.x, 1.0f - (event.getY() / mScreenSize.y));
				return super.onTouchEvent(event);
			}
		} 
		else 
		{
			if(v instanceof ImageView) 
			{
				if(event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE)
				{
					mRenderer.setTouch();//false);
					//mRenderer.setTouch(event.getX() / mScreenSize.x, 1.0f - (event.getY() / mScreenSize.y));
					mRenderer.setShipVelocity((event.getX() - JOYSTICK_SIZE_HALF) / (float)JOYSTICK_SIZE_HALF, (event.getY() - JOYSTICK_SIZE_HALF) / (float)JOYSTICK_SIZE_HALF);
				}
				else if(event.getAction() == MotionEvent.ACTION_OUTSIDE || event.getAction() == MotionEvent.ACTION_UP)
				{
					mRenderer.addJoystickVelocity(false);
				}
				return true;
			}
		}
		return super.onTouchEvent(event);
	}
	
	@Override
	public void onDestroy() {
		if (wakeLock != null && wakeLock.isHeld())
    	{
        	wakeLock.release();
    	}
		
		BASS.BASS_Free();
		
		super.onDestroy();
		
		/*if(mRenderer!=null && mRenderer.mIsInitialized)
		{
			int pid = android.os.Process.myPid(); 
			android.os.Process.killProcess(pid);
		}*/
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			mGravity[0] = event.values[0] * ALPHA + mGravity[0] * (1.0f - ALPHA);
			mGravity[1] = event.values[1] * ALPHA + mGravity[1] * (1.0f - ALPHA);
			mGravity[2] = event.values[2] * ALPHA + mGravity[2] * (1.0f - ALPHA);

			switch (mDisplay.getRotation()) {
			case Surface.ROTATION_0:
				mRenderer.setAccelerometerValues(event.values[0] - mGravity[0] * SENSITIVITY, event.values[1] - mGravity[1] * SENSITIVITY, event.values[2]
						- mGravity[2] * SENSITIVITY);
				break;
			case Surface.ROTATION_90:
				mRenderer.setAccelerometerValues(event.values[1] - mGravity[1] * -SENSITIVITY, event.values[0] - mGravity[0] * SENSITIVITY, event.values[2]
						- mGravity[2] * SENSITIVITY);
				break;
			case Surface.ROTATION_180:
				mRenderer.setAccelerometerValues(event.values[0] - mGravity[0] * -SENSITIVITY, event.values[1] - mGravity[1] * -SENSITIVITY, event.values[2]
						- mGravity[2] * SENSITIVITY);
				break;
			case Surface.ROTATION_270:
				mRenderer.setAccelerometerValues(event.values[1] - mGravity[1] * SENSITIVITY, event.values[0] - mGravity[0] * -SENSITIVITY, event.values[2]
						- mGravity[2] * SENSITIVITY);
				break;
			}
		}
	}
	
	public void gameOverTime()
	{
		mHandler.post(new Runnable()
		{
			public void run() 
			{
				finish();
			}
		});
	}

	public void gameOverTop33Time()
	{
		mLayout.post(new Runnable() {
			public void run() {
				if(HighScore.mScore[2]==0)
					return;
				
				rowThirdLabel.setGravity(Gravity.CENTER_HORIZONTAL);  
			    
				thirdLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, spTrackName);
				thirdLabel.setTextColor(Color.parseColor("#37ff37"));
				thirdLabel.setText("3.");  
				thirdLabel.setTypeface(Typeface.DEFAULT_BOLD);  
			    
			    name3Label.setTextSize(TypedValue.COMPLEX_UNIT_PX, spTrackName);
			    name3Label.setTextColor(Color.parseColor("#37ff37"));
			    name3Label.setText(String.format("%" + HighScore.MAX_VIEW_TRACK + "s", HighScore.mViewTrack[2])); 
			    name3Label.setGravity(Gravity.LEFT);  
			    
			    name3ScoreLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, spTrackName);
			    name3ScoreLabel.setTextColor(Color.parseColor("#37ff37"));
			    name3ScoreLabel.setText(String.format("%12d", HighScore.mScore[2]));  
			    name3ScoreLabel.setGravity(Gravity.LEFT);  
			    
			    rowThirdLabel.addView(thirdLabel);  
			    rowThirdLabel.addView(name3Label);  
			    rowThirdLabel.addView(name3ScoreLabel); 
			    
			    mGameOverTable.addView(rowThirdLabel); 
				
				
			}
		});
	}
	
	public void gameOverTop32Time()
	{
		mLayout.post(new Runnable() {
			public void run() {
				if(HighScore.mScore[1]==0)
					return;
				
				rowSecondLabel.setGravity(Gravity.CENTER_HORIZONTAL);  
			    
				secondLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, spTrackName);
				secondLabel.setTextColor(Color.parseColor("#37ff37"));
				secondLabel.setText("2.");  
				secondLabel.setTypeface(Typeface.DEFAULT_BOLD);  
			    
			    name2Label.setTextSize(TypedValue.COMPLEX_UNIT_PX, spTrackName);
			    name2Label.setTextColor(Color.parseColor("#37ff37"));
			    name2Label.setText(String.format("%" + HighScore.MAX_VIEW_TRACK + "s", HighScore.mViewTrack[1])); 
			    name2Label.setGravity(Gravity.LEFT);  
			    
			    name2ScoreLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, spTrackName);
			    name2ScoreLabel.setTextColor(Color.parseColor("#37ff37"));
			    name2ScoreLabel.setText(String.format("%12d", HighScore.mScore[1]));  
			    name2ScoreLabel.setGravity(Gravity.LEFT);  
			    
			    rowSecondLabel.addView(secondLabel);  
			    rowSecondLabel.addView(name2Label);  
			    rowSecondLabel.addView(name2ScoreLabel); 
			    
			    mGameOverTable.addView(rowSecondLabel); 
			    
			    
			}
		});
	}
	
	public void gameOverTop31Time()
	{
		mLayout.post(new Runnable() {
			public void run() {
				if(HighScore.mScore[0]==0)
					return;
				
				rowFirstLabels.setGravity(Gravity.CENTER_HORIZONTAL);  
				
				firstLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, spTrackName);
				firstLabel.setTextColor(Color.parseColor("#37ff37"));
			    firstLabel.setText("1.");  
			    firstLabel.setTypeface(Typeface.DEFAULT_BOLD);  
			    
			    name1Label.setTextSize(TypedValue.COMPLEX_UNIT_PX, spTrackName);
			    name1Label.setTextColor(Color.parseColor("#37ff37"));
			    name1Label.setText(String.format("%" + HighScore.MAX_VIEW_TRACK + "s", HighScore.mViewTrack[0])); 
			    name1Label.setGravity(Gravity.LEFT);  
			    
			    name1ScoreLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, spTrackName);
			    name1ScoreLabel.setTextColor(Color.parseColor("#37ff37"));
			    name1ScoreLabel.setText(String.format("%12d", HighScore.mScore[0]));  
			    name1ScoreLabel.setGravity(Gravity.LEFT);  
			    
			    rowFirstLabels.addView(firstLabel);  
			    rowFirstLabels.addView(name1Label);  
			    rowFirstLabels.addView(name1ScoreLabel); 
			    
			    mGameOverTable.addView(rowFirstLabels); 
			    
				
			}
		});
	}
	
	public void gameOverTop3Time()
	{
		mLayout.post(new Runnable() {
			public void run() {
				if(HighScore.mScore[0]==0)
					return;
				
				mGameOverTable.setStretchAllColumns(true);  
			    mGameOverTable.setShrinkAllColumns(true);  
			  
			    mGameOverRowTitle.setGravity(Gravity.CENTER_HORIZONTAL);  
			    
			    
			    
			    // title 
			    mTop3title.setText("Top 3");  
			    mTop3title.setTextColor(Color.parseColor("#37ff37"));
			    mTop3title.setTextSize(TypedValue.COMPLEX_UNIT_PX, spTrackName);
			    mTop3title.setGravity(Gravity.CENTER);  
			    mTop3title.setTypeface(Typeface.SERIF, Typeface.BOLD);  
			  
			    TableRow.LayoutParams params = new TableRow.LayoutParams();  
			    params.span = 6;  
			  
			    mGameOverRowTitle.addView(mTop3title, params);
			    
			    mGameOverTable.addView(mGameOverRowTitle);
			    
			    mGameOverLL.addView(mGameOverTable);
			    
			    
			   
			}
		});
	}
	
	public void gameOverFreeVersionTime()
	{
		mLayout.post(new Runnable() {
			public void run() {
				/*mGameoverFreeLabel.setTextColor(Color.parseColor("#00ffff"));//Color.WHITE);
				mGameoverFreeLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, spBuy);
				mGameoverFreeLabel.setGravity(Gravity.CENTER);
				mGameoverFreeLabel.setText("Free-Version Time Limit Reached");
				mGameoverFreeLabel.setTypeface(null, Typeface.BOLD_ITALIC);
				mGameOverLL.addView(mGameoverFreeLabel);
			    
				
				
				buyButton2.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.buy));
				FrameLayout.LayoutParams shareParams = new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT,Gravity.CENTER);
				buyButton2.setLayoutParams(shareParams);
				buyButton2.setBackgroundColor(Color.TRANSPARENT);
				mGameOverLL.addView(buyButton2);
				
				buyButton2.setOnClickListener(new View.OnClickListener() {
		             public void onClick(View v) {
		            	 launchBrowser(v);
		             }
		         });*/
				
				
			}
		});
	}
	
	public void launchBrowser(View view) {
		Uri uriUrl = Uri.parse("http://mevsmusic.netau.net/play/");
		Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
        startActivity(launchBrowser);
    }
	
	/*public void searchBrowserGoogle(View view) {
        Intent search = new Intent(Intent.ACTION_WEB_SEARCH);
        search.putExtra(SearchManager.QUERY, "me vs music");
        startActivity(search);
    }*/
	
	public void gameOverTitleTime()
	{
		mLayout.post(new Runnable() {
			public void run() {
				mTracknameLabel.setTextColor(Color.parseColor("#37ff37"));
				mTracknameLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, spTrackName);
				mTracknameLabel.setGravity(Gravity.LEFT);
				mTracknameLabel.setText("  " + mSaveTitle);
				mTracknameLabel.setTypeface(null, Typeface.BOLD_ITALIC);
				mGameOverLL.addView(mTracknameLabel);
				
				mTrackscoreLabel.setTextColor(Color.parseColor("#37ff37"));
				mTrackscoreLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, spTrackName);
				mTrackscoreLabel.setGravity(Gravity.LEFT);
				mTrackscoreLabel.setText("  Score: " + mSaveScore);
				mTrackscoreLabel.setTypeface(null, Typeface.BOLD_ITALIC);
				mGameOverLL.addView(mTrackscoreLabel);
			}
		});
	}
	
	public void gameOverStringTime()
	{
		mLayout.post(new Runnable() {
			public void run() {
				mGameOverLL.setOrientation(LinearLayout.VERTICAL);
				mGameOverLL.setGravity(Gravity.TOP);
				
				mGameoverLabel.setTextColor(Color.parseColor("#00ffff"));//Color.WHITE);
				mGameoverLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, spGameOver);
				mGameoverLabel.setGravity(Gravity.CENTER);
				mGameoverLabel.setText("GAME OVER");
				mGameoverLabel.setTypeface(null, Typeface.BOLD_ITALIC);
				mGameOverLL.addView(mGameoverLabel);
				
				mLayout.removeView(mGameLL);
				mLayout.addView(mGameOverLL);
			}
		});
	}
	
	public void gameOverData(String title, long scoreCount)
	{
		mSaveTitle = title;
		mSaveScore = scoreCount;
		
		if(mSaveScore>=HighScore.mScore[0])
		{
			HighScore.mTrack[2] = HighScore.mTrack[1];
			HighScore.mScore[2] = HighScore.mScore[1];
			
			HighScore.mTrack[1] = HighScore.mTrack[0];
			HighScore.mScore[1] = HighScore.mScore[0];
			
			HighScore.mTrack[0] = mSaveTitle;
			HighScore.mScore[0] = mSaveScore;
			
			HighScore.save(fileIO);
			return;
		}
		
		if(mSaveScore>=HighScore.mScore[1])
		{
			HighScore.mTrack[2] = HighScore.mTrack[1];
			HighScore.mScore[2] = HighScore.mScore[1];
			
			HighScore.mTrack[1] = mSaveTitle;
			HighScore.mScore[1] = mSaveScore;
			
			HighScore.save(fileIO);
			return;
		}
		
		if(mSaveScore>=HighScore.mScore[2])
		{
			HighScore.mTrack[2] = mSaveTitle;
			HighScore.mScore[2] = mSaveScore;
			
			HighScore.save(fileIO);
			return;
		}
				
		
	}
	
}