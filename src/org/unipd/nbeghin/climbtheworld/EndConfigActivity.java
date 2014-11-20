package org.unipd.nbeghin.climbtheworld;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class EndConfigActivity extends Activity {

	//vari campi per le stringhe e per i componenti grafici utilizzati 
	private Button backToMainBtt;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_end_config);
		
	    backToMainBtt = (Button) findViewById(R.id.btt_endConfig);
	    
	    
	    backToMainBtt.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				//nell'intent per chiamare l'activity principale si setta il flag 
				//'FLAG_ACTIVITY_CLEAR_TOP' per fare in modo che essa sia al top dello stack,
				//facendo il pop delle precedenti activity
				
				startActivity(new Intent(getApplicationContext(), MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
				
			}
		});
	}
	
	//si fa l'override del metodo che gestisce il back button per fare in modo di
	//ritornare all'activity principale
			
	//per versioni 2.0 e superiori
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		
		
		
	}
	//per versioni inferiori alla 2.0
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return super.onKeyDown(keyCode, event);
	}
	
}
