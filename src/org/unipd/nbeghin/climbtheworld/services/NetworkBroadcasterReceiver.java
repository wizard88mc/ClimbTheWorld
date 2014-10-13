package org.unipd.nbeghin.climbtheworld.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Toast;

public class NetworkBroadcasterReceiver extends BroadcastReceiver{


	
	@Override
	public void onReceive(Context context, Intent intent) {
		//Toast.makeText(context, "sync climbtheworld", Toast.LENGTH_SHORT).show();
		System.out.println("broadcaster receiver");
		
		Intent filter = new Intent(context, UpdateService.class);
        context.startService(filter);
	}

}
