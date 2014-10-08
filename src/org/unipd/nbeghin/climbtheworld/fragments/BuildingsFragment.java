package org.unipd.nbeghin.climbtheworld.fragments;

import java.util.List;

import org.unipd.nbeghin.climbtheworld.ClimbActivity;
import org.unipd.nbeghin.climbtheworld.MainActivity;
import org.unipd.nbeghin.climbtheworld.R;
import org.unipd.nbeghin.climbtheworld.TeamPreparationActivity;
import org.unipd.nbeghin.climbtheworld.models.Building;
import org.unipd.nbeghin.climbtheworld.models.Climbing;
import org.unipd.nbeghin.climbtheworld.ui.card.BuildingCard;

import com.fima.cardsui.views.CardUI;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Toast;

/**
 * Show a list of buildings
 *
 */
public class BuildingsFragment extends Fragment {
	public CardUI				buildingCards;

	private class LoadBuildingsTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... unused) {
			refresh();
			return null;
		}
	}

	public void refresh() {
		buildingCards.clearCards();
		for (final Building building : MainActivity.buildings) {
			BuildingCard buildingCard = new BuildingCard(building, getActivity());
			buildingCard.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					final SharedPreferences pref = MainActivity.getContext().getSharedPreferences("UserSession", 0);
					if(pref.getInt("local_id", -1) != -1){
						List<Climbing> climbs = MainActivity.getClimbingListForBuildingAndUser(building.get_id(), pref.getInt("local_id", -1));
						if((climbs.size() == 2 && (climbs.get(0).getGame_mode() == 3 || climbs.get(1).getGame_mode() == 3))
							|| (climbs.size() == 1 && climbs.get(0).getGame_mode() == 3)	){
							Climbing climb = climbs.get(0).getGame_mode() == 3 ? climbs.get(0) : climbs.get(1);
							if(climb.getId_mode() == null || climb.getId_mode().equals(""))
								Toast.makeText(getActivity(), "Connect to save data online before playing", Toast.LENGTH_SHORT).show();
							else{
								Log.i(MainActivity.AppName, "Building id clicked: "+building.get_id());
								Intent intent = new Intent(getActivity().getApplicationContext(), TeamPreparationActivity.class);
								intent.putExtra(MainActivity.building_intent_object, building.get_id());
								getActivity().startActivity(intent);
							}
						}else{
							Log.i(MainActivity.AppName, "Building id clicked: "+building.get_id());
							Intent intent = new Intent(getActivity().getApplicationContext(), ClimbActivity.class);
							intent.putExtra(MainActivity.building_intent_object, building.get_id());
							startActivity(intent);
						}
							
					}	
					else{
						Log.i(MainActivity.AppName, "Building id clicked: "+building.get_id());
						Intent intent = new Intent(getActivity().getApplicationContext(), ClimbActivity.class);
						intent.putExtra(MainActivity.building_intent_object, building.get_id());
						startActivity(intent);
					}
					
				}
			});
			buildingCards.addCard(buildingCard);
		}
		buildingCards.refresh();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View result = inflater.inflate(R.layout.fragment_buildings, container, false);
		buildingCards = (CardUI) result.findViewById(R.id.cardsBuildings);
		buildingCards.setSwipeable(false);
		refresh();
		return (result);
	}

	@Override
	public void onResume() {
		super.onResume();
		refresh();
	}
}
