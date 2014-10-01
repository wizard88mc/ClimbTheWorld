package org.unipd.nbeghin.climbtheworld;

import org.unipd.nbeghin.climbtheworld.db.DbHelper;

import com.parse.Parse;
import com.parse.ParseFacebookUtils;

import android.app.Application;
import android.util.Log;

public class ClimbApplication extends Application{
	public static final int N_MEMBERS_PER_GROUP = 6; // 5 amici + me

	
	 @Override
	  public void onCreate()
	  {
	    super.onCreate();
	    Log.d("enable local datastorage", "local db");
	   // Parse.enableLocalDatastore(getApplicationContext());
	    Log.d("enable local datastorage", "local db opened");

	    Parse.initialize(this, "e9wlYQPdpXlFX3XQc9Lq0GJFecuYrDSzwVNSovvd",
				"QVII1Qhy8pXrjAZiL07qaTKbaWpkB87zc88UMWv2");
		ParseFacebookUtils.initialize(getString(R.string.app_id));
	  }
	 
	 
}
