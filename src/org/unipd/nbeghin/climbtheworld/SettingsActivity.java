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
 * A {@link PreferenceActivity} that presents a set of application settings. On handset devices, settings are presented as a single list. On tablets, settings are split by category, with category headers shown to the left of the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html"> Android Design: Settings</a> for design guidelines and the <a href="http://developer.android.com/guide/topics/ui/settings.html">Settings API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity {
	/**
	 * Determines whether to always show the simplified settings UI, where settings are presented in a single list. When false, settings are shown as a master/detail two-pane view on tablets. When true, a single pane is shown on tablets.
	 */
	private static final boolean	ALWAYS_SIMPLE_PREFS	= true;
	private UiLifecycleHelper		uiHelper;
	private Session mSession;
	private Session.StatusCallback	callback			= new Session.StatusCallback() {
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
		
		boolean isnull = savedInstanceState == null;
		
		Session session = Session.getActiveSession();
		if (session != null && session.isOpened()) {
			updateFacebookSession(session, session.getState());
		}
		
		
	}
	
	ProgressDialog PD;
	 private class MyAsync extends AsyncTask<Void, Void, Void> {

		// Session session;
		 ParseUser user;
		 
		 MyAsync(ParseUser user){
			// session = session;
			 this.user = user;
		 }
		  @Override
		  protected void onPreExecute() {
		 
		   super.onPreExecute();
		   PD = new ProgressDialog(SettingsActivity.this);
		   PD.setTitle(SettingsActivity.this.getString(R.string.wait));
		   PD.setMessage(SettingsActivity.this.getString(R.string.loading_progress));
		   PD.setCancelable(false);
		   PD.show();
		  }

		  @Override
		  protected Void doInBackground(Void... params) {
						ClimbApplication.BUSY = true;
						JSONArray badges = user.getJSONArray("badges");
						if(badges != null)
							saveBadges(badges);
						else{
							badges = new JSONArray();
							user.put("badges", badges);
							user.saveEventually();
						}
					  loadProgressFromParse();
					  synchronized (ClimbApplication.lock) {
						while(ClimbApplication.BUSY){
							try {
								ClimbApplication.lock.wait();
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				
		
			//updateFacebookSession(session, session.getState());
		   return null;
		  }

		  @Override
		  protected void onPostExecute(Void result) {  
		   super.onPostExecute(result);
		   PD.dismiss();
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
	    }
	    else if (session.getAccessToken() != null) {
	        return true;
	    }

	    // Nothing changed
	    return false;
	}
	
	private void saveProgressToParse(){
		ClimbApplication.refreshClimbings();
		System.out.println(ClimbApplication.climbings.size());
		for(Climbing climbing:ClimbApplication.climbings){
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
	
	private void loadProgressFromParse(){//date non salvate
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
				if(e == null){
					//save results locally
					for(ParseObject climb : climbings){
						int idx = climbings.indexOf(climb);
						boolean last = idx == (climbings.size() - 1);
						Climbing localClimb = null;
//						if(climb.getString("id_mode").equalsIgnoreCase("paused"))
//							localClimb = ClimbApplication.getClimbingForBuildingAndUserPaused(climb.getInt("building"), pref.getInt("local_id", -1));
//						else
//							localClimb = ClimbApplication.getClimbingForBuildingAndUserNotPaused(climb.getInt("building"), pref.getInt("local_id", -1));
//						//System.out.println("cerco building " + climb.getInt("building") + " e user " + pref.getInt("local_id", -1));
						List<Climbing> climbs = ClimbApplication.getClimbingListForBuildingAndUser(climb.getInt("building"), pref.getInt("local_id", -1));
						if(climbs.size() > 0){
							for(Climbing c : climbs){
								if(c.getGame_mode() == climb.getInt("game_mode"))
									localClimb = c;
							}
						}
						if(localClimb == null){ System.out.println("creo nuovo");
							//save new climbing locally
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
							switch(c.getGame_mode()){
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
						}else{
							System.out.println("modifica");
							long localTime = localClimb.getModified();
							long parseTime = climb.getDate("modified").getTime();
							if (localTime < parseTime){ //parseTime è piu recente
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
							switch(localClimb.getGame_mode()){
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
						if(last){
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

				}else{
					Toast.makeText(getApplicationContext(), getString(R.string.connection_problem), Toast.LENGTH_SHORT).show();
					Log.e("loadProgressFromParse", e.getMessage());
				}
				
				
			}
		});
		
		
	}
	
	private int sumOthersStep(JSONObject others_step){
		int sum = 0;
		Iterator keys = others_step.keys();
		while(keys.hasNext()){
			try {
				sum += ((Integer) others_step.get((String) keys.next()));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return sum;
	}
	
	private void loadTeamDuelsFromParse(String id, final boolean last){
		final SharedPreferences pref = getApplicationContext().getSharedPreferences("UserSession", 0);
		ParseQuery<ParseObject> query = ParseQuery.getQuery("TeamDuel");
		query.whereEqualTo("objectId", id);
		query.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> duels, ParseException e) {
				if(e == null){
					boolean created = false;
					ParseObject duel = duels.get(0);
					TeamDuel local_duel = ClimbApplication.getTeamDuelById(duel.getObjectId());
					if(local_duel == null){
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
						try{
						Iterator<String> it;
						if(creator.has(pref.getString("FBid", ""))){
							local_duel.setCreator(true);
							local_duel.setMygroup(Group.CREATOR);
							local_duel.setSteps_my_group(ModelsUtil.sum(creator_stairs));
							local_duel.setSteps_other_group(ModelsUtil.sum(challenger_stairs));
							if(challenger.length() > 0){
								it = challenger.keys();
								if(it.hasNext()) local_duel.setChallenger_name(challenger.getString(it.next()));
								else local_duel.setChallenger_name("");
							}
							local_duel.setChallenger(false);
							local_duel.setCreator_name(me.getName());
							local_duel.setMy_steps(creator_stairs.getInt(me.getFBid()));
							
						}else if(challenger.has(pref.getString("FBid", ""))) {
							local_duel.setCreator(false);
							local_duel.setChallenger_name(me.getName());
							local_duel.setChallenger(true);
							if(creator.length() > 0){
								it = creator.keys();
								if(it.hasNext()) local_duel.setCreator_name(creator.getString(it.next()));
								else local_duel.setCreator_name("");}
							local_duel.setMygroup(Group.CHALLENGER);
							local_duel.setSteps_my_group(ModelsUtil.sum(challenger_stairs));
							local_duel.setSteps_my_group(ModelsUtil.sum(creator_stairs));
							local_duel.setMy_steps(challenger_stairs.getInt(me.getFBid()));
							local_duel.setChallenger_name(me.getName());
							
						}else{
							if(creator_stairs.has(pref.getString("FBid", ""))){
								local_duel.setCreator(false);
								local_duel.setMygroup(Group.CREATOR);
								local_duel.setSteps_my_group(ModelsUtil.sum(creator_stairs));
								local_duel.setSteps_other_group(ModelsUtil.sum(challenger_stairs));
								if(challenger.length() > 0){
									it = challenger.keys();
									if(it.hasNext()) local_duel.setChallenger_name(challenger.getString(it.next()));
									else local_duel.setChallenger_name("");}
								local_duel.setChallenger(false);
								
								if(creator.length() > 0){
									it = creator.keys();
									if(it.hasNext()) local_duel.setCreator_name(creator.getString(it.next()));
									else local_duel.setCreator_name("");}
								local_duel.setMy_steps(creator_stairs.getInt(me.getFBid()));	
							}else{
								local_duel.setCreator(false);
								if(challenger.length() > 0){
									it = challenger.keys();
									if(it.hasNext()) local_duel.setChallenger_name(challenger.getString(it.next()));
									else local_duel.setChallenger_name("");}
								local_duel.setChallenger(false);
								if(creator.length() > 0){
									it = creator.keys();
									if(it.hasNext()) local_duel.setCreator_name(creator.getString(it.next()));
									else local_duel.setCreator_name("");}
								local_duel.setMygroup(Group.CHALLENGER);
								local_duel.setSteps_my_group(ModelsUtil.sum(challenger_stairs));
								local_duel.setSteps_my_group(ModelsUtil.sum(creator_stairs));
								local_duel.setMy_steps(challenger_stairs.getInt(me.getFBid()));
							}
						}
						}catch(JSONException ex){
							ex.printStackTrace();
						}
						if(challenger_stairs.length() == ClimbApplication.N_MEMBERS_PER_GROUP && creator_stairs.length() == ClimbApplication.N_MEMBERS_PER_GROUP)
							local_duel.setReadyToPlay(true);
						else local_duel.setReadyToPlay(false);
						local_duel.setSaved(true);
						if (created) ClimbApplication.teamDuelDao.create(local_duel);
						else ClimbApplication.teamDuelDao.update(local_duel);
					
				}else{
					Toast.makeText(getApplicationContext(), getString(R.string.connection_problem), Toast.LENGTH_SHORT).show();
					Log.e("loadTeamDuelsFromParse", e.getMessage());
				}
				if(last){
					synchronized (ClimbApplication.lock) {
					ClimbApplication.lock.notify();
					ClimbApplication.BUSY = false;
				}
				}
			}

		});

	}
	
	private void loadCollaborationsFromParse(String id, final boolean last){
		final SharedPreferences pref = getApplicationContext().getSharedPreferences("UserSession", 0);
		//ClimbApplication.refreshCollaborations();
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Collaboration");
		//query.whereEqualTo("collaborators." + pref.getString("FBid", ""), pref.getString("username", ""));
		query.whereEqualTo("objectId", id);
		System.out.println("loadCollabortion: " + id);
		query.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> collabs, ParseException e) {
					if(e == null){
							ParseObject collaboration = collabs.get(0);
							JSONObject others_steps = collaboration.getJSONObject("stairs");
							boolean completed = collaboration.getBoolean("completed");
							Collaboration local_collab = ClimbApplication.getCollaborationById(collaboration.getObjectId());
							if(local_collab == null){
								//crea nuova collaborazione
								Collaboration coll = new Collaboration();
								coll.setBuilding(ClimbApplication.getBuildingById(collaboration.getInt("building")));
								coll.setId(collaboration.getObjectId());
								coll.setLeaved(false);
								coll.setMy_stairs(collaboration.getInt("my_stairs"));
								coll.setOthers_stairs(sumOthersStep(others_steps));
								coll.setSaved(true);
								coll.setUser(ClimbApplication.getUserById(pref.getInt("local_id", -1)));
								ClimbApplication.collaborationDao.create(coll);
								
								
							}else{//update collaborazione esistente
								if(local_collab.getMy_stairs() < collaboration.getInt("my_stairs"))
									local_collab.setMy_stairs(collaboration.getInt("my_stairs"));
								JSONObject others = collaboration.getJSONObject("stairs");
								local_collab.setOthers_stairs(sumOthersStep(others));
								local_collab.setSaved(true);
								local_collab.setCompleted(collaboration.getBoolean("completed"));
								ClimbApplication.collaborationDao.update(local_collab);
								
							}
						
					}else{
						Toast.makeText(getApplicationContext(), getString(R.string.connection_problem), Toast.LENGTH_SHORT).show();
						Log.e("loadCollaborationsFromParse", e.getMessage());
					}
					if(last){
						synchronized (ClimbApplication.lock) {

						ClimbApplication.lock.notify();
						ClimbApplication.BUSY = false;
					}
					}
			}
		});
	}
	
	
	
	private void loadCompetitionsFromParse(String id, final boolean last){
		final SharedPreferences pref = getApplicationContext().getSharedPreferences("UserSession", 0);
	//	ClimbApplication.refreshCompetitions();
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Competition");
		//query.whereEqualTo("competitors." + pref.getString("FBid", ""), pref.getString("username", ""));
		query.whereEqualTo("objectId", id);
		query.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> compets, ParseException e) {
					if(e == null){
						ParseObject competition = compets.get(0);
						JSONObject others_steps = competition.getJSONObject("stairs");
							boolean completed = competition.getBoolean("completed");
							Competition local_compet = ClimbApplication.getCompetitionById(competition.getObjectId());
							if(local_compet == null){
								//crea nuova collaborazione
								Competition comp = new Competition();
								comp.setBuilding(ClimbApplication.getBuildingById(competition.getInt("building")));
								comp.setId_online(competition.getObjectId());
								comp.setLeaved(false);
								comp.setMy_stairs(competition.getInt("my_stairs"));
								//setcurrentposition
								comp.setCurrent_position(ModelsUtil.getMyPosition(pref.getString("FBid", ""), ModelsUtil.fromJsonToSortedMap(competition.getJSONObject("stairs"))));
								comp.setSaved(true);
								
								comp.setUser(ClimbApplication.getUserById(pref.getInt("local_id", -1)));
								ClimbApplication.competitionDao.create(comp);
								
							}else{//update collaborazione esistente
								if(local_compet.getMy_stairs() < competition.getInt("my_stairs"))
									local_compet.setMy_stairs(competition.getInt("my_stairs"));
								JSONObject others = competition.getJSONObject("stairs");
								//setcurrentposition
								local_compet.setCurrent_position(ModelsUtil.getMyPosition(pref.getString("FBid", ""), ModelsUtil.fromJsonToSortedMap(competition.getJSONObject("stairs"))));
								local_compet.setSaved(true);
								ClimbApplication.competitionDao.update(local_compet);
								
							}
						
					}else{
						Toast.makeText(getApplicationContext(), getString(R.string.connection_problem), Toast.LENGTH_SHORT).show();
						Log.e("loadCompetitionsFromParse", e.getMessage());
					}
					if(last){
						synchronized (ClimbApplication.lock) {

						ClimbApplication.lock.notify();
						ClimbApplication.BUSY = false;
					}
					}
			}
		});
	}
	
	private void updateFacebookSession(final Session session, SessionState state) {
		if(FacebookUtils.isOnline(this)){
		final TextView lblFacebookUser = (TextView) findViewById(R.id.lblFacebookUser);
		final ProfilePictureView profilePictureView = (ProfilePictureView) findViewById(R.id.fb_profile_picture);
		final Preference profile_name=findPreference("profile_name");
		final SharedPreferences pref = getApplicationContext().getSharedPreferences("UserSession", 0);

		if (state.isOpened()) {
	//		if (mSession == null || isSessionChanged(session)) {
	   //         mSession = session;

			Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {
				@Override
				public void onCompleted(GraphUser user, Response response) {
					if (session == Session.getActiveSession()) {
						if (user != null && profilePictureView != null) {
							if(pref.getString("FBid", "none").equalsIgnoreCase("none")){
							//look for my FBid
							Map<String, Object> conditions = new HashMap<String, Object>();
							conditions.put("FBid", user.getId());
							User newUser = null;
							List<User> users = ClimbApplication.userDao.queryForFieldValuesArgs(conditions);
							
							Map<String, Object> conditions2 = new HashMap<String, Object>();
							conditions2.put("owner", 1);
							List<User> users2 = ClimbApplication.userDao.queryForFieldValuesArgs(conditions2);
							
							//already connected FB account????
							if(users.isEmpty()){			//no, am I the owner????					
								if(!users2.isEmpty()){ //there is an owner locally
									System.out.println("c'è owner");
									if(users2.get(0).getFBid().equalsIgnoreCase("empty")){
										//this is FBis of the owner
										System.out.println("prendo owner e ci setto fb");
										newUser = users2.get(0);
										newUser.setFBid(user.getId());
										newUser.setName(user.getName());
										ClimbApplication.userDao.update(newUser);
									}else{
										System.out.println("nuovo host con fb");
										//new host user
										newUser = new User();
										newUser.setFBid(user.getId());
										newUser.setName(user.getName());
										newUser.setLevel(0);
										newUser.setXP(0);
										newUser.setOwner(false);
										ClimbApplication.userDao.create(newUser);
										createBadges();
										System.out.println("creo nuovo user locale");
									}
								//no, connect it now
								}
								else{ 
									Log.d("vuoto", "creo owner");
									//create new local user owner
									User userOwner = new User();
									userOwner.setFBid("empty");
									userOwner.setLevel(0);
									userOwner.setXP(0);
									userOwner.setOwner(true);
									//final Preference profile_name=findPreference("profile_name");
									//profile_name.setSummary("New User");
									user.setName("user owner"/*profile_name.getSummary().toString()*/);
									ClimbApplication.userDao.create(userOwner);
									createBadges();

									Editor editor = pref.edit();
									editor.putInt("local_id", userOwner.get_id()); 
									editor.commit();
									Log.d("local id", String.valueOf(userOwner.get_id()));
								}
							}else{//yes, get my account
								newUser = users.get(0);
								System.out.println("gia utente locale con fb no owner");
							}
							//save data locally
							SharedPreferences pref = getApplicationContext().getSharedPreferences("UserSession", 0);
							Editor editor = pref.edit();
							editor.putString("FBid", newUser.getFBid()); 
							editor.putString("username", newUser.getName());
							editor.putInt("local_id", newUser.get_id());
							editor.commit(); 
							Log.d("local id", "salvato " + pref.getInt("local_id", 0));
							Log.d("FBid", "salvo " + pref.getString("FBid", ""));
							
							userExists(user, session);
							
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
		//	}	
		} else if (state.isClosed()) {
			Log.i("Climb The World", "Logged out...");
			ParseUser.getCurrentUser().logOut();
			if(!pref.getString("FBid", "none").equalsIgnoreCase("none")){
				Editor editor = pref.edit();
				Map<String, Object> conditions2 = new HashMap<String, Object>();
				conditions2.put("owner", 1);
				List<User> users2 = ClimbApplication.userDao.queryForFieldValuesArgs(conditions2);
				if(users2.size() > 0){
					Log.d("logout", "carico user owner");
					editor.putInt("local_id", users2.get(0).get_id()); 
					editor.putString("username", "owner");
					editor.commit();

				}else{
					Log.d("vuoto", "creo owner");
					//create new local user owner
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
				}
			
			editor.putString("FBid", "none");
			editor.commit();
			profilePictureView.setProfileId(null);
			lblFacebookUser.setText(getString(R.string.not_logged_in));
			profile_name.setSummary(getString(R.string.no_user_defined));
		}
			
			
		}
		}
		else
			Toast.makeText(getApplicationContext(),getString(R.string.check_connection), Toast.LENGTH_LONG).show();
	}
	
 private void userExists(final GraphUser fbUser, final Session session){
	 Log.d("settings activity", "userExists");
		ParseQuery<ParseUser> sameFBid = ParseUser.getQuery();
		sameFBid.whereEqualTo("FBid", fbUser.getId());
	/*	
		ParseQuery<ParseUser> sameName = ParseQuery.getQuery("User");
		sameName.whereEqualTo("Username", fbUser.getName());
		
		List<ParseQuery<ParseUser>> queries = new ArrayList<ParseQuery<ParseUser>>();
		queries.add(sameFBid);
		queries.add(sameName);
		 
		ParseQuery<ParseUser> mainQuery = ParseQuery.or(queries);*/
		sameFBid.findInBackground(new FindCallback<ParseUser>() {
		  public void done(List<ParseUser> results, ParseException e) {
		    if(results.isEmpty()){//user not saved in Parse
		    	System.out.println("salvo");
		    		saveUserToParse(fbUser, session);
		    }else{//user already saved in Parse
		    	System.out.println("c'è già");
		    	ParseUser user = results.get(0);
		    	ParseUser.logInInBackground(user.getUsername(), "", new LogInCallback() {
		    		  public void done(ParseUser user, ParseException e) {
		    		    if (user != null) {
		    		      // Hooray! The user is logged in.
				    		//loadProgressFromParse();
				    		new MyAsync(user).execute();

		    		    } else {
		    		      // Signup failed. Look at the ParseException to see what happened.
		    		    	Toast.makeText(getApplicationContext(), getString(R.string.connection_problem), Toast.LENGTH_SHORT).show();
		    		    	Log.e("userExists", e.getMessage());
		    		    }
		    		  }
		    		});
		    		//Toast.makeText(getApplicationContext(), "Choose a valid name", Toast.LENGTH_SHORT);
		    		
		    }
		  }
		});
	}
	
    private void saveUserToParse(GraphUser fbUser, Session session) {
    	SharedPreferences pref = getSharedPreferences("UserSession", 0);
    		User me = ClimbApplication.getUserById(pref.getInt("local_id", -1));
    	 	ParseUser user = new ParseUser();
    		user.setUsername(fbUser.getName());
    		user.setPassword("");
    		user.put("FBid", fbUser.getId()); //System.out.println(fbUser.getId());
    		user.put("level", me.getLevel());
    		user.put("XP", me.getXP());
    		JSONArray badges = new JSONArray();
    		List<UserBadge> ubs = ClimbApplication.getUserBadgeByUser(me.get_id());
    		for(UserBadge ub : ubs){
    			JSONObject badge = new JSONObject();
    			try {	
    				badge.put("badge_id", ub.getBadge().get_id());
				badge.put("obj_id", ub.getObj_id());
	    			badge.put("perentage", ub.getPercentage());
	    			badges.put(badge);
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
    			
    		}
    		user.put("badges", badges);
    		
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
    		
    		
    		/*if (!ParseFacebookUtils.isLinked(user)) {
    			  ParseFacebookUtils.link(user, this, new SaveCallback() {
    			    @Override
    			    public void done(ParseException ex) {
    			      if (ParseFacebookUtils.isLinked(user)) {
    			        Log.d("MyApp", "Woohoo, user logged in with Facebook!");
    			      }
    			    }
    			  });
    			}*/
    		
    		
    	/*List<String> permissions = Arrays.asList("public_profile", "user_friends");
        ParseFacebookUtils.logIn(fbUser.getId(), session.getAccessToken(), 
                 session.getExpirationDate(), new LogInCallback() {
             @Override
             public void done(ParseUser user, ParseException err) {                 
                  if (user == null) {
                       // The user wasn't saved. Check the exception.
                       Log.d("Parse", "User was not saved to Parse: " + err.getMessage());
                  } else {
                       // The user has been saved to Parse.
                       Log.d("Parse", "User has successfully been saved to Parse.");
                  }
             }
        });*/
    }
    
    private void createBadges(){
		SharedPreferences pref = getSharedPreferences("UserSession", 0);
		boolean emptybadges = ClimbApplication.areThereUserBadges(pref.getInt("local_id", -1)) == 0 ? false : true;
		if(emptybadges){
			for(Building building: ClimbApplication.buildings){
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
    
    private void saveBadges(JSONArray badges){
    	Log.d("SettingsActivity", "saveBadges");
		SharedPreferences pref = getSharedPreferences("UserSession", 0);
		
    		ClimbApplication.refreshUserBadge();
    		for(int i = 0; i < badges.length(); i++){
    			try {
    			JSONObject badge = badges.getJSONObject(i);	   	
    			int badge_id = badge.getInt("badge_id");
    			int obj_id = badge.getInt("obj_id");
    			int user_id = pref.getInt("local_id", -1);
    			UserBadge userBadge = ClimbApplication.getUserBadgeForUserAndBadge(badge_id, obj_id, user_id); 
    			System.out.println("ub " + userBadge.get_id());
    			if(userBadge == null){
    				UserBadge ub = new UserBadge();
    				ub.setBadge(ClimbApplication.getBadgeById(badge_id));
    				ub.setObj_id(obj_id);
    				ub.setUser(ClimbApplication.getUserById(user_id));
    				ub.setPercentage(badge.getDouble("percentage"));
    				ClimbApplication.userBadgeDao.create(ub);
    			}else{
    				userBadge.setPercentage(badge.getDouble("percentage"));
    				ClimbApplication.userBadgeDao.update(userBadge);
    			}
    			}catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
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
	 * Shows the simplified settings UI if the device configuration if the device configuration dictates that a simplified, single-pane UI should be shown.
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
		// float detectedSamplingRate=PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getFloat("detectedSamplingRate", 0.0f);
		// findPreference("detectedSamplingRate").setSummary(new DecimalFormat("#.##").format(detectedSamplingRate)+"Hz");
	}

	/** {@inheritDoc} */
	@Override
	public boolean onIsMultiPane() {
		return isXLargeTablet(this) && !isSimplePreferences(this);
	}

	/**
	 * Helper method to determine if the device has an extra-large screen. For example, 10" tablets are extra-large.
	 */
	private static boolean isXLargeTablet(Context context) {
		return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
	}

	/**
	 * Determines whether the simplified settings UI should be shown. This is true if this is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device doesn't have newer APIs like {@link PreferenceFragment}, or the device doesn't have an extra-large screen. In these cases, a single-pane "simplified" settings UI should be shown.
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
	 * A preference value change listener that updates the preference's summary to reflect its new value.
	 */
	private static Preference.OnPreferenceChangeListener	sBindPreferenceSummaryToValueListener	= new Preference.OnPreferenceChangeListener() {
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
	 * Binds a preference's summary to its value. More specifically, when the preference's value is changed, its summary (line of text below the preference title) is updated to reflect the value. The summary is also immediately updated upon calling this method. The exact display format is dependent on the type of preference.
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
	 * This fragment shows general preferences only. It is used when the activity is showing a two-pane settings UI.
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
	    if (session != null &&
	           (session.isOpened() || session.isClosed()) ) {
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
