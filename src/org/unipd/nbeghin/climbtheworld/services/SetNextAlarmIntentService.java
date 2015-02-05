package org.unipd.nbeghin.climbtheworld.services;

import org.unipd.nbeghin.climbtheworld.util.AlarmUtils;

import android.app.IntentService;
import android.content.Intent;

public class SetNextAlarmIntentService extends IntentService {

	public SetNextAlarmIntentService() {
		super("SetNextAlarmIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		
		//si recuperano i parametri del metodo passati attraverso l'intent
		boolean takeAllAlarms = intent.getBooleanExtra("takeAllAlarms", false);		
		boolean prevAlarmNotAvailable = intent.getBooleanExtra("prevAlarmNotAvailable", false);
		int current_alarm_id = intent.getIntExtra("current_alarm_id", -1);		
		
		//si imposta e si lancia il prossimo alarm
    	AlarmUtils.setNextAlarm(getApplicationContext(),AlarmUtils.getAllAlarms(getApplicationContext()),takeAllAlarms,prevAlarmNotAvailable,current_alarm_id);		
	}

}
