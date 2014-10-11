package org.unipd.nbeghin.climbtheworld.ui.card;

import org.unipd.nbeghin.climbtheworld.R;
import org.unipd.nbeghin.climbtheworld.models.UserBadge;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.fima.cardsui.objects.Card;

public class BadgeCard extends Card{
	
	ImageView cup;
	TextView description;
	ProgressBar progressBar;
	UserBadge userBadge;
	
	public BadgeCard(UserBadge userBadge) {
		this.userBadge = userBadge;
	}

	@Override
	public View getCardContent(Context context) {
		View view = LayoutInflater.from(context).inflate(R.layout.card_building_ex, null);
		cup = (ImageView) view.findViewById(R.id.imageCup);
		description = (TextView) view.findViewById(R.id.textDescription);
		progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
		description.setText(userBadge.getDescription());
		if(userBadge.getPercentage() < 1.00){
			cup.setImageResource(R.drawable.lock_win);
		}else
			cup.setImageResource(R.drawable.unlock_win);
		progressBar.setIndeterminate(false);
		progressBar.setProgress((int)(userBadge.getPercentage() * 100));
		return view;
	}

	@Override
	public boolean convert(View convertCardView) {
		// TODO Auto-generated method stub
		return false;
	}

}
