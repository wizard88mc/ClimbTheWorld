package org.unipd.nbeghin.climbtheworld.util;

import org.json.JSONException;
import org.json.JSONObject;
import org.unipd.nbeghin.climbtheworld.ClimbApplication;
import org.unipd.nbeghin.climbtheworld.R;
import org.unipd.nbeghin.climbtheworld.models.Climbing;
import org.unipd.nbeghin.climbtheworld.models.Collaboration;
import org.unipd.nbeghin.climbtheworld.models.Competition;
import org.unipd.nbeghin.climbtheworld.models.Microgoal;
import org.unipd.nbeghin.climbtheworld.models.TeamDuel;
import org.unipd.nbeghin.climbtheworld.models.User;

import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.parse.DeleteCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;
/**
 * Class containing methods to be used when saving object in Parse
 * DO NOT USE PARSE METHOD saveEventually/deleteEventually
 * @author Silvia
 *
 */
public class ParseUtils {

	public static void saveClimbing(ParseObject p_climbing, final Climbing l_climbing){
		p_climbing.saveInBackground(new SaveCallback() {
			
			@Override
			public void done(ParseException ex) {
				if(ex == null){
					//no problems
					l_climbing.setSaved(true);
					ClimbApplication.climbingDao.update(l_climbing);
					Log.i(getClass().getName(), "Climbing correctly saved in Parse");
				}else{
					l_climbing.setSaved(false);
					ClimbApplication.climbingDao.update(l_climbing);
					Toast.makeText(ClimbApplication.getContext(), ClimbApplication.getContext().getString(R.string.connection_problem2), Toast.LENGTH_SHORT).show();
					Log.e(getClass().getName(), ex.getMessage());
				}
				
			}
		});
	}
	
	public static void saveMicrogoal(ParseObject p_microgoal, final Microgoal l_microgoal){
		p_microgoal.saveInBackground(new SaveCallback() {
			
			@Override
			public void done(ParseException ex) {
				if(ex == null){
					//no problems
					Log.i(getClass().getName(), "Microgoal correctly saved in Parse");
					l_microgoal.setSaved(true);
					ClimbApplication.microgoalDao.update(l_microgoal);
				}else{
					l_microgoal.setSaved(false);
					ClimbApplication.microgoalDao.update(l_microgoal);
					Toast.makeText(ClimbApplication.getContext(), ClimbApplication.getContext().getString(R.string.connection_problem2), Toast.LENGTH_SHORT).show();
					Log.e(getClass().getName(), ex.getMessage());
				}
				
			}
		});
	}
	
	public static void saveUserInParse(ParseUser user){
		user.saveInBackground(new SaveCallback() {
			
			@Override
			public void done(ParseException e) {
				if(e == null){
					Log.i(getClass().getName(), "User correctly saved in Parse");
				}else{
					Toast.makeText(ClimbApplication.getContext(), ClimbApplication.getContext().getString(R.string.connection_problem2), Toast.LENGTH_SHORT).show();
					Log.e(getClass().getName(), e.getMessage());
				}
			}
		});
	}
	
	public static void saveCollaboration(ParseObject p_collaboration, final Collaboration l_collaboration){
		p_collaboration.saveInBackground(new SaveCallback() {
			
			@Override
			public void done(ParseException ex) {
				if(ex == null){
					//no problems
					Log.i(getClass().getName(), "Collaboration correctly saved in Parse");
					if(l_collaboration.isLeaved())
						ClimbApplication.collaborationDao.delete(l_collaboration);
					else{
						l_collaboration.setSaved(true);
						ClimbApplication.collaborationDao.update(l_collaboration);
					}
				}else{
					l_collaboration.setSaved(false);
					ClimbApplication.collaborationDao.update(l_collaboration);
					Toast.makeText(ClimbApplication.getContext(), ClimbApplication.getContext().getString(R.string.connection_problem2), Toast.LENGTH_SHORT).show();
					Log.e(getClass().getName(), ex.getMessage());
				}
				
			}
		});
	}
	
	public static void saveCompetition(ParseObject p_competition, final Competition l_competition){
		p_competition.saveInBackground(new SaveCallback() {
			
			@Override
			public void done(ParseException ex) {
				if(ex == null){
					//no problems
					Log.i(getClass().getName(), "Competition correctly saved in Parse");
					if(l_competition.isLeaved())
						ClimbApplication.competitionDao.delete(l_competition);
					else{
						l_competition.setSaved(true);
						ClimbApplication.competitionDao.update(l_competition);
					}
				}else{
					l_competition.setSaved(false);
					ClimbApplication.competitionDao.update(l_competition);
					Toast.makeText(ClimbApplication.getContext(), ClimbApplication.getContext().getString(R.string.connection_problem2), Toast.LENGTH_SHORT).show();
					Log.e(getClass().getName(), ex.getMessage());
				}
				
			}
		});
	}
	
	public static void saveTeamDuel(ParseObject p_teamduel, final TeamDuel l_teamduel){
		p_teamduel.saveInBackground(new SaveCallback() {
			
			@Override
			public void done(ParseException ex) {
				if(ex == null){
					//no problems
					Log.i(getClass().getName(), "Competition correctly saved in Parse");
					l_teamduel.setSaved(true);
					ClimbApplication.teamDuelDao.update(l_teamduel);
				}else{
					l_teamduel.setSaved(false);
					ClimbApplication.teamDuelDao.update(l_teamduel);
					Toast.makeText(ClimbApplication.getContext(), ClimbApplication.getContext().getString(R.string.connection_problem2), Toast.LENGTH_SHORT).show();
					Log.e(getClass().getName(), ex.getMessage());
				}
				
			}
		});
	}
	
	public static void deleteCollaboration(ParseObject p_collaboration, final Collaboration l_collaboration){
		p_collaboration.deleteInBackground(new DeleteCallback() {
			
			@Override
			public void done(ParseException ex) {
				if(ex == null){
					Log.i(getClass().getName(), "Competition correctly saved in Parse");
					ClimbApplication.collaborationDao.delete(l_collaboration);
				}else{
					l_collaboration.setLeaved(true);
					l_collaboration.setSaved(false);
					ClimbApplication.collaborationDao.update(l_collaboration);
					Toast.makeText(ClimbApplication.getContext(), ClimbApplication.getContext().getString(R.string.connection_problem2), Toast.LENGTH_SHORT).show();
					Log.e(getClass().getName(), ex.getMessage());
				}
				
			}
		});
	}
	
	public static void deleteCompetition(ParseObject p_competition, final Competition l_competition){
		p_competition.deleteInBackground(new DeleteCallback() {
			
			@Override
			public void done(ParseException ex) {
				if(ex == null){
					Log.i(getClass().getName(), "Competition correctly saved in Parse");
					ClimbApplication.competitionDao.delete(l_competition);
				}else{
					l_competition.setLeaved(true);
					l_competition.setSaved(false);
					ClimbApplication.competitionDao.update(l_competition);
					Toast.makeText(ClimbApplication.getContext(), ClimbApplication.getContext().getString(R.string.connection_problem2), Toast.LENGTH_SHORT).show();
					Log.e(getClass().getName(), ex.getMessage());
				}
				
			}
		});
	}
	
	public static void updateCurrentUserData(){
		ParseUser.getCurrentUser().fetchInBackground(new GetCallback<ParseUser>() {
			  public void done(ParseUser object, ParseException e) {
				    if (e == null) {
				    	SharedPreferences pref = ClimbApplication.getContext().getSharedPreferences("UserSession", 0);
				    	User me = ClimbApplication.getUserByFBId(pref.getString("FBid", "none"));
						ParseUser user = ParseUser.getCurrentUser();
						me.setLevel(user.getInt("level"));
						me.setXP(user.getInt("XP"));
						me.setHeight(user.getDouble("height"));
						JSONObject stats = user.getJSONObject("mean_daily_steps");
						if (stats != null && stats.length() > 0) {
						try {
							me.setBegin_date(String.valueOf(stats.getLong("begin_date")));
							me.setMean(stats.getLong("mean"));
							me.setN_measured_days(stats.getInt("n_days"));
							me.setCurrent_steps_value(stats.getInt("current_value"));
						} catch (JSONException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						}
						ClimbApplication.userDao.update(me);
				    } else {
				    		Toast.makeText(ClimbApplication.getContext(), ClimbApplication.getContext().getString(R.string.connection_problem2), Toast.LENGTH_SHORT).show();
						Log.e(getClass().getName(), e.getMessage());
				    }
				  }
				});
	}
}
