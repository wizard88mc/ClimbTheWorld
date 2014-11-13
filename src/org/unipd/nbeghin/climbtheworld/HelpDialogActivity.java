package org.unipd.nbeghin.climbtheworld;

import org.unipd.nbeghin.climbtheworld.R;
import org.unipd.nbeghin.climbtheworld.models.GameModeType;

import android.app.Dialog;
import android.content.Context;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class HelpDialogActivity extends Dialog {

	
	public HelpDialogActivity(Context context, int theme, GameModeType mode, double percentage, boolean new_steps, int n_icons, boolean samplingEnabled) {
		super(context, theme);
		// the content
				//final RelativeLayout root = new RelativeLayout(getContext());

				// creating the fullscreen dialog
				requestWindowFeature(Window.FEATURE_NO_TITLE);
				setContentView(R.layout.dialog_climb_help);
				RelativeLayout root = (RelativeLayout) findViewById(R.id.main_layout);
				//root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

				//getWindow().setBackgroundDrawable(new ColorDrawable(Color.YELLOW));
				getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
				root.setOnTouchListener(new View.OnTouchListener() {
					public boolean onTouch(View v, MotionEvent event) {
							dismiss();
							return true;
						
					}
				});
				
				ImageView imageShare =(ImageView) findViewById(R.id.imageShareHelp);
				ImageView imageGallery = (ImageView) findViewById(R.id.imageGallery);
				ImageView imageUpdate = (ImageView) findViewById(R.id.imageUpdate);
				ImageView imageMicrogoal = (ImageView) findViewById(R.id.imageMicrogoal);
				TextView textShare = ((TextView) findViewById(R.id.textShare));
				TextView textGallery = ((TextView) findViewById(R.id.textGallery));
				TextView textUpdate = ((TextView) findViewById(R.id.textUpdate));
				TextView textMicrogoal = ((TextView) findViewById(R.id.textMicrogoal));
				
				if(n_icons == 2){
					setMargins(imageMicrogoal, 30);
				}else{
					setMargins(imageMicrogoal, 150);
				}

				System.out.println("n_icons " + n_icons);
				if(percentage >= 1.00){
					//end climb
					imageShare.setVisibility(View.VISIBLE);
					textShare.setText(ClimbApplication.getContext().getString(R.string.share_victory_help));
					
					imageGallery.setVisibility(View.VISIBLE);
					textGallery.setText(ClimbApplication.getContext().getString(R.string.gallery_help));
					
					imageMicrogoal.setVisibility(View.INVISIBLE);
					textMicrogoal.setVisibility(View.INVISIBLE);
					
					imageUpdate.setVisibility(View.INVISIBLE);
					textUpdate.setVisibility(View.INVISIBLE);
				}else{
					imageShare.setVisibility(View.VISIBLE);
					imageMicrogoal.setVisibility(View.VISIBLE);
					textShare.setVisibility(View.VISIBLE);
					textMicrogoal.setVisibility(View.VISIBLE);
					textMicrogoal.setText(ClimbApplication.getContext().getString(R.string.microgoal_help));
					if(!samplingEnabled)
						textShare.setText(ClimbApplication.getContext().getString(R.string.play_help));
					else
						textShare.setText(ClimbApplication.getContext().getString(R.string.pause_help));

					if(!new_steps){ //play then pause and no steps done
						imageGallery.setVisibility(View.INVISIBLE);
						textGallery.setVisibility(View.INVISIBLE);
					}else{
						textGallery.setText(ClimbApplication.getContext().getString(R.string.share_progress_help));
						textGallery.setVisibility(View.VISIBLE);
						imageGallery.setVisibility(View.VISIBLE);
					}
					
						
					if(mode != GameModeType.SOLO_CLIMB){
						textUpdate.setText(ClimbApplication.getContext().getString(R.string.update_help));
						imageUpdate.setVisibility(View.VISIBLE);
						textUpdate.setVisibility(View.VISIBLE);
					}else{
						textUpdate.setVisibility(View.INVISIBLE);
						imageUpdate.setVisibility(View.INVISIBLE);
					}
				}
	}

	

	@Override
	public boolean onTouchEvent(MotionEvent event) {
			return true;
	
	}
	
	public static void setMargins (View v, int r) {
	    if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
	        ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
	        p.setMargins(p.leftMargin, p.topMargin, r, p.bottomMargin);
	        v.requestLayout();
	    }
	}

}



class MyGestureDetector extends SimpleOnGestureListener {
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

		//System.out.println("Help im being touched!");
		return false;
	}
}

// public class ClimbHelpActivity extends Activity{
//
// @Override
// protected void onCreate(Bundle savedInstanceState) {
// super.onCreate(savedInstanceState);
// setContentView(R.layout.activity_climb_help);
// RelativeLayout mainLayout = (RelativeLayout) findViewById(R.id.main_layout);
// mainLayout.setOnTouchListener(new OnTouchListener() {
//
// public boolean onTouch(View v, MotionEvent event) {
//
// finish();
// return true;
// }
// });
// }
//
//
// }
