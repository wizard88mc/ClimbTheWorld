package org.unipd.nbeghin.climbtheworld;

import java.util.List;

import org.unipd.nbeghin.climbtheworld.adapters.StatAdapter;
import org.unipd.nbeghin.climbtheworld.models.Stat;
import org.unipd.nbeghin.climbtheworld.models.User;
import org.unipd.nbeghin.climbtheworld.util.StatUtils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ProfileActivity extends Activity {
	
	Button improveBtn;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);
		SharedPreferences pref = getSharedPreferences("UserSession", 0);
		User me = ClimbApplication.getUserById(pref.getInt("local_id", -1));
		List<Stat> stats = StatUtils.calculateStats();
		((ListView) findViewById(R.id.listStatistics)).setAdapter(new StatAdapter(this, R.layout.stat_item, stats));
		((TextView) findViewById(R.id.textUserName)).setText(me.getName());
		((TextView) findViewById(R.id.textLevel)).setText(getString(R.string.level, String.valueOf((me.getLevel()))));
		((TextView) findViewById(R.id.textXP)).setText(String.valueOf(me.getXP()) + " XP");
		((TextView) findViewById(R.id.MyStepsText)).setText(getString(R.string.mean_text, String.valueOf(me.getMean())));
		((TextView) findViewById(R.id.textCurrentValue)).setText(getString(R.string.today_step, String.valueOf(me.getCurrent_steps_value())));
		int total = ClimbApplication.levelToXP(me.getLevel() + 1);
		int percentage = 0;
		if(total != 0)
			percentage = ((100 * me.getXP()) / total);
		ProgressBar levelPB = (ProgressBar) findViewById(R.id.progressBarLevel);
		levelPB.setIndeterminate(false);
		levelPB.setProgress(percentage);
		
		improveBtn = (Button) findViewById(R.id.buttonCounter);
		improveBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(getApplicationContext(), ClimbActivity.class);
				intent.putExtra(ClimbApplication.counter_mode, true);
				startActivity(intent);	
			}
		});
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.profile, menu);
		return true;
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
				NavUtils.navigateUpFromSameTask(this);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
