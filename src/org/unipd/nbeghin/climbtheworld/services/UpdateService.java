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
		System.out.println("Climbings: " + climbings.size());
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
							Log.e("NetworkBroadcasterReceiver", e.getMessage());
						}
				}
			});
		}
	}
	
	private void saveCollaborations(final SharedPreferences pref, final Context context){
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
							ParseObject collabParse = new ParseObject("Collaboration");
							JSONObject stairs = collabParse.getJSONObject("stairs");

							if(collaboration.isLeaved()){
								JSONObject collaborators = collabParse.getJSONObject("collaborators");
								collaborators.remove(pref.getString("FBid", ""));
								stairs.remove(pref.getString("FBid", ""));
								collabParse.put("collaborators", collaborators);
								collabParse.put("stairs", stairs);
								collabParse.saveEventually();
								collaboration.setSaved(true);
								collaborationDao.update(collaboration);
							}else{
								try {
									stairs.put(pref.getString("FBid", ""), collaboration.getMy_stairs());
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
						Log.e("NetworkBroadcasterReceiver", e.getMessage());
					}
				}
				
			});
		}
	}
    
    @Override
    protected void onHandleIntent(Intent intent) {
    	SharedPreferences pref = getSharedPreferences("UserSession", 0);
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
		if(isOnline(this)){
			saveClimbings(this);
			saveCollaborations(pref, this);
		}
      
    }
    
    
}