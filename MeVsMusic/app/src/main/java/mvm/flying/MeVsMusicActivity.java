package mvm.flying;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import mvm.settings.GameFileIO;
import mvm.settings.GameSettings;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
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
	private static final String[] EXTENSIONS_NAMES = {"mp3", "wav", "ogg", "aiff", "mp2"};

	private List<String> mTracks = new ArrayList<String>();
	private List<String> mTrackNames = new ArrayList<String>(); //Playable Track Titles

	public static String DEMO_TRACK1 = "Sunset.mp3";
	public static String DEMO_TRACK2 = "FeelsGood2B.mp3";
	public static String DEMO_TRACK3 = ".. (search my device)";

	ImageView introImage;

	int NUM_TOAST = 6;
	Toast[] toast = new Toast[NUM_TOAST];

	boolean bStartActivityAllowed = false;

	GameFileIO fileIO;

	private static Handler mHandler = new Handler();

	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		introImage = (ImageView) findViewById(R.id.imageView1);
		introImage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				openOptionsMenu();
			}
		});

		for (int i = 0; i < NUM_TOAST; i++) {
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
	}

	@Override
	public void onResume() {
		try {
			loadList();
		} catch (Exception e) {
		}

		super.onResume();
	}

	@Override
	public void onPause() {
		for (int i = 0; i < NUM_TOAST; i++) {
			try {
				toast[i].cancel();
			} catch (Exception e) {
			}
		}

		super.onPause();
	}

	private void loadList() {
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) || Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
		} else {
			toast[0].show();
		}

		toast[1].show();

		try {
			FindTracks();
		} catch (Exception e) {

		}

		listReady();
	}

	private void listReady() {
		setList();
		toast[2].setText("Loaded " + Integer.toString(mTrackNames.size()) + " Tracks");
		toast[2].show();
		if (mTracks.size() == 1) {
			toast[3].show();
			toast[4].show();
		}
	}

	protected void setList() {
		setListAdapter(new ExamplesAdapter(this, mTrackNames));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.sb_main_menu, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem item = menu.findItem(R.id.accelerometer);
		if (GameSettings.bAccelerometer == false) {
			item.setTitle(R.string.accelerometeroff);
		} else {
			item.setTitle(R.string.accelerometeron);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.refresh:
				try {
					loadList();
				} catch (Exception e) {
					e.printStackTrace(); // Log the exception
				}
				return true;
			case R.id.choose:
				OpenClicked(null);
				return true;
			case R.id.accelerometer:
				GameSettings.bAccelerometer = !GameSettings.bAccelerometer;
				try {
					GameSettings.save(fileIO);
				} catch (Exception e) {
					e.printStackTrace(); // Log the exception
				}
				if (GameSettings.bAccelerometer) {
					item.setTitle(R.string.accelerometeron);
				} else {
					item.setTitle(R.string.accelerometeroff);
				}
				return true;
			case R.id.info:
				Uri uriUrl = Uri.parse("http://mevsmusic.netau.net/m/"); // For Google Play
				Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
				startActivity(launchBrowser);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	String[] filelist;
	File filepath = new File("/mnt/extsd");

	public void OpenClicked(View v) {
		try {
			// User need to select the track file from inside a directory:
			String[] list = filepath.list();//getAllTracks(filepath);//filepath.list();
			if (list == null) list = new String[0];
			if (!filepath.getPath().equals("/")) {
				filelist = new String[list.length + 1];
				filelist[0] = "..";
				System.arraycopy(list, 0, filelist, 1, list.length);
			} else
				filelist = list;
			new AlertDialog.Builder(this)
					.setTitle("Choose a file to play")
					.setItems(filelist, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							File sel;
							if (filelist[which].equals("..")) sel = filepath.getParentFile();
							else sel = new File(filepath, filelist[which]);
							if (sel.isDirectory()) {
								filepath = sel;
								OpenClicked(null);
							} else {
								String file = sel.getAbsoluteFile().getPath();
								String title = sel.getAbsoluteFile().getName();

								boolean typeSupport = false;
								int mid = title.lastIndexOf(".");
								String ext = title.substring(mid + 1, title.length());
								for (int j = 0; j < EXTENSIONS_NAMES.length; j++) {
									if (ext.equalsIgnoreCase(EXTENSIONS_NAMES[j])) {
										typeSupport = true;
										break;
									}
								}
								if (typeSupport) {
									//if(bStartActivityAllowed)
									//	return;
									//bStartActivityAllowed = true;
									StartFlyingActivity(title, file);
								} else
									toast[5].show();
							}
						}
					})
					.show();
		} catch (Exception e) {

		}
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

		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) || Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
			getAllTracks(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getPath());

		} else {
			//Toast.makeText(getBaseContext(), "SD Card is either mounted elsewhere or is unusable", Toast.LENGTH_LONG).show();
			return;
		}

		if (mTracks.size() > 0) {
			if (mTrackNames.size() > 0) {
				//Toast.makeText(getBaseContext(), "Loaded " + Integer.toString(mTrackNames.size()) + " Tracks", Toast.LENGTH_SHORT).show();
				return;
			} else {
				//Toast.makeText(getBaseContext(), "No playable tracks found", Toast.LENGTH_LONG).show();
				return;
			}
		} else {
			//Toast.makeText(getBaseContext(), "Tracks lists are empty", Toast.LENGTH_LONG).show();
			return;
		}


	}

	public void onListItemClick(ListView parent, View v, int position, long id) {
		//if(bStartActivityAllowed)
		//	return;
		//bStartActivityAllowed = true;
		if (mTrackNames.get(position).startsWith("..")) {
			OpenClicked(null);
			return;
		}

		StartFlyingActivity(mTrackNames.get(position), mTracks.get(position));
	}

	private void StartFlyingActivity(String title, String fileName) {
		for (int i = 0; i < NUM_TOAST; i++) {
			try {
				toast[i].cancel();
			} catch (Exception e) {
			}
		}

		Intent intent = new Intent(MeVsMusicActivity.this, FlyingActivity.class);
		intent.putExtra("Title", title);
		intent.putExtra("FileName", fileName);
		if (GameSettings.bAccelerometer) {
			intent.putExtra("Accelerometer", "ON");
		} else {
			intent.putExtra("Accelerometer", "OFF");
		}
		//Log.i("MeVsMusicActivity", "1");
		startActivity(intent);
		//startActivityForResult(intent, 1);
	}

	class ExampleItem {
		public String title;
		public File file;

		public ExampleItem() {

		}
	}

	public void getAllTracks(String dirPath) {
		File f = new File(dirPath);
		File[] files = f.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].isFile()) {
				int mid = files[i].getName().lastIndexOf(".");
				String ext = files[i].getName().substring(mid + 1, files[i].getName().length());
				for (int j = 0; j < EXTENSIONS_NAMES.length; j++) {
					if (ext.equalsIgnoreCase(EXTENSIONS_NAMES[j])) {
						mTracks.add(files[i].getAbsoluteFile().getPath());
						mTrackNames.add(files[i].getAbsoluteFile().getName());
						break;
					}

				}
			} else
				getAllTracks(files[i].getAbsoluteFile().getPath());
		}
	}

}