package org.unipd.nbeghin.climbtheworld.ui.card;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.unipd.nbeghin.climbtheworld.MainActivity;
import org.unipd.nbeghin.climbtheworld.R;
import org.unipd.nbeghin.climbtheworld.models.Building;
import org.unipd.nbeghin.climbtheworld.models.Climbing;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.fima.cardsui.objects.Card;

/**
 * CardsUI card for a single building associated to a tour
 *
 */
public class BuildingForTourCard extends Card {
	final private Building	building;
	final private int		order;

	public BuildingForTourCard(Building building, int order) {
		super(building.getName());
		this.building = building;
		this.order = order;
	}

	@Override
	public View getCardContent(Context context) {
		View view = LayoutInflater.from(context).inflate(R.layout.card_building_for_tour_ex, null);
		((TextView) view.findViewById(R.id.title)).setText(building.getName());
		int imageId = context.getResources().getIdentifier(building.getPhoto(), "drawable", context.getPackageName());
		if (imageId > 0) ((ImageView) view.findViewById(R.id.photo)).setImageResource(imageId);
		((TextView) view.findViewById(R.id.buildingStat)).setText(building.getSteps() + " steps (" + building.getHeight() + "m)");
		((TextView) view.findViewById(R.id.location)).setText(building.getLocation());
		((TextView) view.findViewById(R.id.description)).setText(building.getDescription());
		((TextView) view.findViewById(R.id.tourOrder)).setText(Integer.toString(order));
		TextView climbingStatus = (TextView) view.findViewById(R.id.climbingStatus);
		Climbing climbing = MainActivity.getClimbingForBuilding(building.get_id());
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy.MM.dd");
		if (climbing != null) {
			if (climbing.getPercentage() >= 100) {
				climbingStatus.setText("Climbing: COMPLETED! (on "+sdf.format(new Date(climbing.getModified()))+")");
			} else {
				climbingStatus.setText("Climbing status: " + new DecimalFormat("#.##").format(climbing.getPercentage()*100) + "%, (last climb on "+sdf.format(new Date(climbing.getModified()))+")");
			}
		} else {
			climbingStatus.setText("Not climbed yet");
		}
		return view;
	}
}
