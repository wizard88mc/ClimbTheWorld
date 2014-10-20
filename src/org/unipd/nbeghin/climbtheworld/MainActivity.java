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
import android.app.Activity;
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
	

	private WebDialog dialog = null;
	private String dialogAction = null;
	private Bundle dialogParams = null;
	ProgressDialog PD;

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

		sContext = getApplicationContext();

		//if not logged in, login default owner user
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

	private void setUserOwner() {
		Map<String, Object> conditions = new HashMap<String, Object>();
		conditions.put("owner", new Integer(1));
		List<User> users = ClimbApplication.userDao.queryForFieldValuesArgs(conditions);

		if (users.isEmpty()) { //is there user owner in the db????
			// no, create local user owner
			User user = new User();
			user.setFBid("empty");
			user.setLevel(0);
			user.setXP(0);
			user.setOwner(true);
			user.setName("user owner");
			ClimbApplication.userDao.create(user);
			ClimbApplication.setCurrentUser(user);
			SharedPreferences pref = getApplicationContext().getSharedPreferences("UserSession", 0);
			Editor editor = pref.edit();
			editor.putInt("local_id", user.get_id());
			editor.commit();
		} else {
			//owner found
			User user = users.get(0);
			ClimbApplication.setCurrentUser(user);
			Log.d("MainActivity", "current User set");
			SharedPreferences pref = getApplicationContext().getSharedPreferences("UserSession", 0);
			Editor editor = pref.edit();
			editor.putInt("local_id", user.get_id());
			editor.commit();

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

	/**
	 * Method necessary not to repeat makeMeRequest twice
	 * @param session
	 * @return
	 */
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
		Log.d("MainActivity", "updateFBSession");
		if (FacebookUtils.isOnline(this)) {
			if (state.isOpened()) {
				//this check is necessary not to repeat the newMeRequest twice
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
										//this FBid is already linked to a user
										newUser = users.get(0);
										ClimbApplication.setCurrentUser(newUser);
										// save data locally
										SharedPreferences pref = getApplicationContext().getSharedPreferences("UserSession", 0);
										Editor editor = pref.edit();
										editor.putString("FBid", newUser.getFBid());
										editor.putString("username", newUser.getName());
										editor.putInt("local_id", newUser.get_id());
										editor.commit();
										String own = newUser.isOwner() ? "\n" + getString(R.string.owner) : "";
										Toast.makeText(getApplicationContext(), getString(R.string.logged_as, newUser.getName()) + own, Toast.LENGTH_SHORT).show();
							
										ClimbApplication.userExists(user, session, PD, MainActivity.this);
									} else {
										Toast.makeText(getApplicationContext(), getString(R.string.not_logged), Toast.LENGTH_SHORT).show();
									}
								} else {
									System.err.println("no user");
									//to do only when the user opens main activity for the first time
									if (!pref.getString("FBid", "none").equalsIgnoreCase("none") && pref.getBoolean("openedFirst", false)){
										new MyAsync(MainActivity.this, PD).execute();
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

	private class NotificationAsyncTask extends AsyncTask<Void, Void, Void> {

		final SharedPreferences pref = ClimbApplication.getContext().getSharedPreferences("UserSession", 0);
		private ProgressDialog PD;
		
		
		NotificationAsyncTask() {
			
		}

		@Override
		protected void onPreExecute() {

			super.onPreExecute();
			//me = ClimbApplication.getUserById(activity.getSharedPreferences("UserSession", 0).getInt("local_id", -1));
			PD = new ProgressDialog(MainActivity.this);
			PD.setTitle(MainActivity.this.getString(R.string.wait));
			PD.setMessage(MainActivity.this.getString(R.string.download_notifications));
			PD.setCancelable(false);
			PD.show();
		}

		@Override
		protected Void doInBackground(Void... params) {
			Session session = Session.getActiveSession();
			Bundle params_ = new Bundle();
			params_.putString("access_token", session.getAccessToken());
			Request request = new Request(session, "me/apprequests", params_, HttpMethod.GET, new Request.Callback() {

			    @Override
			    public void onCompleted(Response response) {
			        try {
			        		List<Request> deleteRequests = new ArrayList<Request>();
			            GraphObject res = response.getGraphObject();
			            JSONArray array = (JSONArray) res.getProperty("data");
			            Log.d("MainActivity", "Notifications: " + array.length());
			            for(int i = 0; i < array.length(); i++){
			            		createNotification((JSONObject) array.get(i), deleteRequests);
			            }
			            Request.executeBatchAndWait(deleteRequests);
			        } catch (Exception e) {
			        }
			    }
			});
			Request.executeAndWait(request);

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			PD.dismiss();
			if(ClimbApplication.notifications.isEmpty())
				Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.no_notification), Toast.LENGTH_SHORT).show();
			else
				Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.n_notification, ClimbApplication.notifications.size()), Toast.LENGTH_SHORT).show();

		}
	}

	/**
	 * Starts to download facebook notification for current user e create the corresponding Notification objects.
	 * @param v
	 */
	public void downloadFBNotification(MenuItem v){
		new NotificationAsyncTask().execute();
	}



	/**
	 * Check for an incoming notifications. If there's any and if they're valid,
	 * then create the corresponding Notification object and add it to the
	 * Nofitication list.
	 */
	public void onUpdateNotifications(MenuItem v) {
		// Check for an incoming notification. Save the info if it is valid
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
				if(ClimbApplication.notifications.isEmpty())
					Toast.makeText(this, getString(R.string.no_notification), Toast.LENGTH_SHORT).show();
				else
					Toast.makeText(this, getString(R.string.n_notification, ClimbApplication.notifications.size()), Toast.LENGTH_SHORT).show();

			}

			System.out.println("notf " + ClimbApplication.notifications.size());
		}
	}
	
	private void createNotification(JSONObject notification, List<Request> deleted){System.out.println("create notification");
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
			dataObject = new JSONObject(notification.getString("data"));
			fromObject = new JSONObject(notification.getString("from"));
			toObject = new JSONObject(notification.getString("to"));
			type = dataObject.getInt("type");
			sender = fromObject.getString("name");
			toId = toObject.getString("id");
			id =  notification.getString("id");

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

		


		String time = notification.getString("created_time");
		boolean isNotfValid = isValid(time);
		if (isNotfValid && toId.equalsIgnoreCase(pref.getString("FBid", ""))) {
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

		}else if (!isNotfValid && toId.equalsIgnoreCase(pref.getString("FBid", ""))){
			deleted.add(deleteRequest(id));
			//Request.executeBatchAndWait(deleteRequest(id));
		}
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();

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
					boolean isNotfValid = isValid(time);
					if (isNotfValid && toId.equalsIgnoreCase(pref.getString("FBid", ""))) {
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

					}else if (!isNotfValid && toId.equalsIgnoreCase(pref.getString("FBid", ""))){
						Request.executeBatchAsync(deleteRequest(inRequestId));
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
	

	/**
	 * Checks if the notification is valid (that is if the given date is older than 24h).
	 * @param creation_time creation time of the notification to check
	 * @return true if (current time - creation_time) < 24h, false otherwise
	 */
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
		if (FacebookUtils.isOnline(this)){
			sendInviteToFriends();			
		}else
			Toast.makeText(getApplicationContext(), getString(R.string.check_connection), Toast.LENGTH_LONG).show();
	}

	
	private void sendInviteToFriends() {
		Session session = Session.getActiveSession();
		if (session == null || !session.isOpened()) {
			Toast.makeText(getApplicationContext(), getString(R.string.not_logged_in), Toast.LENGTH_LONG).show();
			return;
		}
		 Intent intent = new Intent(sContext, FBPickFriendActivity.class);
		 startActivity(intent);

	}

	/**
	 * Send invite to user's facebook friends.
	 */
	private void sendInvites() {
		Bundle params = new Bundle();

		// Uncomment following link once uploaded on Google Play for deep
		// linking
		// params.putString("link",
		// "https://play.google.com/store/apps/details?id=com.facebook.android.friendsmash");

		// 1. No additional parameters provided - enables generic Multi-friend
		// selector
		params.putString("message", "Join me in Climb the world");

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

//DEBUG ONLY
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

	/**
	 * Create a Request object to delete a request in Facebook
	 * @param inRequestId the id of the request to be deleted
	 * @return the Request object to delete the request with the given id from Facebook
	 */
	private Request deleteRequest(final String inRequestId) {
		// Create a new request for an HTTP delete with the
		// request ID as the Graph path.
		Request request = new Request(Session.getActiveSession(), inRequestId, null, HttpMethod.DELETE, new Request.Callback() {

			@Override
			public void onCompleted(Response response) {
				// Show a confirmation of the deletion
				// when the API call completes successfully.
				Log.d("deleteRequest", "Request " + inRequestId + " succesfully deleted");
				Toast.makeText(MainActivity.getContext(), getString(R.string.request_deleted), Toast.LENGTH_SHORT).show();
			}
		});
		// Execute the request asynchronously.
		//Request.executeBatchAsync(request);
		return request;
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
