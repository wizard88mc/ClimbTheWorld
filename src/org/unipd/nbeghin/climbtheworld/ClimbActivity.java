package org.unipd.nbeghin.climbtheworld;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.unipd.nbeghin.climbtheworld.R;
import org.unipd.nbeghin.climbtheworld.activity.recognition.ActivityRecognitionIntentService;
import org.unipd.nbeghin.climbtheworld.exceptions.ClimbingNotFound;
import org.unipd.nbeghin.climbtheworld.exceptions.NoFBSession;
import org.unipd.nbeghin.climbtheworld.listeners.AccelerometerSamplingRateDetect;
import org.unipd.nbeghin.climbtheworld.models.Alarm;
import org.unipd.nbeghin.climbtheworld.models.Building;
import org.unipd.nbeghin.climbtheworld.models.ClassifierCircularBuffer;
import org.unipd.nbeghin.climbtheworld.models.Climbing;
import org.unipd.nbeghin.climbtheworld.receivers.StairsClassifierReceiver;
import org.unipd.nbeghin.climbtheworld.services.ActivityRecognitionRecordService;
import org.unipd.nbeghin.climbtheworld.services.SamplingClassifyService;
import org.unipd.nbeghin.climbtheworld.services.SamplingRateDetectorService;
import org.unipd.nbeghin.climbtheworld.util.AlarmUtils;
import org.unipd.nbeghin.climbtheworld.util.CountDownTimerPausable;
import org.unipd.nbeghin.climbtheworld.util.FacebookUtils;
import org.unipd.nbeghin.climbtheworld.util.GeneralUtils;
import org.unipd.nbeghin.climbtheworld.util.StatUtils;
import org.unipd.nbeghin.climbtheworld.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.backup.RestoreObserver;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.opengl.Visibility;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.HoloCircleSeekBar;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VerticalSeekBar;

/**
 * Climbing activity: shows a given building and starts classifier. At start it calculates the sampling rate of the device it's run from (only once, after that it just saves the value in standard Android preferences)
 * 
 */
public class ClimbActivity extends Activity {
	public static final String		SAMPLING_TYPE				= "ACTION_SAMPLING";														// intent's action
	public static final String		SAMPLING_TYPE_NON_STAIR		= "NON_STAIR";																// classifier's output
	public static final String		SAMPLING_DELAY				= "DELAY";																	// intent's action
	public static boolean			samplingEnabled				= false;	//aggiunto static e public															// sentinel if sampling is running
	private static double			detectedSamplingRate		= 0;																		// detected sampling rate (after sampling rate detector)
	private static int				samplingDelay;																							// current sampling delay (SensorManager)
	private double					minimumSamplingRate			= 13;																		// minimum sampling rate for using this app
	private Intent					backgroundClassifySampler;																				// classifier background service intent
	private Intent					backgroundSamplingRateDetector;																		// sampling rate detector service intent
	//private IntentFilter			classifierFilter			= new IntentFilter(ClassifierCircularBuffer.CLASSIFIER_ACTION);			// intent filter (for BroadcastReceiver)
	private IntentFilter			samplingRateDetectorFilter	= new IntentFilter(AccelerometerSamplingRateDetect.SAMPLING_RATE_ACTION);	// intent filter (for BroadcastReceiver)
	//private BroadcastReceiver		classifierReceiver			= new ClassifierReceiver();												// implementation of BroadcastReceiver for classifier service
	private BroadcastReceiver		sampleRateDetectorReceiver	= new SamplingRateDetectorReceiver();										// implementation of BroadcastReceiver for sampling rate detector
	private int						num_steps					= 0;																		// number of currently detected steps
	private double					percentage					= 0.0;																		// current progress percentage
	private Building				building;																								// current building
	private Climbing				climbing;																								// current climbing
	private VerticalSeekBar			seekbarIndicator;																						// reference to vertical seekbar
	private int						vstep_for_rstep				= 1;
	private boolean					used_bonus					= false;
	private double					percentage_bonus			= 0.25f; //0.50f
	private boolean climbedYesterday=false;
	
	// number of virtual step for each real step
	/**
	 * Whether or not the system UI should be auto-hidden after {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
	 */
	private static final boolean	AUTO_HIDE					= true;
	/**
	 * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after user interaction before hiding the system UI.
	 */
	private static final int		AUTO_HIDE_DELAY_MILLIS		= 3000;
	/**
	 * If set, will toggle the system UI visibility upon interaction. Otherwise, will show the system UI visibility upon interaction.
	 */
	private static final boolean	TOGGLE_ON_CLICK				= true;
	/**
	 * The flags to pass to {@link SystemUiHider#getInstance}.
	 */
	private static final int		HIDER_FLAGS					= 0;
	/**
	 * The instance of the {@link SystemUiHider} for this activity.
	 */
	private SystemUiHider			mSystemUiHider;

	
	//altri campi per visualizzare opportune stringhe di testo ed elementi grafici
	private static long time_bk = 7 * 1000;		
	private static HoloCircleSeekBar picker; //progress bar circolare che esegue il countdown		
	private static boolean hasFinished = false;
	private CountDownTimerPausable cp; //oggetto per implementare il countdown iniziale
	
	private boolean firstTimeStart = true;
	
	private static boolean stepsInGamePeriod = false;
	
	//application context
	private Context appContext;
	//reference to android preferences
	private SharedPreferences settings;
	
	
	//finestra di dialogo che mostra la non disponibilità della connessione dati
	private static AlertDialog.Builder alertBuilder;
	//relativo booleano
	private boolean alertIsShown = false;

	
	// public static double getDetectedSamplingRate() {
	// return detectedSamplingRate;
	// }
	
	/**
	 * Handles classifier service intents (STAIR/NON_STAIR)
	 * 
	 */
	/*
	public class ClassifierReceiver extends BroadcastReceiver {
		
		private static final double tradeoffG = 0.001;
		private static final double g = tradeoffG / (double)100;
		private List<Double> history = new ArrayList<Double>();
		private static final int historySize = 10;
		
		@Override
		public void onReceive(Context context, Intent intent) {
			//String result = intent.getExtras().getString(ClassifierCircularBuffer.CLASSIFIER_NOTIFICATION_STATUS);
			Double result = intent.getExtras().getDouble(ClassifierCircularBuffer.CLASSIFIER_NOTIFICATION_STATUS);
			
			double correction = 0.0;
			for (int indexHistory = 0; indexHistory < history.size(); indexHistory++) {
				correction += (100 / Math.pow(2, indexHistory + 1)) * (double)history.get(indexHistory) * g;
			}
			
			if (Double.isNaN(correction)) {
				correction = 0.0;
			}
			
			double finalClassification = result + correction;
			
			if (result * finalClassification >= 0) {
				if (history.size() == historySize) {
					history.remove(historySize - 1);
					history.add(0, (finalClassification > 0 ? 1.0 : -1.0));
				}
				else {
					history.add(0, (finalClassification > 0 ? 1.0 : -1.0));
				}
			}
			else {
				history.clear();
				history.add(result > 0 ? 1.0 : -1.0);
			}
			
			if (finalClassification > 0) {

				if (climbedYesterday && percentage > 0.25f && percentage < 0.50f && used_bonus == false) { // bonus at 25%
					apply_percentage_bonus();
				} else { // standard, no bonus
					num_steps += vstep_for_rstep; // increase the number of steps
					seekbarIndicator.setProgress(num_steps); // increase the seekbar progress
					percentage = (double) num_steps / (double) building.getSteps(); // increase the progress percentage
					boolean win = (num_steps >= building.getSteps()); // user wins?
					if (win) { // ensure it did not exceed the number of steps (when multiple steps-at-once are detected)
						num_steps = building.getSteps();
						percentage = 1.00;
					}
					updateStats(); // update the view of current stats
					if (win) {
						stopClassify(); // stop classifier service service
						apply_win();
					}
				}
				
				//l'utente ha fatto almeno uno scalino nel periodo di gioco corrente
				stepsInGamePeriod=true;
				Log.d(MainActivity.AppName,"ClimbActivity - STEP");
			}
			
			
			((TextView) findViewById(R.id.lblClassifierOutput)).setText(finalClassification > 0 ? "STAIR" : "NON_STAIR"); // debug: show currently detected classifier output
		}
	}
	*/
	
	

	
	public void refreshOnStep(){
		
		if (climbedYesterday && percentage > 0.25f && percentage < 0.50f && used_bonus == false) { // bonus at 25%
			apply_percentage_bonus();
		} else { // standard, no bonus
			num_steps += vstep_for_rstep; // increase the number of steps
			seekbarIndicator.setProgress(num_steps); // increase the seekbar progress
			percentage = (double) num_steps / (double) building.getSteps(); // increase the progress percentage
			boolean win = (num_steps >= building.getSteps()); // user wins?
			if (win) { // ensure it did not exceed the number of steps (when multiple steps-at-once are detected)
				num_steps = building.getSteps();
				percentage = 1.00;
			}
			updateStats(); // update the view of current stats
			if (win) {
				stopClassify(AlarmUtils.getAlarm(appContext, settings.getInt("alarm_id", -1)), settings.getInt("artificialDayIndex", 0)); // stop classifier service service
				apply_win();
			}
		}
		
		//l'utente ha fatto almeno uno scalino nel periodo di gioco corrente
		stepsInGamePeriod=true;
		Log.d(MainActivity.AppName,"ClimbActivity - STEP");
	}
	
	
	public void printClassification(double finalClassification){
		((TextView) findViewById(R.id.lblClassifierOutput)).setText(finalClassification > 0 ? "STAIR" : "NON_STAIR"); // debug: show currently detected classifier output
	}
	
	
	
	private void apply_percentage_bonus() {
		Log.i(MainActivity.AppName, "Applying percentage bonus");
		percentage += percentage_bonus;
		num_steps = (int) (((double) building.getSteps()) * percentage);
		//stopClassify(); //prima era attivo
		used_bonus = true;
		Toast.makeText(appContext, "BONUS: you climbed less than 24h ago, you earn +25%", Toast.LENGTH_LONG).show(); //+50%
		enableRocket();
		updateStats(); // update the view of current stats
		seekbarIndicator.setProgress(num_steps); // increase the seekbar progress
	}

	private void apply_win() {
		
		//si abilita il bottone di start nel caso la scalata sia già stata completata
		//(quindi non si passa prima per il countdown)
		findViewById(R.id.btnStartClimbing).setEnabled(true);
		
		Log.i(MainActivity.AppName, "Succesfully climbed building #"+building.get_id());
		Toast.makeText(appContext, "You successfully climbed " + building.getSteps() + " steps (" + building.getHeight() + "m) of " + building.getName() + "!", Toast.LENGTH_LONG).show(); // show completion text
		findViewById(R.id.lblWin).setVisibility(View.VISIBLE); // load and animate completed climbing test
		findViewById(R.id.lblWin).startAnimation(AnimationUtils.loadAnimation(appContext, R.anim.blink));
		((ImageButton) findViewById(R.id.btnStartClimbing)).setImageResource(R.drawable.social_share);
		findViewById(R.id.btnAccessPhotoGallery).startAnimation(AnimationUtils.loadAnimation(appContext, R.anim.abc_fade_in));
		findViewById(R.id.btnAccessPhotoGallery).setVisibility(View.VISIBLE);
	}

	private void enableRocket() {
		Animation animSequential = AnimationUtils.loadAnimation(appContext, R.anim.rocket);
		findViewById(R.id.imgRocket).startAnimation(animSequential);
	}

	/**
	 * Handles sampling rate detector service intents (STAIR/NON_STAIR)
	 * 
	 */
	public class SamplingRateDetectorReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			
			detectedSamplingRate = intent.getExtras().getDouble(AccelerometerSamplingRateDetect.SAMPLING_RATE); // get detected sampling rate from received intent
						
			samplingDelay = backgroundSamplingRateDetector.getExtras().getInt(SAMPLING_DELAY); // get used sampling delay from received intent
						
			Log.i(MainActivity.AppName, "Detected sampling rate: " + Double.toString(detectedSamplingRate) + "Hz");
			if (detectedSamplingRate >= minimumSamplingRate) { // sampling rate high enough
				SharedPreferences.Editor editor = settings.edit(); // get reference to android preferences
				editor.putFloat("detectedSamplingRate", (float) detectedSamplingRate); // store detected sampling rate
				editor.putInt("sensor_delay", samplingDelay); // store used sampling delay
				editor.apply(); // commit preferences
				Log.i(MainActivity.AppName, "Stored detected sampling rate of " + detectedSamplingRate + "Hz");
				Log.i(MainActivity.AppName, "Stored sampling delay of " + samplingDelay);
				stopService(backgroundSamplingRateDetector); // stop sampling rate detector service
				unregisterReceiver(this); // unregister listener
				setupByDetectedSamplingRate(); // setup app with detected sampling rate
			} else { // sampling rate not high enough: try to decrease the sampling delay
				if (backgroundSamplingRateDetector.getExtras().getInt(ClimbActivity.SAMPLING_DELAY) != SensorManager.SENSOR_DELAY_UI) { // decrease sampling delay in order to increase sampling rate
					Log.w(MainActivity.AppName, "Sampling rate not high enough: trying to decrease the sampling delay");
					stopService(backgroundSamplingRateDetector); // stop previous sampling rate detector service
					backgroundSamplingRateDetector.putExtra(SAMPLING_DELAY, SensorManager.SENSOR_DELAY_UI); // set new sampling delay (lower than the previous one)
					startService(backgroundSamplingRateDetector); // start new sampling rate detector service
				} else { // unable to determine a sampling rate high enough for our purposes: stop
					Log.e(MainActivity.AppName, "Sampling rate not high enough for this application");
					unregisterReceiver(this); // unregister listener
					stopService(backgroundSamplingRateDetector); // stop sampling rate detector service
					((TextView) findViewById(R.id.lblSamplingRateDetected)).setText("TOO LOW: " + (int) detectedSamplingRate + " Hz");
					AlertDialog.Builder alert = new AlertDialog.Builder(appContext);
					alert.setTitle("Sampling rate not high enough");
					alert.setMessage("Your accelerometer is not fast enough for this application. Make sure to use at least " + minimumSamplingRate + " Hz");
					alert.show();
				}
			}
		}
	}

	public void accessPhotoGallery(View v) {
		Log.i(MainActivity.AppName, "Accessing gallery for building "+building.get_id());
						
		//si controlla dapprima se è attiva una qualche connessione dati; se
		//non è attiva, si mostra un alert dialog
		if(GeneralUtils.isInternetConnectionUp(appContext)){
		    
			//TODO task per fare il load della gallery?
			
			Intent intent = new Intent(this, GalleryActivity.class);
			intent.putExtra("gallery_building_id", building.get_id());
			startActivity(intent);
		}
		else{
			alertBuilder.show();
			alertIsShown=true;
		}
		
		
		
	}

	/**
	 * Update the stat panel
	 */
	private void updateStats() {
		((TextView) findViewById(R.id.lblNumSteps)).setText(Integer.toString(num_steps) + " of " + Integer.toString(building.getSteps()) + " ("
				+ (new DecimalFormat("#.##")).format(percentage * 100.00) + "%)");
	}

	/**
	 * Setup the activity with a given sampling rate and sampling delay
	 */
	private void setupByDetectedSamplingRate() {
		backgroundClassifySampler.putExtra(AccelerometerSamplingRateDetect.SAMPLING_RATE, detectedSamplingRate);
		backgroundClassifySampler.putExtra(SAMPLING_DELAY, samplingDelay);
		// if (climbing.getPercentage() < 100) {
		// Toast.makeText(appContext, "Start climbing some stairs!", Toast.LENGTH_LONG).show();
		// }
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_climb);
		
		//si recupera il contesto dell'applicazione
		appContext = getApplicationContext();
		//si ottiene il riferimento alle shared preferences
		settings = PreferenceManager.getDefaultSharedPreferences(appContext);
				
		//all'inizio si disabilita il bottone di start (lo si abilita in seguito alla fine del
		//countdown oppure se la scalata è già stata completata)
		findViewById(R.id.btnStartClimbing).setEnabled(false);
				
		//si crea una finestra di dialogo da mostrare nel caso non ci sia connessione
        alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle(R.string.netalert_title)
            .setMessage(R.string.netalert_msg)
            .setCancelable(false)
            .setIcon(R.drawable.error)
            .setPositiveButton(R.string.netalert_yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                	startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                	alertIsShown=false;
                }
            })
            .setNegativeButton(R.string.netalert_no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                    alertIsShown=false;
                }
            }).create();
		
		setupActionBar();
		final View controlsView = findViewById(R.id.fullscreen_content_controls);
		final View contentView = findViewById(R.id.lblReadyToClimb);
		// Set up an instance of SystemUiHider to control the system UI for
		// this activity.
		mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);//contentView
		mSystemUiHider.setup();
		mSystemUiHider.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
			// Cached values.
			int	mControlsHeight;
			int	mShortAnimTime;

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
	    //findViewById(R.id.btnStartClimbing).setOnTouchListener(mDelayHideTouchListener);
		// app-specific logic
		seekbarIndicator = (VerticalSeekBar) findViewById(R.id.seekBarPosition); // get reference to vertical seekbar (only once for performance-related reasons)
		seekbarIndicator.setOnTouchListener(new OnTouchListener() { // disable user-driven seekbar changes
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						return true;
					}
				});
		int building_id = getIntent().getIntExtra(MainActivity.building_intent_object, 0); // get building id from received intent
		try {
			// get building ID from intent
			if (building_id == 0) throw new Exception("ERROR: unable to get intent data"); // no building id found in received intent
			building = MainActivity.buildingDao.queryForId(building_id); // query db to get asked building
			setup_from_building(); // load building info
			backgroundClassifySampler = new Intent(this, SamplingClassifyService.class); // instance (without starting) background classifier			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void onNewIntent (Intent intent) {
		int building_id = intent.getIntExtra(MainActivity.building_intent_object, 0); // get building id from received intent
		Log.i(MainActivity.AppName, "New intent building id: "+building_id);
	}
	
	/**
	 * Setup view with a given building and create/load an associated climbing
	 */
	private void setup_from_building() {
		int imageId = appContext.getResources().getIdentifier(building.getPhoto(), "drawable", appContext.getPackageName()); // get building's photo resource ID
		if (imageId > 0) ((ImageView) findViewById(R.id.buildingPhoto)).setImageResource(imageId);
		// set building info
		((TextView) findViewById(R.id.lblBuildingName)).setText(building.getName() + " (" + building.getLocation() + ")"); // building's location
		((TextView) findViewById(R.id.lblNumSteps)).setText(Integer.toString(building.getSteps()) + " steps"); // building's steps
		((TextView) findViewById(R.id.lblHeight)).setText(Integer.toString(building.getHeight()) + "mt"); // building's height (in mt)
		loadPreviousClimbing(); // get previous climbing for this building
	}

	/**
	 * Check (and load) if a climbing exists for the given building
	 * 
	 * @throws ClimbingNotFound
	 */
	private void loadPreviousClimbing() {
		climbing = MainActivity.getClimbingForBuilding(building.get_id());
		if (climbing == null) { // no climbing found
			Log.i(MainActivity.AppName, "No previous climbing found");
			num_steps = 0;
			percentage = 0;
			climbing = new Climbing(); // create a new empty climbing for this building
			climbing.setBuilding(building);
			climbing.setCompleted(0);
			climbing.setRemaining_steps(building.getSteps());
			climbing.setCompleted_steps(num_steps);
			climbing.setCreated(new Date().getTime());
			climbing.setModified(new Date().getTime());
			MainActivity.climbingDao.create(climbing);
			Log.i(MainActivity.AppName, "Created new climbing #" + climbing.get_id());
		} else {
			num_steps = climbing.getCompleted_steps();
			percentage = climbing.getPercentage();
			Log.i(MainActivity.AppName, "Loaded existing climbing (#" + climbing.get_id() + ")");
		}
		seekbarIndicator.setMax(building.getSteps());
		seekbarIndicator.setProgress(climbing.getCompleted_steps());

		updateStats();
		if (percentage >= 1.00) { // building already climbed
						
			findViewById(R.id.lblReadyToClimb).setVisibility(View.GONE);			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
			((TextView) findViewById(R.id.lblWin)).setText("ALREADY CLIMBED ON " + sdf.format(new Date(climbing.getCompleted())));
			apply_win();
		} else { // building to be completed
			// animate "ready to climb" text			
            Animation anim = AnimationUtils.loadAnimation(appContext, R.anim.ready_to_climb); //abc_slide_in_top
            //Animation arrowAnim = AnimationUtils.loadAnimation(appContext, R.anim.arrow);
            anim.setDuration(2500);
            findViewById(R.id.lblReadyToClimb).startAnimation(anim);
			//findViewById(R.id.imgArrow).startAnimation(arrowAnim);
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
//				NavUtils.navigateUpFromSameTask(this);
				if (samplingEnabled == false) finish();
				else { // disable back button if sampling is enabled
					Toast.makeText(appContext, "Sampling running - Stop it before exiting", Toast.LENGTH_SHORT).show();
				}
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Touch listener to use for in-layout UI controls to delay hiding the system UI. This is to prevent the jarring behavior of controls going away while interacting with activity UI.
	 */
	View.OnTouchListener mDelayHideTouchListener	= new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				if (AUTO_HIDE) {
					delayedHide(AUTO_HIDE_DELAY_MILLIS);
				}
				return false;
			}
		};
	Handler	mHideHandler			= new Handler();
	Runnable mHideRunnable			= new Runnable() {
		@Override
		public void run() {
			mSystemUiHider.hide();
		}
	};
	

	/**
	 * Schedules a call to hide() in [delay] milliseconds, canceling any previously scheduled calls.
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
				Intent intent = new Intent(appContext, SettingsActivity.class);
				startActivity(intent);
			}
		} else {
			
			//System.out.println("Click button - steps: "+StairsClassifierReceiver.getStepsNumber());
						
			//si recupera il prossimo alarm impostato
			int next_alarm_id = settings.getInt("alarm_id", -1);	
			Alarm next_alarm = AlarmUtils.getAlarm(appContext, next_alarm_id);
			
			//si recupera l'indice del giorno corrente all'interno della settimana
			/////////		
	    	//PER TEST ALGORITMO
			int current_day_index = settings.getInt("artificialDayIndex", 0);
			///////// altrimenti l'indice del giorno è (Calendar.getInstance().get(Calendar.DAY_OF_WEEK))-1;
						
			if (samplingEnabled) { // if sampling is enabled stop the classifier
				stopClassify(next_alarm,current_day_index);
								
				//l'utente ferma il gioco, ponendo fine al "periodo di gioco" iniziato
				//in precedenza
				
				//se il prossimo alarm è di stop significa che si è all'interno di un
				//intervallo di esplorazione attivo
			//	int next_alarm_id = settings.getInt("alarm_id", -1);	
			//	Alarm next_alarm = AlarmUtils.getAlarm(appContext, next_alarm_id);
				if(!next_alarm.get_actionType()){
				
					if(stepsInGamePeriod){
						
						Log.d(MainActivity.AppName,"STOP GAME IN ACTIVE INTERVAL - with steps");
						
						//si associa il fatto che nell'intervallo di esplorazione corrente l'utente
						//ha fatto almeno uno scalino (salvataggio nelle preferences, così 
						//l'informazione viene mantenuta anche se l'utente spegne il device)
						settings.edit().putInt("last_interval_with_steps", next_alarm_id).commit();
					
						//si resetta a 'false' il booleano che indica se l'utente ha fatto
						//almeno 1 scalino nel periodo di gioco corrente
						stepsInGamePeriod=false;
					}
										
					
					//se non è un intervallo di gioco, si ri-attiva il servizio di activity
					//recognition
					//TODO controllo livello batteria 					
					if(!next_alarm.isStepsInterval(current_day_index)){ //&&!stepsInGame
						Log.d(MainActivity.AppName,"STOP GAME IN ACTIVE INTERVAL - START ACTIVITY REC");
						appContext.startService(new Intent(appContext, ActivityRecognitionRecordService.class));
					}
					//altrimenti è un intervallo di gioco e, quindi, continua l'esecuzione
					//del classificatore scalini/non_scalini
					
				}
				
			} else { // if sampling is not enabled stop the classifier
				climbedYesterday=StatUtils.climbedYesterday(climbing.get_id());
				// FOR TESTING PURPOSES
//				climbedYesterday=true;
				startClassifyService(next_alarm,current_day_index);
				
				//l'utente fa partire il gioco, generando un "periodo di gioco"
				
				//se il prossimo alarm settato è un evento di stop allora significa che 
				//si è all'interno di un intervallo attivo; se quest'ultimo non è un intervallo
				//di gioco, vuol dire che il servizio di activity recognition è in esecuzione;
				//in questo caso tale servizio viene fermato	
				if(!next_alarm.get_actionType() && !next_alarm.isStepsInterval(current_day_index)){
										
					Log.d(MainActivity.AppName,"START GAME IN ACTIVE INTERVAL - STOP ACTIVITY REC");
					
					//stop servizio di activity recognition (si tengono le variabili legate
					//all'eventuale valutazione dell'attività svolta nel primo intervallo
					//della lista, il cui ascolto è stato interrotto dall'inizio del gioco)					
					if(GeneralUtils.isActivityRecognitionServiceRunning(appContext)){
						appContext.stopService(new Intent(appContext, ActivityRecognitionRecordService.class));
					}
					
					Log.d(MainActivity.AppName,"START GAME IN ACTIVE INTERVAL - Total number of values: " + ActivityRecognitionIntentService.getValuesNumber());
				   	Log.d(MainActivity.AppName,"START GAME IN ACTIVE INTERVAL - Number of activities: " + ActivityRecognitionIntentService.getActivitiesNumber());	
				}
				
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
			//si recupera il prossimo alarm impostato
			int next_alarm_id = settings.getInt("alarm_id", -1);	
			Alarm next_alarm = AlarmUtils.getAlarm(appContext, next_alarm_id);			
			//si recupera l'indice del giorno corrente all'interno della settimana
			/////////		
	    	//PER TEST ALGORITMO
			int current_day_index = settings.getInt("artificialDayIndex", 0);
			///////// altrimenti l'indice del giorno è (Calendar.getInstance().get(Calendar.DAY_OF_WEEK))-1;
						
			if(next_alarm.get_actionType() || !next_alarm.get_actionType() && !next_alarm.isStepsInterval(current_day_index)){
				
				appContext.getPackageManager().setComponentEnabledSetting(new ComponentName(appContext, StairsClassifierReceiver.class), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
				stopService(backgroundClassifySampler);
			}
			
			//unregisterReceiver(classifierReceiver);
			//stopService(backgroundClassifySampler);
		} catch (Exception e) {
			Log.e(MainActivity.AppName, "Classifier service not running or unable to stop");
		}
	}

	/**
	 * Stop background classifier service
	 */
	public void stopClassify(Alarm next_alarm, int day_index) {
		
		//se il prossimo alarm è di start oppure è di stop e non definisce un int. di gioco
		//allora si ferma il classificatore scalini/non_scalini
		if(next_alarm.get_actionType() || !next_alarm.get_actionType() && !next_alarm.isStepsInterval(day_index)){
			stopService(backgroundClassifySampler); // stop background service	
			//unregisterReceiver(classifierReceiver); // unregister listener
			appContext.getPackageManager().setComponentEnabledSetting(new ComponentName(appContext, StairsClassifierReceiver.class), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
			Log.d(MainActivity.AppName,"Climb - STOP CLASSIFY: is not a game interval");
		}
		else{
			Log.d(MainActivity.AppName,"Climb - NO STOP CLASSIFY: is a game interval");
		}
				
		StairsClassifierReceiver.setClimb(null);
		samplingEnabled = false;
		
		// update db
		climbing.setModified(new Date().getTime()); // update climbing last edit date
		climbing.setCompleted_steps(num_steps); // update completed steps
		climbing.setPercentage(percentage); // update progress percentage
		climbing.setRemaining_steps(building.getSteps() - num_steps); // update remaining steps
		if (percentage >= 1.00) climbing.setCompleted(new Date().getTime());
		MainActivity.climbingDao.update(climbing); // save to db
		Log.i(MainActivity.AppName, "Updated climbing #" + climbing.get_id());
		((ImageButton) findViewById(R.id.btnStartClimbing)).setImageResource(R.drawable.av_play); // set button icon to play again
		findViewById(R.id.progressBarClimbing).startAnimation(AnimationUtils.loadAnimation(appContext, R.anim.abc_fade_out)); // hide progress bar
		findViewById(R.id.progressBarClimbing).setVisibility(View.INVISIBLE);
	}

	/**
	 * Start background classifier service
	 */
	public void startClassifyService(Alarm next_alarm, int day_index) {
		
		firstTimeStart = false; //aggiunto
		
		//SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()); // get reference to android preferences
		int difficulty = Integer.parseInt(settings.getString("difficulty", "10")); // get difficulty from preferences
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
		
		
		//se il prossimo alarm è di stop e definisce la fine di un intervallo di gioco,
		//allora il classificatore scalini/non_scalini è già in esecuzione; quindi non lo si
		//attiva nuovamente; se, invece, il prossimo alarm è di start (quindi non si è in
		//un intervallo attivo) oppure è di stop ma l'intervallo in questione non è un int. di
		//gioco, allora si deve attivare il cl. scalini/non_scalini
		if(next_alarm.get_actionType() || !next_alarm.get_actionType() && !next_alarm.isStepsInterval(day_index)){
			
			System.out.println("START CLASSIFY NO GAME INTERVAL");
			
			startService(backgroundClassifySampler); // start background service
			//registerReceiver(classifierReceiver, classifierFilter); // register listener	
			appContext.getPackageManager().setComponentEnabledSetting(new ComponentName(appContext, StairsClassifierReceiver.class), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
		}		
		
		/*
		if(!GeneralUtils.isSamplingClassifyServiceRunning(appContext)){
			startService(backgroundClassifySampler); // start background service
			registerReceiver(classifierReceiver, classifierFilter); // register listener
		}*/
		
			
		StairsClassifierReceiver.setClimb(this);
		samplingEnabled = true;
		
		((ImageButton) findViewById(R.id.btnStartClimbing)).setImageResource(R.drawable.av_pause); // set button image to stop service
		
		//findViewById(R.id.lblReadyToClimb).setVisibility(View.GONE); // prima c'era; necessario per far rivedere il pulsante di start/stop al click
		((TextView)findViewById(R.id.lblReadyToClimb)).setText("");		
		findViewById(R.id.progressBarClimbing).startAnimation(AnimationUtils.loadAnimation(appContext, R.anim.abc_fade_in));
		findViewById(R.id.progressBarClimbing).setVisibility(View.VISIBLE);
	}

	@Override
	protected void onResume() {
		Log.i(MainActivity.AppName, "ClimbActivity onResume");
		int building_id = getIntent().getIntExtra(MainActivity.building_intent_object, 0); // get building id from received intent
		Log.i(MainActivity.AppName, "Building id: "+building_id);
		super.onResume();
			
		//se la scalata non è completata
		if (percentage < 1.00) { 												
			cp.start(); //si fa ripartire il countdown timer			
		}	
	}

	@Override
	protected void onPause() {
		Log.i(MainActivity.AppName, "ClimbActivity onPause");
		super.onPause();
		//this.finish(); //prima c'era e faceva chiudere l'activity 
		
		//se la scalata non è completata
		if (percentage < 1.00) { 
			cp.pause(); //si mette in pausa il countdown timer	
		}
	}

	@Override
	protected void onDestroy() {
		Log.i(MainActivity.AppName, "ClimbActivity onDestroy");
		stopAllServices(); // make sure to stop all background services
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		if (samplingEnabled == false) super.onBackPressed();
		else { // disable back button if sampling is enabled
			Toast.makeText(appContext, "Sampling running - Stop it before exiting", Toast.LENGTH_SHORT).show();
		}	
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		Log.i(MainActivity.AppName, "ClimbActivity onStop");
		super.onStop();
	}
	
	@Override
	protected void onStart() {
		
		super.onStart();
				
		picker = (HoloCircleSeekBar) findViewById(R.id.picker);
				
		if (percentage < 1.00) { //se la scalata non è completata
								
			System.out.println("on start perc > 100");
			
			if((time_bk == 5 * 1000 && !hasFinished) || (cp == null)) {
							
				//si inizializza l'oggetto per eseguire il countdown
				cp = new CountDownTimerPausable(5* 1000, 1000){

					@Override
					public void onTick(long millisUntilFinished) {
						time_bk = millisUntilFinished/1000;
						picker.setValue((int)time_bk - 1, 4);	//6								
					}

					@Override
					public void onFinish() {
						picker.setValue(0, 4); //6
						picker.setVisibility(View.INVISIBLE);
						hasFinished = true;
						time_bk = 5 * 1000; //7
					
						//si preme automaticamente il bottone di start, rendendo visibile
						//(per il tempo di auto_hide) il controllo per stoppare/far ripartire
						//il classify service
						
						if(firstTimeStart){							
							findViewById(R.id.btnStartClimbing).setEnabled(true);
							findViewById(R.id.btnStartClimbing).performClick();
							findViewById(R.id.lblReadyToClimb).performClick();							
						}
						
					}}.start(); //fa partire il countdown timer
			}			
		}
		else{
			picker.setVisibility(View.INVISIBLE);
		}
	}
	
	
	
	
	
	public static boolean stepsInCurrentGamePeriod(){
		return stepsInGamePeriod;
	}
	
	/*
	//metodo di aggiornamento degli intervalli interessati dal periodo di gioco
	private void updateIntervalsEvaluation(Context context, boolean steps){
				
		//si recupera il DAO associato alla tabella degli alarm attraverso il gestore del DB
		RuntimeExceptionDao<Alarm, Integer> alarmDao = DbHelper.getInstance(context).getAlarmDao();
		
		//si recupera l'indice del giorno corrente all'interno della settimana
		/////////		
    	//PER TEST ALGORITMO
		int current_day_index = settings.getInt("artificialDayIndex", 0);
		///////// altrimenti l'indice del giorno è (Calendar.getInstance().get(Calendar.DAY_OF_WEEK))-1;
		
		//se in tale periodo di gioco l'utente ha fatto almeno uno scalino
		//allora si aggiorna la valutazione di tutti gli individui che sono stati
		//interessati da esso (si pone valutazione=1, questi intervalli diventano
		//"intervalli di gioco")
		if(steps){
			//per ogni coppia di alarm (start,stop) che definisce un intervallo interessato dal
			//periodo di gioco si pone: attivo la prossima settimana, valutazione=1 e l'intervallo
			//diventa un "intervallo di gioco" (buono per lanciare trigger)
			for (int a_id : intervals_id){
				
				Alarm this_alarm = AlarmUtils.getAlarm(context, a_id);
				
				this_alarm.setRepeatingDay(current_day_index, true);
				this_alarm.setEvaluation(current_day_index, 1.0f);
				this_alarm.setGameInterval(current_day_index, true);			
				//ora si salvano queste modifiche anche nel database
				alarmDao.update(this_alarm);
			}
			
			
			//l'intervallo corrente non deve essere ri-aggiornato nell'evento
			//di stop in quanto in esso l'utente ha fatto scalini (valutazione massima)
			settings.edit().putBoolean("steps_in_alarm", true).commit();
			
			//si resetta il booleano che indica se durante il periodo di gioco 
			//l'utente ha fatto scalini
			stepsInGamePeriod=false;
			
		}
		else{						
			//valutazione intervalli interessati da un periodo di gioco senza scalini:
			//
			//si considera la valutazione parziale del primo intervallo di ascolto
			//interrotto dall'inizio del gioco; questo valore sommato ad un fattore che
			//considera che l'utente ha aperto il gioco (pur senza fare scalini) sarà la 
			//valutazione dei vari intervalli di ascolto interessati da questo periodo di
			//gioco: (val+0.5)/2
			//nota: in seguito, la valutazione dell'ultimo intervallo in cui l'utente ha
			//chiuso il gioco può cambiare, in quanto si fa ripartire il servizio di activity
			//recognition
			
			
			// [ alternative: 
			//   1) mettere valutazione=1 anche se nel periodo di gioco non fa scalini
			//   2) activity recognition con frequenza bassa anche durante il gioco 
			// ]
			
			float qn = ActivityRecognitionIntentService.getActivityAmount();
			Log.d(MainActivity.AppName,"STOP GAME IN INTERVAL - Amount of physical activity: " + qn);			
			float evaluation = 0f;			
			if(qn>0){
				evaluation=(GeneralUtils.evaluateInterval(qn, ActivityRecognitionIntentService.getConfidencesList(), ActivityRecognitionIntentService.getWeightsList()) 
						+0.5f) / 2;
			}
			else{
				evaluation=0.5f;
			}
			
			Log.d(MainActivity.AppName,"STOP GAME IN INTERVAL - Interval Evaluation: " + evaluation);
			
			
			boolean good = false;
			
			if(evaluation>=0.5){	
				good=true;
				Log.d(MainActivity.AppName,"STOP GAME IN INTERVAL - intervalli buoni, li tengo per la prossima settimana");	
			}
			else{
				Log.d(MainActivity.AppName,"STOP GAME IN INTERVAL - intervalli non buoni, li disattivo per la prossima settimana");
			}
			
			
			for (int a_id : intervals_id){
				
				Alarm this_alarm = AlarmUtils.getAlarm(context, a_id);
				
				this_alarm.setRepeatingDay(current_day_index, good);
				this_alarm.setEvaluation(current_day_index, evaluation);
				this_alarm.setGameInterval(current_day_index, false);			
				//ora si salvano queste modifiche anche nel database
				alarmDao.update(this_alarm);
			}
			

			
			//se alla fine del gioco si è ancora in un intervallo di ascolto attivo
			//allora si fa ripartire il servizio di activity recognition
			if(!AlarmUtils.getAlarm(appContext,settings.getInt("alarm_id", -1)).get_actionType()){
				
				appContext.startService(new Intent(appContext, ActivityRecognitionRecordService.class));
			}
			else{ //non si è in un intervallo di ascolto attivo: per sicurezza si chiama il
				//metodo per impostare il prossimo alarm (in quanto nel caso peggiore la
				//nuova valutazione calcolata 
				
			}
		}
		//set next alarm per sicurezza
		
	}
	*/
	
}