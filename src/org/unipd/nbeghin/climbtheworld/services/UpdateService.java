package org.unipd.nbeghin.climbtheworld.services;

import java.text.DateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.unipd.nbeghin.climbtheworld.db.DbHelper;
import org.unipd.nbeghin.climbtheworld.db.PreExistingDbLoader;
import org.unipd.nbeghin.climbtheworld.models.Climbing;
import org.unipd.nbeghin.climbtheworld.models.Collaboration;
import org.unipd.nbeghin.climbtheworld.models.Competition;
import org.unipd.nbeghin.climbtheworld.models.TeamDuel;

import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

public class UpdateService extends IntentService {
    public static final String PARAM_IN_MSG = "imsg";
    public static final String PARAM_OUT_MSG = "omsg";
 
    public UpdateService() {
        super("UpdateService");
    }
 
	private DbHelper dbHelper;
	public RuntimeExceptionDao<Collaboration, Integer> collaborationDao;
	public RuntimeExceptionDao<Competition, Integer> competitionDao;
	public RuntimeExceptionDao<Climbing, Integer> climbingDao; 
	public RuntimeExceptionDao<TeamDuel, Integer> teamDuelDao;
	
	public boolean isOnline(Context context) {
	    ConnectivityManager cm =
	        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
	        return true;
	    }
	    return false;
	}
	

	
	private void saveClimbings(final Context context){
		//salvo tutti i climbing online
		Map<String, Object> conditions = new HashMap<String, Object>();
		conditions.put("saved", 0); 
		List<Climbing> climbings = climbingDao.queryForFieldValuesArgs(conditions);
		Log.d("updateService", "Climbings: " + climbings.size());
		for(final Climbing climbing: climbings)
		{
			ParseQuery<ParseObject> query = ParseQuery.getQuery("Climbing");
			query.whereEqualTo("building", climbing.getBuilding().get_id());
			query.whereEqualTo("users_id", climbing.getUser().getFBid());
			query.findInBackground(new FindCallback<ParseObject>() {
				
				@Override
				public void done(List<ParseObject> climbs, ParseException e) {
						if(e == null){
							if(climbs.size() == 0){
								climbingDao.delete(climbing);
							}else{
								ParseObject climbOnline = climbs.get(0);
								climbOnline.put("completed_steps", climbing.getCompleted_steps());
								climbOnline.put("remaining_steps", climbing.getRemaining_steps());
								climbOnline.put("modified", climbing.getModified());
								climbOnline.put("compleatedAt", climbing.getCompleted());
								climbOnline.put("percentage", climbing.getPercentage());
								climbOnline.put("game_mode", climbing.getGame_mode());
								climbOnline.saveEventually();
								climbing.setSaved(true);
								climbingDao.update(climbing);
							}
						}else{
							Toast.makeText(context, "Connection Problems", Toast.LENGTH_SHORT).show();
							Log.e("updateService - climbings", e.getMessage());
						}
				}
			});
		}
	}
	
	private void saveCollaborations(final Context context){
		//salvo tutti i climbing online
		Map<String, Object> conditions = new HashMap<String, Object>();
		conditions.put("saved", 0); 
		List<Collaboration> collaborations = collaborationDao.queryForFieldValuesArgs(conditions);
		Log.d("updateService", "Collaborations: " + collaborations.size());
		for(final Collaboration collaboration : collaborations){
			ParseQuery<ParseObject> query = ParseQuery.getQuery("Collaboration");
			query.whereEqualTo("objectId", collaboration.getId());
			query.findInBackground(new FindCallback<ParseObject>() {
				
				@Override
				public void done(List<ParseObject> collabs, ParseException e) {
					if(e == null){
						if(collabs.size() == 0){
							collaborationDao.delete(collaboration);
						}else{
							ParseObject collabParse = collabs.get(0);
							JSONObject stairs = collabParse.getJSONObject("stairs");

							if(collaboration.isLeaved()){
								JSONObject collaborators = collabParse.getJSONObject("collaborators");
								collaborators.remove(collaboration.getUser().getFBid());
								stairs.remove(collaboration.getUser().getFBid());
								collabParse.put("collaborators", collaborators);
								collabParse.put("stairs", stairs);
								collabParse.saveEventually();
								collaboration.setSaved(true);
								collaborationDao.update(collaboration);
							}else{
								try {
									stairs.put(collaboration.getUser().getFBid(), collaboration.getMy_stairs());
									collabParse.put("stairs", stairs);
									collabParse.saveEventually();
									collaboration.setSaved(true);
									collaborationDao.update(collaboration);
								} catch (JSONException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
							}
						}
					}else{
						Toast.makeText(context, "Connection Problems", Toast.LENGTH_SHORT).show();
						Log.e("updateService - collaborations", e.getMessage());
					}
				}
				
			});
		}
	}
    
	private void saveCompetitions(final Context context){
		//salvo tutti i climbing online
		Map<String, Object> conditions = new HashMap<String, Object>();
		conditions.put("saved", 0); 
		List<Competition> competitions = competitionDao.queryForFieldValuesArgs(conditions);
		Log.d("updateService", "Collaborations: " + competitions.size());
		for(final Competition competition : competitions){
			ParseQuery<ParseObject> query = ParseQuery.getQuery("Collaboration");
			query.whereEqualTo("objectId", competition.getId_online());
			query.findInBackground(new FindCallback<ParseObject>() {
				
				@Override
				public void done(List<ParseObject> collabs, ParseException e) {
					if(e == null){
						if(collabs.size() == 0){
							competitionDao.delete(competition);
						}else{
							ParseObject collabParse = collabs.get(0);
							JSONObject stairs = collabParse.getJSONObject("stairs");

							if(competition.isLeaved()){
								JSONObject collaborators = collabParse.getJSONObject("competitors");
								collaborators.remove(competition.getUser().getFBid());
								stairs.remove(competition.getUser().getFBid());
								collabParse.put("competitors", collaborators);
								collabParse.put("stairs", stairs);
								collabParse.saveEventually();
								competition.setSaved(true);
								competitionDao.update(competition);
							}else{
								try {
									stairs.put(competition.getUser().getFBid(), competition.getMy_stairs());
									collabParse.put("stairs", stairs);
									collabParse.saveEventually();
									competition.setSaved(true);
									competitionDao.update(competition);
								} catch (JSONException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
							}
						}
					}else{
						Toast.makeText(context, "Connection Problems", Toast.LENGTH_SHORT).show();
						Log.e("UpdateService - competitors", e.getMessage());
					}
				}
				
			});
		}
	}
	
	private void saveTeamDuels(final Context context){
		Map<String, Object> conditions = new HashMap<String, Object>();
		conditions.put("saved", 0); 
		List<TeamDuel> duels = teamDuelDao.queryForFieldValuesArgs(conditions);
		Log.d("updateService", "Duels: " + duels.size());
		for(final TeamDuel duel : duels){
			ParseQuery<ParseObject> query = ParseQuery.getQuery("TeamDuel");
			query.whereEqualTo("objectId", duel.getId_online());
			query.findInBackground(new FindCallback<ParseObject>() {

				@Override
				public void done(List<ParseObject> duelsParse, ParseException e) {
						if(e == null){
								if(duelsParse.size() == 0){
									teamDuelDao.delete(duel);
								}else{
									ParseObject parseDuel = duelsParse.get(0);
									if(duel.isCreator()){
										JSONObject creator = new JSONObject();
										try {
											creator.put(duel.getUser().getFBid(), duel.getUser().getName());
											JSONObject stairs = parseDuel.getJSONObject("creator_stairs");
											stairs.put(duel.getUser().getFBid(), duel.getMy_steps());
											parseDuel.put("creator_stairs", stairs);
										} catch (JSONException e1) {
											// TODO Auto-generated catch block
											e1.printStackTrace();
										}
										parseDuel.put("creator", creator);
									}else{
										JSONObject challenger = new JSONObject();
										try {
											challenger.put(duel.getUser().getFBid(), duel.getUser().getName());
											JSONObject stairs = parseDuel.getJSONObject("challenger_stairs");
											parseDuel.put(duel.getUser().getFBid(), duel.getMy_steps());
											parseDuel.put("challeger_stairs", stairs);
										} catch (JSONException e1) {
											// TODO Auto-generated catch block
											e1.printStackTrace();
										}
									}
									parseDuel.saveEventually();
									duel.setSaved(true);
									teamDuelDao.update(duel);
									
								}
						}else{
							Toast.makeText(context, "Connection Problems", Toast.LENGTH_SHORT).show();
							Log.e("UpdateService - duels", e.getMessage());
						}
				}
				
			});
		}
	}
	

	
	
    @Override
    protected void onHandleIntent(Intent intent) {
System.out.println("service on");
PreExistingDbLoader preExistingDbLoader = new PreExistingDbLoader(
				this); // extract db from zip
		Log.d("Load normal db", "fine service");
		SQLiteDatabase db = preExistingDbLoader.getReadableDatabase();
		db.close(); // close connection to extracted db
		dbHelper = new DbHelper(getApplicationContext());
		climbingDao = dbHelper.getClimbingDao(); // create climbing DAO
		collaborationDao = dbHelper.getCollaborationDao();
		competitionDao = dbHelper.getCompetitionDao();
		teamDuelDao = dbHelper.getTeamDuelDao();
		if(isOnline(this)){
			saveClimbings(this);
			saveCollaborations(this);
			saveCompetitions(this);
			saveTeamDuels(this);
		}
      
    }
    
    
}