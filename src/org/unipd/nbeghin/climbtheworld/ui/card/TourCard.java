package org.unipd.nbeghin.climbtheworld.ui.card;

import java.util.List;

import org.unipd.nbeghin.climbtheworld.ClimbApplication;
import org.unipd.nbeghin.climbtheworld.MainActivity;
import org.unipd.nbeghin.climbtheworld.R;
import org.unipd.nbeghin.climbtheworld.models.Tour;
import org.unipd.nbeghin.climbtheworld.models.TourText;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fima.cardsui.objects.Card;

/**
 * CardsUI card for a single tour
 *
 */
public class TourCard extends Card {
	final Tour	tour;
	TourText tourText;

	public TourCard(TourText tour) {
		super(tour.getTour().getTitle());
		this.tourText = tour;
		this.tour = tour.getTour();
	}

	@Override
	public View getCardContent(Context context) {
		View view = LayoutInflater.from(context).inflate(R.layout.card_tour_ex, null);
		((TextView) view.findViewById(R.id.title)).setText(tourText.getTitle());
		((TextView) view.findViewById(R.id.numBuildingForTour)).setText(ClimbApplication.getContext().getString(R.string.num_buildings,tour.getNum_buildings()));
		((TextView) view.findViewById(R.id.description)).setText(tourText.getDescription());
		LinearLayout layout=(LinearLayout) view.findViewById(R.id.buildingsForTourPhotoList);
		List<Integer> images=ClimbApplication.getBuildingPhotosForTour(tour.get_id());
		for(int image: images) {
			ImageView imageView=new ImageView(context);
			imageView.setImageResource(image);
			imageView.setAdjustViewBounds(true);
			imageView.setPadding(0, 0, 7, 0);
			imageView.setScaleType(ScaleType.FIT_START);
			layout.addView(imageView);
		}
		layout.refreshDrawableState();
		return view;
	}

	@Override
	public boolean convert(View convertCardView) {
		// TODO Auto-generated method stub
		return false;
	}
}
