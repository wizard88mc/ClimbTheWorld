package org.unipd.nbeghin.climbtheworld;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.unipd.nbeghin.climbtheworld.models.Building;
import org.unipd.nbeghin.climbtheworld.models.Group;
import org.unipd.nbeghin.climbtheworld.models.TeamDuel;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class TeamPreparationActivity extends ActionBarActivity {

	// Our created menu to use
	private Menu mymenu;

	ImageButton addMyMembersBtn;
	ImageButton addChallengerBtn;
	ImageButton startPlay;
	ImageButton addChallengerTeamBtn;
	List<TextView> myMembers = new ArrayList<TextView>();
	List<TextView> theirMembers = new ArrayList<TextView>();
	TextView challengerName;
	TextView creatorName;

	TeamDuel duel;

	int building_id = 0;
	int team_online_id = 0;
	Building building;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_preparation_teams);
		SharedPreferences pref = getSharedPreferences("UserSession", 0);
		building_id = getIntent().getIntExtra(ClimbApplication.building_intent_object, 0);
		building = ClimbApplication.getBuildingById(building_id);
		addMyMembersBtn = (ImageButton) findViewById(R.id.addMyTeamBtn);
		addChallengerBtn = (ImageButton) findViewById(R.id.addChallengerBtn);
		startPlay = (ImageButton) findViewById(R.id.btnStartClimbing);
		addChallengerTeamBtn = (ImageButton) findViewById(R.id.addChallengerTeam);
		challengerName = (TextView) findViewById(R.id.textChallenger);
		creatorName = (TextView) findViewById(R.id.textCreator);
		
		startPlay.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				onStartClimbingBtn();
			}
		});
		
		addChallengerBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onAddChallengerBtn();
			}
		});
		
		addMyMembersBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onAddMyMembersBtn();
			}
		});
		
		addChallengerTeamBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				onAddChallengerMemberBtn();
			}
		});
		
		addMyMembersBtn.setEnabled(false);
		addChallengerBtn.setEnabled(false);
		startPlay.setEnabled(false);

		for (int i = 0; i < ClimbApplication.N_MEMBERS_PER_GROUP - 1; i++) {
			int id = getResources().getIdentifier("textMyMember" + (i + 1), "id", getPackageName());
			myMembers.add((TextView) findViewById(id));
		}
		for (int i = 0; i < ClimbApplication.N_MEMBERS_PER_GROUP - 1; i++) {
			int id = getResources().getIdentifier("textOtherMember" + (i + 1), "id", getPackageName());
			theirMembers.add((TextView) findViewById(id));
		}

		//int team_online_id = getIntent().getIntExtra(ClimbApplication.duel_intent_object, 0);

		duel = ClimbApplication.getTeamDuelByBuildingAndUser(building_id, pref.getInt("local_id", -1));
		// quando arrivo qui, id online di duel deve essere settato
		
		if(duel.isCreator()){
			addChallengerBtn.setVisibility(View.VISIBLE);
			addChallengerTeamBtn.setVisibility(View.GONE);
			addMyMembersBtn.setVisibility(View.VISIBLE);
		}else if(duel.isChallenger()){
			addChallengerBtn.setVisibility(View.GONE);
			addChallengerTeamBtn.setVisibility(View.VISIBLE);
			addMyMembersBtn.setVisibility(View.GONE);
		}else if(!duel.isCreator() && !duel.isChallenger()){
			addChallengerBtn.setVisibility(View.GONE);
			addChallengerTeamBtn.setVisibility(View.GONE);
			addMyMembersBtn.setVisibility(View.GONE);
		}
		
		getTeams(false);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.team_preparation, menu);

		// We should save our menu so we can use it to reset our updater.
		mymenu = menu;

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.itemUpdate:
			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			ImageView iv = (ImageView) inflater.inflate(R.layout.refresh, null);
			Animation rotation = AnimationUtils.loadAnimation(this, R.anim.rotate_refresh);
			rotation.setRepeatCount(Animation.INFINITE);
			iv.startAnimation(rotation);
			MenuItemCompat.setActionView(item, iv);
			onUpdate();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void resetUpdating() {
		// Get our refresh item from the menu
		MenuItem m = mymenu.findItem(R.id.itemUpdate);
		if (MenuItemCompat.getActionView(m) != null) {
			// Remove the animation.
			MenuItemCompat.getActionView(m).clearAnimation();
			MenuItemCompat.setActionView(m, null);
		}
	}

	/**
	 * Get the team from Parse and shows its data in current activity
	 * @param isUpdate
	 */
	private void getTeams(final boolean isUpdate) {
		ParseQuery<ParseObject> query = ParseQuery.getQuery("TeamDuel");
		query.getInBackground(duel.getId_online(), new GetCallback<ParseObject>() {

			@Override
			public void done(ParseObject obj, ParseException e) {
				if (e == null) {
					try {
						JSONObject challenger = obj.getJSONObject("challenger");
						JSONObject creator = obj.getJSONObject("creator");
						if (duel.getChallenger_name() == null || duel.getChallenger_name().equals("")) {
								Iterator<String> it = challenger.keys();
								if(it.hasNext()){
									duel.setChallenger_name(challenger.getString(it.next()));
									addChallengerBtn.setEnabled(false);
								}else{
									addChallengerBtn.setEnabled(true);
								}
						}else{
							addChallengerBtn.setEnabled(false);

						}
						if (duel.getCreator_name() == null || duel.getCreator_name().equals("")) {
								Iterator<String> it = creator.keys();
								if(it.hasNext()){
									duel.setCreator_name(creator.getString(it.next()));	
								}
								
						}
						
						showTeams(obj, isUpdate);
					} catch (JSONException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

				} else {
					Toast.makeText(getApplicationContext(), "Connection problems. Unable to update", Toast.LENGTH_SHORT).show();
					Log.e("onUpdate", e.getMessage());
				}

			}
		});
	}
	
	/**
	 * Shows teams to user
	 * @param duelParse
	 * @param isUpdate
	 * @throws JSONException
	 */
	private void showTeams(ParseObject duelParse, boolean isUpdate) throws JSONException{
		if(duel.getCreator_name() != null && !duel.getCreator_name().equals(""))
			creatorName.setText("Creator: " + duel.getCreator_name());
		else
			creatorName.setText("Creator: - ");
		if ((duel.getChallenger_name() != null && !duel.getChallenger_name().equals("")) )
			challengerName.setText("Challenger: " + duel.getChallenger_name());
		else
			challengerName.setText("Challenger: - ");
		if(duel.isCreator()){
			if(duel.getChallenger_name() != null && !duel.getChallenger_name().equals(""))
			addChallengerBtn.setEnabled(false);
		else
			addChallengerBtn.setEnabled(true);
		}
		JSONObject myTeam;
		JSONObject challengerTeam;
//		if(duel.getMygroup() == Group.CREATOR){			
			myTeam = duelParse.getJSONObject("creator_team");
			challengerTeam = duelParse.getJSONObject("challenger_team");
//		}else{
//			challengerTeam = duelParse.getJSONObject("creator_team");
//			myTeam = duelParse.getJSONObject("challenger_team");
//		}
		
		showTeam(myTeam, myMembers);
		showTeam(challengerTeam, theirMembers);
		if(duel.isCreator()){
			if(myTeam.length() < 5)
				addMyMembersBtn.setEnabled(true);
			else
				addMyMembersBtn.setEnabled(false);
		}
		if(myTeam.length() == 5 && challengerTeam.length() == 5){
			startPlay.setEnabled(true);
			duel.setReadyToPlay(true);
			ClimbApplication.teamDuelDao.update(duel);
		}	
		else{
			startPlay.setEnabled(false);
			duel.setReadyToPlay(false);
			ClimbApplication.teamDuelDao.update(duel);
		}
		
		if(isUpdate)
			// Change the menu back
			resetUpdating();
		
	}
	
	private void showTeam(JSONObject team, List<TextView> textTeam) throws JSONException{
		Iterator<String> it1 = team.keys();
		int i = 0;
		while(it1.hasNext()){
			textTeam.get(i).setText(team.getString(it1.next()));
			i++;
		}
		for(; i < textTeam.size(); i++){
			textTeam.get(i).setText("   -");
		}
	}

	
	public void onUpdate() {
		getTeams(true);
	}

	/**
	 * Opens ClimbActivity to start the game
	 */
	public void onStartClimbingBtn() { // passo anche team id????
		Log.i("Team Preparation", "Building id clicked: " + String.valueOf(building_id));
		Intent intent = new Intent(getApplicationContext(), ClimbActivity.class);
		intent.putExtra(ClimbApplication.building_intent_object, building_id);
		finish();
		startActivity(intent);
	}
	
	/**
	 * Open a dialog to let user choose the friend to challenge
	 */
	public void onAddChallengerBtn(){
		Bundle params = new Bundle();
		params.putString("data", "{\"idCollab\":\"" + duel.getId_online() + "\","
				+ "" + "\"idBuilding\":\"" + building_id + "\","
				+ "\"nameBuilding\":\"" + building.getName() + "\","
				+ " \"type\": \"3\","
				+ "\"challenger\": \""+  true + "\","	//you will be my challenger
				+ "\"isSenderCreator\": \"" + duel.isCreator() + "\"" 
				+ "}");
		params.putString("message", "team challenge");


	sendRequests(params);
	}
	
	/**
	 * Open a dialog to let user choose the friends to invite as member of its team.
	 */
	public void onAddMyMembersBtn(){
		
			Bundle params = new Bundle();
				params.putString("data", "{\"idCollab\":\"" + duel.getId_online() + "\","
						+ "" + "\"idBuilding\":\"" + building_id + "\","
						+ "\"nameBuilding\":\"" + building.getName() + "\","
						+ " \"type\": \"3\","
						+ "\"challenger\": \""+  false + "\","	//you will not be my challenger
						+ "\"isSenderCreator\": \"" + duel.isCreator() + "\"" //enter in my team
						+ "}");
				params.putString("message", "team challenge");


			sendRequests(params);
		
	}
	
	public void onAddChallengerMemberBtn(){
		Bundle params = new Bundle();
		params.putString("data", "{\"idCollab\":\"" + duel.getId_online() + "\","
				+ "" + "\"idBuilding\":\"" + building_id + "\","
				+ "\"nameBuilding\":\"" + building.getName() + "\","
				+ " \"type\": \"3\","
				+ "\"challenger\": \""+  false + "\","	//you will not be my challenger
				+ "\"isSenderCreator\": \"" + duel.isCreator() + "\"" //enter in my team
				+ "}");
		params.putString("message", "team challenge");


	sendRequests(params);
	}

	/**
	 * Opens a dialog to let user send facebook requests to his friends, given parameters
	 * @param params the given parameters
	 */
	private void sendRequests(Bundle params){
		if(Session.getActiveSession() != null && Session.getActiveSession().isOpened()){

		WebDialog requestsDialog = (new WebDialog.RequestsDialogBuilder(TeamPreparationActivity.this, Session.getActiveSession(), params)).setOnCompleteListener(new OnCompleteListener() {

			@Override
			public void onComplete(Bundle values, FacebookException error) {
				if (error != null) {
					if (error instanceof FacebookOperationCanceledException) {
						Toast.makeText(getApplicationContext(), "Request cancelled", Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(getApplicationContext(), "Network Error", Toast.LENGTH_SHORT).show();
					}
				} else {
					final String requestId = values.getString("request");
					if (requestId != null) {
						Toast.makeText(getApplicationContext(), "Request sent", Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(getApplicationContext(), "Request cancelled", Toast.LENGTH_SHORT).show();
					}
				}
			}

		}).build();
		requestsDialog.show();
		}else{
			Toast.makeText(getApplicationContext(), "Currently not logged to FB", Toast.LENGTH_SHORT).show();
		}
	}
}
