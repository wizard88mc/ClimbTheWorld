package org.unipd.nbeghin.climbtheworld.fragments;

import org.unipd.nbeghin.climbtheworld.ClimbActivity;
import org.unipd.nbeghin.climbtheworld.MainActivity;
import org.unipd.nbeghin.climbtheworld.R;
import org.unipd.nbeghin.climbtheworld.models.Building;
import org.unipd.nbeghin.climbtheworld.ui.card.BuildingCard;

import com.fima.cardsui.views.CardUI;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;

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
			BuildingCard buildingCard = new BuildingCard(building);
			buildingCard.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Log.i(MainActivity.AppName, "Building id clicked: "+building.get_id());
					Intent intent = new Intent(getActivity().getApplicationContext(), ClimbActivity.class);
					intent.putExtra(MainActivity.building_intent_object, building.get_id());
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
		refresh();
		return (result);
	}

	@Override
	public void onResume() {
		super.onResume();
		refresh();
	}
}
