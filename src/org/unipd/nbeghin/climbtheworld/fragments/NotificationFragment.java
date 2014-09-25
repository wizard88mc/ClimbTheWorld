package org.unipd.nbeghin.climbtheworld.fragments;

import org.unipd.nbeghin.climbtheworld.ClimbActivity;
import org.unipd.nbeghin.climbtheworld.MainActivity;
import org.unipd.nbeghin.climbtheworld.R;
import org.unipd.nbeghin.climbtheworld.models.Building;
import org.unipd.nbeghin.climbtheworld.models.Notification;
import org.unipd.nbeghin.climbtheworld.ui.card.BuildingCard;
import org.unipd.nbeghin.climbtheworld.ui.card.NotificationCard;

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

public class NotificationFragment extends Fragment{
	public CardUI	notificationCards;
	
	private class LoadNotificationTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... unused) {
			refresh();
			return null;
		}
	}
	
	public void refresh() {
		notificationCards.clearCards();
		for (final Notification notification : MainActivity.notifications) {
			if(!notification.isRead()){
			NotificationCard notificationCard = new NotificationCard(notification);
			/*notificationCard.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Log.i(MainActivity.AppName, "Building id clicked: "+building.get_id());
					Intent intent = new Intent(getActivity().getApplicationContext(), ClimbActivity.class);
					intent.putExtra(MainActivity.building_intent_object, building.get_id());
					startActivity(intent);
				}
			});*/
			notificationCards.addCard(notificationCard);
			}else{
				MainActivity.notifications.remove(notification);
			}
		}
		notificationCards.refresh();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View result = inflater.inflate(R.layout.fragment_buildings, container, false);
		notificationCards = (CardUI) result.findViewById(R.id.cardsBuildings);
		notificationCards.setSwipeable(true);
		refresh();
		return (result);
	}

	@Override
	public void onResume() {
		super.onResume();
		refresh();
	}
}
