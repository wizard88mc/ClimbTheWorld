package org.unipd.nbeghin.climbtheworld;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import org.unipd.nbeghin.climbtheworld.adapters.PagerAdapter;
import org.unipd.nbeghin.climbtheworld.db.DbHelper;
import org.unipd.nbeghin.climbtheworld.fragments.BuildingsFragment;
import org.unipd.nbeghin.climbtheworld.fragments.ToursFragment;
import org.unipd.nbeghin.climbtheworldAlgorithm.R;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;



/**
 * Main activity
 * 
 */
@SuppressLint("NewApi")
public class MainActivity extends ActionBarActivity {
	private static final String									APP_TITLE						= "Climb the world";
	public static final String									AppName							= "ClimbTheWorld";																							
	private ActionBar											ab;																							// reference to action bar
	public static final String									settings_file					= "ClimbTheWorldPreferences";
	public static final String									settings_detected_sampling_rate	= "samplingRate";
	private List<Fragment>										fragments						= new Vector<Fragment>();										// list of fragments to be loaded
	private PagerAdapter										mPagerAdapter;																					// page adapter for ViewPager
	public static final String									building_intent_object			= "org.unipd.nbeghin.climbtheworld.intents.object.building";	// intent key for sending building id
	private ViewPager											mPager;
	private static Context										sContext;

	public static boolean logEnabled=true; 
	
	//finestra di dialogo che propone all'utente di configurare l'algoritmo
	private static Dialog alertDialog;
	
	@SuppressLint("InflateParams")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		sContext = MainActivity.this;

		//the loading of the DB and the initializing of classifier's parameters are done
		//in ClimbTheWorldApp class
		
		// loading fragments
		fragments.add(Fragment.instantiate(this, BuildingsFragment.class.getName())); // instance building fragments
		fragments.add(Fragment.instantiate(this, ToursFragment.class.getName())); // instance tours fragments
		mPagerAdapter = new PagerAdapter(super.getSupportFragmentManager(), fragments);
		mPager = (ViewPager) super.findViewById(R.id.pager);
		mPager.setAdapter(this.mPagerAdapter);
		
				
		if(!PreferenceManager.getDefaultSharedPreferences(sContext).getBoolean("algorithm_configured", false)){
			
			//si crea una finestra di dialogo che propone all'utente di configurare l'algoritmo
			alertDialog = new Dialog(this);			
	        LayoutInflater inflater = getLayoutInflater();
	        View dialoglayout = inflater.inflate(R.layout.dialog_algorithm_config, null);
	        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
	        alertDialog.setContentView(dialoglayout);
	        alertDialog.setCancelable(false);
	        alertDialog.show();
		}
		
	}

	
	
	public void selectAction(View v){
		
		switch(v.getId()) {
		case R.id.dialog_config_btt_ok:
			alertDialog.dismiss();
			startActivity(new Intent(this,StartConfigActivity.class));
			break;
		case R.id.dialog_config_btt_cancel:
			alertDialog.dismiss();
			break;
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


	
	
	public void onShowActionProfile(MenuItem v) {
		Intent intent = new Intent(sContext, ProfileActivity.class);
		startActivity(intent);
	}

	public void onShowSettings(MenuItem v) {
		Intent intent = new Intent(sContext, SettingsActivity.class);
		startActivity(intent);
	}

	public void onShowLog(MenuItem v) {
		Intent intent = new Intent(sContext, ShowLogActivity.class);
		startActivity(intent);
	}
	
	/*
	public void onConfigAlgorithm(MenuItem v) {
		Intent intent = new Intent(sContext, AlgorithmConfigActivity.class);
		startActivity(intent);
	}
	*/
	
	
	public void onBtnShowGallery(View v) {
		Intent intent = new Intent(sContext, GalleryActivity.class);
		intent.putExtra("building_id", 1);
		startActivity(intent);
	}


	@Override
	protected void onResume() {
		Log.i(MainActivity.AppName, "MainActivity onResume");		
		super.onResume();
		ClimbTheWorldApp.refresh();
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
		//Intent intent = new Intent(sContext, FBPickFriendActivity.class);
		//startActivity(intent);
	}

	private void shareDb() {
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
		String output_name = "ClimbTheWorld_" + df.format(new Date()) + ".db";
		try {
			File file = new File(DbHelper.getInstance(sContext).getDbPath()); // get private db reference
			if (file.exists() == false || file.length() == 0) throw new Exception("Empty DB");
			this.copyFile(new FileInputStream(file), this.openFileOutput(output_name, MODE_WORLD_READABLE));
			file = this.getFileStreamPath(output_name);
			Intent i = new Intent(Intent.ACTION_SEND);
			i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
			i.setType("*/*");
			startActivity(Intent.createChooser(i, "Share to"));
		} catch (Exception e) {
			Toast.makeText(sContext, "Unable to export db: " + e.getMessage(), Toast.LENGTH_SHORT).show();
			Log.e(AppName, e.getMessage());
		}
	}
}
