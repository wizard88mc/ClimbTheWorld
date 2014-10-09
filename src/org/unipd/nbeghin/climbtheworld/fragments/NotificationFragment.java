package org.unipd.nbeghin.climbtheworld.fragments;

import org.unipd.nbeghin.climbtheworld.MainActivity;
import org.unipd.nbeghin.climbtheworld.R;
import org.unipd.nbeghin.climbtheworld.models.Notification;
import org.unipd.nbeghin.climbtheworld.ui.card.NotificationCard;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fima.cardsui.objects.Card;
import com.fima.cardsui.objects.Card.OnCardSwiped;
import com.fima.cardsui.views.CardUI;

public class NotificationFragment extends Fragment{
	static public CardUI	notificationCards;
	
	private class LoadNotificationTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... unused) {
			refresh();
			return null;
		}
	}
	
	static public void refresh() {
		notificationCards.clearCards();
		for (final Notification notification : MainActivity.notifications) {
			if(!notification.isRead()){
				NotificationCard notificationCard;
				if(MainActivity.notifications.indexOf(notification) == 0)
					notificationCard = new NotificationCard(notification, true);
				else
					notificationCard = new NotificationCard(notification, false);

					
			notificationCard.setOnCardSwipedListener(new OnCardSwiped() {
				
				@Override
				public void onCardSwiped(Card card, View layout) {
					System.out.println("swiiiiipe");					
				}
			});
			
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
