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
import org.unipd.nbeghin.climbtheworld.models.GameModeType;

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



/**
 * CardsUI card for a single building
 *
 */
public class BuildingCard extends Card {
	final Building	building;
	Climbing climbing;
	final SharedPreferences pref = MainActivity.getContext().getSharedPreferences("UserSession", 0);
	GameModeType mode;

	public BuildingCard(Building building) {
		super(building.getName());
		this.building = building;
	}

	private String setModeText(){
		if(climbing != null){ //a climb has began
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
		}else{
			mode = GameModeType.SOLO_CLIMB;
			return "Solo Climb";
		}
	}
	
	@Override
	public View getCardContent(Context context) {
		View view = LayoutInflater.from(context).inflate(R.layout.card_building_ex, null);
		final TextView gameMode = ((TextView) view.findViewById(R.id.textModalita));
		((TextView) view.findViewById(R.id.title)).setText(building.getName());
		int imageId = MainActivity.getBuildingImageResource(building);
		if (imageId > 0) ((ImageView) view.findViewById(R.id.photo)).setImageResource(imageId);
		((TextView) view.findViewById(R.id.buildingStat)).setText(building.getSteps() + " steps (" + building.getHeight() + "m)");
		((TextView) view.findViewById(R.id.location)).setText(building.getLocation());
		((TextView) view.findViewById(R.id.description)).setText(building.getDescription());
		TextView climbingStatus = (TextView) view.findViewById(R.id.climbingStatus);
		//final SharedPreferences pref = MainActivity.getContext().getSharedPreferences("UserSession", 0);
		climbing = MainActivity.getClimbingForBuildingAndUser(building.get_id(), pref.getInt("local_id", -1));
		gameMode.setText("Modalità: " + setModeText());
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy.MM.dd");
		if (climbing != null) {
			if (climbing.getPercentage() >= 100) {
				climbingStatus.setText("Climbing: COMPLETED! (on "+sdf.format(new Date(climbing.getModified()))+")");
			} else {
				climbingStatus.setText("Climbing status: " + new DecimalFormat("#").format(climbing.getPercentage()*100) + "% (last attempt @ "+sdf.format(new Date(climbing.getModified()))+")");
			}
		} else {
			climbingStatus.setText("Not climbed yet");
		}
		
		Button socialClimbButton = ((Button) view.findViewById(R.id.socialClimbButton));
		socialClimbButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//crea collaborazione
				//crea climbing se non c'è gia e cambia modalita
				//invia richieste
			switch (mode) {
			case SOLO_CLIMB:
				if(climbing == null){
					//crea nuovo
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
				}else{
					//aggiorna esistente
					climbing.setGame_mode(1);
					MainActivity.climbingDao.update(climbing);
					updateClimbingInParse();
				}
				
				String idCollab = saveCollaboration();
				
				
				
				gameMode.setText("Modalità: " + setModeText());
				((Button) v.findViewById(R.id.socialClimbButton)).setText("Back to Solo Climb");
				((Button) v.findViewById(R.id.socialChallengeButton)).setEnabled(false);
				((Button) v.findViewById(R.id.teamVsTeamButton)).setEnabled(false);
				
				mode = GameModeType.SOCIAL_CLIMB;
				
				sendRequest(GameModeType.SOCIAL_CLIMB, idCollab);
				break;

			case SOCIAL_CLIMB:
				climbing.setGame_mode(0);
				MainActivity.climbingDao.update(climbing);
				updateClimbingInParse();
				leaveCollaboration();
				break;
			}	
				
				
			}
		});
		
		Button socialChallengeButton = ((Button) view.findViewById(R.id.socialChallengeButton));
		socialChallengeButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				climbing.setGame_mode(3);
				MainActivity.climbingDao.update(climbing);
				updateClimbingInParse();
				gameMode.setText("Modalità: " + setModeText());
				sendRequest(GameModeType.SOCIAL_CHALLENGE, "");

				
			}
		});
		
		Button teamVsTeamButton = ((Button) view.findViewById(R.id.teamVsTeamButton));
		teamVsTeamButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				climbing.setGame_mode(4);
				MainActivity.climbingDao.update(climbing);
				updateClimbingInParse();
				gameMode.setText("Modalità: " + setModeText());
				sendRequest(GameModeType.TEAM_VS_TEAM, "");
				
				
			}
		});
		return view;
	}
	
	private void updateClimbingInParse(){
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Climbing");
		query.whereEqualTo("building", climbing.getBuilding().get_id());
		query.whereEqualTo("users_id", climbing.getUser().getFBid());
		query.findInBackground(new FindCallback<ParseObject>() {
			
			@Override
			public void done(List<ParseObject> climbings, ParseException e) {
				if(e == null){
					ParseObject c = climbings.get(0);
					c.put("game_mode", climbing.getGame_mode());
					c.saveEventually();
				}else{
					Toast.makeText(MainActivity.getContext(), "Connection Problems", Toast.LENGTH_SHORT).show();
					Log.e("updateClimbingInParse", e.getMessage());
				}
			}
		});
	}
	
	private void saveClimbingInParse(){
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
	
	private String saveCollaboration(){
		JSONObject stairs = new JSONObject();
		JSONObject collaborators = new JSONObject();
		
		try {
			collaborators.put(pref.getString("FBid", ""), pref.getString("username", ""));
			stairs.put(pref.getString("FBid", ""), 0);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ParseObject collabParse = new ParseObject("Climbing");
		collabParse.put("building", building.get_id());
		collabParse.put("stairs", stairs);
		collabParse.put("collaborators", collaborators);
		collabParse.saveEventually();
		
		Collaboration collab = new Collaboration();
		collab.setBuilding(building);
		collab.setId(collabParse.getObjectId());
		collab.setMy_stairs(0);
		collab.setOthers_stairs(0);
		MainActivity.collaborationDao.create(collab);
		return collab.getId();
		
	}
	
	private void leaveCollaboration(){
		Collaboration collab = MainActivity.getCollaborationByBuilding(building.get_id());
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Collaboration");
		query.whereEqualTo("objectId", collab.getId());
		query.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> collabs, ParseException e) {
					if(e == null){
						if(collabs.size() == 0){
							Toast.makeText(MainActivity.getContext(), "This collaboration does not exists anymore", Toast.LENGTH_SHORT).show();
						}else{
							ParseObject c = collabs.get(0);
							JSONObject collaborators = c.getJSONObject("collaborators");
							JSONObject stairs = c.getJSONObject("stairs");
							collaborators.remove(pref.getString("FBid", ""));
							stairs.remove(pref.getString("FBid", ""));
							c.put("collaborators", collaborators);
							c.put("stairs", stairs);
							c.saveEventually();
						}
					}else{
						Toast.makeText(MainActivity.getContext(), "Connection Problems", Toast.LENGTH_SHORT).show();
						Log.e("updateClimbingInParse", e.getMessage());
					}
			}
		});
	}
	
	private void sendRequest(GameModeType mode, String idCollab){
		    Bundle params = new Bundle();
		    
		    switch (mode) {
			case SOCIAL_CLIMB:
				params.putString("data",
			            "{\"idCollab\":\""+ idCollab + "\"," +
			            "\"idBuilding\":\"" + building.get_id() + "\"}");
			    params.putString("message", "Please, help me!!!!");
				break;
			case SOCIAL_CHALLENGE:
			    params.putString("message", "challenge");

				break;
			case TEAM_VS_TEAM:
			    params.putString("message", "challenge");

				break;
			}
		    

		    WebDialog requestsDialog = (
		        new WebDialog.RequestsDialogBuilder(MainActivity.getContext(),
		            Session.getActiveSession(),
		            params))
		            .setOnCompleteListener(new OnCompleteListener() {

		                @Override
		                public void onComplete(Bundle values,
		                    FacebookException error) {
		                    if (error != null) {
		                        if (error instanceof FacebookOperationCanceledException) {
		                            Toast.makeText(MainActivity.getContext().getApplicationContext(), 
		                                "Request cancelled", 
		                                Toast.LENGTH_SHORT).show();
		                        } else {
		                            Toast.makeText(MainActivity.getContext().getApplicationContext(), 
		                                "Network Error", 
		                                Toast.LENGTH_SHORT).show();
		                        }
		                    } else {
		                        final String requestId = values.getString("request");
		                        if (requestId != null) {
		                            Toast.makeText(MainActivity.getContext().getApplicationContext(), 
		                                "Request sent",  
		                                Toast.LENGTH_SHORT).show();
		                        } else {
		                            Toast.makeText(MainActivity.getContext().getApplicationContext(), 
		                                "Request cancelled", 
		                                Toast.LENGTH_SHORT).show();
		                        }
		                    }   
		                }

		            })
		            .build();
		    requestsDialog.show();
		}

	@Override
	public boolean convert(View convertCardView) {
		// TODO Auto-generated method stub
		return false;
	}
}
