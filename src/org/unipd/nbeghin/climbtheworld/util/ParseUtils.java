package org.unipd.nbeghin.climbtheworld.util;

import org.unipd.nbeghin.climbtheworld.ClimbApplication;
import org.unipd.nbeghin.climbtheworld.R;
import org.unipd.nbeghin.climbtheworld.models.Climbing;
import org.unipd.nbeghin.climbtheworld.models.Collaboration;
import org.unipd.nbeghin.climbtheworld.models.Competition;
import org.unipd.nbeghin.climbtheworld.models.Microgoal;
import org.unipd.nbeghin.climbtheworld.models.TeamDuel;

import android.util.Log;
import android.widget.Toast;

import com.parse.DeleteCallback;
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
}
