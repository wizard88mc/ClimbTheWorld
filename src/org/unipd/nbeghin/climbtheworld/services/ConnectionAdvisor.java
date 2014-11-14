package org.unipd.nbeghin.climbtheworld.services;

import org.unipd.nbeghin.climbtheworld.ClimbApplication;
import org.unipd.nbeghin.climbtheworld.util.FacebookUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.widget.Toast;

public class ConnectionAdvisor extends BroadcastReceiver{


	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		if(!FacebookUtils.isOnline(context) && ClimbApplication.isActivityVisible()){
			Toast t = Toast.makeText(context, "Sei offline ora, ma i tuoi progressi saranno salvati non appena la connessione torna", Toast.LENGTH_SHORT);
			t.setGravity(Gravity.TOP, 0, 0);
			t.show();
		}
	}

}
