package org.unipd.nbeghin.climbtheworld.services;

import org.unipd.nbeghin.climbtheworld.ClimbApplication;
import org.unipd.nbeghin.climbtheworld.util.FacebookUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.widget.Toast;

public class NotificationDeletedReceiver extends BroadcastReceiver{


	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		ClimbApplication.bonus_notification = 0;
	}

}
