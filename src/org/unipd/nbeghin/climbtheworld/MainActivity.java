package org.unipd.nbeghin.climbtheworld;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.unipd.nbeghin.climbtheworld.adapters.PagerAdapter;
import org.unipd.nbeghin.climbtheworld.db.DbHelper;
import org.unipd.nbeghin.climbtheworld.db.PreExistingDbLoader;
import org.unipd.nbeghin.climbtheworld.fragments.BuildingsFragment;
import org.unipd.nbeghin.climbtheworld.fragments.NotificationFragment;
import org.unipd.nbeghin.climbtheworld.fragments.ToursFragment;
import org.unipd.nbeghin.climbtheworld.models.Building;
import org.unipd.nbeghin.climbtheworld.models.BuildingTour;
import org.unipd.nbeghin.climbtheworld.models.Climbing;
import org.unipd.nbeghin.climbtheworld.models.InviteNotification;
import org.unipd.nbeghin.climbtheworld.models.Notification;
import org.unipd.nbeghin.climbtheworld.models.Photo;
import org.unipd.nbeghin.climbtheworld.models.Tour;
import org.unipd.nbeghin.climbtheworld.models.User;
import org.unipd.nbeghin.climbtheworld.services.NetworkBroadcasterReceiver;
import org.unipd.nbeghin.climbtheworld.util.FacebookUtils;
import org.unipd.nbeghin.climbtheworld.weka.WekaClassifier;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphObject;
import com.facebook.widget.WebDialog;
import com.j256.ormlite.dao.RuntimeExceptionDao;

/**
 * Main activity
 * 
 */
@SuppressLint("NewApi")
public class MainActivity extends ActionBarActivity {
	private static final String APP_TITLE = "Climb the world";
	public static final String AppName = "ClimbTheWorld";
	public static List<Building> buildings;
	private List<Climbing> climbings; // list of loaded climbings
	public static List<Tour> tours; // list of loaded tours
	public static List<Notification> notifications;
	private ActionBar ab; // reference to action bar
	public static RuntimeExceptionDao<Building, Integer> buildingDao; // DAO for
																		// buildings
	public static RuntimeExceptionDao<Climbing, Integer> climbingDao; // DAO for
																		// climbings
	public static RuntimeExceptionDao<Tour, Integer> tourDao; // DAO for tours
	public static RuntimeExceptionDao<BuildingTour, Integer> buildingTourDao; // DAO
																				// for
																				// building_tours
	public static RuntimeExceptionDao<Photo, Integer> photoDao;
	public static RuntimeExceptionDao<User, Integer> userDao;
	public static final String settings_file = "ClimbTheWorldPreferences";
	public static final String settings_detected_sampling_rate = "samplingRate";
	private List<Fragment> fragments = new Vector<Fragment>(); // list of
																// fragments to
																// be loaded
	private PagerAdapter mPagerAdapter; // page adapter for ViewPager
	public static final String building_intent_object = "org.unipd.nbeghin.climbtheworld.intents.object.building"; // intent
																													// key
																													// for
																													// sending
																													// building
																													// id
	private ViewPager mPager;
	private static Context sContext;
	private DbHelper dbHelper;
	// NEW
	private WebDialog dialog = null;
	private String dialogAction = null;
	private Bundle dialogParams = null;

	private String requestId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState); Log.d("MainActivity", "inizio");
		setContentView(R.layout.activity_main); Log.d("MainActivity", "dopo layout");

		// this.registerReceiver(new NetworkBroadcasterReceiver(),
		//         new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
		
		try {
			PackageInfo info = getPackageManager().getPackageInfo(
					"org.unipd.nbeghin.climbtheworld",
					PackageManager.GET_SIGNATURES);
			for (Signature signature : info.signatures) {
				System.out.println("qui");
				MessageDigest md = MessageDigest.getInstance("SHA");
				md.update(signature.toByteArray());
				Log.d("KeyHash:",
						Base64.encodeToString(md.digest(), Base64.DEFAULT));
			}
		} catch (NameNotFoundException e) {

		} catch (NoSuchAlgorithmException e) {

		}

		loadDb(); // instance db connection
		
		
		// TODO prendere dati richiesta, vedere se è valida e salvare su db
				// Check for an incoming notification. Save the info
				notifications = new ArrayList<Notification>();
				Uri intentUri = getIntent().getData();
				if (intentUri != null) {
					String requestIdParam = intentUri.getQueryParameter("request_ids");
					if (requestIdParam != null) {
						String array[] = requestIdParam.split(","); System.out.println("array " + array.length);
						for(int i = 0; i < array.length; i++){
							requestId = array[i];
							Log.i("onActivityCreated", "Request id: " + requestId);
							//deleteRequest(requestId);
							getRequestData(requestId);
						}
					}
					
					System.out.println("notf " + notifications.size());
				}
		
		// loading fragments
		fragments.add(Fragment.instantiate(this,
				BuildingsFragment.class.getName())); // instance building
														// fragments
		fragments
				.add(Fragment.instantiate(this, ToursFragment.class.getName())); // instance
																					// tours
																					// fragments
		fragments.add(Fragment.instantiate(this, NotificationFragment.class.getName()));
		
		mPagerAdapter = new PagerAdapter(super.getSupportFragmentManager(),
				fragments);
		mPager = (ViewPager) super.findViewById(R.id.pager);
		mPager.setAdapter(this.mPagerAdapter);
		sContext = getApplicationContext();

		try {
			WekaClassifier.initializeParameters(getResources().openRawResource(
					R.raw.modelvsw30osl0));
		} catch (IOException exc) {
			this.finish();
		}

		

		
	}

	private void getRequestData(final String inRequestId) {
		// Create a new request for an HTTP GET with the
		// request ID as the Graph path.
		Request request = new Request(Session.getActiveSession(), inRequestId,
				null, HttpMethod.GET, new Request.Callback() {

					@Override
					public void onCompleted(Response response) {
						// Process the returned response
						GraphObject graphObject = response.getGraphObject();
						FacebookRequestError error = response.getError();
						// Default message
						String message = "Incoming request";
						if (graphObject != null) {
							Log.d("graph obj not null", graphObject.toString());

							// Get the data, parse info to get the key/value
							// info
							JSONObject dataObject;
							JSONObject fromObject;
							String groupName = "";
							String sender = "";
							int type = -1;
							String id = "";

							try {
								dataObject = new JSONObject((String) graphObject.getProperty("data"));
								fromObject = (JSONObject)graphObject.getProperty("from");
								groupName = dataObject.getString("team_name");
								type = dataObject.getInt("type");
								sender = fromObject.getString("name");
								id = ((String) graphObject.getProperty("id")) ;

							} catch (JSONException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							
							
							message += " " + groupName;

							String time = (String) graphObject
									.getProperty("created_time");
							message += " " + time;
							if (isValid(time)) {
								Log.d("qui", "query valida");
								
								Notification notf = new InviteNotification(id, sender, groupName, type);
								notifications.add(notf);
								
								
											
										
									
								
							}

							String title = "";
							// Create the text for the alert based on the sender
							// and the data
							message = title;

						}
						Toast.makeText(getApplicationContext(), "Richiesta arrivata",
								Toast.LENGTH_LONG).show();
						//deleteRequest(inRequestId);//da chiamare solo se non ci sono errori
					}
				});
		// Execute the request asynchronously.
		Request.executeBatchAsync(request);
	}

	private boolean isValid(String creation_time) {
		SimpleDateFormat ISO8601DATEFORMAT = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ssZ", Locale.ITALY);
		Date now = new Date();
		Date olderDate = null;

		try {
			olderDate = ISO8601DATEFORMAT.parse(creation_time);
		} catch (java.text.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		double diffInMin = (double) ((now.getTime() - olderDate.getTime()) / (1000 * 60));

		// if 24h older
		if (diffInMin >= 1440)
			return false;
		else
			return true;
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
	 * Check and return if a climbing exists for given building Returns null if
	 * no climbing exists yet
	 * 
	 * @param building_id
	 *            building ID
	 * @return Climbing
	 */
	public static Climbing getClimbingForBuilding(int building_id) {
		Map<String, Object> conditions = new HashMap<String, Object>();
		conditions.put("building_id", building_id); // filter for building ID
		List<Climbing> climbings = climbingDao
				.queryForFieldValuesArgs(conditions);
		if (climbings.size() == 0)
			return null;
		return climbings.get(0);
	}

	/**
	 * Load db and setup DAOs NB: extracts DB from
	 * assets/databases/ClimbTheWorld.zip
	 */
	private void loadDb() {
		Log.d("Load normal db", "inizio");
		PreExistingDbLoader preExistingDbLoader = new PreExistingDbLoader(
				getApplicationContext()); // extract db from zip
		Log.d("Load normal db", "fine");
		SQLiteDatabase db = preExistingDbLoader.getReadableDatabase();
		db.close(); // close connection to extracted db
		dbHelper = new DbHelper(getApplicationContext()); // instance new db
															// connection to
															// now-standard db
		buildingDao = dbHelper.getBuildingDao(); // create building DAO
		climbingDao = dbHelper.getClimbingDao(); // create climbing DAO
		tourDao = dbHelper.getTourDao(); // create tour DAO
		buildingTourDao = dbHelper.getBuildingTourDao(); // create building tour
															// DAO
		photoDao = dbHelper.getPhotoDao();
		userDao = dbHelper.getUserDao();
		refresh(); // loads all buildings and tours
	}

	public void onShowActionProfile(MenuItem v) {
		Intent intent = new Intent(getApplicationContext(),
				ProfileActivity.class);
		startActivity(intent);
	}

	public void onShowSettings(MenuItem v) {
		Intent intent = new Intent(getApplicationContext(),
				SettingsActivity.class);
		startActivity(intent);
	}

	public void onShowGroups(MenuItem v) {
		Intent intent = new Intent(getApplicationContext(),
				GroupsActivity.class);
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
		return buildingTourDao.queryForFieldValuesArgs(conditions); // get all
																	// buildings
																	// associated
																	// to a tour
	}

	public static int getBuildingImageResource(Building building) {
		return getContext().getResources().getIdentifier(building.getPhoto(),
				"drawable", getContext().getPackageName());
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
		/*
		 * Intent intent = new Intent(sContext, FBPickFriendActivity.class);
		 * startActivity(intent);
		 */
		if (FacebookUtils.isOnline(this))
			sendCustomChallenge();
		else
			Toast.makeText(getApplicationContext(),
					"Check your intenet connection", Toast.LENGTH_LONG).show();
	}

	// -----NEW-------
	// NEW
	private void sendCustomChallenge() {
		Session session = Session.getActiveSession();
		if (session == null || !session.isOpened()) {
			return;
		}
		// List<String> permissions = session.getPermissions();
		sendChallenge();

	}

	private void sendChallenge() {
		Bundle params = new Bundle();

		// Uncomment following link once uploaded on Google Play for deep
		// linking
		// params.putString("link",
		// "https://play.google.com/store/apps/details?id=com.facebook.android.friendsmash");

		// 1. No additional parameters provided - enables generic Multi-friend
		// selector
		params.putString("message", "I just smashed  friends! Can you beat it?");

		// 2. Optionally provide a 'to' param to direct the request at a
		// specific user
		// params.putString("to", "515768651");

		// 3. Suggest friends the user may want to request - could be game
		// specific
		// e.g. players you are in a match with, or players who recently played
		// the game etc.
		// Normally this won't be hardcoded as follows but will be context
		// specific
		// String [] suggestedFriends = {
		// "695755709",
		// "685145706",
		// "569496010",
		// "286400088",
		// "627802916",
		// };
		// params.putString("suggestions", TextUtils.join(",",
		// suggestedFriends));
		//
		// Show FBDialog without a notification bar
		showDialogWithoutNotificationBar("apprequests", params);
	}

	private void showDialogWithoutNotificationBar(String action, Bundle params) {
		// Create the dialog
		dialog = new WebDialog.Builder(this, Session.getActiveSession(),
				action, params).setOnCompleteListener(
				new WebDialog.OnCompleteListener() {

					@Override
					public void onComplete(Bundle values,
							FacebookException error) {
						if (error != null) {
							if (error instanceof FacebookOperationCanceledException) {
								Toast.makeText(getApplicationContext(),
										"Request cancelled", Toast.LENGTH_SHORT)
										.show();
							} /*
							 * else { Toast.makeText(getApplicationContext(),
							 * "Network Error", Toast.LENGTH_SHORT).show(); }
							 */
						} else {
							final String requestId = values
									.getString("request");
							if (requestId != null) {
								Toast.makeText(getApplicationContext(),
										"Request sent", Toast.LENGTH_SHORT)
										.show();
							} else {
								Toast.makeText(getApplicationContext(),
										"Request cancelled", Toast.LENGTH_SHORT)
										.show();
							}
						}
					}

				}).build();

		// Hide the notification bar and resize to full screen
		Window dialog_window = dialog.getWindow();
		dialog_window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		// Store the dialog information in attributes
		dialogAction = action;
		dialogParams = params;

		// Show the dialog
		dialog.show();
	}

	// -----NEW-----

	private void shareDb() {
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
		String output_name = "ClimbTheWorld_" + df.format(new Date()) + ".db";
		try {
			File file = new File(dbHelper.getDbPath()); // get private db
														// reference
			if (file.exists() == false || file.length() == 0)
				throw new Exception("Empty DB");
			this.copyFile(new FileInputStream(file),
					this.openFileOutput(output_name, MODE_WORLD_READABLE));
			file = this.getFileStreamPath(output_name);
			Intent i = new Intent(Intent.ACTION_SEND);
			i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
			i.setType("*/*");
			startActivity(Intent.createChooser(i, "Share to"));
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(),
					"Unable to export db: " + e.getMessage(),
					Toast.LENGTH_SHORT).show();
			Log.e(AppName, e.getMessage());
		}
	}
	
	public static void emptyNotificationList(){
		notifications.clear();
	}
}
