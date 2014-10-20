package org.unipd.nbeghin.climbtheworld.fragments;

import org.unipd.nbeghin.climbtheworld.ClimbApplication;
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
import android.widget.TextView;

import com.fima.cardsui.objects.Card;
import com.fima.cardsui.objects.Card.OnCardSwiped;
import com.fima.cardsui.views.CardUI;

public class NotificationFragment extends Fragment{
	static public CardUI	notificationCards;
	static TextView empty;
	
	private class LoadNotificationTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... unused) {
			refresh();
			return null;
		}
	}
	
	static public void refresh() {
		if(ClimbApplication.notifications.isEmpty()){
			empty.setVisibility(View.VISIBLE);
			notificationCards.setVisibility(View.GONE);
		}else{
			empty.setVisibility(View.GONE);
			notificationCards.setVisibility(View.VISIBLE);
		
		notificationCards.clearCards();
		for (final Notification notification : ClimbApplication.notifications) {
			if(!notification.isRead()){
				NotificationCard notificationCard;
				if(ClimbApplication.notifications.indexOf(notification) == 0)
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
				ClimbApplication.notifications.remove(notification);
			}
		}
		notificationCards.refresh();
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View result = inflater.inflate(R.layout.fragment_notification, container, false);
		empty = (TextView) result.findViewById(R.id.empty);
		notificationCards = (CardUI) result.findViewById(R.id.cardsNotification);
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
