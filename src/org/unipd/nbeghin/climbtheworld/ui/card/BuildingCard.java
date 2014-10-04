package org.unipd.nbeghin.climbtheworld.ui.card;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.SimpleTimeZone;

import org.apache.http.cookie.SetCookie;
import org.json.JSONException;
import org.json.JSONObject;
import org.unipd.nbeghin.climbtheworld.MainActivity;
import org.unipd.nbeghin.climbtheworld.R;
import org.unipd.nbeghin.climbtheworld.models.Building;
import org.unipd.nbeghin.climbtheworld.models.Climbing;
import org.unipd.nbeghin.climbtheworld.models.Collaboration;
import org.unipd.nbeghin.climbtheworld.models.Competition;
import org.unipd.nbeghin.climbtheworld.models.GameModeType;
import org.unipd.nbeghin.climbtheworld.models.TeamDuel;
import org.unipd.nbeghin.climbtheworld.util.FacebookUtils;

import android.app.Activity;
import android.content.Context;
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
		climbing = MainActivity.getClimbingForBuildingAndUser(building.get_id(), pref.getInt("local_id", -1));
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
				case SOLO_CLIMB:
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
						updateClimbingInParse(climbing);
					}

					saveCollaboration();
					climbing.setId_mode(collab.getId());
					updateClimbingInParse(climbing);
					
					break;

				case SOCIAL_CLIMB: //back to solo climb
					Log.d("onClick", "solo");
					climbing.setGame_mode(0);
					climbing.setId_mode("");
					MainActivity.climbingDao.update(climbing);
					updateClimbingInParse(climbing);
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
							climbing.setId_mode("paused");
							MainActivity.climbingDao.update(climbing);
							updateClimbingInParse(climbing);
							soloClimbing = climbing;
						}
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
						saveCompetition();
						/*if (climbing == null) {
							// crea nuovo
							climbing = new Climbing();
							climbing.setBuilding(building);
							climbing.setCompleted(0);
							climbing.setCompleted_steps(0);
							climbing.setRemaining_steps(building.getSteps());
							climbing.setCreated(new Date().getTime());
							climbing.setModified(new Date().getTime());
							climbing.setGame_mode(2);
							climbing.setPercentage(0);
							climbing.setUser(MainActivity.getUserById(pref.getInt("local_id", -1)));
							MainActivity.climbingDao.create(climbing);
							saveClimbingInParse();
						} else {
							// aggiorna esistente
							climbing.setGame_mode(2);
							if(climbing.getPercentage() >= 1.00){
								climbing.setPercentage(0);
								climbing.setCompleted_steps(0);
								climbing.setRemaining_steps(building.getSteps());
							}
							MainActivity.climbingDao.update(climbing);
							updateClimbingInParse();
						}*/

						
						
						break;

					case SOCIAL_CHALLENGE:
						Log.d("onClick", "solo");
						/*climbing.setGame_mode(0);
						climbing.setId_mode("");
						MainActivity.climbingDao.update(climbing);*/
						if(soloClimbing == null){
							climbing.setGame_mode(0);
							climbing.setId_mode("");
							MainActivity.climbingDao.update(climbing);
							updateClimbingInParse(climbing);

						}else{
							deleteClimbingInParse(climbing);
							soloClimbing.setId_mode("");
							MainActivity.climbingDao.update(soloClimbing);
							climbing = soloClimbing;
							updateClimbingInParse(climbing);
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
				if(FacebookUtils.isOnline(activity)){
						if(climbing != null){
							climbing.setId_mode("paused");
							MainActivity.climbingDao.update(climbing);
							updateClimbingInParse(climbing);
						}
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
						saveTeamCompetition();
				}else{
					Toast.makeText(activity.getApplicationContext(), "Connection needed", Toast.LENGTH_SHORT).show();

				}
			}
		});
		return view;
	}

	private void deleteClimbingInParse(final Climbing climb){
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Climbing");
		query.whereEqualTo("building", climb.getBuilding().get_id());
		query.whereEqualTo("users_id", climb.getUser().getFBid());	
		query.whereEqualTo("id_mode", climb.getId_mode());
		query.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> climbs, ParseException e) {
				if(e == null){
					if(climbs.size() != 0){
						climbs.get(0).deleteEventually();
						MainActivity.climbingDao.delete(climb);

					}
				}else{
					Toast.makeText(activity.getApplicationContext(), "Connection needed", Toast.LENGTH_SHORT).show();
				}
			}
		});
	}
	
	/**
	 * Save the update of climbing object in Parse.
	 * Here, only game mode and id_mode fields can change
	 */
	private void updateClimbingInParse(final Climbing climbing) {
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
					c.saveEventually();
					climbing.setSaved(true);
					MainActivity.climbingDao.update(climbing);
				} else {
					Toast.makeText(MainActivity.getContext(), "Connection Problems", Toast.LENGTH_SHORT).show();
					Log.e("updateClimbingInParse", e.getMessage());
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
				}else{
					climbing.setSaved(false);
					MainActivity.climbingDao.update(climbing);
					Toast.makeText(activity, "Unable to connect: data will be saved during next reconnection", Toast.LENGTH_SHORT).show();
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
	 * Create and save a new Collaboration object both online and offine.
	 * Then, open dialog to let user choose the friends to invite to his collaboration.
	 */
	private void saveCollaboration() {
		JSONObject stairs = new JSONObject();
		JSONObject collaborators = new JSONObject();

		try {
			collaborators.put(pref.getString("FBid", ""), pref.getString("username", ""));
			stairs.put(pref.getString("FBid", ""), climbing.getCompleted_steps());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		collabParse = new ParseObject("Collaboration");
		collabParse.put("building", building.get_id());
		collabParse.put("stairs", stairs);
		collabParse.put("collaborators", collaborators);
		collabParse.put("completed", false);
		collabParse.saveInBackground(new SaveCallback() {
			
			@Override
			public void done(ParseException e) {
				if(e == null){
				collab.setBuilding(building);
				collab.setId(collabParse.getObjectId());
				collab.setMy_stairs(0);
				collab.setOthers_stairs(0);
				collab.setSaved(true);
				collab.setLeaved(false);
				collab.setUser(MainActivity.getUserById(pref.getInt("local_id", -1)));
				collab.setCompleted(false);
				MainActivity.collaborationDao.create(collab);
				climbing.setId_mode(collab.getId());
				MainActivity.climbingDao.update(climbing);
				updateClimbingInParse(climbing);

				mode = GameModeType.SOCIAL_CLIMB;
				setSocialClimb();
				sendRequest(GameModeType.SOCIAL_CLIMB, collab.getId());
				}else{
					collab.setSaved(false);
					MainActivity.collaborationDao.update(collab);
					Toast.makeText(MainActivity.getContext(), "Connection Problems: cannot create collaboration", Toast.LENGTH_SHORT).show();
					Log.e("saveCollaboration", e.getMessage());					
				}
			}
		});

		
		
		

	}
	
	private void saveTeamCompetition() {
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
				duel.setBuilding(building);
				duel.setUser(MainActivity.getUserById(pref.getInt("local_id", -1)));
				duel.setSteps_my_group(0);
				duel.setSteps_other_group(0);
				duel.setId_online(teamDuelParse.getObjectId());
				duel.setMy_steps(0);
				duel.setSaved(true);
				duel.setChallenger_name("");
				duel.setCreator(true);
				MainActivity.teamDuelDao.create(duel);
				climbing.setId_mode(duel.getId_online());
				MainActivity.climbingDao.update(climbing);
				System.out.println("id compet: " + compet.getId_online());
				
				updateClimbingInParse(climbing);

				mode = GameModeType.SOCIAL_CHALLENGE;
				setTeamChallenge();
				sendRequest(GameModeType.SOCIAL_CHALLENGE, duel.getId_online());
				}else{
					duel.setSaved(false);
					MainActivity.teamDuelDao.update(duel);
					Toast.makeText(MainActivity.getContext(), "Connection Problems: cannot create competition", Toast.LENGTH_SHORT).show();
					Log.e("saveTeamCompetition", e.getMessage());					
				}
			}
		});
	}
	
	private void saveCompetition() {
		JSONObject stairs = new JSONObject();
		JSONObject collaborators = new JSONObject();

		try {
			collaborators.put(pref.getString("FBid", ""), pref.getString("username", ""));
			stairs.put(pref.getString("FBid", ""), climbing.getCompleted_steps());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		competParse = new ParseObject("Competition");
		competParse.put("building", building.get_id());
		competParse.put("stairs", stairs);
		competParse.put("competitors", collaborators);
		competParse.put("completed", false);
		competParse.saveInBackground(new SaveCallback() {
			
			@Override
			public void done(ParseException e) {
				if(e == null){
				compet.setBuilding(building);
				compet.setId_online(competParse.getObjectId());
				compet.setMy_stairs(0);
				compet.setCurrent_position(0);
				compet.setSaved(true);
				compet.setLeaved(false);
				compet.setUser(MainActivity.getUserById(pref.getInt("local_id", -1)));
				compet.setCompleted(false);
				System.out.println("id salvato: " + climbing.getId_mode());
				MainActivity.competitionDao.create(compet);
				climbing.setId_mode(compet.getId_online());
				MainActivity.climbingDao.update(climbing);
				System.out.println("id compet: " + compet.getId_online());
				updateClimbingInParse(climbing);

				mode = GameModeType.SOCIAL_CHALLENGE;
				setSocialChallenge();
				sendRequest(GameModeType.SOCIAL_CHALLENGE, compet.getId_online());
				}else{
					compet.setSaved(false);
					MainActivity.competitionDao.update(compet);
					Toast.makeText(MainActivity.getContext(), "Connection Problems: cannot create competition", Toast.LENGTH_SHORT).show();
					Log.e("saveCompetition", e.getMessage());					
				}
			}
		});

		
		
		

	}

	private void leaveCollaboration() {
		final Collaboration collab = MainActivity.getCollaborationByBuilding(building.get_id());
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
						MainActivity.climbingDao.update(climbing);
						updateClimbingInParse(climbing);
					}
				} else {
					collab.setLeaved(true);
					collab.setSaved(false);
					MainActivity.collaborationDao.update(collab);
					Toast.makeText(MainActivity.getContext(), "Connection Problems", Toast.LENGTH_SHORT).show();
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

	private void leaveCompetition() {
		final Competition competit = MainActivity.getCompetitionByBuilding(building.get_id());
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
						/*collab.setLeaved(true);
						collab.setSaved(true);
						MainActivity.collaborationDao.update(collab);*/
						MainActivity.competitionDao.delete(competit);
						/*climbing.setGame_mode(0);
						MainActivity.climbingDao.update(climbing);
						updateClimbingInParse();*/
					}
				} else {
					competit.setLeaved(true);
					competit.setSaved(false);
					MainActivity.competitionDao.update(competit);
					Toast.makeText(MainActivity.getContext(), "Connection Problems", Toast.LENGTH_SHORT).show();
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
			updateClimbingInParse(climbing);
			graphicsRollBack(type);
			
			mode = GameModeType.SOLO_CLIMB;
			gameMode.setText("Modalitˆ: " + setModeText());
			break;

		case 2:
			competParse.deleteEventually();
			MainActivity.competitionDao.delete(compet);
			
			if(soloClimbing != null){
			soloClimbing.setId_mode("");
			MainActivity.climbingDao.update(soloClimbing);
			updateClimbingInParse(soloClimbing);
			
			deleteClimbingInParse(climbing);		}
			else{
				climbing.setGame_mode(0);
				climbing.setId_mode("");
				MainActivity.climbingDao.update(climbing);
				updateClimbingInParse(climbing);
			}
			graphicsRollBack(type);
			
			mode = GameModeType.SOLO_CLIMB;
			gameMode.setText("Modalitˆ: " + setModeText());
			break;
			
		case 3:
			teamDuelParse.deleteEventually();
			MainActivity.teamDuelDao.delete(duel);
			
			soloClimbing.setId_mode("");
			MainActivity.climbingDao.update(soloClimbing);
			updateClimbingInParse(climbing);
			
			deleteClimbingInParse(climbing);
			graphicsRollBack(type);
			
			mode = GameModeType.SOLO_CLIMB;
			gameMode.setText("Modalitˆ: " + setModeText());
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
