package org.unipd.nbeghin.climbtheworld.services;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SimpleTimeZone;

import org.json.JSONException;
import org.json.JSONObject;
import org.unipd.nbeghin.climbtheworld.MainActivity;
import org.unipd.nbeghin.climbtheworld.db.DbHelper;
import org.unipd.nbeghin.climbtheworld.db.PreExistingDbLoader;
import org.unipd.nbeghin.climbtheworld.models.Climbing;
import org.unipd.nbeghin.climbtheworld.models.Collaboration;
import org.unipd.nbeghin.climbtheworld.models.Competition;
import org.unipd.nbeghin.climbtheworld.models.TeamDuel;

import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

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
	
	private Collaboration getCollaborationByBuildingAndUser(int building_id, int user_id){
		//per ogni edificio, una sola collaborazione
			QueryBuilder<Collaboration, Integer> query = collaborationDao.queryBuilder();
			Where<Collaboration, Integer> where = query.where();
			
			try {
				where.eq("building_id", building_id);
				where.and();
				where.eq("user_id", user_id);
				PreparedQuery<Collaboration> preparedQuery = query.prepare();
				List<Collaboration> collabs = collaborationDao.query(preparedQuery);
				if(collabs.size() == 0)
					return null;
				else return collabs.get(0);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
			
		
	}
	
	private void saveClimbings(final Context context, int mode) throws SQLException{
		//salvo tutti i climbing online
		QueryBuilder<Climbing, Integer> query1 = climbingDao.queryBuilder();
		Where<Climbing, Integer> where = query1.where();
		where.eq("saved", 0);
		where.and();
		where.eq("game_mode", mode);
		PreparedQuery<Climbing> preparedQuery = query1.prepare();
		List<Climbing> climbings = climbingDao.query(preparedQuery);
		Log.d("updateService", "Climbings: " + climbings.size());
		for(final Climbing climbing: climbings)
		{
			ParseQuery<ParseObject> query = ParseQuery.getQuery("Climbing");
			query.whereEqualTo("building", climbing.getBuilding().get_id());
			query.whereEqualTo("users_id", climbing.getUser().getFBid());
				if(climbing.getGame_mode() == 3)
					query.whereEqualTo("game_mode", 3);
			
			query.findInBackground(new FindCallback<ParseObject>() {
				
				@Override
				public void done(List<ParseObject> climbs, ParseException e) {
						if(e == null){ 
							if(climbs.size() == 0){
									final ParseObject climbOnline = new ParseObject("Climbing");
									climbOnline.put("building", climbing.getBuilding().get_id());
									climbOnline.put("users_id", climbing.getUser().getFBid());
									climbOnline.put("completed_steps", climbing.getCompleted_steps());
									climbOnline.put("remaining_steps", climbing.getRemaining_steps());
									DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
									df.setTimeZone(new SimpleTimeZone(0, "GMT"));
									try {
										climbOnline.put("modified", df.parse(df.format(climbing.getModified())));
										climbOnline.put("created", df.parse(df.format(climbing.getCreated())));
										climbOnline.put("completedAt", df.parse(df.format(climbing.getCompleted())));
									} catch (java.text.ParseException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
									climbOnline.put("percentage", String.valueOf(climbing.getPercentage()));
									climbOnline.put("game_mode", climbing.getGame_mode());
									if(climbing.getGame_mode() != 0 && (climbing.getId_mode() == null || climbing.getId_mode().equals(""))){
										switch(climbing.getGame_mode()){
										case 1:
											Collaboration coll = getCollaborationByBuildingAndUser(climbing.getBuilding().get_id(), climbing.getUser().get_id());
											climbing.setId_mode(coll.getId());
											climbingDao.update(climbing);
											break;
										}
									}
									climbOnline.put("id_mode", climbing.getId_mode());
									climbOnline.saveInBackground(new SaveCallback() {
										
										@Override
										public void done(ParseException e) {
											if(e == null){
												climbing.setId_online(climbOnline.getObjectId());
												climbing.setSaved(true);
												climbingDao.update(climbing);
											}else{
												climbing.setSaved(false);
												climbingDao.update(climbing);
												Toast.makeText(context, "Connection Problems: your data will be saved during next reconnection", Toast.LENGTH_SHORT).show();
											}
										}
									});
								
									
								
							}else{System.out.println(climbs.size());
								if(!climbing.isDeleted()){
								DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
								df.setTimeZone(new SimpleTimeZone(0, "GMT"));
								ParseObject climbOnline = climbs.get(0); System.out.println(climbOnline.getObjectId());
								climbOnline.put("completed_steps", climbing.getCompleted_steps());
								climbOnline.put("remaining_steps", climbing.getRemaining_steps());
								try {
									climbOnline.put("modified", df.parse(df.format(climbing.getModified())));
									climbOnline.put("created", df.parse(df.format(climbing.getCreated())));
									climbOnline.put("completedAt", df.parse(df.format(climbing.getCompleted())));
								} catch (java.text.ParseException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
								climbOnline.put("percentage", String.valueOf(climbing.getPercentage()));
								climbOnline.put("game_mode", climbing.getGame_mode());
								if(climbing.getGame_mode() != 0 && (climbing.getId_mode() == null || climbing.getId_mode().equals(""))){
									switch(climbing.getGame_mode()){
									case 1:
										Collaboration coll = getCollaborationByBuildingAndUser(climbing.getBuilding().get_id(), climbing.getUser().get_id());
										if(coll != null) climbing.setId_mode(coll.getId());
										else{
											climbing.setGame_mode(0);
											climbing.setId_mode("");
										}
										climbingDao.update(climbing);
										break;
									}
								}
								climbOnline.put("id_mode", climbing.getId_mode());
								climbOnline.saveEventually();
								climbing.setSaved(true);
								climbingDao.update(climbing);
								}else{
									climbs.get(0).deleteEventually();
									climbingDao.delete(climbing);
								}
							}
						}else{
							Toast.makeText(context, "Connection Problems", Toast.LENGTH_SHORT).show();
							Log.e("updateService - climbings", e.getMessage());
						}
				}
			});
		}
	}
	
//	//devo settare il suo id online
//	private String saveSingleCollaboration(final Collaboration coll){
//		final ParseObject collabParse = new ParseObject("Collaboration");
//		
//		JSONObject stairs = new JSONObject();
//		JSONObject collaborators = new JSONObject();
//
//		try {
//			collaborators.put(coll.getUser().getFBid(), coll.getUser().getName());
//			stairs.put(coll.getUser().getFBid(), 0);
//		} catch (JSONException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		collabParse.put("building", coll.getBuilding().get_id());
//		collabParse.put("stairs", stairs);
//		collabParse.put("collaborators", collaborators);
//		collabParse.put("completed", false);
//		collabParse.saveInBackground(new SaveCallback() {
//			
//			@Override
//			public void done(ParseException e) {
//				if(e == null){
//					coll.setId(collabParse.getObjectId());
//					coll.setSaved(true);
//					collaborationDao.update(coll);
//				}else{
//					coll.setSaved(false);
//					collaborationDao.update(coll);
//				}
//				
//			}
//		});
//		
//	}
	
	private void saveCollaborations(final Context context){
		//salvo tutti i climbing online
		Map<String, Object> conditions = new HashMap<String, Object>();
		conditions.put("saved", 0); 
		List<Collaboration> collaborations = collaborationDao.queryForFieldValuesArgs(conditions);
		Log.d("updateService", "Collaborations: " + collaborations.size());
		for(final Collaboration collaboration : collaborations){
			ParseQuery<ParseObject> query = ParseQuery.getQuery("Collaboration");
			if(collaboration.getId() == null || collaboration.getId().equals("")){
				query.whereEqualTo("collaborators." + collaboration.getUser().getFBid(), collaboration.getUser().getName());
				query.whereEqualTo("building", collaboration.getBuilding().get_id());
				query.whereEqualTo("completed", false);
			}else
				query.whereEqualTo("objectId", collaboration.getId());
			query.findInBackground(new FindCallback<ParseObject>() {
				
				@Override
				public void done(List<ParseObject> collabs, ParseException e) {
					if(e == null){
						if(collabs.size() == 0){
							//collaborationDao.delete(collaboration);
							JSONObject stairs = new JSONObject();
							JSONObject collaborators = new JSONObject();
							final ParseObject collabParse = new ParseObject("Collaboration");

							try {
								collaborators.put(collaboration.getUser().getFBid(), collaboration.getUser().getName());
								stairs.put(collaboration.getUser().getFBid(), 0);
							} catch (JSONException ex) {
								// TODO Auto-generated catch block
								ex.printStackTrace();
							}

							collabParse.put("building", collaboration.getBuilding().get_id());
							collabParse.put("stairs", stairs);
							collabParse.put("collaborators", collaborators);
							collabParse.put("completed", false);
							collabParse.saveInBackground(new SaveCallback() {
								
								@Override
								public void done(ParseException e) {
									if(e == null){
										collaboration.setId(collabParse.getObjectId());
										collaboration.setSaved(true);
										collaborationDao.update(collaboration);
									}else{
										collaboration.setSaved(false);
										collaborationDao.update(collaboration);
										Log.e("load collab", e.getMessage());
									}
									
								}
							});
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
									collaboration.setId(collabParse.getObjectId());
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
		try {
			saveClimbings(context, 1);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		try {
			saveClimbings(context, 2);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		try {
			saveClimbings(context, 3);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			saveCollaborations(this);
			saveCompetitions(this);
			saveTeamDuels(this);
			try {
				saveClimbings(this, 0);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
      
    }
    
    
}