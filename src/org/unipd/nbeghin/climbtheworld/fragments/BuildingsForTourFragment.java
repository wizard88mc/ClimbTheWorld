package org.unipd.nbeghin.climbtheworld.fragments;

import java.util.List;

import org.unipd.nbeghin.climbtheworld.ClimbActivity;
import org.unipd.nbeghin.climbtheworld.ClimbApplication;
import org.unipd.nbeghin.climbtheworld.R;
import org.unipd.nbeghin.climbtheworld.models.Building;
import org.unipd.nbeghin.climbtheworld.models.BuildingText;
import org.unipd.nbeghin.climbtheworld.ui.card.BuildingForTourCard;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.fima.cardsui.views.CardUI;

/**
 * Show a list of buildings for a given tour
 *
 */
public class BuildingsForTourFragment extends Fragment {
	public static final String	building_text_intent_object	= "org.unipd.nbeghin.climbtheworld.intents.object.buildingText";
	public CardUI				buildingCards;

	public void loadBuildings(List<Building> buildings) {
		buildingCards.clearCards();
		int i=0;
		for (final Building building : buildings) {
			i++;
			BuildingForTourCard buildingCard = new BuildingForTourCard(building, i);
			buildingCard.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) { // start climbing for a given building
					BuildingText 	buildingText = ClimbApplication.getBuildingTextByBuilding(building.get_id());
					Intent intent = new Intent(getActivity().getApplicationContext(), ClimbActivity.class);
					intent.putExtra(ClimbApplication.counter_mode, false);
					intent.putExtra(building_text_intent_object, buildingText.get_id());
					startActivity(intent);
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
		return (result);
	}
}
