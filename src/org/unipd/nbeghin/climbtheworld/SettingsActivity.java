package org.unipd.nbeghin.climbtheworld;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SimpleTimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.unipd.nbeghin.climbtheworld.models.Building;
import org.unipd.nbeghin.climbtheworld.models.Climbing;
import org.unipd.nbeghin.climbtheworld.models.Collaboration;
import org.unipd.nbeghin.climbtheworld.models.Competition;
import org.unipd.nbeghin.climbtheworld.models.Group;
import org.unipd.nbeghin.climbtheworld.models.Microgoal;
import org.unipd.nbeghin.climbtheworld.models.TeamDuel;
import org.unipd.nbeghin.climbtheworld.models.User;
import org.unipd.nbeghin.climbtheworld.models.UserBadge;
import org.unipd.nbeghin.climbtheworld.util.FacebookUtils;
import org.unipd.nbeghin.climbtheworld.util.ModelsUtil;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.ProfilePictureView;
import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity {
	/**
	 * Determines whether to always show the simplified settings UI, where
	 * settings are presented in a single list. When false, settings are shown
	 * as a master/detail two-pane view on tablets. When true, a single pane is
	 * shown on tablets.
	 */
	private static final boolean ALWAYS_SIMPLE_PREFS = true;
	private ProgressDialog PD;
	private UiLifecycleHelper uiHelper;
	private Session mSession;
	private Session.StatusCallback callback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state, Exception exception) {
			onSessionStateChange(session, state, exception);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("SettingsActivity", "onCreate");
		uiHelper = new UiLifecycleHelper(this, callback);
		uiHelper.onCreate(savedInstanceState);

		Session session = Session.getActiveSession();
		if (session != null && session.isOpened()) {
			updateFacebookSession(session, session.getState());
		}

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

	private boolean isSessionChanged(Session session) {

		// Check if session state changed
		if (mSession.getState() != session.getState())
			return true;

		// Check if accessToken changed
		if (mSession.getAccessToken() != null) {
			if (!mSession.getAccessToken().equals(session.getAccessToken()))
				return true;
		} else if (session.getAccessToken() != null) {
			return true;
		}

		// Nothing changed
		return false;
	}

	


	private void updateFacebookSession(final Session session, SessionState state) {
		if (FacebookUtils.isOnline(this)) {
			final TextView lblFacebookUser = (TextView) findViewById(R.id.lblFacebookUser);
			final ProfilePictureView profilePictureView = (ProfilePictureView) findViewById(R.id.fb_profile_picture);
			final Preference profile_name = findPreference("profile_name");
			final SharedPreferences pref = getApplicationContext().getSharedPreferences("UserSession", 0);

			if (state.isOpened()) {
				Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {
					@Override
					public void onCompleted(GraphUser user, Response response) {
						if (session == Session.getActiveSession()) {
							if (user != null && profilePictureView != null) {
								//if current user is not logged with facebook
								if (pref.getString("FBid", "none").equalsIgnoreCase("none")) {
									// look for my FBid
									Map<String, Object> conditions = new HashMap<String, Object>();
									conditions.put("FBid", user.getId());
									User newUser = null;
									List<User> users = ClimbApplication.userDao.queryForFieldValuesArgs(conditions);

									Map<String, Object> conditions2 = new HashMap<String, Object>();
									conditions2.put("owner", 1);
									List<User> users2 = ClimbApplication.userDao.queryForFieldValuesArgs(conditions2);

									// already connected FB account????
									if (users.isEmpty()) { 
										// no, am I the owner????
										if (!users2.isEmpty()) { // there is an
																	// owner
																	// locally
											if (users2.get(0).getFBid().equalsIgnoreCase("empty")) {
												// this is FBid of the owner
												newUser = users2.get(0);
												newUser.setFBid(user.getId());
												newUser.setName(user.getName());
												ClimbApplication.userDao.update(newUser);
											} else {
												// new host user
												newUser = new User();
												newUser.setFBid(user.getId());
												newUser.setName(user.getName());
												newUser.setLevel(0);
												newUser.setXP(0);
												newUser.setOwner(false);
												ClimbApplication.userDao.create(newUser);
												createBadges();
											}
											// no, connect it now
										} else {
											// create new local user owner
											User userOwner = new User();
											userOwner.setFBid("empty");
											userOwner.setLevel(0);
											userOwner.setXP(0);
											userOwner.setOwner(true);

											user.setName("user owner");
											ClimbApplication.userDao.create(userOwner);
											createBadges();

											Editor editor = pref.edit();
											editor.putInt("local_id", userOwner.get_id());
											editor.commit();
											Log.d("local id", String.valueOf(userOwner.get_id()));
										}
									} else {// yes, get my account
										newUser = users.get(0);
									}
									String own = newUser.isOwner() ? "\n" + getString(R.string.owner) : "";
									Toast.makeText(getApplicationContext(), getString(R.string.logged_as, newUser.getName()) + own, Toast.LENGTH_SHORT).show();
									// save data locally
									SharedPreferences pref = getApplicationContext().getSharedPreferences("UserSession", 0);
									Editor editor = pref.edit();
									editor.putString("FBid", newUser.getFBid());
									editor.putString("username", newUser.getName());
									editor.putInt("local_id", newUser.get_id());
									editor.commit();
									ClimbApplication.setCurrentUser(newUser);
									ClimbApplication.userExists(user, session, PD, SettingsActivity.this);

								}
								profilePictureView.setCropped(true);
								profilePictureView.setProfileId(user.getId());
								lblFacebookUser.setText(user.getName());
								profile_name.setSummary(user.getName());
							} else
								System.err.println("no user");
						}
						if (response.getError() != null) {
							Log.e("Settings Activity", "FB exception: " + response.getError());
						}
					}
				});
				request.executeAsync();
				// }
			} else if (state.isClosed()) {
				Log.i("Climb The World", "Logged out...");
				ParseUser.getCurrentUser().logOut();
				ClimbApplication.setCurrentUser(null);
				if (!pref.getString("FBid", "none").equalsIgnoreCase("none")) {
					Editor editor = pref.edit();
					//the owner returns to be the current user
					Map<String, Object> conditions2 = new HashMap<String, Object>();
					conditions2.put("owner", 1);
					List<User> users2 = ClimbApplication.userDao.queryForFieldValuesArgs(conditions2);
					if (users2.size() > 0) {
						Log.d("logout", "carico user owner");
						editor.putInt("local_id", users2.get(0).get_id());
						editor.putString("username", "owner");
						editor.commit();
						ClimbApplication.setCurrentUser(users2.get(0));
					} else {
						Log.d("vuoto", "creo owner");
						// create new local user owner
						User userOwner = new User();
						userOwner.setFBid("empty");
						userOwner.setLevel(0);
						userOwner.setXP(0);
						userOwner.setOwner(true);
						ClimbApplication.userDao.create(userOwner);
						editor.putInt("local_id", userOwner.get_id());
						editor.putString("username", "owner");

						editor.commit();
						Log.d("local id", String.valueOf(userOwner.get_id()));
						ClimbApplication.setCurrentUser(userOwner);

					}

					editor.putString("FBid", "none");
					editor.commit();
					profilePictureView.setProfileId(null);
					lblFacebookUser.setText(getString(R.string.not_logged_in));
					profile_name.setSummary(getString(R.string.no_user_defined));
				}

			}
		} else
			Toast.makeText(getApplicationContext(), getString(R.string.check_connection), Toast.LENGTH_LONG).show();
	}

	

	
	/**
	 * Creates UserBadges for the current logged in user
	 */
	private void createBadges() {
		SharedPreferences pref = getSharedPreferences("UserSession", 0);
		boolean emptybadges = ClimbApplication.areThereUserBadges(pref.getInt("local_id", -1)) == 0 ? false : true;
		if (emptybadges) {
			for (Building building : ClimbApplication.buildings) {
				UserBadge ub = new UserBadge();
				ub.setBadge(ClimbApplication.getBadgeByCategory(0));
				ub.setObj_id(building.get_id());
				ub.setPercentage(0.0);
				ub.setSaved(true);
				ub.setUser(ClimbApplication.getUserById(pref.getInt("local_id", -1)));
				ClimbApplication.userBadgeDao.create(ub);
			}
		}
		ClimbApplication.refreshUserBadge();
	}

	

	private void onSessionStateChange(final Session session, SessionState state, Exception exception) {
		updateFacebookSession(session, state);
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
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		setupSimplePreferencesScreen();
	}

	/**
	 * Shows the simplified settings UI if the device configuration if the
	 * device configuration dictates that a simplified, single-pane UI should be
	 * shown.
	 */
	@SuppressWarnings("deprecation")
	private void setupSimplePreferencesScreen() {
		if (!isSimplePreferences(this)) {
			return;
		}
		// In the simplified UI, fragments are not used at all and we instead
		// use the older PreferenceActivity APIs.
		setContentView(R.layout.facebook_settings);
		addPreferencesFromResource(R.xml.pref_general);
		// Bind the summaries of EditText/List/Dialog/Ringtone preferences to
		// their values. When their values change, their summaries are updated
		// to reflect the new value, per the Android Design guidelines.
		bindPreferenceSummaryToValue(findPreference("profile_name"));
		// bindPreferenceSummaryToValue(findPreference("vstep_for_rstep"));
		// bindPreferenceSummaryToValue(findPreference("userWeight"));
		// bindPreferenceSummaryToValue(findPreference("userHeight"));
		// bindPreferenceSummaryToValue(findPreference("stepHeight"));
		bindPreferenceSummaryToValue(findPreference("difficulty"));
		// float
		// detectedSamplingRate=PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getFloat("detectedSamplingRate",
		// 0.0f);
		// findPreference("detectedSamplingRate").setSummary(new
		// DecimalFormat("#.##").format(detectedSamplingRate)+"Hz");
	}

	/** {@inheritDoc} */
	@Override
	public boolean onIsMultiPane() {
		return isXLargeTablet(this) && !isSimplePreferences(this);
	}

	/**
	 * Helper method to determine if the device has an extra-large screen. For
	 * example, 10" tablets are extra-large.
	 */
	private static boolean isXLargeTablet(Context context) {
		return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
	}

	/**
	 * Determines whether the simplified settings UI should be shown. This is
	 * true if this is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device
	 * doesn't have newer APIs like {@link PreferenceFragment}, or the device
	 * doesn't have an extra-large screen. In these cases, a single-pane
	 * "simplified" settings UI should be shown.
	 */
	private static boolean isSimplePreferences(Context context) {
		return ALWAYS_SIMPLE_PREFS || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB || !isXLargeTablet(context);
	}

	/** {@inheritDoc} */
	// @Override
	// @TargetApi(Build.VERSION_CODES.HONEYCOMB)
	// public void onBuildHeaders(List<Header> target) {
	// if (!isSimplePreferences(this)) {
	// loadHeadersFromResource(R.xml.pref_headers, target);
	// }
	// }
	/**
	 * A preference value change listener that updates the preference's summary
	 * to reflect its new value.
	 */
	private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object value) {
			String stringValue = value.toString();
			if (preference instanceof ListPreference) {
				// For list preferences, look up the correct display value in
				// the preference's 'entries' list.
				ListPreference listPreference = (ListPreference) preference;
				int index = listPreference.findIndexOfValue(stringValue);
				// Set the summary to reflect the new value.
				preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
			} else {
				// For all other preferences, set the summary to the value's
				// simple string representation.
				preference.setSummary(stringValue);
			}
			return true;
		}
	};

	/**
	 * Binds a preference's summary to its value. More specifically, when the
	 * preference's value is changed, its summary (line of text below the
	 * preference title) is updated to reflect the value. The summary is also
	 * immediately updated upon calling this method. The exact display format is
	 * dependent on the type of preference.
	 * 
	 * @see #sBindPreferenceSummaryToValueListener
	 */
	private static void bindPreferenceSummaryToValue(Preference preference) {
		// Set the listener to watch for value changes.
		preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
		// Trigger the listener immediately with the preference's
		// current value.
		sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), ""));
	}

	/**
	 * This fragment shows general preferences only. It is used when the
	 * activity is showing a two-pane settings UI.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class GeneralPreferenceFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_general);
			// Bind the summaries of EditText/List/Dialog/Ringtone preferences
			// to their values. When their values change, their summaries are
			// updated to reflect the new value, per the Android Design
			// guidelines.
			bindPreferenceSummaryToValue(findPreference("profile_name"));
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		Session session = Session.getActiveSession();
		if (session != null && (session.isOpened() || session.isClosed())) {
			onSessionStateChange(session, session.getState(), null);
		}

		uiHelper.onResume();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		uiHelper.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onPause() {
		super.onPause();
		uiHelper.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		uiHelper.onDestroy();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		uiHelper.onSaveInstanceState(outState);
	}
}
