package org.unipd.nbeghin.climbtheworld.ui.card;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.unipd.nbeghin.climbtheworld.MainActivity;
import org.unipd.nbeghin.climbtheworld.R;
import org.unipd.nbeghin.climbtheworld.models.Building;
import org.unipd.nbeghin.climbtheworld.models.Climbing;
import org.unipd.nbeghin.climbtheworld.models.GameModeType;
import org.w3c.dom.Text;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.fima.cardsui.objects.Card;

/**
 * CardsUI card for a single building
 *
 */
public class BuildingCard extends Card {
	final Building	building;

	public BuildingCard(Building building) {
		super(building.getName());
		this.building = building;
	}

	private String setModeText(){
		switch (GameModeType.values()[building.getGame_mode()]) {
		case SOCIAL_CHALLENGE:
			return "Social Challenge";
		case SOCIAL_CLIMB:
			return "Social Climb";
		case SOLO_CLIMB:
			return "Solo Climb";
		case TEAM_VS_TEAM:
			return "Team vs Team";
		default:
				return "";
					
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
		gameMode.setText("Modalitˆ: " + setModeText());
		TextView climbingStatus = (TextView) view.findViewById(R.id.climbingStatus);
		SharedPreferences pref = MainActivity.getContext().getSharedPreferences("UserSession", 0);
		Climbing climbing = MainActivity.getClimbingForBuildingAndUser(building.get_id(), pref.getInt("local_id", -1));
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
				building.setGame_mode(1);
				MainActivity.buildingDao.update(building);
				gameMode.setText("Modalitˆ: " + setModeText());
				
			}
		});
		
		Button socialChallengeButton = ((Button) view.findViewById(R.id.socialChallengeButton));
		socialChallengeButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				building.setGame_mode(2);
				MainActivity.buildingDao.update(building);
				gameMode.setText("Modalitˆ: " + setModeText());
				
			}
		});
		
		Button teamVsTeamButton = ((Button) view.findViewById(R.id.teamVsTeamButton));
		teamVsTeamButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				building.setGame_mode(3);
				MainActivity.buildingDao.update(building);
				gameMode.setText("Modalitˆ: " + setModeText());
				
			}
		});
		return view;
	}

	@Override
	public boolean convert(View convertCardView) {
		// TODO Auto-generated method stub
		return false;
	}
}
