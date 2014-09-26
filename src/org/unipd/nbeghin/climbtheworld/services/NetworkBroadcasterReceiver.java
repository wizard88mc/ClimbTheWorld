package org.unipd.nbeghin.climbtheworld.services;

import org.unipd.nbeghin.climbtheworld.util.FacebookUtils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkBroadcasterReceiver extends BroadcastReceiver{

	public boolean isOnline(Context context) {
	    ConnectivityManager cm =
	        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
	        return true;
	    }
	    return false;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if(isOnline(context)){
			//da tab locale a parse
			//Locale_user
			
		}
	}

}
