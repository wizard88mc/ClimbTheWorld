package org.unipd.nbeghin.climbtheworld;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SimpleTimeZone;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.unipd.nbeghin.climbtheworld.adapters.PagerAdapter;
import org.unipd.nbeghin.climbtheworld.db.DbHelper;
import org.unipd.nbeghin.climbtheworld.db.PreExistingDbLoader;
import org.unipd.nbeghin.climbtheworld.fragments.BadgesFragment;
import org.unipd.nbeghin.climbtheworld.fragments.BuildingsFragment;
import org.unipd.nbeghin.climbtheworld.fragments.NotificationFragment;
import org.unipd.nbeghin.climbtheworld.fragments.ToursFragment;
import org.unipd.nbeghin.climbtheworld.models.AskCollaborationNotification;
import org.unipd.nbeghin.climbtheworld.models.AskCompetitionNotification;
import org.unipd.nbeghin.climbtheworld.models.AskTeamDuelNotification;
import org.unipd.nbeghin.climbtheworld.models.Building;
import org.unipd.nbeghin.climbtheworld.models.BuildingTour;
import org.unipd.nbeghin.climbtheworld.models.Climbing;
import org.unipd.nbeghin.climbtheworld.models.Collaboration;
import org.unipd.nbeghin.climbtheworld.models.Competition;
import org.unipd.nbeghin.climbtheworld.models.Group;
import org.unipd.nbeghin.climbtheworld.models.InviteNotification;
import org.unipd.nbeghin.climbtheworld.models.Microgoal;
import org.unipd.nbeghin.climbtheworld.models.Notification;
import org.unipd.nbeghin.climbtheworld.models.NotificationType;
import org.unipd.nbeghin.climbtheworld.models.Photo;
import org.unipd.nbeghin.climbtheworld.models.TeamDuel;
import org.unipd.nbeghin.climbtheworld.models.Tour;
import org.unipd.nbeghin.climbtheworld.models.User;
import org.unipd.nbeghin.climbtheworld.models.UserBadge;
import org.unipd.nbeghin.climbtheworld.util.FacebookUtils;
import org.unipd.nbeghin.climbtheworld.util.ModelsUtil;
import org.unipd.nbeghin.climbtheworld.weka.WekaClassifier;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
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
import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;
import com.facebook.widget.WebDialog;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

/**
 * Main activity
 * 
 */
@SuppressLint("NewApi")
public class MainActivity extends ActionBarActivity {
	private static final String APP_TITLE = "Climb the world";
	public static final String AppName = "ClimbTheWorld";

	private List<Fragment> fragments = new Vector<Fragment>(); // list of
																// fragments to
																// be loaded
	private PagerAdapter mPagerAdapter; // page adapter for ViewPager

	private ViewPager mPager;
	private static Context sContext;
	// NEW
	private WebDialog dialog = null;
	private String dialogAction = null;
	private Bundle dialogParams = null;

	private String requestId;
	SharedPreferences pref;

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
		setContentView(R.layout.activity_main);
		pref = getSharedPreferences("UserSession", 0);
		uiHelper = new UiLifecycleHelper(this, callback);
		uiHelper.onCreate(savedInstanceState);
		// try {
		// PackageInfo info =
		// getPackageManager().getPackageInfo("org.unipd.nbeghin.climbtheworld",
		// PackageManager.GET_SIGNATURES);
		// for (Signature signature : info.signatures) {
		// System.out.println("qui");
		// MessageDigest md = MessageDigest.getInstance("SHA");
		// md.update(signature.toByteArray());
		// Log.d("KeyHash:", Base64.encodeToString(md.digest(),
		// Base64.DEFAULT));
		// }
		// } catch (NameNotFoundException e) {
		//
		// } catch (NoSuchAlgorithmException e) {
		//
		// }

		ClimbApplication.notifications = new ArrayList<Notification>();

		// loadDb(); // instance db connection
		sContext = getApplicationContext();
		Log.d("create", "LOCAL ID: " + pref.getInt("local_id", -1));

		if (pref.getInt("local_id", -1) == -1) {
			setUserOwner();
		}

		// loading fragments
		fragments.add(Fragment.instantiate(this, BuildingsFragment.class.getName())); // instance
																						// building
																						// fragments
		fragments.add(Fragment.instantiate(this, ToursFragment.class.getName())); // instance
																					// tours
																					// fragments
		fragments.add(Fragment.instantiate(this, NotificationFragment.class.getName()));

		fragments.add(Fragment.instantiate(this, BadgesFragment.class.getName()));

		mPagerAdapter = new PagerAdapter(super.getSupportFragmentManager(), fragments);
		mPager = (ViewPager) super.findViewById(R.id.pager);
		mPager.setAdapter(this.mPagerAdapter);

		try {
			WekaClassifier.initializeParameters(getResources().openRawResource(R.raw.modelvsw30osl0));
		} catch (IOException exc) {
			this.finish();
		}

	}

	private void onSessionStateChange(final Session session, SessionState state, Exception exception) {
		updateFacebookSession(session, state);
	}

	private void saveBadges() {
    	Log.d("SettingsActivity", "saveBadges");
		ParseQuery<ParseUser> query = ParseUser.getQuery();
		query.whereEqualTo("FBid", pref.getString("FBid", ""));
		query.getFirstInBackground(new GetCallback<ParseUser>() {

			@Override
			public void done(ParseUser user, ParseException e) {
				if (e == null) {
					JSONArray badges = user.getJSONArray("badges");
					if(badges != null){
					for (int i = 0; i < badges.length(); i++) {
						try {
							JSONObject badge = badges.getJSONObject(i);
							int badge_id = badge.getInt("badge_id");
							int obj_id = badge.getInt("obj_id");
							int user_id = pref.getInt("local_id", -1);
							UserBadge userBadge = ClimbApplication.getUserBadgeForUserAndBadge(badge_id, obj_id, user_id);
			    			System.out.println("ub " + userBadge.get_id());
							if (userBadge == null) {
								UserBadge ub = new UserBadge();
								ub.setBadge(ClimbApplication.getBadgeById(badge_id));
								ub.setObj_id(obj_id);
								ub.setUser(ClimbApplication.getUserById(user_id));
								ub.setPercentage(badge.getDouble("percentage"));
								ClimbApplication.userBadgeDao.create(ub);
							} else {
								userBadge.setPercentage(badge.getDouble("percentage"));
								ClimbApplication.userBadgeDao.update(userBadge);
							}
						} catch (JSONException ex) {
							// TODO Auto-generated catch block
							ex.printStackTrace();
						}
					}
					}else{
						badges = new JSONArray();
						user.put("badges", badges);
						user.saveEventually();
					}
				} else {
					Toast.makeText(sContext, getString(R.string.connection_problem), Toast.LENGTH_SHORT).show();
					Log.e("saveBadges", e.getMessage());
				}

			}
		});
		
	}
	
	private void loadMicrogoalFromParse(){
		
		final SharedPreferences pref = getApplicationContext().getSharedPreferences("UserSession", 0);

		ParseQuery<ParseObject> query = ParseQuery.getQuery("Microgoal");
		query.whereEqualTo("users_id", pref.getString("FBid", ""));
		query.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> microgoals, ParseException e) {
				if(e == null){
					for(ParseObject microgoal : microgoals){
						Microgoal current_microgoal = ClimbApplication.getMicrogoalByUserAndBuilding(pref.getInt("local_id", -1), microgoal.getInt("building"));
						if(current_microgoal == null){
							current_microgoal = new Microgoal();
							current_microgoal.setDeleted(false);
							current_microgoal.setDone_steps(microgoal.getInt("done_steps"));
							current_microgoal.setSaved(true);
							current_microgoal.setStory_id(microgoal.getInt("story_id"));
							current_microgoal.setTot_steps(microgoal.getInt("tot_steps"));
							ClimbApplication.microgoalDao.create(current_microgoal);
						}else{
							current_microgoal.setDone_steps(microgoal.getInt("done_steps"));
							current_microgoal.setTot_steps(microgoal.getInt("tot_steps"));
							current_microgoal.setSaved(true);
							ClimbApplication.microgoalDao.update(current_microgoal);
						}
					}
				}else{
					Toast.makeText(getApplicationContext(), getString(R.string.connection_problem), Toast.LENGTH_SHORT).show();
					Log.e("loadMicrogoalFromParse", e.getMessage());
				}
				ClimbApplication.refreshMicrogoals();
	
			}
			
		});
	}

	ProgressDialog PD;

	private class MyAsync extends AsyncTask<Void, Void, Void> {

		// Session session;

		MyAsync() {
			// session = session;
		}

		@Override
		protected void onPreExecute() {

			super.onPreExecute();
			PD = new ProgressDialog(MainActivity.this);
			PD.setTitle(getString(R.string.wait));
			PD.setMessage(getString(R.string.loading_progress));
			PD.setCancelable(false);
			PD.show();
		}

		@Override
		protected Void doInBackground(Void... params) {
			ClimbApplication.BUSY = true;
			saveBadges();
			loadMicrogoalFromParse();
			loadProgressFromParse();
			synchronized (ClimbApplication.lock) {
				while (ClimbApplication.BUSY) {
					try {
						ClimbApplication.lock.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			// updateFacebookSession(session, session.getState());
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			PD.dismiss();
			onUpdateNotifications(null); // get FB notifications

		}
	}

	private void setUserOwner() {
		System.out.println("No FB");
		Map<String, Object> conditions = new HashMap<String, Object>();
		conditions.put("owner", new Integer(1));
		List<User> users = ClimbApplication.userDao.queryForFieldValuesArgs(conditions);

		if (users.isEmpty()) {
			Log.d("vuoto", "creo owner");
			// create new local user
			User user = new User();
			user.setFBid("empty");
			user.setLevel(0);
			user.setXP(0);
			user.setOwner(true);
			// final Preference profile_name=findPreference("profile_name");
			// profile_name.setSummary("New User");
			user.setName("user owner"/* profile_name.getSummary().toString() */);
			ClimbApplication.userDao.create(user);
			SharedPreferences pref = getApplicationContext().getSharedPreferences("UserSession", 0);
			Editor editor = pref.edit();
			editor.putInt("local_id", user.get_id());
			editor.commit();
			Log.d("local id", String.valueOf(user.get_id()));
		} else {
			System.out.println("trovato owner");
			User user = users.get(0);
			SharedPreferences pref = getApplicationContext().getSharedPreferences("UserSession", 0);
			Editor editor = pref.edit();
			editor.putInt("local_id", user.get_id());
			editor.commit();
			Log.d("local id", String.valueOf(user.get_id()));

		}
		ClimbApplication.refreshClimbings();
		ClimbApplication.refreshCollaborations();
		ClimbApplication.refreshCompetitions();
		ClimbApplication.refreshTeamDuels();
		ClimbApplication.refreshUserBadge();

		if (FacebookUtils.isOnline(this)) {
			Session session = Session.getActiveSession();
			if (session != null && session.isOpened()) {
				updateFacebookSession(session, session.getState());
			} else {
				Toast.makeText(sContext, getString(R.string.not_logged), Toast.LENGTH_SHORT).show();
			}
		} else {
			Toast.makeText(sContext, getString(R.string.connection_needed), Toast.LENGTH_SHORT).show();
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
			if (state.isOpened()) {
				if (mSession == null || isSessionChanged(session)) {
					mSession = session;

					Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {
						@Override
						public void onCompleted(GraphUser user, Response response) {
							if (session == Session.getActiveSession()) {
								if (user != null && pref.getString("FBid", "none").equalsIgnoreCase("none")) {
									// look for my FBid
									Map<String, Object> conditions = new HashMap<String, Object>();
									conditions.put("FBid", user.getId());
									User newUser = null;
									List<User> users = ClimbApplication.userDao.queryForFieldValuesArgs(conditions);
									if (users.size() > 0) {
										newUser = users.get(0);
										System.out.println("gia utente locale con fb no owner");

										// save data locally
										SharedPreferences pref = getApplicationContext().getSharedPreferences("UserSession", 0);
										Editor editor = pref.edit();
										editor.putString("FBid", newUser.getFBid());
										editor.putString("username", newUser.getName());
										editor.putInt("local_id", newUser.get_id());
										editor.commit();
										Log.d("local id", "salvato " + pref.getInt("local_id", 0));
										Log.d("FBid", "salvo " + pref.getString("FBid", ""));

										userExists(user, session);
									} else {
										Toast.makeText(getApplicationContext(), getString(R.string.not_logged), Toast.LENGTH_SHORT).show();
									}
								} else {
									System.err.println("no user");
									if (!pref.getString("FBid", "none").equalsIgnoreCase("none") && pref.getBoolean("openedFirst", false)){
										new MyAsync().execute();
										Editor edit = pref.edit();
										edit.putBoolean("openedFirst", false);
										edit.commit();
									}
								}
							}
							if (response.getError() != null) {
								Log.e(MainActivity.AppName, "FB exception: " + response.getError());
							}
						}
					});
					request.executeAsync();
				}
			} else if (state.isClosed()) {
				Log.i(MainActivity.AppName, "Logged out...");
			}
		} else
			Toast.makeText(getApplicationContext(), getString(R.string.check_connection), Toast.LENGTH_LONG).show();
	}

	private void userExists(final GraphUser fbUser, final Session session) {
		Log.d("main activity", "userExists");
		 final User me = ClimbApplication.getUserByFBId(fbUser.getId());

		ParseQuery<ParseUser> sameFBid = ParseUser.getQuery();
		sameFBid.whereEqualTo("FBid", fbUser.getId());
		sameFBid.findInBackground(new FindCallback<ParseUser>() {
			public void done(List<ParseUser> results, ParseException e) {
				if (results.isEmpty()) {// user not saved in Parse
					System.out.println("salvo");
					saveUserToParse(fbUser, session);
				} else {// user already saved in Parse
					System.out.println("c'è già");
					ParseUser user = results.get(0);
					ParseUser.logInInBackground(user.getUsername(), "", new LogInCallback() {
						public void done(ParseUser user, ParseException e) {
							if (user != null) {
								// Hooray! The user is logged in.
								// loadProgressFromParse();
								me.setLevel(user.getInt("level"));
		    		    				me.setXP(user.getInt("XP"));
		    		    				JSONObject stats = user.getJSONObject("man_daily_steps");
		    		    				try {		
		    		    					me.setBegin_date(String.valueOf(stats.getLong("begin_date")));
			    		    				me.setMean(stats.getLong("mean"));
			    		    				me.setN_measured_days(stats.getInt("n_days"));
			    		    				me.setCurrent_steps_value(stats.getInt("current_value"));
										} catch (JSONException e1) {
											// TODO Auto-generated catch block
											e1.printStackTrace();
										}
		    		    				ClimbApplication.userDao.update(me);
								new MyAsync().execute();
							} else {
								// Signup failed. Look at the ParseException to
								// see what happened.
								Toast.makeText(getApplicationContext(), getString(R.string.connection_problem), Toast.LENGTH_SHORT).show();
								Log.e("userExists", e.getMessage());
							}
						}
					});
				}
			}
		});
	}

	private void loadProgressFromParse() {// date non salvate
		Log.d("setting activity", "loadProgressFromParse");
		final SharedPreferences pref = getApplicationContext().getSharedPreferences("UserSession", 0);
		ClimbApplication.refreshClimbings();
		ClimbApplication.refreshCollaborations();
		ClimbApplication.refreshCompetitions();
		ClimbApplication.refreshTeamDuels();
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Climbing");
		query.whereEqualTo("users_id", pref.getString("FBid", ""));
		query.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> climbings, ParseException e) {
				if (e == null) {
					// save results locally
					for (ParseObject climb : climbings) {
						int idx = climbings.indexOf(climb);
						boolean last = idx == (climbings.size() - 1);
						Climbing localClimb = null;
						List<Climbing> climbs = ClimbApplication.getClimbingListForBuildingAndUser(climb.getInt("building"), pref.getInt("local_id", -1));
						if (climbs.size() > 0) {
							for (Climbing c : climbs) {
								if (c.getGame_mode() == climb.getInt("game_mode"))
									localClimb = c;
							}
						}
						if (localClimb == null) {
							System.out.println("creo nuovo");
							// save new climbing locally
							Climbing c = new Climbing();
							c.setBuilding(ClimbApplication.getBuildingById(climb.getInt("building")));
							c.setCompleted(climb.getDate("completedAt").getTime());
							c.setCompleted_steps(climb.getInt("completed_steps"));
							c.setCreated(climb.getDate("created").getTime());
							c.setModified(climb.getDate("modified").getTime());
							c.setPercentage(Float.valueOf(climb.getString("percentage")));
							c.setRemaining_steps(climb.getInt("remaining_steps"));
							c.setUser(ClimbApplication.getUserById(pref.getInt("local_id", -1)));
							c.setSaved(true);
							c.setId_mode(climb.getString("id_mode"));
							c.setGame_mode(climb.getInt("game_mode"));
							c.setId_online(climb.getObjectId());
							ClimbApplication.climbingDao.create(c);
							System.out.println("set id online" + c.getId_online());
							System.out.println("getgamemode" + c.getGame_mode());
							switch (c.getGame_mode()) {
							case 1:
								loadCollaborationsFromParse(c.getId_mode(), last);
								break;
							case 2:
								loadCompetitionsFromParse(c.getId_mode(), last);
								break;
							case 3:
								loadTeamDuelsFromParse(c.getId_mode(), last);
								break;
							default:
								break;
							}
							System.out.println("user " + c.getUser().get_id());
						} else {
							System.out.println("modifica");
							long localTime = localClimb.getModified();
							long parseTime = climb.getDate("modified").getTime();
							if (localTime < parseTime) { // parseTime è piu
															// recente
								System.out.println("c'è un aggiornamento");
								localClimb.setCompleted(climb.getDate("completedAt").getTime());
								localClimb.setCompleted_steps(climb.getInt("completed_steps"));
								localClimb.setCreated(climb.getDate("created").getTime());
								localClimb.setModified(climb.getDate("modified").getTime());
								localClimb.setPercentage(Float.valueOf(climb.getString("percentage")));
								localClimb.setRemaining_steps(climb.getInt("remaining_steps"));
								localClimb.setGame_mode(climb.getInt("game_mode"));
								localClimb.setSaved(true);
								localClimb.setId_mode(climb.getString("id_mode"));
								localClimb.setId_online(climb.getObjectId());
								System.out.println("set id online" + localClimb.getId_online());
								ClimbApplication.climbingDao.update(localClimb);
							}
							switch (localClimb.getGame_mode()) {
							case 1:
								loadCollaborationsFromParse(localClimb.getId_mode(), last);
								break;
							case 2:
								loadCompetitionsFromParse(localClimb.getId_mode(), last);
								break;
							case 3:
								loadTeamDuelsFromParse(localClimb.getId_mode(), last);
								break;
							default:
								break;
							}
						}
						if (last) {
							synchronized (ClimbApplication.lock) {
								ClimbApplication.lock.notify();
								ClimbApplication.BUSY = false;
							}
						}
					}
					ClimbApplication.refreshClimbings();
					ClimbApplication.refreshCollaborations();
					ClimbApplication.refreshCompetitions();
					ClimbApplication.refreshTeamDuels();

				} else {
					Toast.makeText(getApplicationContext(), getString(R.string.connection_problem), Toast.LENGTH_SHORT).show();
					Log.e("loadProgressFromParse", e.getMessage());
				}

			}
		});

	}

	private void loadTeamDuelsFromParse(String id, final boolean last) {
		final SharedPreferences pref = getApplicationContext().getSharedPreferences("UserSession", 0);
		ParseQuery<ParseObject> query = ParseQuery.getQuery("TeamDuel");
		query.whereEqualTo("objectId", id);
		query.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> duels, ParseException e) {
				if (e == null) {
					boolean created = false;
					ParseObject duel = duels.get(0);
					TeamDuel local_duel = ClimbApplication.getTeamDuelById(duel.getObjectId());
					if (local_duel == null) {
						local_duel = new TeamDuel();
						created = true;
					}
					User me = ClimbApplication.getUserById(pref.getInt("local_id", -1));
					local_duel.setId_online(duel.getObjectId());
					local_duel.setBuilding(ClimbApplication.getBuildingById(duel.getInt("building")));
					local_duel.setUser(me);
					local_duel.setDeleted(false);
					local_duel.setCompleted(duel.getBoolean("completed"));
					JSONObject challenger = duel.getJSONObject("challenger");
					JSONObject creator = duel.getJSONObject("creator");
					JSONObject creator_stairs = duel.getJSONObject("creator_stairs");
					JSONObject challenger_stairs = duel.getJSONObject("challenger_stairs");
					try {
						Iterator<String> it;
						if (creator.has(pref.getString("FBid", ""))) {
							local_duel.setCreator(true);
							local_duel.setMygroup(Group.CREATOR);
							local_duel.setSteps_my_group(ModelsUtil.sum(creator_stairs));
							local_duel.setSteps_other_group(ModelsUtil.sum(challenger_stairs));
							if (challenger.length() > 0) {
								it = challenger.keys();
								if (it.hasNext())
									local_duel.setChallenger_name(challenger.getString(it.next()));
								else
									local_duel.setChallenger_name("");
							}
							local_duel.setChallenger(false);
							local_duel.setCreator_name(me.getName());
							local_duel.setMy_steps(creator_stairs.getInt(me.getFBid()));

						} else if (challenger.has(pref.getString("FBid", ""))) {
							local_duel.setCreator(false);
							local_duel.setChallenger_name(me.getName());
							local_duel.setChallenger(true);
							if (creator.length() > 0) {
								it = creator.keys();
								if (it.hasNext())
									local_duel.setCreator_name(creator.getString(it.next()));
								else
									local_duel.setCreator_name("");
							}
							local_duel.setMygroup(Group.CHALLENGER);
							local_duel.setSteps_my_group(ModelsUtil.sum(challenger_stairs));
							local_duel.setSteps_my_group(ModelsUtil.sum(creator_stairs));
							local_duel.setMy_steps(challenger_stairs.getInt(me.getFBid()));
							local_duel.setChallenger_name(me.getName());

						} else {
							if (creator_stairs.has(pref.getString("FBid", ""))) {
								local_duel.setCreator(false);
								local_duel.setMygroup(Group.CREATOR);
								local_duel.setSteps_my_group(ModelsUtil.sum(creator_stairs));
								local_duel.setSteps_other_group(ModelsUtil.sum(challenger_stairs));
								if (challenger.length() > 0) {
									it = challenger.keys();
									if (it.hasNext())
										local_duel.setChallenger_name(challenger.getString(it.next()));
									else
										local_duel.setChallenger_name("");
								}
								local_duel.setChallenger(false);

								if (creator.length() > 0) {
									it = creator.keys();
									if (it.hasNext())
										local_duel.setCreator_name(creator.getString(it.next()));
									else
										local_duel.setCreator_name("");
								}
								local_duel.setMy_steps(creator_stairs.getInt(me.getFBid()));
							} else {
								local_duel.setCreator(false);
								if (challenger.length() > 0) {
									it = challenger.keys();
									if (it.hasNext())
										local_duel.setChallenger_name(challenger.getString(it.next()));
									else
										local_duel.setChallenger_name("");
								}
								local_duel.setChallenger(false);
								if (creator.length() > 0) {
									it = creator.keys();
									if (it.hasNext())
										local_duel.setCreator_name(creator.getString(it.next()));
									else
										local_duel.setCreator_name("");
								}
								local_duel.setMygroup(Group.CHALLENGER);
								local_duel.setSteps_my_group(ModelsUtil.sum(challenger_stairs));
								local_duel.setSteps_my_group(ModelsUtil.sum(creator_stairs));
								local_duel.setMy_steps(challenger_stairs.getInt(me.getFBid()));
							}
						}
					} catch (JSONException ex) {
						ex.printStackTrace();
					}
					if (challenger_stairs.length() == ClimbApplication.N_MEMBERS_PER_GROUP && creator_stairs.length() == ClimbApplication.N_MEMBERS_PER_GROUP)
						local_duel.setReadyToPlay(true);
					else
						local_duel.setReadyToPlay(false);
					local_duel.setSaved(true);
					if (created)
						ClimbApplication.teamDuelDao.create(local_duel);
					else
						ClimbApplication.teamDuelDao.update(local_duel);

				} else {
					Toast.makeText(getApplicationContext(), getString(R.string.connection_problem), Toast.LENGTH_SHORT).show();
					Log.e("loadTeamDuelsFromParse", e.getMessage());
				}
				if (last) {
					synchronized (ClimbApplication.lock) {
						ClimbApplication.lock.notify();
						ClimbApplication.BUSY = false;
					}
				}
			}

		});

	}

	private void loadCollaborationsFromParse(String id, final boolean last) {
		final SharedPreferences pref = getApplicationContext().getSharedPreferences("UserSession", 0);
		// MainActivity.refreshCollaborations();
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Collaboration");
		// query.whereEqualTo("collaborators." + pref.getString("FBid", ""),
		// pref.getString("username", ""));
		query.whereEqualTo("objectId", id);
		System.out.println("loadCollabortion: " + id);
		query.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> collabs, ParseException e) {
				if (e == null) {
					ParseObject collaboration = collabs.get(0);
					JSONObject others_steps = collaboration.getJSONObject("stairs");
					boolean completed = collaboration.getBoolean("completed");
					Collaboration local_collab = ClimbApplication.getCollaborationById(collaboration.getObjectId());
					if (local_collab == null) {
						// crea nuova collaborazione
						Collaboration coll = new Collaboration();
						coll.setBuilding(ClimbApplication.getBuildingById(collaboration.getInt("building")));
						coll.setId(collaboration.getObjectId());
						coll.setLeaved(false);
						coll.setMy_stairs(collaboration.getInt("my_stairs"));
						coll.setOthers_stairs(sumOthersStep(others_steps));
						coll.setSaved(true);
						coll.setUser(ClimbApplication.getUserById(pref.getInt("local_id", -1)));
						ClimbApplication.collaborationDao.create(coll);

					} else {// update collaborazione esistente
						if (local_collab.getMy_stairs() < collaboration.getInt("my_stairs"))
							local_collab.setMy_stairs(collaboration.getInt("my_stairs"));
						JSONObject others = collaboration.getJSONObject("stairs");
						local_collab.setOthers_stairs(sumOthersStep(others));
						local_collab.setSaved(true);
						local_collab.setCompleted(collaboration.getBoolean("completed"));
						ClimbApplication.collaborationDao.update(local_collab);

					}

				} else {
					Toast.makeText(getApplicationContext(), getString(R.string.connection_problem), Toast.LENGTH_SHORT).show();
					Log.e("loadCollaborationsFromParse", e.getMessage());
				}
				if (last) {
					synchronized (ClimbApplication.lock) {
						ClimbApplication.lock.notify();
						ClimbApplication.BUSY = false;
					}
				}
			}
		});
	}

	private int sumOthersStep(JSONObject others_step) {
		int sum = 0;
		Iterator keys = others_step.keys();
		while (keys.hasNext()) {
			try {
				sum += ((Integer) others_step.get((String) keys.next()));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return sum;
	}

	private void loadCompetitionsFromParse(String id, final boolean last) {
		final SharedPreferences pref = getApplicationContext().getSharedPreferences("UserSession", 0);
		// MainActivity.refreshCompetitions();
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Competition");
		// query.whereEqualTo("competitors." + pref.getString("FBid", ""),
		// pref.getString("username", ""));
		query.whereEqualTo("objectId", id);
		query.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> compets, ParseException e) {
				if (e == null) {
					ParseObject competition = compets.get(0);
					JSONObject others_steps = competition.getJSONObject("stairs");
					boolean completed = competition.getBoolean("completed");
					Competition local_compet = ClimbApplication.getCompetitionById(competition.getObjectId());
					if (local_compet == null) {
						// crea nuova collaborazione
						Competition comp = new Competition();
						comp.setBuilding(ClimbApplication.getBuildingById(competition.getInt("building")));
						comp.setId_online(competition.getObjectId());
						comp.setLeaved(false);
						comp.setMy_stairs(competition.getInt("my_stairs"));
						// setcurrentposition
						comp.setCurrent_position(ModelsUtil.getMyPosition(pref.getString("FBid", ""), ModelsUtil.fromJsonToSortedMap(competition.getJSONObject("stairs"))));
						comp.setSaved(true);

						comp.setUser(ClimbApplication.getUserById(pref.getInt("local_id", -1)));
						ClimbApplication.competitionDao.create(comp);

					} else {// update collaborazione esistente
						if (local_compet.getMy_stairs() < competition.getInt("my_stairs"))
							local_compet.setMy_stairs(competition.getInt("my_stairs"));
						JSONObject others = competition.getJSONObject("stairs");
						// setcurrentposition
						local_compet.setCurrent_position(ModelsUtil.getMyPosition(pref.getString("FBid", ""), ModelsUtil.fromJsonToSortedMap(competition.getJSONObject("stairs"))));
						local_compet.setSaved(true);
						ClimbApplication.competitionDao.update(local_compet);

					}

				} else {
					Toast.makeText(getApplicationContext(), getString(R.string.connection_problem), Toast.LENGTH_SHORT).show();
					Log.e("loadCompetitionsFromParse", e.getMessage());
				}
				if (last) {
					synchronized (ClimbApplication.lock) {
						ClimbApplication.lock.notify();
						ClimbApplication.BUSY = false;
					}
				}
			}
		});
	}

	private void saveUserToParse(GraphUser fbUser, Session session) {

		ParseUser user = new ParseUser();
		user.setUsername(fbUser.getName());
		user.setPassword("");
		user.put("FBid", fbUser.getId()); // System.out.println(fbUser.getId());

		user.signUpInBackground(new SignUpCallback() {
			public void done(ParseException e) {
				if (e == null) {
					Log.d("SIGN UP", "SIGN UP");
					saveProgressToParse();
				} else {
					Toast.makeText(getApplicationContext(), getString(R.string.connection_problem), Toast.LENGTH_SHORT).show();
					Log.e("signUpInBackground", e.getMessage());
					// Sign up didn't succeed. Look at the ParseException
					// to figure out what went wrong
				}
			}
		});
	}

	private void saveProgressToParse() {
		ClimbApplication.refreshClimbings();
		System.out.println(ClimbApplication.climbings.size());
		for (Climbing climbing : ClimbApplication.climbings) {
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			df.setTimeZone(new SimpleTimeZone(0, "GMT"));
			ParseObject climb = new ParseObject("Climbing");
			climb.put("building", climbing.getBuilding().get_id());
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
			climb.saveEventually();

		}
	}

	/**
	 * Check for an incoming notifications. If there's any and if they're valid,
	 * then create the corresponding Notification object and add it to the
	 * Nofitication list.
	 */
	public void onUpdateNotifications(MenuItem v) {
		// prendere dati richiesta, vedere se è valida e salvare su db
		// Check for an incoming notification. Save the info
		Uri intentUri = getIntent().getData();
		if (intentUri != null) {
			String requestIdParam = intentUri.getQueryParameter("request_ids");
			if (requestIdParam != null) {
				String array[] = requestIdParam.split(",");
				System.out.println("array " + array.length);
				for (int i = 0; i < array.length; i++) {
					requestId = array[i];
					Log.i("onActivityCreated", "Request id: " + requestId);
					// deleteRequest(requestId);
					getRequestData(requestId);
				}
			}

			System.out.println("notf " + ClimbApplication.notifications.size());
		}
	}

	/**
	 * Examines the request with given id and create the corrispondent
	 * Notification object
	 * 
	 * @param inRequestId
	 *            the id of the request to examine
	 */
	private void getRequestData(final String inRequestId) {
		// Create a new request for an HTTP GET with the
		// request ID as the Graph path.
		Request request = new Request(Session.getActiveSession(), inRequestId, null, HttpMethod.GET, new Request.Callback() {

			@Override
			public void onCompleted(Response response) {
				// Process the returned response
				GraphObject graphObject = response.getGraphObject();
				FacebookRequestError error = response.getError();
				String message = "";
				if (graphObject != null) {
					Log.d("graph obj not null", graphObject.toString());

					// Get the data, parse info to get the key/value
					// info
					JSONObject dataObject;
					JSONObject fromObject;
					JSONObject toObject;
					String groupName = "";
					String sender = "";
					String toId = "noId";
					int type = -1;
					String id = "";

					int building_id = -1;
					String building_name = "";
					String collaboration_id = "";
					boolean isReceiverChallenged = false;
					boolean isSenderCreator = false;
					String duel_id = "";

					try {
						dataObject = new JSONObject((String) graphObject.getProperty("data"));
						fromObject = (JSONObject) graphObject.getProperty("from");
						toObject = (JSONObject) graphObject.getProperty("to");
						type = dataObject.getInt("type");
						sender = fromObject.getString("name");
						toId = toObject.getString("id");
						id = ((String) graphObject.getProperty("id"));

						System.out.println("type " + type);

						if (type == 1 || type == 2) {
							building_id = dataObject.getInt("idBuilding");
							building_name = dataObject.getString("nameBuilding");
							collaboration_id = dataObject.getString("idCollab");
						} else if (type == 3) {
							isReceiverChallenged = dataObject.getBoolean("challenger");
							isSenderCreator = dataObject.getBoolean("isSenderCreator");
							duel_id = dataObject.getString("idCollab");
							building_id = dataObject.getInt("idBuilding");
							building_name = dataObject.getString("nameBuilding");
						}

					} catch (JSONException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();

					}

					message += " " + groupName;

					String time = (String) graphObject.getProperty("created_time");
					message += " " + time;
					if (isValid(time) && toId.equalsIgnoreCase(pref.getString("FBid", ""))) {
						Log.d("qui", "request valida");
						switch (type) {
						case 0:
							Notification notf = new InviteNotification(id, sender, groupName, type);
							ClimbApplication.notifications.add(notf);
							break;

						case 1:
							Notification notfA = new AskCollaborationNotification(id, sender, groupName, type);
							((AskCollaborationNotification) notfA).setBuilding_id(building_id);
							((AskCollaborationNotification) notfA).setBuilding_name(building_name);
							((AskCollaborationNotification) notfA).setCollaborationId(collaboration_id);
							ClimbApplication.notifications.add(notfA);
							break;

						case 2:
							Notification notfB = new AskCompetitionNotification(id, sender, groupName, type);
							((AskCompetitionNotification) notfB).setBuilding_id(building_id);
							((AskCompetitionNotification) notfB).setBuilding_name(building_name);
							((AskCompetitionNotification) notfB).setCompetitionId(collaboration_id);
							ClimbApplication.notifications.add(notfB);
							break;
						case 3:
							Notification notfC = new AskTeamDuelNotification(id, sender, duel_id, type, isReceiverChallenged, isSenderCreator);
							((AskTeamDuelNotification) notfC).setBuilding_id(building_id);
							((AskTeamDuelNotification) notfC).setBuilding_name(building_name);
							if (isReceiverChallenged)
								((AskTeamDuelNotification) notfC).setType(NotificationType.ASK_TEAM_COMPETITION_CHALLENGER);
							else
								((AskTeamDuelNotification) notfC).setType(NotificationType.ASK_TEAM_COMPETITION_TEAM);

							ClimbApplication.notifications.add(notfC);
							break;
						}

					}

					String title = "";
					// Create the text for the alert based on the sender
					// and the data
					message = title;

				} else {
					System.out.println("graph obj null");
				}
				Toast.makeText(getApplicationContext(), getString(R.string.request_arrived), Toast.LENGTH_SHORT).show();
				// deleteRequest(inRequestId);//da chiamare solo se non ci sono
				// errori
			}
		});
		// Execute the request asynchronously.
		Request.executeBatchAsync(request);
	}

	private boolean isValid(String creation_time) {
		SimpleDateFormat ISO8601DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ITALY);
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

	public void onShowActionProfile(MenuItem v) {
		Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
		startActivity(intent);
	}

	public void onShowSettings(MenuItem v) {
		Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
		startActivity(intent);
	}

	public void onShowGroups(MenuItem v) {
		Intent intent = new Intent(getApplicationContext(), GroupsActivity.class);
		startActivity(intent);
	}

	@Override
	protected void onResume() {
		Log.i(MainActivity.AppName, "MainActivity onResume");
		super.onResume();
		ClimbApplication.refresh();
		Session session = Session.getActiveSession();
		if (session != null && (session.isOpened() || session.isClosed())) {
			onSessionStateChange(session, session.getState(), null);
		}

		uiHelper.onResume();
	}

	@Override
	protected void onPause() {
		Log.i(MainActivity.AppName, "MainActivity onPause");
		super.onPause();
		uiHelper.onPause();

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
			Toast.makeText(getApplicationContext(), getString(R.string.check_connection), Toast.LENGTH_LONG).show();
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
		dialog = new WebDialog.Builder(this, Session.getActiveSession(), action, params).setOnCompleteListener(new WebDialog.OnCompleteListener() {

			@Override
			public void onComplete(Bundle values, FacebookException error) {
				if (error != null) {
					if (error instanceof FacebookOperationCanceledException) {
						Toast.makeText(getApplicationContext(), getString(R.string.request_cancelled), Toast.LENGTH_SHORT).show();
					} /*
					 * else { Toast.makeText(getApplicationContext(),
					 * "Network Error", Toast.LENGTH_SHORT).show(); }
					 */
				} else {
					final String requestId = values.getString("request");
					if (requestId != null) {
						Toast.makeText(getApplicationContext(), getString(R.string.request_sent), Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(getApplicationContext(), getString(R.string.request_cancelled), Toast.LENGTH_SHORT).show();
					}
				}
			}

		}).build();

		// Hide the notification bar and resize to full screen
		Window dialog_window = dialog.getWindow();
		dialog_window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

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
			File file = new File(ClimbApplication.dbHelper.getDbPath()); // get
																			// private
																			// db
			// reference
			if (file.exists() == false || file.length() == 0)
				throw new Exception("Empty DB");
			this.copyFile(new FileInputStream(file), this.openFileOutput(output_name, MODE_WORLD_READABLE));
			file = this.getFileStreamPath(output_name);
			Intent i = new Intent(Intent.ACTION_SEND);
			i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
			i.setType("*/*");
			startActivity(Intent.createChooser(i, "Share to"));
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), getString(R.string.db_error, e.getMessage()), Toast.LENGTH_SHORT).show();
			Log.e(AppName, e.getMessage());
		}
	}

	public static void emptyNotificationList() {
		ClimbApplication.notifications.clear();
	}

	// ONLY FOR DEBUG
	private void deleteRequest(String inRequestId) {
		// Create a new request for an HTTP delete with the
		// request ID as the Graph path.
		Request request = new Request(Session.getActiveSession(), inRequestId, null, HttpMethod.DELETE, new Request.Callback() {

			@Override
			public void onCompleted(Response response) {
				// Show a confirmation of the deletion
				// when the API call completes successfully.
				Toast.makeText(MainActivity.getContext(), getString(R.string.request_deleted), Toast.LENGTH_SHORT).show();
			}
		});
		// Execute the request asynchronously.
		Request.executeBatchAsync(request);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		uiHelper.onActivityResult(requestCode, resultCode, data);
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
