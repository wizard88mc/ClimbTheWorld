package org.unipd.nbeghin.climbtheworld;

import android.app.Activity;
import android.app.ActionBar.LayoutParams;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class OnBoardingActivity extends Activity{

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_begin);
		TextView text = (TextView) findViewById(R.id.textOnborading);
		Button okBtn = (Button) findViewById(R.id.buttonOk);
		Typeface tf = Typeface.createFromAsset(ClimbApplication.getContext().getAssets(),"fonts/cake.ttf");  //sketch-me
		text.setTypeface(tf);
		okBtn.setTypeface(tf);
	
		
		okBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		Intent i = getIntent();
		String source = i.getExtras().getString("source");
		if(source.equalsIgnoreCase("MainActivity")){
			text.setText(getString(R.string.onboarding_1));
			okBtn.setText(getString(R.string.onboarding_btn_1));
		}else if(source.equalsIgnoreCase("ClimbActivity")){
			text.setText(getString(R.string.onboarding_2));
			okBtn.setText(getString(R.string.onboarding_btn_2));
		}else if(source.equalsIgnoreCase("ClimbActivityVictory")){
			text.setText(getString(R.string.onboarding_3));
			okBtn.setText(getString(R.string.onboarding_btn_3));
		}

	}
}
