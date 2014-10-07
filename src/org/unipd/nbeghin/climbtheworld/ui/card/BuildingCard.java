package org.unipd.nbeghin.climbtheworld.ui.card;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.SimpleTimeZone;

import org.json.JSONException;
import org.json.JSONObject;
import org.unipd.nbeghin.climbtheworld.MainActivity;
import org.unipd.nbeghin.climbtheworld.R;
import org.unipd.nbeghin.climbtheworld.TeamPreparationActivity;
import org.unipd.nbeghin.climbtheworld.models.Building;
import org.unipd.nbeghin.climbtheworld.models.Climbing;
import org.unipd.nbeghin.climbtheworld.models.Collaboration;
import org.unipd.nbeghin.climbtheworld.models.Competition;
import org.unipd.nbeghin.climbtheworld.models.GameModeType;
import org.unipd.nbeghin.climbtheworld.models.Group;
import org.unipd.nbeghin.climbtheworld.models.TeamDuel;
import org.unipd.nbeghin.climbtheworld.models.User;
import org.unipd.nbeghin.climbtheworld.util.FacebookUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
import com.fima.cardsui.objects.Card;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

/**
 * CardsUI card for a single building
 * 
 */
public class BuildingCard extends Card {
	final Building building;
	Climbing climbing;
	Climbing soloClimbing;
	final SharedPreferences pref = MainActivity.getContext().getSharedPreferences("UserSession", 0);
	GameModeType mode;
	Activity activity;
	ParseObject collabParse = new ParseObject("Collaboration");
	ParseObject competParse = new ParseObject("Competition");
	ParseObject teamDuelParse = new ParseObject("TeamDuel");

	final Collaboration collab = new Collaboration();
	final Competition compet = new Competition();
	final TeamDuel duel = new TeamDuel();
	
	TextView gameMode;
	TextView climbingStatus;
	Button socialClimbButton;
	Button socialChallengeButton;
	Button teamVsTeamButton;

	public BuildingCard(Building building, Activity activity) {
		super(building.getName());
		this.building = building;
		this.activity = activity;
	}

	/**
	 * Set modality text
	 * @return the string to be shown
	 */
	private String setModeText() {
		if (climbing != null) { // a climb has began
			switch (GameModeType.values()[climbing.getGame_mode()]) {
			case SOCIAL_CHALLENGE:
				mode = GameModeType.SOCIAL_CHALLENGE;
				return "Social Challenge";
			case SOCIAL_CLIMB:
				mode = GameModeType.SOCIAL_CLIMB;
				return "Social Climb";
			case SOLO_CLIMB:
				mode = GameModeType.SOLO_CLIMB;
				return "Solo Climb";
			case TEAM_VS_TEAM:
				mode = GameModeType.TEAM_VS_TEAM;
				return "Team vs Team";
			default:
				return "Solo Climb";

			}
		} else {
			mode = GameModeType.SOLO_CLIMB;
			return "Solo Climb";
		}
	}

	@Override
	public View getCardContent(Context context) {
		View view = LayoutInflater.from(context).inflate(R.layout.card_building_ex, null);
		gameMode = ((TextView) view.findViewById(R.id.textModalita));
		socialClimbButton = ((Button) view.findViewById(R.id.socialClimbButton));
		socialChallengeButton = ((Button) view.findViewById(R.id.socialChallengeButton));
		teamVsTeamButton = ((Button) view.findViewById(R.id.teamVsTeamButton));
		((TextView) view.findViewById(R.id.title)).setText(building.getName());
		int imageId = MainActivity.getBuildingImageResource(building);
		if (imageId > 0)
			((ImageView) view.findViewById(R.id.photo)).setImageResource(imageId);
		((TextView) view.findViewById(R.id.buildingStat)).setText(building.getSteps() + " steps (" + building.getHeight() + "m)");
		((TextView) view.findViewById(R.id.location)).setText(building.getLocation());
		((TextView) view.findViewById(R.id.description)).setText(building.getDescription());
		climbingStatus = (TextView) view.findViewById(R.id.climbingStatus);
		// final SharedPreferences pref =
		// MainActivity.getContext().getSharedPreferences("UserSession", 0);
		List<Climbing> climbs = MainActivity.getClimbingListForBuildingAndUser(building.get_id(), pref.getInt("local_id", -1));
		if(climbs.size() == 0)
			climbing = null;
		else if( climbs.size() == 1)
			climbing = climbs.get(0);
		else if(climbs.size() == 2){ //if there are both a solo climbed 'paused' Climbing and a social-mode Climbing, show the Social-mode Climbing
			if(climbs.get(0).getGame_mode() ==2){
				climbing = climbs.get(0);
				soloClimbing = climbs.get(1);}
			else{
				climbing = climbs.get(1);
				soloClimbing = climbs.get(0);
			}
		}
		//climbing = MainActivity.getClimbingForBuildingAndUser(building.get_id(), pref.getInt("local_id", -1));
		gameMode.setText("Modalitˆ: " + setModeText());
		if(climbing != null){
			switch (climbing.getGame_mode()) {
			case 1:
				setSocialClimb();
				break;
			case 2:
				setSocialChallenge();
				break;
			case 3:
				setTeamChallenge();
				break;
			default:
				break;
			}
		}
			
		updateStatus();	
		if (climbing != null && climbing.getGame_mode() == 0) {
			if (climbing.getPercentage() >= 1.00) {
				socialClimbButton.setEnabled(false);
				socialChallengeButton.setEnabled(true);
				teamVsTeamButton.setEnabled(true);
			} else {
				socialClimbButton.setEnabled(true);
				socialChallengeButton.setEnabled(true);
				teamVsTeamButton.setEnabled(true);
			}
		} else if(climbing == null){
			climbingStatus.setText("Not climbed yet");
			socialClimbButton.setEnabled(true);
			socialChallengeButton.setEnabled(true);
			teamVsTeamButton.setEnabled(true);
		}

		

		socialClimbButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(FacebookUtils.isOnline(activity)){
				
				switch (mode) {
				case SOLO_CLIMB: //from Solo Climb to Social Climb
					if (climbing == null) {
						// create new collaboration if none exists for current building
						climbing = new Climbing();
						climbing.setBuilding(building);
						climbing.setCompleted(0);
						climbing.setCompleted_steps(0);
						climbing.setRemaining_steps(building.getSteps());
						climbing.setCreated(new Date().getTime());
						climbing.setModified(new Date().getTime());
						climbing.setGame_mode(1);
						climbing.setPercentage(0);
						climbing.setUser(MainActivity.getUserById(pref.getInt("local_id", -1)));
						MainActivity.climbingDao.create(climbing);
						saveClimbingInParse();
					} else {
						// update existing Climbing object
						climbing.setGame_mode(1);
						if(climbing.getPercentage() >= 1.00){ // if I have already climbed this building, keep in memory that I have completed it once then reuse it for current collaboration
							climbing.setPercentage(0);
							climbing.setCompleted_steps(0);
							climbing.setRemaining_steps(building.getSteps());
						}
						MainActivity.climbingDao.update(climbing);
						updateClimbingInParse(climbing, false);
					}

					
					
					break;

				case SOCIAL_CLIMB: //back to solo climb
					Log.d("onClick", "solo");
					climbing.setGame_mode(0);
					climbing.setId_mode("");
					MainActivity.climbingDao.update(climbing);
					updateClimbingInParse(climbing, true);
					leaveCollaboration();
					break;
				}

			
			}else{
				Toast.makeText(activity.getApplicationContext(), "Connection needed", Toast.LENGTH_SHORT).show();
			}
			}
		});

		
		socialChallengeButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if(FacebookUtils.isOnline(activity)){
					
					switch (mode) {
					case SOLO_CLIMB: 
						if(climbing != null){
							//if there is an existing Climbing object with game mode as 0
							//then 'pause' it and create a new Climbing object with game mode as 2
							//when the social challenge has finished, delete the last Climbing object and 'resume' the first one in solo climb
							//climbing.setId_mode("paused");
							//climbing.setSaved(false);
							//MainActivity.climbingDao.update(climbing);
							//updateClimbingInParse(climbing, false);
							soloClimbing = climbing;
						}
						//otherwise create a new Climbing object with game mode as 2
						climbing = new Climbing();
						climbing.setBuilding(building);
						climbing.setCompleted(0);
						climbing.setCompleted_steps(0);
						climbing.setRemaining_steps(building.getSteps());
						climbing.setCreated(new Date().getTime());
						climbing.setModified(new Date().getTime());
						climbing.setGame_mode(2);
						climbing.setPercentage(0);
						climbing.setId_mode("");
						climbing.setUser(MainActivity.getUserById(pref.getInt("local_id", -1)));
						MainActivity.climbingDao.create(climbing);
						saveClimbingInParse();
						break;

					case SOCIAL_CHALLENGE: //back to Solo Climb
						Log.d("onClick", "solo");
						/*climbing.setGame_mode(0);
						climbing.setId_mode("");
						MainActivity.climbingDao.update(climbing);*/
						if(soloClimbing == null){
							climbing.setGame_mode(0);
							climbing.setId_mode("");
							MainActivity.climbingDao.update(climbing);
							updateClimbingInParse(climbing, true);

						}else{
							Climbing del = climbing;
							deleteClimbingInParse(del);
							climbing = soloClimbing;
						}	
						leaveCompetition();
						
						break;
					}
				
				}else{
					Toast.makeText(activity.getApplicationContext(), "Connection needed", Toast.LENGTH_SHORT).show();

				}

			}
		});

		teamVsTeamButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				switch(mode){
				case SOLO_CLIMB:
				if(FacebookUtils.isOnline(activity)){
					if(climbing != null){
						//if there is an existing Climbing object with game mode as 0
						//then 'pause' it and create a new Climbing object with game mode as 3
						//when the social challenge has finished, delete the last Climbing object and 'resume' the first one in solo climb
						soloClimbing = climbing;
					}
					//otherwise create a new Climbing object with game mode as 2
					climbing = new Climbing();
					climbing.setBuilding(building);
					climbing.setCompleted(0);
					climbing.setCompleted_steps(0);
					climbing.setRemaining_steps(building.getSteps());
					climbing.setCreated(new Date().getTime());
					climbing.setModified(new Date().getTime());
					climbing.setGame_mode(3);
					climbing.setPercentage(0);
					climbing.setId_mode("");
					climbing.setUser(MainActivity.getUserById(pref.getInt("local_id", -1)));
					MainActivity.climbingDao.create(climbing);
					saveClimbingInParse();
					
					
					
					
				}else{
					Toast.makeText(activity.getApplicationContext(), "Connection needed", Toast.LENGTH_SHORT).show();

				}
				break;
				case TEAM_VS_TEAM:
					TeamDuel currentDuel = MainActivity.getTeamDuelByBuildingAndUser(building.get_id(), pref.getInt("local_id", -1));
					if(currentDuel.getId_online() == null || currentDuel.getId_online().equals(""))
						Toast.makeText(activity.getApplicationContext(), "Duel not yet saved. Connect to save it", Toast.LENGTH_SHORT).show();
					else{
						Log.i(MainActivity.AppName, "Building id clicked: "+building.get_id());
						Intent intent = new Intent(activity.getApplicationContext(), TeamPreparationActivity.class);
						intent.putExtra(MainActivity.building_intent_object, building.get_id());
						intent.putExtra(MainActivity.duel_intent_object, currentDuel.get_id());
						activity.startActivity(intent);
					}
					break;
			}
			}
		});
		return view;
	}

	/**
	 * Delete the given Climbing object from Parse. If the elimination is not successfull, remember locally to delete it later.
	 * @param climb the Climbing object to be removed
	 */
	private void deleteClimbingInParse(final Climbing climb){ System.out.println("delete " + climb.getId_online());
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Climbing");
		query.whereEqualTo("objectId", climb.getId_online());
		query.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> climbs, ParseException e) {
				if(e == null){
					if(climbs.size() != 0){
						climbs.get(0).deleteEventually();
						MainActivity.climbingDao.delete(climb);

					}
				}else{
					climb.setDeleted(true);
					climb.setSaved(false);
					MainActivity.climbingDao.update(climb);
					Toast.makeText(activity.getApplicationContext(), " 6 Connection needed", Toast.LENGTH_SHORT).show();
				}
			}
		});
	}
	
	/**
	 * Save the update of climbing object in Parse.
	 * Here, only game mode and id_mode fields can change
	 */
	private void updateClimbingInParse(final Climbing climbing, final boolean rollback) {
		Log.d("update climbing", String.valueOf(climbing.getGame_mode()));
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Climbing");
		/*query.whereEqualTo("building", climbing.getBuilding().get_id());
		query.whereEqualTo("users_id", climbing.getUser().getFBid());	
		if(used && mode == GameModeType.SOCIAL_CHALLENGE){
			if(paused){
				if(equal)
					query.whereEqualTo("id_mode", "paused");
				else
					query.whereNotEqualTo("id_mode", "paused");
			}
			else
				query.whereEqualTo("id_mode", climbing.getId_mode());
		}*/
		query.whereEqualTo("objectId", climbing.getId_online());
		query.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> climbings, ParseException e) {
				if (e == null) { System.out.println("update " + climbings.get(0).getObjectId());
					ParseObject c = climbings.get(0);
					c.put("game_mode", climbing.getGame_mode());
					if(climbing.getId_mode() != null)
						c.put("id_mode", climbing.getId_mode());
					else 						
						c.put("id_mode", "");
					System.out.println("climbing id mode: " + climbing.getId_mode());
					c.saveInBackground(new SaveCallback() {
						
						@Override
						public void done(ParseException e) {
								if(e == null){
									if(!rollback && climbing.getId_mode() == null || climbing.getId_mode().equalsIgnoreCase("")){
									switch (climbing.getGame_mode()) {
									case 1:
										saveCollaboration();
										break;
									case 2:
										
										break;
									default:
										break;
									}
									}
								}else{
									Toast.makeText(MainActivity.getContext(), " 1 Connection Problems", Toast.LENGTH_SHORT).show();
									Log.e("updateClimbingInParse", e.getMessage());
									if(climbing.getGame_mode() == 1 && collab.getBuilding() == null){
										climbing.setGame_mode(0);
										climbing.setId_mode("");
									}
									//if(climbing.getId_mode().equals("paused")){
										//climbing.setId_mode("");
										//rollback(2);
									//}
									climbing.setSaved(false);
									MainActivity.climbingDao.update(climbing);
								}
						}
					});
					climbing.setSaved(true);
					MainActivity.climbingDao.update(climbing);
				} else {
					Toast.makeText(MainActivity.getContext(), "2 Connection Problems", Toast.LENGTH_SHORT).show();
					Log.e("updateClimbingInParse", e.getMessage());
					if(climbing.getGame_mode() == 1 && collab.getBuilding() == null){
						climbing.setGame_mode(0);
						climbing.setId_mode("");
					}
					
					climbing.setSaved(false);
					MainActivity.climbingDao.update(climbing);
				}
			}
		});
	}

	/**
	 * Save current Collaboration object in Parse
	 */
	private void saveClimbingInParse() {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		df.setTimeZone(new SimpleTimeZone(0, "GMT"));
		final ParseObject climb = new ParseObject("Climbing");
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
		if(climbing.getId_mode() != null)
			climb.put("id_mode", climbing.getId_mode());
		else
			climb.put("id_mode", "");
		climb.saveInBackground(new SaveCallback() {
			
			@Override
			public void done(ParseException e) {
				if(e == null){
					climbing.setId_online(climb.getObjectId());
					climbing.setSaved(true);
					MainActivity.climbingDao.update(climbing);
					switch(climbing.getGame_mode()){
					case 1:
						saveCollaboration();
						break;
					case 2:
						saveCompetition();
						break;
					case 3:
						saveTeamDuel();
					}
					
				}else{
					if(climbing.getGame_mode() == 1){
					//unable to save the collaboration, save only the climbing
					climbing.setGame_mode(0);
					climbing.setId_mode("");
					}
					if(climbing.getGame_mode() == 2 || climbing.getGame_mode() == 3){
						if(soloClimbing == null){
							climbing.setGame_mode(0);
							climbing.setId_mode("");						
						}else{
							Climbing del = climbing;
							climbing = soloClimbing;
							climbing.setGame_mode(0);
							climbing.setId_mode("");
							del.setDeleted(true);
							del.setSaved(false);
							MainActivity.climbingDao.update(del);
						}
					}
					climbing.setSaved(false);
					MainActivity.climbingDao.update(climbing);
					Toast.makeText(activity, "77 Unable to connect: data will be saved during next reconnection", Toast.LENGTH_SHORT).show();
				}
				
			}
		});
		
	}
	
	/**
	 * Update button graphics after changing game mode
	 */
	void setSocialClimb(){
		gameMode.setText("Modalitˆ: " + setModeText());
		socialClimbButton.setText("Back to Solo Climb");
		socialChallengeButton.setEnabled(false);
		teamVsTeamButton.setEnabled(false);
	}
	
	void setSocialChallenge(){
		gameMode.setText("Modalitˆ: " + setModeText());
		socialChallengeButton.setText("Back to Solo Climb");
		socialClimbButton.setEnabled(false);
		teamVsTeamButton.setEnabled(false);
	}
	
	void setTeamChallenge(){
		gameMode.setText("Modalitˆ: " + setModeText());
		teamVsTeamButton.setEnabled(false);
		socialClimbButton.setEnabled(false);
		socialChallengeButton.setEnabled(false);
	}

	/**
	 * Create and save a new Collaboration object both online and offline.
	 * Then, open dialog to let user choose the friends to invite to his collaboration.
	 */
	private void saveCollaboration() { Log.d("saveCollaboration", "into");
		JSONObject stairs = new JSONObject();
		JSONObject collaborators = new JSONObject();

		try {
			collaborators.put(pref.getString("FBid", ""), pref.getString("username", ""));
			stairs.put(pref.getString("FBid", ""), climbing.getCompleted_steps());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		collab.setBuilding(building);
		collab.setMy_stairs(0);
		collab.setOthers_stairs(0);
		collab.setLeaved(false);
		collab.setUser(MainActivity.getUserById(pref.getInt("local_id", -1)));
		collab.setCompleted(false);
		MainActivity.collaborationDao.create(collab);

		collabParse = new ParseObject("Collaboration");
		collabParse.put("building", building.get_id());
		collabParse.put("stairs", stairs);
		collabParse.put("collaborators", collaborators);
		collabParse.put("completed", false);
		collabParse.saveInBackground(new SaveCallback() {
			
			@Override
			public void done(ParseException e) {
				if(e == null){
				collab.setId(collabParse.getObjectId());
				collab.setSaved(true);
				climbing.setId_mode(collab.getId());
				MainActivity.climbingDao.update(climbing);
				updateClimbingInParse(climbing, false);

				mode = GameModeType.SOCIAL_CLIMB;
				setSocialClimb();
				sendRequest(GameModeType.SOCIAL_CLIMB, collab.getId());
				}else{
					collab.setSaved(false);
					MainActivity.collaborationDao.update(collab);
					/*climbing.setId_mode("set");
					climbing.setGame_mode(1);*/
					climbing.setSaved(false);
					MainActivity.climbingDao.update(climbing);
					Toast.makeText(MainActivity.getContext(), "3 Connection Problems: cannot create collaboration", Toast.LENGTH_SHORT).show();
					Log.e("saveCollaboration", e.getMessage());					
				}
			}
		});

		
		
		

	}
	

	/**
	 * Saves the Competition object both locally and in Parse. If the operation is not successfull, remember locally to retry it later.
	 */
	private void saveCompetition() { System.out.println("save competition");
		JSONObject stairs = new JSONObject();
		JSONObject collaborators = new JSONObject();

		try {
			collaborators.put(pref.getString("FBid", ""), pref.getString("username", ""));
			stairs.put(pref.getString("FBid", ""), climbing.getCompleted_steps());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		compet.setBuilding(building);
		compet.setMy_stairs(0);
		compet.setCurrent_position(0);
		compet.setLeaved(false);
		compet.setUser(MainActivity.getUserById(pref.getInt("local_id", -1)));
		compet.setCompleted(false);
		MainActivity.competitionDao.create(compet);

		competParse = new ParseObject("Competition");
		competParse.put("building", building.get_id());
		competParse.put("stairs", stairs);
		competParse.put("competitors", collaborators);
		competParse.put("completed", false);
		System.out.println("provo a salvare");
		competParse.saveInBackground(new SaveCallback() {
			
			@Override
			public void done(ParseException e) {
				if(e == null){ System.out.println("assegnato id a competiz" + competParse.getObjectId());
					compet.setId_online(competParse.getObjectId());
					compet.setSaved(true);
				MainActivity.competitionDao.update(compet);
				
				climbing.setId_mode(compet.getId_online());
				MainActivity.climbingDao.update(climbing);
				updateClimbingInParse(climbing, false);

				mode = GameModeType.SOCIAL_CHALLENGE;
				setSocialChallenge();
				sendRequest(GameModeType.SOCIAL_CHALLENGE, compet.getId_online());
				}else{
					System.out.println("assegnato id a competiz????" + competParse.getObjectId());
						compet.setSaved(false);
						climbing.setSaved(false);
						MainActivity.competitionDao.update(compet);
						MainActivity.climbingDao.update(climbing);
					
					Toast.makeText(MainActivity.getContext(), "5 Connection Problems: cannot create competition", Toast.LENGTH_SHORT).show();
					Log.e("saveCompetition", e.getMessage());					
				}
			}
		});
	}
	
	private void saveTeamDuel() {
		User me = MainActivity.getUserById(pref.getInt("local_id", -1));
		duel.setBuilding(building);
		duel.setUser(me);
		duel.setSteps_my_group(0);
		duel.setSteps_other_group(0);
		duel.setMy_steps(0);
		duel.setChallenger_name("");
		duel.setCreator_name(me.getName());
		duel.setMygroup(Group.CREATOR);
		duel.setCreator(true);
		duel.setDeleted(false);
		duel.setCreator_name(me.getName());
		MainActivity.teamDuelDao.create(duel);

		
		JSONObject creator_stairs = new JSONObject();
		JSONObject creator_team = new JSONObject();
		JSONObject challenger_stairs = new JSONObject();
		JSONObject challenger_team = new JSONObject();
		JSONObject creator = new JSONObject();

		try {
			creator_team.put(pref.getString("FBid", ""), pref.getString("username", ""));
			creator_stairs.put(pref.getString("FBid", ""), 0);
			creator.put(pref.getString("FBid", ""), pref.getString("username", ""));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		teamDuelParse = new ParseObject("TeamDuel");
		teamDuelParse.put("creator", creator);
		teamDuelParse.put("building", building.get_id());
		teamDuelParse.put("creator_stairs", creator_stairs);
		teamDuelParse.put("creator_team", creator_team);
		teamDuelParse.put("challenger_team", challenger_team);
		teamDuelParse.put("challenger_stairs", challenger_stairs);
		teamDuelParse.saveInBackground(new SaveCallback() {
			
			@Override
			public void done(ParseException e) {
				if(e == null){
				
					duel.setId_online(teamDuelParse.getObjectId());
					duel.setSaved(true);
					MainActivity.teamDuelDao.update(duel);

				climbing.setId_mode(duel.getId_online());
				MainActivity.climbingDao.update(climbing);
				System.out.println("id duel: " + compet.getId_online());
				
				updateClimbingInParse(climbing, false);

				mode = GameModeType.SOCIAL_CHALLENGE;
				setTeamChallenge();
				
				Log.i(MainActivity.AppName, "Building id clicked: "+building.get_id());
				Intent intent = new Intent(activity.getApplicationContext(), TeamPreparationActivity.class);
				intent.putExtra(MainActivity.building_intent_object, building.get_id());
				activity.startActivity(intent);
				
				}else{
					duel.setSaved(false);
					climbing.setSaved(false);
					MainActivity.teamDuelDao.update(duel);
					MainActivity.climbingDao.update(climbing);
					Toast.makeText(MainActivity.getContext(), "4 Connection Problems: cannot create competition", Toast.LENGTH_SHORT).show();
					Log.e("saveTeamCompetition", e.getMessage());					
				}
			}
		});
	}
	
	

	/**
	 * Makes the current logged user leaving the current Collaboration.
	 * It saved the update in Parse if it is possible, otherwise, it saved the update locally e remembers to retry it later.
	 */
	private void leaveCollaboration() {
		final Collaboration collab = MainActivity.getCollaborationByBuildingAndUser(building.get_id(), pref.getInt("local_id", -1));
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Collaboration");
		query.whereEqualTo("objectId", collab.getId());
		query.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> collabs, ParseException e) {
				if (e == null) {
					if (collabs.size() == 0) {
						Toast.makeText(MainActivity.getContext(), "This collaboration does not exists anymore", Toast.LENGTH_SHORT).show();
						MainActivity.collaborationDao.delete(collab);
					} else {
						ParseObject c = collabs.get(0);
						JSONObject collaborators = c.getJSONObject("collaborators");
						JSONObject stairs = c.getJSONObject("stairs");
						collaborators.remove(pref.getString("FBid", ""));
						stairs.remove(pref.getString("FBid", ""));
						c.put("collaborators", collaborators);
						c.put("stairs", stairs);
						if(collaborators.length() == 0)
							c.deleteEventually();
						else
							c.saveEventually();
						/*collab.setLeaved(true);
						collab.setSaved(true);
						MainActivity.collaborationDao.update(collab);*/
						MainActivity.collaborationDao.delete(collab);
						climbing.setGame_mode(0);
						climbing.setId_mode("");
						MainActivity.climbingDao.update(climbing);
						updateClimbingInParse(climbing, true);
					}
				} else {
					collab.setLeaved(true);
					collab.setSaved(false);
					MainActivity.collaborationDao.update(collab);
					Toast.makeText(MainActivity.getContext(), "6 Connection Problems", Toast.LENGTH_SHORT).show();
					Log.e("updateClimbingInParse", e.getMessage());
				}
				socialChallengeButton.setEnabled(true);
				socialClimbButton.setEnabled(true);
				teamVsTeamButton.setEnabled(true);
				socialClimbButton.setText("Collab");
				mode = GameModeType.SOLO_CLIMB;
				gameMode.setText("Modalitˆ: " + setModeText());
			}
		});
	}

	/**
	 * Makes the current logged user leaving the current Competition.
	 * It saved the update in Parse if it is possible, otherwise, it saved the update locally e remembers to retry it later.
	 */
	private void leaveCompetition() {
		final Competition competit = MainActivity.getCompetitionByBuildingAndUser(building.get_id(), pref.getInt("local_id", -1));
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Competition");
		query.whereEqualTo("objectId", competit.getId_online());
		query.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> competits, ParseException e) {
				if (e == null) {
					if (competits.size() == 0) {
						Toast.makeText(MainActivity.getContext(), "This competition does not exists anymore", Toast.LENGTH_SHORT).show();
						MainActivity.competitionDao.delete(competit);
					} else {
						ParseObject c = competits.get(0);
						JSONObject collaborators = c.getJSONObject("competitors");
						JSONObject stairs = c.getJSONObject("stairs");
						collaborators.remove(pref.getString("FBid", ""));
						stairs.remove(pref.getString("FBid", ""));
						c.put("competitors", collaborators);
						c.put("stairs", stairs);
						if(collaborators.length() == 0)
							c.deleteEventually();
						else
							c.saveEventually();
					
						MainActivity.competitionDao.delete(competit);
		
					}
				} else {
					competit.setLeaved(true);
					competit.setSaved(false);
					MainActivity.competitionDao.update(competit);
					Toast.makeText(MainActivity.getContext(), "7 Connection Problems", Toast.LENGTH_SHORT).show();
					Log.e("leaveCompetition", e.getMessage());
				}
				socialChallengeButton.setEnabled(true);
				socialClimbButton.setEnabled(true);
				teamVsTeamButton.setEnabled(true);
				socialChallengeButton.setText("Competiz");
				mode = GameModeType.SOLO_CLIMB;
				gameMode.setText("Modalitˆ: " + setModeText());
			}
		});
	}
	
	/**
	 * Open a dialog to let user choose the friends to invite.
	 * @param mode current game mode
	 * @param idCollab id of the current Collaboration/Competition/TeamDuel object
	 */
	private void sendRequest(GameModeType mode, String idCollab) {
		Bundle params = new Bundle();
		final GameModeType currentMode = mode; 
		switch (mode) {
		case SOCIAL_CLIMB:
			params.putString("data", "{\"idCollab\":\"" + idCollab + "\"," + "\"idBuilding\":\"" + building.get_id() + "\"," + "\"nameBuilding\":\"" + building.getName() + "\", \"type\": \"1\"}");
			params.putString("message", "Please, help me!!!!");
			break;
		case SOCIAL_CHALLENGE:
			params.putString("data", "{\"idCollab\":\"" + idCollab + "\"," + "\"idBuilding\":\"" + building.get_id() + "\"," + "\"nameBuilding\":\"" + building.getName() + "\", \"type\": \"2\"}");
			params.putString("message", "challenge");

			break;
		case TEAM_VS_TEAM:
			params.putString("data", "{\"idCollab\":\"" + idCollab + "\"," + "\"idBuilding\":\"" + building.get_id() + "\"," + "\"nameBuilding\":\"" + building.getName() + "\", \"type\": \"3\"}");
			params.putString("message", "team challenge");

			break;
		}

		WebDialog requestsDialog = (new WebDialog.RequestsDialogBuilder(activity, Session.getActiveSession(), params)).setOnCompleteListener(new OnCompleteListener() {

			@Override
			public void onComplete(Bundle values, FacebookException error) {
				if (error != null) {
					if (error instanceof FacebookOperationCanceledException) {
						Toast.makeText(activity, "Request cancelled", Toast.LENGTH_SHORT).show();
						switch (currentMode) {
						case SOCIAL_CLIMB:
							rollback(1);
							break;

						case SOCIAL_CHALLENGE:
							rollback(2);
							break;
						}
					} else {
						Toast.makeText(activity, "Network Error", Toast.LENGTH_SHORT).show();
						switch (currentMode) {
						case SOCIAL_CLIMB:
							rollback(1);
							break;

						case SOCIAL_CHALLENGE:
							rollback(2);
							break;
						}
					}
				} else {
					final String requestId = values.getString("request");
					if (requestId != null) {
						Toast.makeText(activity, "Request sent", Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(activity, "Request cancelled", Toast.LENGTH_SHORT).show();
						switch (currentMode) {
						case SOCIAL_CLIMB:
							rollback(1);
							break;

						case SOCIAL_CHALLENGE:
							rollback(2);
							break;
						}

					}
				}
			}

		}).build();
		requestsDialog.show();
	}
	
	/**
	 * Rollback in case of deleted operation
	 */
	private void rollback(int type){
		switch (type) {
		case 1:
			collabParse.deleteEventually();
			MainActivity.collaborationDao.delete(collab);

			climbing.setGame_mode(0);
			climbing.setId_mode("");
			MainActivity.climbingDao.update(climbing);
			updateClimbingInParse(climbing, true);
			graphicsRollBack(type);
			
			mode = GameModeType.SOLO_CLIMB;
			gameMode.setText("Modalitˆ: " + setModeText());
			break;

		case 2:
			if(competParse.getJSONObject("competitors") != null){ System.out.println("delete eventually");
				competParse.deleteEventually();
			}
			if(compet.getBuilding() != null){ System.out.println("delete local");
				MainActivity.competitionDao.delete(compet);
			}
			
			if(soloClimbing != null){ System.out.println("sistema due climb");
				Climbing del = climbing;
				climbing = soloClimbing;
				climbing.setId_mode("");
				MainActivity.climbingDao.update(climbing);
				updateClimbingInParse(climbing, true);
			
				deleteClimbingInParse(del);		
			}
			else{ System.out.println("sistema unico climb");
				climbing.setGame_mode(0);
				climbing.setId_mode("");
				MainActivity.climbingDao.update(climbing);
				updateClimbingInParse(climbing, true);
			}
			graphicsRollBack(type);
			
			mode = GameModeType.SOLO_CLIMB;
			gameMode.setText("Modalitˆ: " + setModeText());
			break;
			
		case 3:
		/*	teamDuelParse.deleteEventually();
			MainActivity.teamDuelDao.delete(duel);
			
			soloClimbing.setId_mode("");
			MainActivity.climbingDao.update(soloClimbing);
			updateClimbingInParse(climbing, true);
			
			deleteClimbingInParse(climbing);
			graphicsRollBack(type);
			
			mode = GameModeType.SOLO_CLIMB;
			gameMode.setText("Modalitˆ: " + setModeText());*/
			break;
		}
		updateStatus();
	}
	
	/**
	 * Updates Status text
	 */
	private void updateStatus(){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
		if(climbing != null){
			if(climbing.getPercentage() >= 1.00){
				climbingStatus.setText("Climbing: COMPLETED! (on " + sdf.format(new Date(climbing.getModified())) + ")");
			}else{
				climbingStatus.setText("Climbing status: " + new DecimalFormat("#").format(climbing.getPercentage() * 100) + "% (last attempt @ " + sdf.format(new Date(climbing.getModified())) + ")");
			}
		}
	}
	
	/**
	 * Update graphics in case of deleted operation
	 * @param type
	 */
	private void graphicsRollBack(int type){
		if(climbing.getPercentage() >= 100){
			socialChallengeButton.setEnabled(true);
			socialClimbButton.setEnabled(false);
			teamVsTeamButton.setEnabled(true);
		}else{
			socialChallengeButton.setEnabled(true);
			socialClimbButton.setEnabled(true);
			teamVsTeamButton.setEnabled(true);
		}
		switch(type){
		case 1:
			socialClimbButton.setText("Collab");
			break;
		case 2:
			socialChallengeButton.setText("Competiz");
			break;
		case 3:
			teamVsTeamButton.setText("team vs team");
			break;
			
		}
	}

	@Override
	public boolean convert(View convertCardView) {
		// TODO Auto-generated method stub
		return false;
	}
}
