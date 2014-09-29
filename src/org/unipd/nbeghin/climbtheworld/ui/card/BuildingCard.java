package org.unipd.nbeghin.climbtheworld.ui.card;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.unipd.nbeghin.climbtheworld.MainActivity;
import org.unipd.nbeghin.climbtheworld.R;
import org.unipd.nbeghin.climbtheworld.models.Building;
import org.unipd.nbeghin.climbtheworld.models.Climbing;
import org.unipd.nbeghin.climbtheworld.models.GameModeType;
import org.unipd.nbeghin.climbtheworld.util.FacebookUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

	public BuildingCard(Building building) {
		super(building.getName());
		this.building = building;
	}

	private String setModeText(){
		if(climbing != null){ //a climb has began
		switch (GameModeType.values()[climbing.getGame_mode()]) {
		case SOCIAL_CHALLENGE:
			return "Social Challenge";
		case SOCIAL_CLIMB:
			return "Social Climb";
		case SOLO_CLIMB:
			return "Solo Climb";
		case TEAM_VS_TEAM:
			return "Team vs Team";
		default:
				return "Solo Climb";
					
		}
		}else return "Solo Climb";
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
		SharedPreferences pref = MainActivity.getContext().getSharedPreferences("UserSession", 0);
		climbing = MainActivity.getClimbingForBuildingAndUser(building.get_id(), pref.getInt("local_id", -1));
		gameMode.setText("Modalitˆ: " + setModeText());
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
			public void onClick(View arg0) {
				climbing.setGame_mode(2);
				MainActivity.climbingDao.update(climbing);
				updateClimbingInParse();
				gameMode.setText("Modalitˆ: " + setModeText());
				
			}
		});
		
		Button socialChallengeButton = ((Button) view.findViewById(R.id.socialChallengeButton));
		socialChallengeButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				climbing.setGame_mode(3);
				MainActivity.climbingDao.update(climbing);
				updateClimbingInParse();
				gameMode.setText("Modalitˆ: " + setModeText());
				
			}
		});
		
		Button teamVsTeamButton = ((Button) view.findViewById(R.id.teamVsTeamButton));
		teamVsTeamButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				climbing.setGame_mode(4);
				MainActivity.climbingDao.update(climbing);
				updateClimbingInParse();
				gameMode.setText("Modalitˆ: " + setModeText());
				
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

	@Override
	public boolean convert(View convertCardView) {
		// TODO Auto-generated method stub
		return false;
	}
}
