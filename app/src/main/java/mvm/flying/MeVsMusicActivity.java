package mvm.flying;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import mvm.settings.GameFileIO;
import mvm.settings.GameSettings;
import android.Manifest;
import android.app.ListActivity;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

public class MeVsMusicActivity extends ListActivity {
	
	/** The view to show the ad. */
	//private AdView adView;
    
	private static final String[] EXTENSIONS_NAMES = { "mp3", "wav", "ogg", "aiff", "mp2" };
	
	private List<String> mTracks = new ArrayList<String>();     
	private List<String> mTrackNames = new ArrayList<String>(); //Playable Track Titles
	
	
	public static String DEMO_TRACK1 = "Sunset.mp3";
	public static String DEMO_TRACK2 = "FeelsGood2B.mp3";
	public static String DEMO_TRACK3 = ".. (pick a song from my device)";

	private static final int REQUEST_PICK_AUDIO = 1;
	private static final int REQUEST_AUDIO_PERMISSION = 2;
	private boolean bAudioPermissionRequested = false;
	
	
	//boolean bAccelerometer = false;
	
	//ImageButton imageButton;
	
	//ImageButton settingsImageButton;
	
	ImageView introImage;
	
	int NUM_TOAST = 6;
	Toast[] toast = new Toast[NUM_TOAST];
	
	boolean bStartActivityAllowed = false;
	
	//ListTask listTask;
	
	GameFileIO fileIO;
	
	private static Handler mHandler = new Handler();
    
	public void onCreate(Bundle savedInstanceState) {
		//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
	    // Create an ad.
	    //adView = new AdView(this, AdSize.BANNER, "MYADID");

	    // Add the AdView to the view hierarchy. The view will have no size
	    // until the ad is loaded.
	    //LinearLayout layout = (LinearLayout) findViewById(R.id.main);
	    //layout.addView(adView);

	    // Create an ad request. Check logcat output for the hashed device ID to
	    // get test ads on a physical device.
	    //AdRequest adRequest = new AdRequest();
	    //adRequest.addTestDevice(AdRequest.TEST_EMULATOR);

	    // Start loading the ad in the background.
	   // adView.loadAd(adRequest);

		
		/*
		//LinearLayout mGameLL;
		//mGameLL = new LinearLayout(this);
		//mGameLL.setOrientation(LinearLayout.HORIZONTAL);
		//mGameLL.setGravity(Gravity.BOTTOM);
		LinearLayout mSettingsLL = new LinearLayout(this);
		mSettingsLL.setOrientation(LinearLayout.VERTICAL);
		mSettingsLL.setGravity(Gravity.TOP);
		settingsImageButton.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.settings));
		FrameLayout.LayoutParams shareParams = new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT,Gravity.CENTER);
		settingsImageButton.setLayoutParams(shareParams);
		settingsImageButton.setBackgroundColor(Color.TRANSPARENT);
		mSettingsLL.addView(settingsImageButton);
		*/
		
		
		/*settingsImageButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
            	 openOptionsMenu();
             }
         });*/
		
		/*
		<ImageButton
        android:id="@+id/imageSettingsButton"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="left"
        android:background="@null"
        android:padding="4dp"
        android:src="@drawable/settings" />
         
		settingsImageButton = (ImageButton) findViewById(R.id.imageSettingsButton);
		settingsImageButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
           	 openOptionsMenu();
            }
 
		});*/
		
		/*final Button button = (Button) findViewById(R.id.buybutton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	Uri uriUrl = Uri.parse("http://mevsmusic.netau.net/play/");
        		Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
                startActivity(launchBrowser);
            }
        });*/
		
		/*
		imageButton = (ImageButton) findViewById(R.id.imageBuyButton);
		imageButton.setOnClickListener(new OnClickListener() {
 			@Override
			public void onClick(View arg0) {
				Uri uriUrl = Uri.parse("http://mevsmusic.netau.net/play/");
        		Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
                startActivity(launchBrowser);
			}
 
		});*/
		
		
		introImage = (ImageView) findViewById(R.id.imageView1);
		introImage.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				openOptionsMenu();
            }
 		});
		
		for(int i=0;i<NUM_TOAST;i++)
		{
			toast[i] = new Toast(this);
		}
		
		int duration = Toast.LENGTH_SHORT;
		toast[0] = Toast.makeText(getBaseContext(), "SD Card is either mounted elsewhere or is unusable", duration);
		toast[1] = Toast.makeText(getBaseContext(), "Loading ...", duration);
		toast[2] = Toast.makeText(getBaseContext(), "Loaded xxx Tracks", duration);
		toast[3] = Toast.makeText(getBaseContext(), "Track lists are empty OR No playable tracks found", duration);
		toast[4] = Toast.makeText(getBaseContext(), "Please add music to your phone's music folder", duration);
		toast[5] = Toast.makeText(getBaseContext(), "File Type Not Supported", duration);
        
		
		fileIO = new GameFileIO(getExternalFilesDir(null));
		
		GameSettings.load(fileIO);
		
		//loadList();
		
		//bAccelerometer = GameSettings.bAccelerometer;
				
        //loadList();
		
		/*
		 <TextView
        android:id="@+id/textView1"
        android:layout_width="fill_parent"
        android:layout_height="wrap_content"
        android:gravity="center"
        android:text="@string/sb_link" /> 
		 
		 */
		/*
		TextView linkView = (TextView)findViewById(R.id.textView1);
		linkView.setPadding(10, 0, 10, 20);
		linkView.setTextSize(20);
		linkView.setText(getString(R.string.sb_url));
		linkView.setTextColor(0xaaffffff);
		linkView.setLinkTextColor(0xaaffffff);
		
		Linkify.addLinks(linkView, Linkify.WEB_URLS);
		
		Typeface font=Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");
		linkView.setTypeface(font);*/
	
	
	    
	}
	
	@Override
	public void onResume() {
		/*if(listTask!=null)
		{
			try {
				listTask.cancel(true);
			} catch (Exception e) {
			}
		}*/
		
		try
		{
			loadList();
		} 
		catch (Exception e) 
		{
		}
		
		super.onResume();
	}
	
	@Override
	public void onPause() {
		/*if(listTask!=null)
		{
			try {
				listTask.cancel(true);
			} catch (Exception e) {
			}
		}*/
		
		for(int i=0;i<NUM_TOAST;i++)
		{
			try {
				toast[i].cancel();
			} catch (Exception e) {
			}
		}
		
		super.onPause();
	}
	
	@Override
	public void onDestroy() {
		// Destroy the AdView.
	    //if (adView != null) {
	    //  adView.destroy();
	    //}

	    super.onDestroy();
	}

	
	/*
	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
	    super.onConfigurationChanged(newConfig);
	    // Checks the orientation of the screen
	    if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
	    	setContentView(R.layout.main);//setContentView(R.layout.mainWithlessIntoSize);
	    	//Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show(); 
	    } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
	    	setContentView(R.layout.main);
	    	//Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show(); 
	    }
	} */
	
	private String audioPermission()
	{
		return Build.VERSION.SDK_INT >= 33 ? Manifest.permission.READ_MEDIA_AUDIO : Manifest.permission.READ_EXTERNAL_STORAGE;
	}

	private boolean hasAudioPermission()
	{
		return Build.VERSION.SDK_INT < 23 || checkSelfPermission(audioPermission()) == PackageManager.PERMISSION_GRANTED;
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		if(requestCode == REQUEST_AUDIO_PERMISSION && hasAudioPermission())
		{
			loadList();
		}
	}

	private void loadList()
	{
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) || Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED_READ_ONLY)){
		} else{
			toast[0].show();
		}

		if(Build.VERSION.SDK_INT >= 23 && !hasAudioPermission() && !bAudioPermissionRequested)
		{
			bAudioPermissionRequested = true;
			requestPermissions(new String[]{audioPermission()}, REQUEST_AUDIO_PERMISSION);
		}

		toast[1].show();
		
		//listTask = new ListTask();
		//listTask.execute();
		
		try {
			FindTracks();
		} catch (Exception e) {
			
		}
		
		listReady();
	}
	/*
	private class ListTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
		    try
			{
		    	FindTracks();
		    	publishProgress();
			}
			catch (Exception consumed) {
	        }
		    return null;
        }
        
        protected void onProgressUpdate(Void... params) {
        	try
        	{
        		listReady();
	        }
			catch (Exception consumed) {
	        }
        }
	}*/
	
	private void listReady()
	{
		//mHandler.post(new Runnable()
		//{
			//public void run() 
			//{
				setList();
				toast[2].setText("Loaded " + Integer.toString(mTrackNames.size()) + " Tracks");
				toast[2].show();
				if(mTracks.size()==1){
					toast[3].show();
					toast[4].show();
				}
			//}
		//});
	}
	
	protected void setList() {
		setListAdapter(new ExamplesAdapter(this, mTrackNames));
	}

	
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu){
		
    	
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.sb_main_menu, menu);   
    	
    	
    	
    	return true;
    }
	
	@Override
    public boolean onPrepareOptionsMenu(Menu menu){
		
    	
    	MenuItem item = menu.findItem(R.id.accelerometer);
    	if(GameSettings.bAccelerometer==false)
		{
			item.setTitle(R.string.accelerometeroff);
		}
		else
		{
			item.setTitle(R.string.accelerometeron);
		}
    	
    	return true;
    }
	 
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();

		if (itemId == R.id.refresh) {
			try {
				loadList();
			} catch (Exception e) {
				// Handle exception
			}
			return true;
		} else if (itemId == R.id.choose) {
			OpenClicked(null);
			return true;
		} else if (itemId == R.id.accelerometer) {
			GameSettings.bAccelerometer = !GameSettings.bAccelerometer;
			
			try {
				GameSettings.save(fileIO);
			} catch (Exception e) {
				// Handle exception
			}
			
			if (GameSettings.bAccelerometer == false) {
				item.setTitle(R.string.accelerometeroff);
			} else {
				item.setTitle(R.string.accelerometeron);
			}
			return true;
		} else if (itemId == R.id.info) {
			//Uri uriUrl = Uri.parse("http://mevsmusic.netau.net/");
			Uri uriUrl = Uri.parse("http://mevsmusic.netau.net/m/"); //For Google Play
			Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
			startActivity(launchBrowser);
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}
	
	public void OpenClicked(View v) {
		// System audio picker: works on every Android version without storage permissions.
		Intent intent = new Intent(Build.VERSION.SDK_INT >= 19 ? Intent.ACTION_OPEN_DOCUMENT : Intent.ACTION_GET_CONTENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("audio/*");
		try
		{
			startActivityForResult(intent, REQUEST_PICK_AUDIO);
		}
		catch(ActivityNotFoundException e)
		{
			toast[5].show();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode != REQUEST_PICK_AUDIO || resultCode != RESULT_OK || data == null || data.getData() == null)
			return;

		Uri uri = data.getData();
		String title = queryDisplayName(uri);
		if(title == null || !isSupportedType(title))
		{
			toast[5].show();
			return;
		}

		StartFlyingActivity(title, uri.toString());
	}

	private String queryDisplayName(Uri uri)
	{
		Cursor c = getContentResolver().query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null);
		if(c != null)
		{
			try
			{
				if(c.moveToFirst() && c.getString(0) != null)
					return c.getString(0);
			}
			finally
			{
				c.close();
			}
		}
		return uri.getLastPathSegment();
	}

	private boolean isSupportedType(String fileName)
	{
		String ext = fileName.substring(fileName.lastIndexOf(".") + 1);
		for(int j=0;j<EXTENSIONS_NAMES.length;j++)
		{
			if(ext.equalsIgnoreCase(EXTENSIONS_NAMES[j]))
				return true;
		}
		return false;
	}
	
	public void FindTracks() {
		mTracks.clear();
		mTrackNames.clear();
		mTracks.add(DEMO_TRACK1);
		mTracks.add(DEMO_TRACK2);
		mTracks.add(DEMO_TRACK3);
		//mTracks.add(DEMO_TRACK4);
		//mTracks.add(DEMO_TRACK5);
		//mTracks.add(DEMO_TRACK6);
		mTrackNames.add(DEMO_TRACK1);
		mTrackNames.add(DEMO_TRACK2);
		mTrackNames.add(DEMO_TRACK3);
		//mTrackNames.add(DEMO_TRACK4);
		//mTrackNames.add(DEMO_TRACK5);
		//mTrackNames.add(DEMO_TRACK6);
		
		if(hasAudioPermission())
		{
			getAllTracks();
		}
	}
	
	public void onListItemClick(ListView parent, View v, int position, long id) {
		//if(bStartActivityAllowed)
		//	return;
		//bStartActivityAllowed = true;
		if(mTrackNames.get(position).startsWith(".."))
		{
			OpenClicked(null);
			return;
		}
		
		StartFlyingActivity(mTrackNames.get(position), mTracks.get(position));
	}
	
	private void StartFlyingActivity(final String title, final String fileName)
	{
		// BASS plays from a real file path; content:// tracks are first copied to the app cache.
		if(fileName.startsWith("content:"))
		{
			toast[1].show();
			new Thread(() -> {
				try
				{
					final File cached = copyToCache(Uri.parse(fileName), title);
					mHandler.post(() -> launchGame(title, cached.getPath()));
				}
				catch(Exception e)
				{
					mHandler.post(() -> toast[5].show());
				}
			}).start();
			return;
		}

		launchGame(title, fileName);
	}

	private File copyToCache(Uri uri, String name) throws java.io.IOException
	{
		File out = new File(getCacheDir(), name.replace(File.separatorChar, '_'));
		InputStream in = getContentResolver().openInputStream(uri);
		try
		{
			OutputStream os = new FileOutputStream(out);
			try
			{
				byte[] buf = new byte[64 * 1024];
				int n;
				while((n = in.read(buf)) > 0)
					os.write(buf, 0, n);
			}
			finally
			{
				os.close();
			}
		}
		finally
		{
			if(in != null)
				in.close();
		}
		return out;
	}

	private void launchGame(String title, String fileName)
	{
		for(int i=0;i<NUM_TOAST;i++)
		{
			try {
				toast[i].cancel();
			} catch (Exception e) {
			}
		}

		Intent intent = new Intent(MeVsMusicActivity.this, FlyingActivity.class);
		intent.putExtra("Title", title);
		intent.putExtra("FileName", fileName);
		if(GameSettings.bAccelerometer)
		{
			intent.putExtra("Accelerometer", "ON");
		}
		else
		{
			intent.putExtra("Accelerometer", "OFF");
		}
		//Log.i("MeVsMusicActivity", "1");
        startActivity(intent);
		//startActivityForResult(intent, 1);
	}
	
	/*@Override 
	public void onActivityResult(int requestCode, int resultCode, Intent data) {     
	  super.onActivityResult(requestCode, resultCode, data); 
	  switch(requestCode) { 
	    case (1) : { 
	    	bStartActivityAllowed = true;
	    	
	    	//if (resultCode == Activity.RESULT_OK) { 
	    	//  bStartActivityAllowed = true;
	    	//} 
	      break; 
	    } 
	  } 
	}*/

	// Lists the device's music via MediaStore (the tracks are stored as content:// URI strings).
	public void getAllTracks()
	{
		Cursor c = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				new String[]{MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DISPLAY_NAME},
				null, null, MediaStore.Audio.Media.DISPLAY_NAME);
		if(c == null)
			return;
		try
		{
			while(c.moveToNext())
			{
				String name = c.getString(1);
				if(name != null && isSupportedType(name))
				{
					mTracks.add(ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, c.getLong(0)).toString());
					mTrackNames.add(name);
				}
			}
		}
		finally
		{
			c.close();
		}
	}
}
