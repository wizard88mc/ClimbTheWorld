package org.unipd.nbeghin.climbtheworld.services;

import org.unipd.nbeghin.climbtheworld.ClimbApplication;
import org.unipd.nbeghin.climbtheworld.R;
import org.unipd.nbeghin.climbtheworld.util.FacebookUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.Gravity;
import android.widget.Toast;

public class NetworkBroadcasterReceiver extends BroadcastReceiver{


	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		if(FacebookUtils.isOnline(context)){
			Intent filter = new Intent(context, UpdateService.class);
        	context.startService(filter);
		}
        
        if(!FacebookUtils.isOnline(context) && ClimbApplication.isActivityVisible()){
			Toast t = Toast.makeText(context, context.getString(R.string.offline), Toast.LENGTH_SHORT);
			t.setGravity(Gravity.TOP, 0, 0);
			t.show();
		}
	}

}
