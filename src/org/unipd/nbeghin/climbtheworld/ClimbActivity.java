package org.unipd.nbeghin.climbtheworld;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SimpleTimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.unipd.nbeghin.climbtheworld.exceptions.ClimbingNotFound;
import org.unipd.nbeghin.climbtheworld.exceptions.NoFBSession;
import org.unipd.nbeghin.climbtheworld.listeners.AccelerometerSamplingRateDetect;
import org.unipd.nbeghin.climbtheworld.models.Badge;
import org.unipd.nbeghin.climbtheworld.models.Building;
import org.unipd.nbeghin.climbtheworld.models.ChartMember;
import org.unipd.nbeghin.climbtheworld.models.ClassifierCircularBuffer;
import org.unipd.nbeghin.climbtheworld.models.Climbing;
import org.unipd.nbeghin.climbtheworld.models.Collaboration;
import org.unipd.nbeghin.climbtheworld.models.Competition;
import org.unipd.nbeghin.climbtheworld.models.GameModeType;
import org.unipd.nbeghin.climbtheworld.models.Group;
import org.unipd.nbeghin.climbtheworld.models.TeamDuel;
import org.unipd.nbeghin.climbtheworld.models.User;
import org.unipd.nbeghin.climbtheworld.models.UserBadge;
import org.unipd.nbeghin.climbtheworld.services.SamplingClassifyService;
import org.unipd.nbeghin.climbtheworld.util.FacebookUtils;
import org.unipd.nbeghin.climbtheworld.util.ModelsUtil;
import org.unipd.nbeghin.climbtheworld.util.StatUtils;
import org.unipd.nbeghin.climbtheworld.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VerticalSeekBar;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

//competizioni: se vinco ma senza altri partecipanti, no punti extra vittoria ne badge

/**
 * Climbing activity: shows a given building and starts classifier. At start it
 * calculates the sampling rate of the device it's run from (only once, after
 * that it just saves the value in standard Android preferences)
 * 
 */
public class ClimbActivity extends ActionBarActivity {
	public static final String SAMPLING_TYPE = "ACTION_SAMPLING"; // intent's
																	// action
	public static final String SAMPLING_TYPE_NON_STAIR = "NON_STAIR"; // classifier's
																		// output
	public static final String SAMPLING_DELAY = "DELAY"; // intent's action
	private boolean samplingEnabled = false; // sentinel if sampling is running
	private static double detectedSamplingRate = 0; // detected sampling rate
													// (after sampling rate
													// detector)
	private static int samplingDelay; // current sampling delay (SensorManager)
	private double minimumSamplingRate = 13; // minimum sampling rate for using
												// this app
	private Intent backgroundClassifySampler; // classifier background service
												// intent
	private Intent backgroundSamplingRateDetector; // sampling rate detector
													// service intent
	private IntentFilter classifierFilter = new IntentFilter(ClassifierCircularBuffer.CLASSIFIER_ACTION); // intent
																											// filter
																											// (for
																											// BroadcastReceiver)
	private IntentFilter samplingRateDetectorFilter = new IntentFilter(AccelerometerSamplingRateDetect.SAMPLING_RATE_ACTION); // intent
																																// filter
																																// (for
																																// BroadcastReceiver)
	private BroadcastReceiver classifierReceiver = new ClassifierReceiver(); // implementation
																				// of
																				// BroadcastReceiver
																				// for
																				// classifier
																				// service
	private BroadcastReceiver sampleRateDetectorReceiver = new SamplingRateDetectorReceiver(); // implementation
																								// of
																								// BroadcastReceiver
																								// for
																								// sampling
																								// rate
																								// detector
	private int num_steps = 0; // number of currently detected steps
	private double percentage = 0.0; // current progress percentage
	private Building building; // current building
	private Climbing climbing; // current climbing
	private Climbing soloClimb;
	private VerticalSeekBar seekbarIndicator; // reference to vertical seekbar
	private int vstep_for_rstep = 1;
	private boolean used_bonus = false;
	private double percentage_bonus = 0.50f;
	private boolean climbedYesterday = false;
	private int new_steps = 0;
	private int difficulty;

	SharedPreferences pref;
	// logic for social mode
	private GameModeType mode;
	
	private Collaboration collaboration;
	private Map<String, Integer> others_steps;
	ParseObject collab_parse;
	private 	int threshold;
	
	private Competition competition;
	private List<ChartMember> chart;
	ParseObject compet_parse;
	
	private TeamDuel teamDuel;
	private List<Integer> myTeamScores;
	ParseObject teamDuel_parse;
	JSONObject totScores;
	// Graphics for Social Mode
	private VerticalSeekBar secondSeekbar;
	private TextView current;

	private List<TextView> group_members = new ArrayList<TextView>();
	private List<TextView> group_steps = new ArrayList<TextView>();
	
	// Our created menu to use
		private Menu mymenu;

	// number of virtual step for each real step
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
	private static final int HIDER_FLAGS = 0;
	/**
	 * The instance of the {@link SystemUiHider} for this activity.
	 */
	private SystemUiHider mSystemUiHider;

	// public static double getDetectedSamplingRate() {
	// return detectedSamplingRate;
	// }
	/**
	 * Handles classifier service intents (STAIR/NON_STAIR)
	 * 
	 */
	public class ClassifierReceiver extends BroadcastReceiver {

		private static final double tradeoffG = 0.001;
		private static final double g = tradeoffG / (double) 100;
		private List<Double> history = new ArrayList<Double>();
		private static final int historySize = 10;

		@Override
		public void onReceive(Context context, Intent intent) {
			// String result =
			// intent.getExtras().getString(ClassifierCircularBuffer.CLASSIFIER_NOTIFICATION_STATUS);
			Double result = intent.getExtras().getDouble(ClassifierCircularBuffer.CLASSIFIER_NOTIFICATION_STATUS);

			double correction = 0.0;
			for (int indexHistory = 0; indexHistory < history.size(); indexHistory++) {
				correction += (100 / Math.pow(2, indexHistory + 1)) * (double) history.get(indexHistory) * g;
			}

			if (Double.isNaN(correction)) {
				correction = 0.0;
			}

			double finalClassification = result + correction;

			if (result * finalClassification >= 0) {
				if (history.size() == historySize) {
					history.remove(historySize - 1);
					history.add(0, (finalClassification > 0 ? 1.0 : -1.0));
				} else {
					history.add(0, (finalClassification > 0 ? 1.0 : -1.0));
				}
			} else {
				history.clear();
				history.add(result > 0 ? 1.0 : -1.0);
			}

			if (finalClassification > 0) {
				if (climbedYesterday && percentage > 0.25f && percentage < 0.50f && used_bonus == false) { // bonus
																											// at
																											// 25%
					apply_percentage_bonus();
				} else { // standard, no bonus
					num_steps += vstep_for_rstep; // increase the number of
													// steps
					new_steps += vstep_for_rstep;
					// increase the seekbar progress
					if (mode == GameModeType.SOCIAL_CLIMB){
						seekbarIndicator.setProgress(num_steps + sumOthersStep());
						setThresholdText();
					}else if(mode == GameModeType.TEAM_VS_TEAM){
						seekbarIndicator.setProgress(myTeamScore());
						setThresholdText();
					}else
						seekbarIndicator.setProgress(num_steps);
					percentage = (double) num_steps / (double) building.getSteps(); // increase
																					// the
																					// progress
																					// percentage
					boolean win = false; //user wins?
					if (mode == GameModeType.SOCIAL_CLIMB){
						//consider also my friend' steps
						win = ((num_steps + sumOthersStep()) >= building.getSteps()) && (threshold - num_steps <= 0); 
						setThresholdText();																
					}else if(mode == GameModeType.TEAM_VS_TEAM){
						win = ((myTeamScore()) >= building.getSteps());
						setThresholdText();	
					}else
						win = ((num_steps) >= building.getSteps()); //consider only my steps
					if (win) { // ensure it did not exceed the number of steps
								// (when multiple steps-at-once are detected)
						num_steps = building.getSteps();
						percentage = 1.00;
					}
					updateStats(); // update the view of current stats
					if (win) {
						stopClassify(); // stop classifier service service
						apply_win();
					}
					/*if(mode == GameModeType.SOCIAL_CLIMB && ((num_steps + sumOthersStep()) >= building.getSteps()) && (threshold - num_steps > 0)){
						System.out.println("Social Penalty");
						socialPenalty();
					}*/
				}
			}

			((TextView) findViewById(R.id.lblClassifierOutput)).setText(finalClassification > 0 ? "STAIR" : "NON_STAIR"); // debug:
																															// show
																															// currently
																															// detected
																															// classifier
																															// output
		}
	}

	private void apply_percentage_bonus() {
		Log.i(MainActivity.AppName, "Applying percentage bonus");
		percentage += percentage_bonus;
		num_steps = (int) (((double) building.getSteps()) * percentage);
		stopClassify();
		used_bonus = true;
		Toast.makeText(getApplicationContext(), "BONUS: you climbed less than 24h ago, you earn +50%", Toast.LENGTH_LONG).show();
		enableRocket();
		updateStats(); // update the view of current stats
		if (mode == GameModeType.SOCIAL_CLIMB){
			seekbarIndicator.setProgress(num_steps + sumOthersStep());
			setThresholdText();
		}else
			seekbarIndicator.setProgress(num_steps); // increase the seekbar
														// progress
	}
	


	private void apply_win() {
		Log.i(MainActivity.AppName, "Succesfully climbed building #" + building.get_id());
		Toast.makeText(getApplicationContext(), "You successfully climbed " + building.getSteps() + " steps (" + building.getHeight() + "m) of " + building.getName() + "!", Toast.LENGTH_LONG).show(); // show
																																																		// completion
																																																		// text
		findViewById(R.id.lblWin).setVisibility(View.VISIBLE); // load and
																// animate
																// completed
																// climbing test
		findViewById(R.id.lblWin).startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.blink));
		((ImageButton) findViewById(R.id.btnStartClimbing)).setImageResource(R.drawable.social_share);
		findViewById(R.id.btnAccessPhotoGallery).startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.abc_fade_in));
		findViewById(R.id.btnAccessPhotoGallery).setVisibility(View.VISIBLE);
		((ImageButton) findViewById(R.id.btnAccessPhotoGallery)).setImageResource(R.drawable.device_access_video);
		switch (climbing.getGame_mode()) {
		case 1:
			setThresholdText();
			break;
		case 2:
			break;
		default:
			break;
		}
	}

	private void apply_update() {
		Log.i("apply_update", "apply_update " + new_steps);
		findViewById(R.id.encouragment).setVisibility(View.VISIBLE);
		((TextView) findViewById(R.id.encouragment)).setText(" Well Done!!!!");
		((ImageButton) findViewById(R.id.btnAccessPhotoGallery)).setImageResource(R.drawable.social_share);
		findViewById(R.id.btnAccessPhotoGallery).setVisibility(View.VISIBLE);

	}

	private void enableRocket() {
		Animation animSequential = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rocket);
		findViewById(R.id.imgRocket).startAnimation(animSequential);
	}

	/**
	 * Handles sampling rate detector service intents (STAIR/NON_STAIR)
	 * 
	 */
	public class SamplingRateDetectorReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			detectedSamplingRate = intent.getExtras().getDouble(AccelerometerSamplingRateDetect.SAMPLING_RATE); // get
																												// detected
																												// sampling
																												// rate
																												// from
																												// received
																												// intent
			samplingDelay = backgroundSamplingRateDetector.getExtras().getInt(SAMPLING_DELAY); // get
																								// used
																								// sampling
																								// delay
																								// from
																								// received
																								// intent
			Log.i(MainActivity.AppName, "Detected sampling rate: " + Double.toString(detectedSamplingRate) + "Hz");
			if (detectedSamplingRate >= minimumSamplingRate) { // sampling rate
																// high enough
				SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit(); // get
																																	// refence
																																	// to
																																	// android
																																	// preferences
				editor.putFloat("detectedSamplingRate", (float) detectedSamplingRate); // store
																						// detected
																						// sampling
																						// rate
				editor.putInt("sensor_delay", samplingDelay); // store used
																// sampling
																// delay
				editor.apply(); // commit preferences
				Log.i(MainActivity.AppName, "Stored detected sampling rate of " + detectedSamplingRate + "Hz");
				Log.i(MainActivity.AppName, "Stored sampling delay of " + samplingDelay);
				stopService(backgroundSamplingRateDetector); // stop sampling
																// rate detector
																// service
				unregisterReceiver(this); // unregister listener
				setupByDetectedSamplingRate(); // setup app with detected
												// sampling rate
			} else { // sampling rate not high enough: try to decrease the
						// sampling delay
				if (backgroundSamplingRateDetector.getExtras().getInt(ClimbActivity.SAMPLING_DELAY) != SensorManager.SENSOR_DELAY_UI) { // decrease
																																		// sampling
																																		// delay
																																		// in
																																		// order
																																		// to
																																		// increase
																																		// sampling
																																		// rate
					Log.w(MainActivity.AppName, "Sampling rate not high enough: trying to decrease the sampling delay");
					stopService(backgroundSamplingRateDetector); // stop
																	// previous
																	// sampling
																	// rate
																	// detector
																	// service
					backgroundSamplingRateDetector.putExtra(SAMPLING_DELAY, SensorManager.SENSOR_DELAY_UI); // set
																											// new
																											// sampling
																											// delay
																											// (lower
																											// than
																											// the
																											// previous
																											// one)
					startService(backgroundSamplingRateDetector); // start new
																	// sampling
																	// rate
																	// detector
																	// service
				} else { // unable to determine a sampling rate high enough for
							// our purposes: stop
					Log.e(MainActivity.AppName, "Sampling rate not high enough for this application");
					unregisterReceiver(this); // unregister listener
					stopService(backgroundSamplingRateDetector); // stop
																	// sampling
																	// rate
																	// detector
																	// service
					((TextView) findViewById(R.id.lblSamplingRateDetected)).setText("TOO LOW: " + (int) detectedSamplingRate + " Hz");
					AlertDialog.Builder alert = new AlertDialog.Builder(getApplicationContext());
					alert.setTitle("Sampling rate not high enough");
					alert.setMessage("Your accelerometer is not fast enough for this application. Make sure to use at least " + minimumSamplingRate + " Hz");
					alert.show();
				}
			}
		}
	}

	public void accessPhotoGallery(View v) {
		if (percentage >= 1.0) {
			Log.i(MainActivity.AppName, "Accessing gallery for building " + building.get_id());
			Intent intent = new Intent(this, GalleryActivity.class);
			intent.putExtra("gallery_building_id", building.get_id());
			startActivity(intent);
		} else {
			FacebookUtils fb = new FacebookUtils(this);

			try {
				fb.postUpdateToWall(climbing, new_steps);
			} catch (NoFBSession e) {
				Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
				startActivity(intent);

			}
		}
	}

	/**
	 * Update the stat panel
	 */
	private void updateStats() {
		((TextView) findViewById(R.id.lblNumSteps)).setText(Integer.toString(num_steps) + " of " + Integer.toString(building.getSteps()) + " (" + (new DecimalFormat("#.##")).format(percentage * 100.00) + "%)");
	}

	/**
	 * Setup the activity with a given sampling rate and sampling delay
	 */
	private void setupByDetectedSamplingRate() {
		backgroundClassifySampler.putExtra(AccelerometerSamplingRateDetect.SAMPLING_RATE, detectedSamplingRate);
		backgroundClassifySampler.putExtra(SAMPLING_DELAY, samplingDelay);
		// if (climbing.getPercentage() < 100) {
		// Toast.makeText(getApplicationContext(),
		// "Start climbing some stairs!", Toast.LENGTH_LONG).show();
		// }
	}

	private void setThresholdText(){
		int stepsToThreshold = threshold - num_steps;
		if(stepsToThreshold <= 0)
			current.setText("Threshold passed: you're in!!!!");
		else
			current.setText(getString(R.string.threshold) + ": " + stepsToThreshold);
	}
	
	/**
	 * Sets Graphics in case of social mode.
	 * Shows name and steps of my friends
	 */
	private void setGraphicsSocialMode() {

		secondSeekbar = (VerticalSeekBar) findViewById(R.id.seekBarPosition2);
		secondSeekbar.setOnTouchListener(new OnTouchListener() { // disable
																	// user-driven
																	// seekbar
																	// changes
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						return true;
					}
				});
		secondSeekbar.setMax(building.getSteps());
		current = (TextView) findViewById(R.id.textPosition);
		for (int i = 1; i <= ClimbApplication.N_MEMBERS_PER_GROUP; i++) {
			System.out.println("qui " + i);
			int idNome = getResources().getIdentifier("nome" + i, "id", getPackageName());
			int idPassi = getResources().getIdentifier("passi" + i, "id", getPackageName());
			group_members.add((TextView) findViewById(idNome));
			group_steps.add((TextView) findViewById(idPassi)); System.out.println(idPassi);
			System.out.println(group_members.size());
		}

		switch (GameModeType.values()[climbing.getGame_mode()]) {
		case SOCIAL_CLIMB:
			secondSeekbar.setVisibility(View.GONE);
			for (int i = 0; i < group_members.size() - 1; i++) {
				group_members.get(i).setVisibility(View.VISIBLE);
				group_steps.get(i).setVisibility(View.VISIBLE);
			}
			group_members.get(group_members.size() - 1).setVisibility(View.VISIBLE);
			group_steps.get(group_steps.size() - 1).setVisibility(View.VISIBLE);
			current.setVisibility(View.VISIBLE);
			setThresholdText();
			
			break;
		case SOCIAL_CHALLENGE:
			secondSeekbar.setVisibility(View.INVISIBLE);
			current.setVisibility(View.VISIBLE);
			for (int i = 0; i < group_members.size(); i++) {
				group_members.get(i).setVisibility(View.VISIBLE);
				group_steps.get(i).setVisibility(View.VISIBLE);
			}

			break;
		case TEAM_VS_TEAM:
			secondSeekbar.setVisibility(View.VISIBLE);
			secondSeekbar.setProgressDrawable(getResources()
					.getDrawable(R.drawable.progress_bar_red));
			seekbarIndicator.setProgressDrawable(getResources()
					.getDrawable(R.drawable.progress_bar_green));
		    current.setVisibility(View.VISIBLE);
		    setThresholdText();
			for (int i = 0; i < 2; i++) {
				group_members.get(i).setVisibility(View.VISIBLE);
				group_steps.get(i).setVisibility(View.VISIBLE);
			}
			for (int i = 2; i < group_members.size(); i++) {
				group_members.get(i).setVisibility(View.GONE);
				group_steps.get(i).setVisibility(View.GONE);
			}

			break;
		case SOLO_CLIMB:
			secondSeekbar.setVisibility(View.GONE);
			current.setVisibility(View.GONE);
			for (int i = 0; i < group_members.size(); i++) {
				group_members.get(i).setVisibility(View.GONE);
				group_steps.get(i).setVisibility(View.GONE);
			}

			break;

		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_climb);
		setupActionBar();
		pref = getSharedPreferences("UserSession", 0);
		final View controlsView = findViewById(R.id.fullscreen_content_controls);
		final View contentView = findViewById(R.id.lblReadyToClimb);
		// Set up an instance of SystemUiHider to control the system UI for
		// this activity.
		mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
		mSystemUiHider.setup();
		mSystemUiHider.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
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
						mShortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
					}
					controlsView.animate().translationY(visible ? 0 : mControlsHeight).setDuration(mShortAnimTime);
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
		findViewById(R.id.btnStartClimbing).setOnTouchListener(mDelayHideTouchListener);
		// app-specific logic
		seekbarIndicator = (VerticalSeekBar) findViewById(R.id.seekBarPosition); // get
																					// reference
																					// to
																					// vertical
																					// seekbar
																					// (only
																					// once
																					// for
																					// performance-related
																					// reasons)
		seekbarIndicator.setOnTouchListener(new OnTouchListener() { // disable
																	// user-driven
																	// seekbar
																	// changes
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						return true;
					}
				});
		int building_id = getIntent().getIntExtra(ClimbApplication.building_intent_object, 0); // get
																							// building
																							// id
																							// from
																							// received
																							// intent
		try {
			// get building ID from intent
			if (building_id == 0)
				throw new Exception("ERROR: unable to get intent data"); // no
																			// building
																			// id
																			// found
																			// in
																			// received
																			// intent
			System.out.println(building_id);
			building = ClimbApplication.buildingDao.queryForId(building_id); // query
																			// db
																			// to
																			// get
																			// asked
																			// building
			setup_from_building(); // load building info
			backgroundClassifySampler = new Intent(this, SamplingClassifyService.class); // instance
																							// (without
																							// starting)
																							// background
																							// classifier
		} catch (Exception e) {
			e.printStackTrace();
		}
		setGraphicsSocialMode();

	}

	protected void onNewIntent(Intent intent) {
		int building_id = intent.getIntExtra(ClimbApplication.building_intent_object, 0); // get
																						// building
																						// id
																						// from
																						// received
																						// intent
		Log.i(MainActivity.AppName, "New intent building id: " + building_id);
	}

	/**
	 * Setup view with a given building and create/load an associated climbing
	 */
	private void setup_from_building() {
		int imageId = getApplicationContext().getResources().getIdentifier(building.getPhoto(), "drawable", getApplicationContext().getPackageName()); // get
																																						// building's
																																						// photo
																																						// resource
																																						// ID
		if (imageId > 0)
			((ImageView) findViewById(R.id.buildingPhoto)).setImageResource(imageId);
		// set building info
		((TextView) findViewById(R.id.lblBuildingName)).setText(building.getName() + " (" + building.getLocation() + ")"); // building's
																															// location
		((TextView) findViewById(R.id.lblNumSteps)).setText(Integer.toString(building.getSteps()) + " steps"); // building's
																												// steps
		((TextView) findViewById(R.id.lblHeight)).setText(Integer.toString(building.getHeight()) + "mt"); // building's
																											// height
																											// (in
																											// mt)
		System.out.println("carico climb di prima o creo uno nuovo");
		loadPreviousClimbing(); // get previous climbing for this building
		mode = GameModeType.values()[climbing.getGame_mode()];
		loadSocialMode();

	}

	/**
	 * Load the object with info about the current social mode
	 */
	private void loadSocialMode() {
		System.out.println("mode: " + mode);
		switch (mode) {
		case SOCIAL_CHALLENGE:
			mode = GameModeType.SOCIAL_CHALLENGE;
			loadCompetition();
			break;
		case SOCIAL_CLIMB:
			// cerco collaborazione per il building corrente
			mode = GameModeType.SOCIAL_CLIMB;
			loadCollaboration();
			break;
		case TEAM_VS_TEAM:
			mode = GameModeType.TEAM_VS_TEAM;
			loadTeamDuel();
			break;
		case SOLO_CLIMB:
			break;
		}
	}

	/**
	 * Loads the Collaboration object corresponding to the current climbing
	 */
	private void loadCollaboration() {
		collaboration = ClimbApplication.getCollaborationById(climbing.getId_mode());//MainActivity.getCollaborationForBuilding(building.get_id());
		others_steps = new HashMap<String, Integer>();
		if (collaboration == null) {
			Toast.makeText(this, "No collaboration available for this building", Toast.LENGTH_SHORT).show();
		} else {
			collaboration.setMy_stairs(climbing.getCompleted_steps());
			updateOthers(false);
		}

	}
	
	private void loadTeamDuel(){
		teamDuel = ClimbApplication.getTeamDuelById(climbing.getId_mode());
		myTeamScores = new ArrayList<Integer>();
		if(teamDuel == null){
			Toast.makeText(this, "No team duel available for this building", Toast.LENGTH_SHORT).show();
		}else{
			teamDuel.setMy_steps(climbing.getCompleted_steps());
			teamDuel.setSteps_my_group(teamDuel.getSteps_my_group() + climbing.getCompleted_steps());
			updateTeams(false);
		}
	}
	
	private int myTeamScore(){
		int scoreTot = 0;
		for(Integer score : myTeamScores)
			scoreTot += score;
		return scoreTot + num_steps;
	}
	
	/**
	 * Loads the Competition object corresponding to the current climbing
	 */
	private void loadCompetition() {
		competition = ClimbApplication.getCompetitionById(climbing.getId_mode());//MainActivity.getCompetitionByBuilding(building.get_id());
		if(competition == null){
			Toast.makeText(this, "No competition available for this building", Toast.LENGTH_SHORT).show();
		}else{
			competition.setMy_stairs(climbing.getCompleted_steps());
			updateChart(false);
		}
	}

	/**
	 * Update graphics to let user see the latest update about my friends' steps
	 */
	private void updateOthers(final boolean isUpdate) {
		System.out.println("update others");
		if (!(mode == GameModeType.SOLO_CLIMB) && FacebookUtils.isOnline(this)) {
			ParseQuery<ParseObject> query = ParseQuery.getQuery("Collaboration");
			query.whereEqualTo("objectId", collaboration.getId());
			query.findInBackground(new FindCallback<ParseObject>() {

				@Override
				public void done(List<ParseObject> collabs, ParseException e) {
					if (e == null) {
						if (collabs == null || collabs.size() == 0) {
							Toast.makeText(getApplicationContext(), "This Collaboration is not yet available", Toast.LENGTH_SHORT).show();
							Log.e("loadCollaboration", "Collaboration " + collaboration.getId() + " not present in Parse");
							// delete this collaboration
							//MainActivity.collaborationDao.delete(collaboration);
						} else {
							SharedPreferences pref = getApplicationContext().getSharedPreferences("UserSession", 0);
							collab_parse = collabs.get(0);
							JSONObject others = collab_parse.getJSONObject("stairs");
							JSONObject othersName = collab_parse.getJSONObject("collaborators");
							Iterator members = others.keys();
							int i = 0;
							while (members.hasNext()) {
								String key = (String) members.next();
								if (!key.equalsIgnoreCase(pref.getString("FBid", ""))) {
									System.out.println(key);
									System.out.println(pref.getString("FBid", ""));
									String name = "";
									int steps = -1;
									try {
										name = (String) othersName.getString(key);
										steps = (Integer) others.getInt(key);
									} catch (JSONException e2) {
										// TODO Auto-generated catch block
										e2.printStackTrace();
									}
									group_members.get(i).setText(name);
									others_steps.put(key, steps);
									group_steps.get(i).setVisibility(View.VISIBLE);
									group_steps.get(i).setText(String.valueOf(steps));
									group_members.get(i).setClickable(false);
									group_members.get(i).setVisibility(View.VISIBLE);
									i++;
								}
							}
							if(i < group_members.size() && i <= ClimbApplication.N_MEMBERS_PER_GROUP){
								group_steps.get(i).setVisibility(View.INVISIBLE);
								group_members.get(i).setClickable(true);
								group_members.get(i).setText("  +");
								group_members.get(i).setVisibility(View.VISIBLE);
								group_members.get(i).setOnClickListener(new OnClickListener() {
								
							
								
								@Override
								public void onClick(View arg0) {
									Bundle params = new Bundle();
									params.putString("data", "{\"idCollab\":\"" + collaboration.getId() + "\"," + "\"idBuilding\":\"" + building.get_id() + "\"," + "\"nameBuilding\":\"" + building.getName() + "\", \"type\": \"1\"}");
									params.putString("message", "Please, help me!!!!");
									if(Session.getActiveSession() != null && Session.getActiveSession().isOpened()){

									WebDialog requestsDialog = (new WebDialog.RequestsDialogBuilder(ClimbActivity.this, Session.getActiveSession(), params)).setOnCompleteListener(new OnCompleteListener() {

										@Override
										public void onComplete(Bundle values, FacebookException error) {
											if (error != null) {
												if (error instanceof FacebookOperationCanceledException) {
													Toast.makeText(ClimbActivity.this, "Request cancelled", Toast.LENGTH_SHORT).show();
													
												} else {
													Toast.makeText(ClimbActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
							
												}
											} else {
												final String requestId = values.getString("request");
												if (requestId != null) {
													Toast.makeText(ClimbActivity.this, "Request sent", Toast.LENGTH_SHORT).show();
												} else {
													Toast.makeText(ClimbActivity.this, "Request cancelled", Toast.LENGTH_SHORT).show();
									

												}
											}
										}

									}).build();
									requestsDialog.show();
									}else{
										Toast.makeText(ClimbActivity.this, "Currently not logged to FB", Toast.LENGTH_SHORT).show();
									}
								}
							});
							i++;
							}
							seekbarIndicator.setProgress(climbing.getCompleted_steps() + sumOthersStep());
							for(; i < group_members.size(); i++){
								group_members.get(i).setClickable(false);
								group_members.get(i).setVisibility(View.INVISIBLE);
								group_steps.get(i).setVisibility(View.INVISIBLE);

							}
							System.out.println("num_steps " + num_steps);
							System.out.println("sum other steps " + sumOthersStep());
							if((num_steps + sumOthersStep() >= building.getSteps()) && (num_steps < threshold)){
								//if I and my friend have finished climbing this building, but I didn't pass the threshold
								System.out.println("social penalty");
								socialPenalty();
							}else if((num_steps + sumOthersStep() >= building.getSteps()) && (num_steps < threshold)){
								percentage = 1.0;
								updatePoints();
								saveBadges();
							}

						}
					} else if(!(mode == GameModeType.SOCIAL_CLIMB)) {
						Toast.makeText(getApplicationContext(), "Connections Problem", Toast.LENGTH_SHORT).show();
						Log.e("loadCollaboration", e.getMessage());
					}
					if(isUpdate){
						System.out.println("animaz off");
						resetUpdating();
					}
				}
			});
			
				
		} else {
			Toast.makeText(getApplicationContext(), "No Connection Available", Toast.LENGTH_SHORT).show();
			if(isUpdate){
				System.out.println("animaz off");
				resetUpdating();
			}
		}
		
	}

	private int sumOthersStep() {
		int sum = 0;
		Set<String> keys = others_steps.keySet();
		for (String key : keys) {
			sum += others_steps.get(key);
		}
		return sum;
	}

	/**
	 * Saves the new Climbing object in Parse
	 */
	private void saveClimbingToParse(final Climbing climbing) {
		SharedPreferences pref = getApplicationContext().getSharedPreferences("UserSession", 0);
		// save climbing to parse
		if (!pref.getString("FBid", "none").equals("none")) {
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			df.setTimeZone(new SimpleTimeZone(0, "GMT"));
			final ParseObject climb = new ParseObject("Climbing");
			climb.put("building", climbing.getBuilding().get_id());
			if(climbing.getId_mode() != null)
				climb.put("id_mode", climbing.getId_mode());
			else
				climb.put("id_mode", "");
			
			try {
				climb.put("created", df.parse(df.format(climbing.getCreated())));
				climb.put("modified", df.parse(df.format(climbing.getModified())));
				climb.put("completedAt", df.parse(df.format(climbing.getCompleted())));
			} catch (java.text.ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			climb.put("completed_steps", climbing.getCompleted_steps());
			climb.put("remaining_steps", climbing.getRemaining_steps());
			climb.put("percentage", String.valueOf(climbing.getPercentage()));
			climb.put("users_id", climbing.getUser().getFBid());
			climb.put("game_mode", climbing.getGame_mode());
			climb.saveInBackground(new SaveCallback() {
				
				@Override
				public void done(ParseException e) {
					if(e == null){
						climbing.setId_online(climb.getObjectId());
						climbing.setSaved(true);
						ClimbApplication.climbingDao.update(climbing);
					}else{
						climbing.setSaved(false);
						ClimbApplication.climbingDao.update(climbing);
						Toast.makeText(getApplicationContext(), "Connection not available: your progresses will be saved during next reconnection", Toast.LENGTH_SHORT).show();
					}
					
				}
			});
		}
	}
	
	private void deleteClimbingInParse(final Climbing climbing){
		if(FacebookUtils.isOnline(this)){
			ParseQuery<ParseObject> query = ParseQuery.getQuery("Climbing");
			query.whereEqualTo("users_id", climbing.getUser().getFBid());
			query.whereEqualTo("building", climbing.getBuilding().get_id());
			query.whereEqualTo("id_mode", climbing.getId_mode());
			query.findInBackground(new FindCallback<ParseObject>() {

				@Override
				public void done(List<ParseObject> climbs, ParseException e) {
					if(e == null){ System.out.println("climb size " + climbs.size());
						if(climbs.size() != 0){ System.out.println("delete " + climbs.get(0).getString("id_mode"));
							climbs.get(0).deleteEventually();
							ClimbApplication.climbingDao.delete(climbing);	
						}
					}else{
						climbing.setSaved(false);
						climbing.setDeleted(true);
						ClimbApplication.climbingDao.update(climbing);
						Toast.makeText(getApplicationContext(), "Connection Problem", Toast.LENGTH_SHORT).show();
						Log.e("updateClimbingInParse", e.getMessage());
					}
				}
				
			});
		}
	}

	/**
	 * Update the given Climbing object in Parse
	 * @param myclimbing the given Climbing object
	 * 
	 */
	private void updateClimbingInParse(final Climbing myclimbing, boolean paused) {
		System.out.println("updateClimbingInParse " + myclimbing.getId_online());
		System.out.println(pref.getString("FBid", "none"));
		final SharedPreferences pref = getApplicationContext().getSharedPreferences("UserSession", 0);
		if (FacebookUtils.isOnline(this)) {
			ParseQuery<ParseObject> query = ParseQuery.getQuery("Climbing");
		/*	query.whereEqualTo("users_id", myclimbing.getUser().getFBid());
			query.whereEqualTo("building", myclimbing.getBuilding().get_id());
			if(!paused)
				query.whereEqualTo("id_mode", myclimbing.getId_mode());
			else
				query.whereEqualTo("id_mode", "paused");

			System.out.println("CERCO: " + myclimbing.getId_mode());*/
			if(!(myclimbing.getId_online() == null) || !myclimbing.getId_online().equalsIgnoreCase(""))
				query.whereEqualTo("objectId", myclimbing.getId_online());
			else{
				query.whereEqualTo("users_id", myclimbing.getUser().getFBid());
				query.whereEqualTo("building", myclimbing.getBuilding().get_id());
				query.whereEqualTo("game_mode", myclimbing.getGame_mode());
			}
			query.findInBackground(new FindCallback<ParseObject>() {

				@Override
				public void done(List<ParseObject> climbs, ParseException e) {
					if (e == null && climbs.size() > 0) {
						ParseObject c = climbs.get(0);
						DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
						df.setTimeZone(new SimpleTimeZone(0, "GMT"));
						try {
							c.put("created", df.parse(df.format(myclimbing.getCreated())));
							c.put("modified", df.parse(df.format(myclimbing.getModified())));
							c.put("completedAt", df.parse(df.format(myclimbing.getCompleted())));
						} catch (java.text.ParseException ex) {
							// TODO Auto-generated catch block
							ex.printStackTrace();
						}
						c.put("completed_steps", myclimbing.getCompleted_steps());
						c.put("remaining_steps", myclimbing.getRemaining_steps());
						c.put("percentage", String.valueOf(myclimbing.getPercentage()));
						if(myclimbing.getId_mode() == null)
							c.put("id_mode", "");
						else
							c.put("id_mode", myclimbing.getId_mode());
						c.put("game_mode", myclimbing.getGame_mode());
						c.saveEventually();
						myclimbing.setSaved(true);
						ClimbApplication.climbingDao.update(myclimbing);
					} else {
						myclimbing.setSaved(false);
						ClimbApplication.climbingDao.update(myclimbing);
						Toast.makeText(getApplicationContext(), "Connection Problem", Toast.LENGTH_SHORT).show();
						Log.e("updateClimbingInParse", e.getMessage());
					}
				}
			});
		} else {
			myclimbing.setSaved(false);
			ClimbApplication.climbingDao.update(myclimbing);
			Toast.makeText(getApplicationContext(), "Connection unavailable. Yout data will be saved during next connection", Toast.LENGTH_SHORT).show();

		}
	}

	/**
	 * Check (and load) if a climbing exists for the given building
	 * 
	 * @throws ClimbingNotFound
	 */
	private void loadPreviousClimbing() {
		SharedPreferences pref = getApplicationContext().getSharedPreferences("UserSession", 0);
		//climbing = MainActivity.getClimbingForBuildingAndUserNotPaused(building.get_id(), pref.getInt("local_id", -1));
		//System.out.println(climbing.getId_mode());
		//soloClimb = MainActivity.getClimbingForBuildingAndUserPaused(building.get_id(), pref.getInt("local_id", -1));
		//System.out.println(soloClimb.getId_mode());
		
		List<Climbing> climbs = ClimbApplication.getClimbingListForBuildingAndUser(building.get_id(), pref.getInt("local_id", -1));
		if(climbs.size() == 1){
			climbing = climbs.get(0);
		} else if(climbs.size() == 2){
			for(Climbing c : climbs){
				if(c.getGame_mode() == 0)
					soloClimb = c;
				else if(c.getGame_mode() == 2 || c.getGame_mode() == 3)
					climbing = c;
			}
		}
		
		
		if (climbing == null) { // no climbing found 
			Log.i(MainActivity.AppName, "No previous climbing found");
			num_steps = 0;
			percentage = 0;
			climbing = new Climbing(); // create a new empty climbing for this
										// building
			climbing.setBuilding(building);
			climbing.setCompleted(0);
			climbing.setRemaining_steps(building.getSteps());
			climbing.setCompleted_steps(num_steps);
			climbing.setCreated(new Date().getTime());
			climbing.setModified(new Date().getTime());
			climbing.setGame_mode(0);
			String FBid = pref.getString("FBid", "");
			if (FBid.equals(""))
				climbing.setUser(ClimbApplication.getUserById(pref.getInt("local_id", -1)));
			else
				climbing.setUser(ClimbApplication.getUserByFBId(FBid));
		/*	switch(GameModeType.values()[climbing.getGame_mode()]){
			case SOLO_CLIMB:
				climbing.setId_mode("");
				break;
			case SOCIAL_CLIMB:
				climbing.setId_mode(collaboration.getId());
				break;
			case SOCIAL_CHALLENGE:
				climbing.setId_mode(competition.getId_online());
				break;
			case TEAM_VS_TEAM:
				break;
			}*/

			ClimbApplication.climbingDao.create(climbing);

			saveClimbingToParse(climbing);
			Log.i(MainActivity.AppName, "Created new climbing #" + climbing.get_id());
		} else {
			Log.d(MainActivity.AppName, "uno vecchio trovato");
			System.out.println("paused? " + climbing.getId_mode());

			num_steps = climbing.getCompleted_steps();
			percentage = climbing.getPercentage();
			Log.i(MainActivity.AppName, "Loaded existing climbing (#" + climbing.get_id() + ")");
		}
		threshold = building.getSteps() / ClimbApplication.N_MEMBERS_PER_GROUP;
		seekbarIndicator.setMax(building.getSteps());
		if (mode == GameModeType.SOCIAL_CLIMB)
			seekbarIndicator.setProgress(climbing.getCompleted_steps() + sumOthersStep());
		else if(mode == GameModeType.TEAM_VS_TEAM){
			seekbarIndicator.setProgress(climbing.getCompleted_steps() + myTeamScore());
			secondSeekbar.setMax(building.getSteps());
		}
		else
			seekbarIndicator.setProgress(climbing.getCompleted_steps());

		// per le competizioni setta la seconda seek bar

		updateStats();
		if (percentage >= 1.00) { // building already climbed
			findViewById(R.id.lblReadyToClimb).setVisibility(View.GONE);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
			((TextView) findViewById(R.id.lblWin)).setText("ALREADY CLIMBED ON " + sdf.format(new Date(climbing.getCompleted())));
			apply_win();
		} else { // building to be completed
			// animate "ready to climb" text
			Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.abc_slide_in_top);
			Animation arrowAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.arrow);
			anim.setDuration(2500);
			findViewById(R.id.lblReadyToClimb).startAnimation(anim);
			findViewById(R.id.imgArrow).startAnimation(arrowAnim);
			findViewById(R.id.lblReadyToClimb).setVisibility(View.VISIBLE);
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
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			// Show the Up button in the action bar.
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.itemUpdate:
			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			ImageView iv = (ImageView) inflater.inflate(R.layout.refresh, null);
			Animation rotation = AnimationUtils.loadAnimation(this, R.anim.rotate_refresh);
			rotation.setRepeatCount(Animation.INFINITE);
			iv.startAnimation(rotation);
			MenuItemCompat.setActionView(item, iv);
			System.out.println("animaz on");
			onUpdate();
			return true;
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			// TODO: If Settings has multiple levels, Up should navigate up
			// that hierarchy.
			// NavUtils.navigateUpFromSameTask(this);
			if (samplingEnabled == false)
				finish();
			else { // disable back button if sampling is enabled
				Toast.makeText(getApplicationContext(), "Sampling running - Stop it before exiting", Toast.LENGTH_SHORT).show();
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
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

	/**
	 * onClick listener for starting/stopping classifier
	 * 
	 * @param v
	 */
	public void onBtnStartClimbing(View v) {
		if (percentage >= 1.00) { // already win
			FacebookUtils fb = new FacebookUtils(this);
			try {
				fb.postToWall(climbing);
			} catch (NoFBSession e) {
				Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
				startActivity(intent);
			}
		} else {
			if (samplingEnabled) { // if sampling is enabled stop the classifier
				stopClassify();
				if (new_steps != 0)
					apply_update();
			} else { // if sampling is not enabled stop the classifier
				climbedYesterday = StatUtils.climbedYesterday(climbing.get_id());
				// FOR TESTING PURPOSES
				// climbedYesterday=true;
				startClassifyService();
			}
		}
	}

	/**
	 * Make sure that all background services are stopped
	 */
	private void stopAllServices() {
		try {
			unregisterReceiver(sampleRateDetectorReceiver);
			stopService(backgroundSamplingRateDetector);
		} catch (Exception e) {
			Log.e(MainActivity.AppName, "Sampling rate service not running or unable to stop");
		}
		try {
			unregisterReceiver(classifierReceiver);
			stopService(backgroundClassifySampler);
		} catch (Exception e) {
			Log.e(MainActivity.AppName, "Classifier service not running or unable to stop");
		}
	}
	
	/**
	 * Called when Collabration ended and the user didn't pass the threshold.
	 * It modifies the game mode in 'Solo Climb' and deletes locally the Collaboration object
	 */
	//la collaborazione si  conclusa e non ho superato la soglia minima
	//quindi torno in social climb e elimino la collab localmente
	private void socialPenalty(){
		
			Toast.makeText(this.getApplicationContext(), "Move up next time!!!!", Toast.LENGTH_SHORT).show();
			((ImageButton) findViewById(R.id.btnStartClimbing)).setImageResource(R.drawable.social_share);
			mode = GameModeType.SOLO_CLIMB;
			climbing.setGame_mode(0);
			climbing.setId_mode("");
			updateClimbingInParse(climbing, false);
			saveCollaborationData();
			ClimbApplication.collaborationDao.delete(collaboration);
	}

	/**
	 * Stop background classifier service
	 */
	public void stopClassify() {
		
		stopService(backgroundClassifySampler); // stop background service
		samplingEnabled = false;
		unregisterReceiver(classifierReceiver); // unregister listener
		// update db
		climbing.setModified(new Date().getTime()); // update climbing last edit
													// date
		climbing.setCompleted_steps(num_steps); // update completed steps
		climbing.setPercentage(percentage); // update progress percentage
		climbing.setRemaining_steps(building.getSteps() - num_steps); // update
																		// remaining
																		// steps
		if (percentage >= 1.00 && (mode != GameModeType.SOCIAL_CHALLENGE) && (mode != GameModeType.TEAM_VS_TEAM))
			climbing.setCompleted(new Date().getTime());
		if(percentage >= 1.00){
			switch(mode){
			case SOCIAL_CLIMB: //come back in Solo Climb
				updateOthers(false);
				climbing.setGame_mode(0);
				climbing.setId_mode("");
				ClimbApplication.climbingDao.update(climbing); // save to db
				if(!pref.getString("FBid", "none").equalsIgnoreCase("none")) updateClimbingInParse(climbing, false);
				break;
			case SOCIAL_CHALLENGE:
				updateChart(false);		
				break;
			case TEAM_VS_TEAM:
				updateTeams(false);
				break;
			case SOLO_CLIMB:
				ClimbApplication.climbingDao.update(climbing); // save to db
				if(!pref.getString("FBid", "none").equalsIgnoreCase("none")) updateClimbingInParse(climbing, false);
				break;
			}
			/*climbing.setGame_mode(0);
			climbing.setId_mode("");*/
		}else{
			ClimbApplication.climbingDao.update(climbing); // save to db
			if(!pref.getString("FBid", "none").equalsIgnoreCase("none")) updateClimbingInParse(climbing, false);
		}
		
		Log.i(MainActivity.AppName, "Updated climbing #" + climbing.get_id());
		((ImageButton) findViewById(R.id.btnStartClimbing)).setImageResource(R.drawable.av_play); // set
																									// button
																									// icon
																									// to
																									// play
																									// again
		findViewById(R.id.progressBarClimbing).startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.abc_fade_out)); // hide
																																			// progress
																																			// bar
		findViewById(R.id.progressBarClimbing).setVisibility(View.INVISIBLE);

		if (mode == GameModeType.SOCIAL_CLIMB && collaboration != null)
			saveCollaborationData();
		else if (mode == GameModeType.SOCIAL_CHALLENGE && competition != null)
			saveCompetitionData();
		else if (mode == GameModeType.TEAM_VS_TEAM && teamDuel != null)
			saveTeamDuelData();
		
		updatePoints();
		saveBadges();
	}

	/**
	 * Saves the data about current Collaboration object in Parse if possible, 
	 * otherwise it remembers to save the updates when connetction return available.
	 */
	private void saveCollaborationData() {
		collaboration.setMy_stairs(climbing.getCompleted_steps());
		if (collab_parse == null) {
			collaboration.setSaved(false);
			Toast.makeText(getApplicationContext(), "Connection unavailable. Yout data will be saved during next connection", Toast.LENGTH_SHORT).show();
		} else {
			SharedPreferences pref = getSharedPreferences("UserSession", 0);
			JSONObject stairs = collab_parse.getJSONObject("stairs");
			try {
				stairs.put(pref.getString("FBid", ""), collaboration.getMy_stairs());
				collab_parse.put("stairs", stairs);
				if(percentage >= 1.00){
					System.out.println("elimino collab");
					collab_parse.put("completed", true);
					collaboration.setCompleted(true);
				}
				collab_parse.saveEventually();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			collaboration.setSaved(true);
		}
		if(collaboration.isCompleted())
			ClimbApplication.collaborationDao.delete(collaboration);
		else
			ClimbApplication.collaborationDao.update(collaboration);


	}
	
	private void saveCompetitionData() {
		competition.setMy_stairs(climbing.getCompleted_steps());
		if (compet_parse == null) {
			competition.setSaved(false);
			Toast.makeText(getApplicationContext(), "Connection unavailable. Yout data will be saved during next connection", Toast.LENGTH_SHORT).show();
		} else {
			SharedPreferences pref = getSharedPreferences("UserSession", 0);
			JSONObject stairs = compet_parse.getJSONObject("stairs");
			try {
				stairs.put(pref.getString("FBid", ""), competition.getMy_stairs());
				compet_parse.put("stairs", stairs);
				if(percentage >= 1.00){
					System.out.println("elimino collab");
					compet_parse.put("completed", true);
					competition.setCompleted(true);
				}
				compet_parse.saveEventually();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			competition.setSaved(true);
		}
		if(competition.isCompleted())
			ClimbApplication.competitionDao.delete(competition);
		else
			ClimbApplication.competitionDao.update(competition);

	}
	
	private void saveTeamDuelData(){
		teamDuel.setMy_steps(climbing.getCompleted_steps());
		if (teamDuel_parse == null) {
			teamDuel.setSaved(false);
			Toast.makeText(getApplicationContext(), "Connection unavailable. Yout data will be saved during next connection", Toast.LENGTH_SHORT).show();
		} else {
			SharedPreferences pref = getSharedPreferences("UserSession", 0);
			JSONObject myteam;
			if(teamDuel.getMygroup() == Group.CHALLENGER)
				myteam = teamDuel_parse.getJSONObject("challenger_stairs");
			else
				myteam = teamDuel_parse.getJSONObject("creator_stairs");
			try {
				myteam.put(pref.getString("FBid", ""),  teamDuel.getMy_steps());
				if(percentage >= 1.00){
					System.out.println("elimino team duel");
					teamDuel_parse.put("completed", true);
					teamDuel.setCompleted(true);
				}
				teamDuel_parse.saveEventually();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			teamDuel.setSaved(true);
		}
		if(teamDuel.isCompleted())
			ClimbApplication.teamDuelDao.delete(teamDuel);
		else
			ClimbApplication.teamDuelDao.update(teamDuel);
	}

	/**
	 * Start background classifier service
	 */
	public void startClassifyService() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()); // get
																												// reference
																												// to
																												// android
																												// preferences
		/*int */difficulty = Integer.parseInt(settings.getString("difficulty", "10")); // get
																					// difficulty
																					// from
																					// preferences
		switch (difficulty) { // set several parameters related to difficulty
		case 100: // easy
			Log.i(MainActivity.AppName, "Selected difficulty: EASY");
			vstep_for_rstep = 100;
			break;
		case 1: // hard
			Log.i(MainActivity.AppName, "Selected difficulty: HARD");
			vstep_for_rstep = 1;
			break;
		default: // normal and default
			Log.i(MainActivity.AppName, "Selected difficulty: NORMAL");
			vstep_for_rstep = 10;
			break;
		}
		Log.i(MainActivity.AppName, "Using " + vstep_for_rstep + " steps for each real step");
		startService(backgroundClassifySampler); // start background service
		registerReceiver(classifierReceiver, classifierFilter); // register
																// listener
		samplingEnabled = true;
		((ImageButton) findViewById(R.id.btnStartClimbing)).setImageResource(R.drawable.av_pause); // set
																									// button
																									// image
																									// to
																									// stop
																									// service
		findViewById(R.id.lblReadyToClimb).setVisibility(View.GONE);
		findViewById(R.id.encouragment).setVisibility(View.INVISIBLE);
		findViewById(R.id.btnAccessPhotoGallery).setVisibility(View.INVISIBLE);
		findViewById(R.id.progressBarClimbing).startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.abc_fade_in));
		findViewById(R.id.progressBarClimbing).setVisibility(View.VISIBLE);
	}

	@Override
	protected void onResume() {
		Log.i(MainActivity.AppName, "ClimbActivity onResume");
		int building_id = getIntent().getIntExtra(ClimbApplication.building_intent_object, 0); // get
																							// building
																							// id
																							// from
																							// received
																							// intent
		Log.i(MainActivity.AppName, "Building id: " + building_id);
		super.onResume();
	}

	@Override
	protected void onPause() {
		Log.i(MainActivity.AppName, "ClimbActivity onPause");
		super.onPause();
		this.finish();
	}

	@Override
	protected void onDestroy() {
		Log.i(MainActivity.AppName, "ClimbActivity onDestroy");
		stopAllServices(); // make sure to stop all background services
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		if (samplingEnabled == false)
			super.onBackPressed();
		else { // disable back button if sampling is enabled
			Toast.makeText(getApplicationContext(), "Sampling running - Stop it before exiting", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.climb, menu);
		// We should save our menu so we can use it to reset our updater.
				mymenu = menu;
		return true;
	}

	/**
	 * Action for update button pressed
	 * @param v
	 */
	public void onUpdate(/*MenuItem v*/) {
		switch(climbing.getGame_mode()){
		case 0:
			resetUpdating();
			break;
		case 1:
			updateOthers(true);
			break;
		case 2:
			updateChart(true);
			break;
		case 3:
			updateTeams(true);
			break;
				
		}
	}
	
	public void resetUpdating() {
		// Get our refresh item from the menu
		MenuItem m = mymenu.findItem(R.id.itemUpdate);
		if (MenuItemCompat.getActionView(m) != null) {
			// Remove the animation.
			MenuItemCompat.getActionView(m).clearAnimation();
			MenuItemCompat.setActionView(m, null);
		}
	}
	
	private void endCompetition(){
		Log.d("END COMPETITION", "fineeee");

		if(soloClimb != null){ System.out.println("non scalato per la prima volta " + climbing.getId_mode()); System.out.println("soloclimb: " + soloClimb.getId_mode());
		deleteClimbingInParse(climbing);
			/*if(climbing.getCompleted() != 0){
			//ho giˆ scalato l'edificio una volta, quindi lo lascio scalato per intero
			climbing.setCompleted_steps(building.getSteps());
			climbing.setRemaining_steps(0);
			climbing.setPercentage(100);
			
			}*/
			soloClimb.setGame_mode(0);
			soloClimb.setId_mode("");
			ClimbApplication.climbingDao.update(soloClimb);
			updateClimbingInParse(soloClimb, true);
		}else{
		System.out.println("scalato x prima volta");
		climbing.setGame_mode(0);
		climbing.setId_mode("");
		ClimbApplication.climbingDao.update(climbing);
		updateClimbingInParse(climbing, false);
		((ImageButton) findViewById(R.id.btnStartClimbing)).setImageResource(R.drawable.social_share);		
		}
	}
	
	private void endTeamCompetition(){
		if(soloClimb != null){
			deleteClimbingInParse(climbing);
			soloClimb.setGame_mode(0);
			soloClimb.setId_mode("");
			ClimbApplication.climbingDao.update(soloClimb);
			updateClimbingInParse(soloClimb, true);
		}else{
		System.out.println("scalato x prima volta");
		climbing.setGame_mode(0);
		climbing.setId_mode("");
		ClimbApplication.climbingDao.update(climbing);
		updateClimbingInParse(climbing, false);
		((ImageButton) findViewById(R.id.btnStartClimbing)).setImageResource(R.drawable.social_share);	
		}
	}
	
	//non ho superato la threshold
	private void socialTeamPenality(){
		Toast.makeText(this.getApplicationContext(), "Move up next time!!!!", Toast.LENGTH_SHORT).show();
	}
	
	private void updateTeams(final boolean isUpdate){
		if (!(mode == GameModeType.SOLO_CLIMB) && FacebookUtils.isOnline(this)) {
			ParseQuery<ParseObject> query = ParseQuery.getQuery("TeamDuel");
			query.whereEqualTo("objectId", teamDuel.getId_online());
			query.findInBackground(new FindCallback<ParseObject>() {

				@Override
				public void done(List<ParseObject> tds, ParseException e) {
					if(e == null){
						if (tds == null || tds.size() == 0) {
							Toast.makeText(getApplicationContext(), "This team duel does not exists", Toast.LENGTH_SHORT).show();
							Log.e("updateTeams", "TeamDuel not present in Parse");
							// TODO: la elimino????	
						} else {
							SharedPreferences pref = getApplicationContext().getSharedPreferences("UserSession", 0);
							teamDuel_parse = tds.get(0);
							JSONObject myTeam;
							JSONObject otherTeam;
							if(teamDuel.getMygroup() == Group.CHALLENGER){
								myTeam = teamDuel_parse.getJSONObject("challenger_stairs");
								otherTeam = teamDuel_parse.getJSONObject("creator_stairs");
								try {
									myTeam.put(pref.getString("FBid", ""), num_steps);
								} catch (JSONException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
								teamDuel_parse.put("challenger_stairs", myTeam);
								teamDuel_parse.saveEventually();
							}else{
								myTeam = teamDuel_parse.getJSONObject("creator_stairs");
								otherTeam = teamDuel_parse.getJSONObject("challenger_stairs");
								try {
									myTeam.put(pref.getString("FBid", ""), num_steps);
								} catch (JSONException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
								teamDuel_parse.put("creator_stairs", myTeam);
								teamDuel_parse.saveEventually();
							}
							Iterator<String> keys = myTeam.keys();
							while(keys.hasNext()){
								String key = keys.next();
								if(!key.equals(pref.getString("FBid", ""))){
									try {
										myTeamScores.add(myTeam.getInt(key));
									} catch (JSONException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
								}
							}
							int myGroupScore = ModelsUtil.sum(myTeam);
							int otherGroupScore = ModelsUtil.sum(otherTeam);
							teamDuel.setSteps_my_group(myGroupScore);
							teamDuel.setSteps_other_group(otherGroupScore);
							teamDuel.setMy_steps(num_steps);
							ClimbApplication.teamDuelDao.update(teamDuel);
							if(teamDuel.getMygroup() == Group.CHALLENGER){
								group_members.get(0).setText("Team of " + teamDuel.getChallenger_name());
								group_steps.get(0).setText(String.valueOf(teamDuel.getSteps_my_group()));
								group_members.get(1).setText("Team of " + teamDuel.getCreator_name());
								group_steps.get(1).setText(String.valueOf(teamDuel.getSteps_other_group()));
							}else{
								group_members.get(0).setText("Team of " + teamDuel.getCreator_name());
								group_steps.get(0).setText(String.valueOf(teamDuel.getSteps_my_group()));
								group_members.get(1).setText("Team of " + teamDuel.getChallenger_name());
								group_steps.get(1).setText(String.valueOf(teamDuel.getSteps_other_group()));
							}
							group_members.get(0).setBackgroundColor(Color.parseColor("#adeead"));
							group_steps.get(0).setBackgroundColor(Color.parseColor("#adeead"));
							group_members.get(1).setBackgroundColor(Color.parseColor("#ef9d9d"));
							group_steps.get(1).setBackgroundColor(Color.parseColor("#ef9d9d"));
							seekbarIndicator.setProgress(myTeamScore());
							secondSeekbar.setProgress(ModelsUtil.sum(otherTeam));
							
							if(myGroupScore >= building.getSteps()){
								Toast.makeText(getApplicationContext(), "Your team has won!!!!", Toast.LENGTH_SHORT).show();
								endTeamCompetition();
								if (teamDuel.getMy_steps() < threshold)
									socialTeamPenality();
								saveBadges();
							}else if(otherGroupScore >= building.getSteps()){
								Toast.makeText(getApplicationContext(), "The challenger's team has won", Toast.LENGTH_SHORT).show();
								endTeamCompetition();
								if (teamDuel.getMy_steps() < threshold)
									socialTeamPenality();
							}

							
						}
					}else if(!(mode == GameModeType.TEAM_VS_TEAM)) {
						Toast.makeText(getApplicationContext(), "Connections Problem", Toast.LENGTH_SHORT).show();
						Log.e("updateTeams", e.getMessage());
					}
					if(isUpdate){
						System.out.println("animaz off");
						resetUpdating();
					}
					
				}
				
			});
		} else {
			Toast.makeText(getApplicationContext(), "No Connection Available", Toast.LENGTH_SHORT).show();
			if(isUpdate){
				System.out.println("animaz off");
				resetUpdating();
			}
		}
		
		
	}
	
	private void updateChart(final boolean isUpdate) {
		System.out.println("update chart");
		if (!(mode == GameModeType.SOLO_CLIMB) && FacebookUtils.isOnline(this)) {
			ParseQuery<ParseObject> query = ParseQuery.getQuery("Competition");
			query.whereEqualTo("objectId", competition.getId_online());
			query.findInBackground(new FindCallback<ParseObject>() {

				@Override
				public void done(List<ParseObject> compets, ParseException e) {
					if (e == null) {
						if (compets == null || compets.size() == 0) {
							Toast.makeText(getApplicationContext(), "This competition does not exists", Toast.LENGTH_SHORT).show();
							Log.e("updatechart", "Competition not present in Parse");
							// delete this collaboration
							ClimbApplication.competitionDao.delete(competition);
						} else {
							System.out.println("id: " + competition.getId_online());
							SharedPreferences pref = getApplicationContext().getSharedPreferences("UserSession", 0);
							compet_parse = compets.get(0);
							JSONObject others = compet_parse.getJSONObject("stairs");
							JSONObject othersName = compet_parse.getJSONObject("competitors");
							try {
								others.put(pref.getString("FBid", ""), num_steps);
							} catch (JSONException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							compet_parse.put("stairs", others);
							compet_parse.saveEventually();
							Iterator members = others.keys();
							int i = 0;
							chart = ModelsUtil.fromJsonToChart(others);
							for (ChartMember entry : chart){
								String key = entry.getId();
									System.out.println(key);
									System.out.println(pref.getString("FBid", ""));
									String name = "";
									int steps = -1;
									try {
										name = (String) othersName.getString(key);
										steps = entry.getScore();//(Integer) others.getInt(key);
									} catch (JSONException e2) {
										// TODO Auto-generated catch block
										e2.printStackTrace();
									}
									group_members.get(i).setText(name);
									group_steps.get(i).setVisibility(View.VISIBLE);
									group_steps.get(i).setText(String.valueOf(steps));
									group_members.get(i).setClickable(false);
									group_members.get(i).setVisibility(View.VISIBLE);
									group_members.get(i).setBackgroundColor(Color.parseColor("#dcdcdc"));
									group_steps.get(i).setBackgroundColor(Color.parseColor("#dcdcdc"));

							
							if (key.equalsIgnoreCase(pref.getString("FBid", ""))) {
								group_members.get(i).setBackgroundColor(Color.parseColor("#f7fe2e"));
								group_steps.get(i).setBackgroundColor(Color.parseColor("#f7fe2e"));
								if(steps >= building.getSteps()){
									percentage = 1.0;
									endCompetition();
									saveBadges();
									Toast.makeText(getApplicationContext(),  "Yeah!!!! You won this challenge", Toast.LENGTH_SHORT).show();
								}
							}else{
								if(steps >= building.getSteps()){
									endCompetition();
									Toast.makeText(getApplicationContext(), name + " won this challenge", Toast.LENGTH_SHORT).show();
								}
							}
							i++;

							}
							if(i < group_members.size() && i <= ClimbApplication.N_MEMBERS_PER_GROUP){
								group_steps.get(i).setVisibility(View.INVISIBLE);
								group_members.get(i).setClickable(true);
								group_members.get(i).setText("  +");
								group_members.get(i).setVisibility(View.VISIBLE);
								group_members.get(i).setBackgroundColor(Color.parseColor("#dcdcdc"));
								group_members.get(i).setOnClickListener(new OnClickListener() {
								
							
								
								@Override
								public void onClick(View arg0) {
									Bundle params = new Bundle();
									params.putString("data", "{\"idCollab\":\"" + competition.getId_online() + "\"," + "\"idBuilding\":\"" + building.get_id() + "\"," + "\"nameBuilding\":\"" + building.getName() + "\", \"type\": \"2\"}");
									params.putString("message", "Please, help me!!!!");
									if(Session.getActiveSession() != null && Session.getActiveSession().isOpened()){

									WebDialog requestsDialog = (new WebDialog.RequestsDialogBuilder(ClimbActivity.this, Session.getActiveSession(), params)).setOnCompleteListener(new OnCompleteListener() {

										@Override
										public void onComplete(Bundle values, FacebookException error) {
											if (error != null) {
												if (error instanceof FacebookOperationCanceledException) {
													Toast.makeText(ClimbActivity.this, "Request cancelled", Toast.LENGTH_SHORT).show();
													
												} else {
													Toast.makeText(ClimbActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
							
												}
											} else {
												final String requestId = values.getString("request");
												if (requestId != null) {
													Toast.makeText(ClimbActivity.this, "Request sent", Toast.LENGTH_SHORT).show();
												} else {
													Toast.makeText(ClimbActivity.this, "Request cancelled", Toast.LENGTH_SHORT).show();
									

												}
											}
										}

									}).build();
									requestsDialog.show();
									}else{
										Toast.makeText(ClimbActivity.this, "Currently not logged to FB", Toast.LENGTH_SHORT).show();
									}
								}
							});
							i++;
							}
							for(; i < group_members.size(); i++){
								group_members.get(i).setClickable(false);
								group_members.get(i).setVisibility(View.INVISIBLE);
								group_steps.get(i).setVisibility(View.INVISIBLE);

							}

						}
					} else if(!(mode == GameModeType.SOCIAL_CLIMB)) {
						Toast.makeText(getApplicationContext(), "Connections Problem", Toast.LENGTH_SHORT).show();
						Log.e("updateChart", e.getMessage());
					}
					if(isUpdate){
						System.out.println("animaz off");
						resetUpdating();
					}
				}
			});
		} else {
			Toast.makeText(getApplicationContext(), "No Connection Available", Toast.LENGTH_SHORT).show();
			if(isUpdate){
				System.out.println("animaz off");
				resetUpdating();
			}
		}
		
	}
	
	private void updatePoints(){
		int realSteps = new_steps / difficulty;
		int newXP = 0;
		switch(difficulty){
		case 1://5
			newXP = realSteps * 10;
			break;
		case 10:
			newXP = realSteps * 3;
			break;
		case 100://1
			newXP = realSteps;
			break;
		}
		final User me = ClimbApplication.getUserById(pref.getInt("local_id", -1));
		int newLevel = ClimbApplication.levelUp(newXP, me.getLevel());
		me.setXP(me.getXP() + newXP);
		if(newLevel != me.getLevel())
			me.setLevel(newLevel);
		ClimbApplication.userDao.update(me);
		ParseQuery<ParseUser> query = ParseUser.getQuery();
		query.whereEqualTo("FBid", me.getFBid());
		query.getFirstInBackground(new GetCallback<ParseUser>() {

			@Override
			public void done(ParseUser user, ParseException e) {
				if(e == null){
					user.put("XP", me.getXP());
					user.put("level", me.getLevel());
					user.saveEventually();
				}else{
					Toast.makeText(getApplicationContext(), "Connection Available. Your data will be saved during next connection", Toast.LENGTH_SHORT).show();
					Log.e("updatePoints", e.getMessage());
				}
				
			}
		});
	}
	
	private void saveBadges(){
		User me = ClimbApplication.getUserById(pref.getInt("local_id", -1));
		Badge badge = ClimbApplication.getBadgeByCategory(0);
		UserBadge userbadge = ClimbApplication.getUserBadgeForUserAndBadge(badge.get_id(), building.get_id(), pref.getInt("local_id", -1));
		if(userbadge == null){
			userbadge = new UserBadge();
			userbadge.setBadge(badge);
			userbadge.setObj_id(building.get_id());
			userbadge.setPercentage(percentage);
			userbadge.setUser(me);
			ClimbApplication.userBadgeDao.create(userbadge);
		}else{
			userbadge.setPercentage(percentage);
			ClimbApplication.userBadgeDao.update(userbadge);
		}
		final UserBadge ub = userbadge;
		ParseQuery<ParseUser> query = ParseUser.getQuery();
		query.whereEqualTo("FBid", pref.getString("FBid", ""));
		query.getFirstInBackground(new GetCallback<ParseUser>() {
			
			@Override
			public void done(ParseUser user, ParseException e) {
				if(e == null){
					JSONObject newBadge = new JSONObject();
					try {
						JSONArray bs = user.getJSONArray("badges");
						newBadge.put("badge_id", ub.getBadge().get_id());
						newBadge.put("obj_id", ub.getObj_id());
						newBadge.put("percentage", ub.getPercentage());
						bs.put(newBadge);
						user.put("badges", bs);
						user.saveEventually();
						ub.setSaved(true);
						ClimbApplication.userBadgeDao.update(ub);
					} catch (JSONException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}else{
					ub.setSaved(false);
					ClimbApplication.userBadgeDao.update(ub);
					Toast.makeText(getApplicationContext(), "Connection Available. Your data will be saved during next connection", Toast.LENGTH_SHORT).show();
					Log.e("saveBadges", e.getMessage());
				}
			}
		});
		
	}
}
