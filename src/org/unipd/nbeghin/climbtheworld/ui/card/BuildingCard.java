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
import org.unipd.nbeghin.climbtheworld.models.Building;
import org.unipd.nbeghin.climbtheworld.models.Climbing;
import org.unipd.nbeghin.climbtheworld.models.Collaboration;
import org.unipd.nbeghin.climbtheworld.models.Competition;
import org.unipd.nbeghin.climbtheworld.models.GameModeType;
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
	final SharedPreferences pref = MainActivity.getContext().getSharedPreferences("UserSession", 0);
	GameModeType mode;
	Activity activity;
	ParseObject collabParse = new ParseObject("Collaboration");
	ParseObject competParse = new ParseObject("Competition");

	final Collaboration collab = new Collaboration();
	final Competition compet = new Competition();
	
	TextView gameMode;
	Button socialClimbButton;
	Button socialChallengeButton;
	Button teamVsTeamButton;

	public BuildingCard(Building building, Activity activity) {
		super(building.getName());
		this.building = building;
		this.activity = activity;
	}

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
		TextView climbingStatus = (TextView) view.findViewById(R.id.climbingStatus);
		// final SharedPreferences pref =
		// MainActivity.getContext().getSharedPreferences("UserSession", 0);
		climbing = MainActivity.getClimbingForBuildingAndUser(building.get_id(), pref.getInt("local_id", -1));
		gameMode.setText("Modalità: " + setModeText());
		if(climbing != null && climbing.getGame_mode() == 1)
			setSocialClimb();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
		if (climbing != null) {
			if (climbing.getPercentage() >= 100) {
				climbingStatus.setText("Climbing: COMPLETED! (on " + sdf.format(new Date(climbing.getModified())) + ")");
				socialClimbButton.setEnabled(false);
				socialChallengeButton.setEnabled(true);
				teamVsTeamButton.setEnabled(true);
			} else {
				climbingStatus.setText("Climbing status: " + new DecimalFormat("#").format(climbing.getPercentage() * 100) + "% (last attempt @ " + sdf.format(new Date(climbing.getModified())) + ")");
				socialClimbButton.setEnabled(false);
				socialChallengeButton.setEnabled(true);
				teamVsTeamButton.setEnabled(true);
			}
		} else {
			climbingStatus.setText("Not climbed yet");
			socialClimbButton.setEnabled(false);
			socialChallengeButton.setEnabled(true);
			teamVsTeamButton.setEnabled(true);
		}

		

		socialClimbButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(FacebookUtils.isOnline(activity)){
				// crea collaborazione
				// crea climbing se non c'è gia e cambia modalita
				// invia richieste
				switch (mode) {
				case SOLO_CLIMB:
					//Log.d("onClick", "solo");
					if (climbing == null) {
						// crea nuovo
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
						// aggiorna esistente
						climbing.setGame_mode(1);
						MainActivity.climbingDao.update(climbing);
						updateClimbingInParse();
					}

					saveCollaboration();
					
					break;

				case SOCIAL_CLIMB:
					Log.d("onClick", "solo");
					climbing.setGame_mode(0);
					MainActivity.climbingDao.update(climbing);
					updateClimbingInParse();
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
					case SOCIAL_CHALLENGE:
						if (climbing == null) {
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
							climbing.setGame_mode(1);
							MainActivity.climbingDao.update(climbing);
							updateClimbingInParse();
						}

						saveCompetition();
						break;

					case SOLO_CLIMB:
						Log.d("onClick", "solo");
						climbing.setGame_mode(0);
						MainActivity.climbingDao.update(climbing);
						updateClimbingInParse();
						leaveCompetition();
						break;
					}
				
				}

			}
		});

		teamVsTeamButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if(FacebookUtils.isOnline(activity)){
				climbing.setGame_mode(4);
				MainActivity.climbingDao.update(climbing);
				updateClimbingInParse();
				gameMode.setText("Modalità: " + setModeText());
				sendRequest(GameModeType.TEAM_VS_TEAM, "");
				}
			}
		});
		return view;
	}

	private void updateClimbingInParse() {
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Climbing");
		query.whereEqualTo("building", climbing.getBuilding().get_id());
		query.whereEqualTo("users_id", climbing.getUser().getFBid());
		query.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> climbings, ParseException e) {
				if (e == null) {
					ParseObject c = climbings.get(0);
					c.put("game_mode", climbing.getGame_mode());
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

	private void saveClimbingInParse() {
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
		climbing.setSaved(true);
		MainActivity.climbingDao.update(climbing);
	}
	
	void setSocialClimb(){
		gameMode.setText("Modalità: " + setModeText());
		socialClimbButton.setText("Back to Solo Climb");
		socialChallengeButton.setEnabled(false);
		teamVsTeamButton.setEnabled(false);
	}
	
	void setSocialChallenge(){
		gameMode.setText("Modalità: " + setModeText());
		socialChallengeButton.setText("Back to Solo Climb");
		socialClimbButton.setEnabled(false);
		teamVsTeamButton.setEnabled(false);
	}

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
				MainActivity.collaborationDao.create(collab);
				

				mode = GameModeType.SOCIAL_CLIMB;
				setSocialClimb();
				sendRequest(GameModeType.SOCIAL_CLIMB, collab.getId());
				}else{
					Toast.makeText(MainActivity.getContext(), "Connection Problems: cannot create collaboration", Toast.LENGTH_SHORT).show();
					Log.e("saveCollaboration", e.getMessage());					
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
				compet.setId_online(collabParse.getObjectId());
				compet.setMy_stairs(0);
				compet.setCurrent_position(0);
				compet.setSaved(true);
				compet.setLeaved(false);
				compet.setUser(MainActivity.getUserById(pref.getInt("local_id", -1)));
				compet.setCompleted(false);
				MainActivity.competitionDao.create(compet);
				

				mode = GameModeType.SOCIAL_CLIMB;
				setSocialClimb();
				sendRequest(GameModeType.SOCIAL_CLIMB, compet.getId_online());
				}else{
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
						updateClimbingInParse();
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
				gameMode.setText("Modalità: " + setModeText());
			}
		});
	}

	private void leaveCompetition() {
		final Competition competit = MainActivity.getCompetitionByBuilding(building.get_id());
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Collaboration");
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
						climbing.setGame_mode(0);
						MainActivity.climbingDao.update(climbing);
						updateClimbingInParse();
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
				gameMode.setText("Modalità: " + setModeText());
			}
		});
	}
	
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
			params.putString("message", "challenge");

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
	
	private void rollback(int type){
		switch (type) {
		case 1:
			collabParse.deleteEventually();
			MainActivity.collaborationDao.delete(collab);

			climbing.setGame_mode(0);
			MainActivity.climbingDao.update(climbing);
			updateClimbingInParse();
			
			mode = GameModeType.SOLO_CLIMB;
			gameMode.setText("Modalità: " + setModeText());
			break;

		case 2:
			competParse.deleteEventually();
			MainActivity.competitionDao.delete(compet);
			climbing.setGame_mode(0);
			MainActivity.climbingDao.update(climbing);
			updateClimbingInParse();
			
			graphicsRollBack(type);

			mode = GameModeType.SOLO_CLIMB;
			gameMode.setText("Modalità: " + setModeText());
			break;
		}
	}
	
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
			
		}
	}

	@Override
	public boolean convert(View convertCardView) {
		// TODO Auto-generated method stub
		return false;
	}
}
