package org.unipd.nbeghin.climbtheworld;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.unipd.nbeghin.climbtheworld.adapters.PagerAdapter;
import org.unipd.nbeghin.climbtheworld.db.DbHelper;
import org.unipd.nbeghin.climbtheworld.db.PreExistingDbLoader;
import org.unipd.nbeghin.climbtheworld.fragments.BuildingsFragment;
import org.unipd.nbeghin.climbtheworld.fragments.ToursFragment;
import org.unipd.nbeghin.climbtheworld.models.Building;
import org.unipd.nbeghin.climbtheworld.models.BuildingTour;
import org.unipd.nbeghin.climbtheworld.models.Climbing;
import org.unipd.nbeghin.climbtheworld.models.Photo;
import org.unipd.nbeghin.climbtheworld.models.Tour;
import org.unipd.nbeghin.climbtheworld.util.FacebookUtils;
import org.unipd.nbeghin.climbtheworld.weka.WekaClassifier;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.widget.WebDialog;
import com.j256.ormlite.dao.RuntimeExceptionDao;

/**
 * Main activity
 * 
 */
@SuppressLint("NewApi")
public class MainActivity extends ActionBarActivity {
	private static final String									APP_TITLE						= "Climb the world";
	public static final String									AppName							= "ClimbTheWorld";
	public static List<Building>								buildings;
	private List<Climbing>										climbings;																						// list of loaded climbings
	public static List<Tour>									tours;																							// list of loaded tours
	private ActionBar											ab;																							// reference to action bar
	public static RuntimeExceptionDao<Building, Integer>		buildingDao;																					// DAO for buildings
	public static RuntimeExceptionDao<Climbing, Integer>		climbingDao;																					// DAO for climbings
	public static RuntimeExceptionDao<Tour, Integer>			tourDao;																						// DAO for tours
	public static RuntimeExceptionDao<BuildingTour, Integer>	buildingTourDao;																				// DAO for building_tours
	public static RuntimeExceptionDao<Photo, Integer>			photoDao;
	public static final String									settings_file					= "ClimbTheWorldPreferences";
	public static final String									settings_detected_sampling_rate	= "samplingRate";
	private List<Fragment>										fragments						= new Vector<Fragment>();										// list of fragments to be loaded
	private PagerAdapter										mPagerAdapter;																					// page adapter for ViewPager
	public static final String									building_intent_object			= "org.unipd.nbeghin.climbtheworld.intents.object.building";	// intent key for sending building id
	private ViewPager											mPager;
	private static Context										sContext;
	private DbHelper											dbHelper;
	//NEW
		private WebDialog dialog = null;
	    private String dialogAction = null;
	    private Bundle dialogParams = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		loadDb(); // instance db connection
		// loading fragments
		fragments.add(Fragment.instantiate(this, BuildingsFragment.class.getName())); // instance building fragments
		fragments.add(Fragment.instantiate(this, ToursFragment.class.getName())); // instance tours fragments
		mPagerAdapter = new PagerAdapter(super.getSupportFragmentManager(), fragments);
		mPager = (ViewPager) super.findViewById(R.id.pager);
		mPager.setAdapter(this.mPagerAdapter);
		sContext = getApplicationContext();
		
		try {
			WekaClassifier.initializeParameters(getResources().openRawResource(R.raw.modelvsw30osl0));
		}
		catch(IOException exc) {
			this.finish();
		}
	}

	/**
	 * Helper method to access application context
	 */
	public static Context getContext() {
		return sContext;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.actionbar, menu);
		return true;
	}

	/**
	 * Check and return if a climbing exists for given building Returns null if no climbing exists yet
	 * 
	 * @param building_id building ID
	 * @return Climbing
	 */
	public static Climbing getClimbingForBuilding(int building_id) {
		Map<String, Object> conditions = new HashMap<String, Object>();
		conditions.put("building_id", building_id); // filter for building ID
		List<Climbing> climbings = climbingDao.queryForFieldValuesArgs(conditions);
		if (climbings.size() == 0) return null;
		return climbings.get(0);
	}

	/**
	 * Load db and setup DAOs NB: extracts DB from assets/databases/ClimbTheWorld.zip
	 */
	private void loadDb() {
		PreExistingDbLoader preExistingDbLoader = new PreExistingDbLoader(getApplicationContext()); // extract db from zip
		SQLiteDatabase db = preExistingDbLoader.getReadableDatabase();
		db.close(); // close connection to extracted db
		dbHelper = new DbHelper(getApplicationContext()); // instance new db connection to now-standard db
		buildingDao = dbHelper.getBuildingDao(); // create building DAO
		climbingDao = dbHelper.getClimbingDao(); // create climbing DAO
		tourDao = dbHelper.getTourDao(); // create tour DAO
		buildingTourDao = dbHelper.getBuildingTourDao(); // create building tour DAO
		photoDao = dbHelper.getPhotoDao();
		refresh(); // loads all buildings and tours
	}

	public void onShowActionProfile(MenuItem v) {
		Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
		startActivity(intent);
	}

	public void onShowSettings(MenuItem v) {
		Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
		startActivity(intent);
	}

	/**
	 * Reload all buildings
	 */
	public static void refreshBuildings() {
		buildings = buildingDao.queryForAll();
	}

	/**
	 * Reload all tours
	 */
	public static void refreshTours() {
		tours = tourDao.queryForAll();
	}

	/**
	 * Reload buildings and tours
	 */
	public static void refresh() {
		refreshBuildings();
		refreshTours();
	}

	public void onBtnShowGallery(View v) {
		Intent intent = new Intent(sContext, GalleryActivity.class);
		intent.putExtra("building_id", 1);
		startActivity(intent);
	}

	public static List<BuildingTour> getBuildingsForTour(int tour_id) {
		Map<String, Object> conditions = new HashMap<String, Object>();
		conditions.put("tour_id", tour_id);
		return buildingTourDao.queryForFieldValuesArgs(conditions); // get all buildings associated to a tour
	}

	public static int getBuildingImageResource(Building building) {
		return getContext().getResources().getIdentifier(building.getPhoto(), "drawable", getContext().getPackageName());
	}

	public static List<Integer> getBuildingPhotosForTour(int tour_id) {
		List<Integer> images = new ArrayList<Integer>();
		List<BuildingTour> buildingsTour = getBuildingsForTour(tour_id);
		for (BuildingTour buildingTour : buildingsTour) {
			images.add(getBuildingImageResource(buildingTour.getBuilding()));
		}
		return images;
	}

	@Override
	protected void onResume() {
		Log.i(MainActivity.AppName, "MainActivity onResume");
		super.onResume();
		refresh();
	}

	@Override
	protected void onPause() {
		Log.i(MainActivity.AppName, "MainActivity onPause");
		super.onPause();
	}

	private void copyFile(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while ((read = in.read(buffer)) != -1) {
			out.write(buffer, 0, read);
		}
	}

	public void onShareDb(MenuItem v) {
		shareDb();
	}

	public void onInviteFacebookFriends(MenuItem v) {
		/*Intent intent = new Intent(sContext, FBPickFriendActivity.class);
		startActivity(intent);*/
		if(FacebookUtils.isOnline(this))
			sendCustomChallenge();
		else
			Toast.makeText(getApplicationContext(), 
                    "Check your intenet connection", 
                    Toast.LENGTH_LONG).show();
	}
	
	//-----NEW-------
	//NEW
		private void sendCustomChallenge() {
			Session session = Session.getActiveSession();			
			if (session == null || !session.isOpened()) {
				return;
			}
			//List<String> permissions = session.getPermissions();			 
				sendChallenge();
		
		}
		private void sendChallenge() {
	    	Bundle params = new Bundle();
	    	
	    	// Uncomment following link once uploaded on Google Play for deep linking
	    	// params.putString("link", "https://play.google.com/store/apps/details?id=com.facebook.android.friendsmash");
	    	
	    	// 1. No additional parameters provided - enables generic Multi-friend selector
	    	params.putString("message", "I just smashed  friends! Can you beat it?");
	    	
	    	// 2. Optionally provide a 'to' param to direct the request at a specific user
//	    	params.putString("to", "515768651");
	    	
	    	// 3. Suggest friends the user may want to request - could be game specific
	    	// e.g. players you are in a match with, or players who recently played the game etc.
	    	// Normally this won't be hardcoded as follows but will be context specific
//	    	String [] suggestedFriends = {
//			    "695755709",
//			    "685145706",
//			    "569496010",
//			    "286400088",
//			    "627802916",
//	    	};
//	    	params.putString("suggestions", TextUtils.join(",", suggestedFriends));
//	    	
	    	// Show FBDialog without a notification bar
	    	showDialogWithoutNotificationBar("apprequests", params);
		}
		
		private void showDialogWithoutNotificationBar(String action, Bundle params) {
			// Create the dialog
			dialog = new WebDialog.Builder(this, Session.getActiveSession(), action, params).setOnCompleteListener(
					new WebDialog.OnCompleteListener() {
				

						@Override
		                public void onComplete(Bundle values,
		                    FacebookException error) {
		                    if (error != null) {
		                        if (error instanceof FacebookOperationCanceledException) {
		                            Toast.makeText(getApplicationContext(), 
		                                "Request cancelled", 
		                                Toast.LENGTH_SHORT).show();
		                        } /*else {
		                            Toast.makeText(getApplicationContext(), 
		                                "Network Error", 
		                                Toast.LENGTH_SHORT).show();
		                        }*/
		                    } else {
		                        final String requestId = values.getString("request");
		                        if (requestId != null) {
		                            Toast.makeText(getApplicationContext(), 
		                                "Request sent",  
		                                Toast.LENGTH_SHORT).show();
		                        } else {
		                            Toast.makeText(getApplicationContext(), 
		                                "Request cancelled", 
		                                Toast.LENGTH_SHORT).show();
		                        }
		                    }   
		                }

		            })
		            .build();
			
			// Hide the notification bar and resize to full screen
			Window dialog_window = dialog.getWindow();
	    	dialog_window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
	    	
	    	// Store the dialog information in attributes
	    	dialogAction = action;
	    	dialogParams = params;
	    	
	    	// Show the dialog
	    	dialog.show();
		}
		
		//-----NEW-----

	private void shareDb() {
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
		String output_name = "ClimbTheWorld_" + df.format(new Date()) + ".db";
		try {
			File file = new File(dbHelper.getDbPath()); // get private db reference
			if (file.exists() == false || file.length() == 0) throw new Exception("Empty DB");
			this.copyFile(new FileInputStream(file), this.openFileOutput(output_name, MODE_WORLD_READABLE));
			file = this.getFileStreamPath(output_name);
			Intent i = new Intent(Intent.ACTION_SEND);
			i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
			i.setType("*/*");
			startActivity(Intent.createChooser(i, "Share to"));
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), "Unable to export db: " + e.getMessage(), Toast.LENGTH_SHORT).show();
			Log.e(AppName, e.getMessage());
		}
	}
}
