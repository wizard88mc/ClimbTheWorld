package org.unipd.nbeghin.climbtheworld.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.unipd.nbeghin.climbtheworld.MainActivity;
import org.unipd.nbeghin.climbtheworld.models.Climbing;
import org.unipd.nbeghin.climbtheworld.models.Collaboration;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class NetworkBroadcasterReceiver extends BroadcastReceiver{

	
	
	public boolean isOnline(Context context) {
	    ConnectivityManager cm =
	        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
	        return true;
	    }
	    return false;
	}
	
	private void saveClimbings(){
		//salvo tutti i climbing online
		Map<String, Object> conditions = new HashMap<String, Object>();
		conditions.put("saved", false); 
		List<Climbing> climbings = MainActivity.climbingDao.queryForFieldValuesArgs(conditions);
		
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
								MainActivity.climbingDao.delete(climbing);
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
								MainActivity.climbingDao.update(climbing);
							}
						}else{
							Toast.makeText(MainActivity.getContext(), "Connection Problems", Toast.LENGTH_SHORT).show();
							Log.e("NetworkBroadcasterReceiver", e.getMessage());
						}
				}
			});
		}
	}
	
	private void saveCollaborations(final SharedPreferences pref){
		//salvo tutti i climbing online
		Map<String, Object> conditions = new HashMap<String, Object>();
		conditions.put("saved", false); 
		List<Collaboration> collaborations = MainActivity.collaborationDao.queryForFieldValuesArgs(conditions);
		
		for(final Collaboration collaboration : collaborations){
			ParseQuery<ParseObject> query = ParseQuery.getQuery("Collaboration");
			query.whereEqualTo("objectId", collaboration.getId());
			query.findInBackground(new FindCallback<ParseObject>() {
				
				@Override
				public void done(List<ParseObject> collabs, ParseException e) {
					if(e == null){
						if(collabs.size() == 0){
							MainActivity.collaborationDao.delete(collaboration);
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
								MainActivity.collaborationDao.update(collaboration);
							}else{
								try {
									stairs.put(pref.getString("FBid", ""), collaboration.getMy_stairs());
									collabParse.put("stairs", stairs);
									collabParse.saveEventually();
									collaboration.setSaved(true);
									MainActivity.collaborationDao.update(collaboration);
								} catch (JSONException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
							}
						}
					}else{
						Toast.makeText(MainActivity.getContext(), "Connection Problems", Toast.LENGTH_SHORT).show();
						Log.e("NetworkBroadcasterReceiver", e.getMessage());
					}
				}
				
			});
		}
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences pref = context.getSharedPreferences("UserSession", 0);
		if(isOnline(context)){
			saveClimbings();
			saveCollaborations(pref);
		}
	}

}
