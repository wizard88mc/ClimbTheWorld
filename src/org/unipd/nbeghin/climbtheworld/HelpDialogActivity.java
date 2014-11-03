package org.unipd.nbeghin.climbtheworld;

import org.unipd.nbeghin.climbtheworld.models.GameModeType;

import android.app.Dialog;
import android.content.Context;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class HelpDialogActivity extends Dialog {

	
	public HelpDialogActivity(Context context, int theme, GameModeType mode, double percentage) {
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
				
				if(percentage >= 1.00){
					((TextView) findViewById(R.id.textShare)).setText("Condividi la tua vittoria su facebook");
					((TextView) findViewById(R.id.textGallery)).setText("Qui c'è il tuo premio: la galleria fotografica dell'edificio!!!!");

				}else{
					
				}
	}

	

	@Override
	public boolean onTouchEvent(MotionEvent event) {
			return true;
	
	}

}

class MyGestureDetector extends SimpleOnGestureListener {
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

		System.out.println("Help im being touched!");
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
