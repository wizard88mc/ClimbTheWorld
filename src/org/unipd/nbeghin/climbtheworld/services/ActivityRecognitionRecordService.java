package org.unipd.nbeghin.climbtheworld.services;

import org.unipd.nbeghin.climbtheworld.activity.recognition.ActivityDetectionRemover;
import org.unipd.nbeghin.climbtheworld.activity.recognition.ActivityDetectionRequester;
import org.unipd.nbeghin.climbtheworld.activity.recognition.ActivityRecognitionIntentService;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;

/**
 * 
 *
 */
public class ActivityRecognitionRecordService extends IntentService {

	private static ActivityDetectionRequester requester;
	private static ActivityDetectionRemover remover;
	
	
	
	public ActivityRecognitionRecordService() {
		super("ActivityRecognitionRecordService");
	}

	
	@Override
	public void onCreate() {	
		requester = new ActivityDetectionRequester(getApplicationContext());
		remover = new ActivityDetectionRemover(getApplicationContext());
		
		System.out.println("service On create");
		
	}
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		System.out.println("service On start command");
		
		requester.requestUpdates();	
		
		return START_STICKY;
	}
	
	
	@Override
	protected void onHandleIntent(Intent intent) {
		System.out.println("service On handle intent");
		
		//requester.requestUpdates();		
		//requesterPI = requester.getRequestPendingIntent();
	}
	
	
	
	
	@Override
	public void onDestroy() {
		System.out.println("On destroy");
		
		
		Intent i = new Intent(getApplicationContext(), ActivityRecognitionIntentService.class);
		
		
		PendingIntent ii = PendingIntent.getService(getApplicationContext(), 0, i,
                PendingIntent.FLAG_UPDATE_CURRENT);

		
		
		remover.removeUpdates(ii);
	}
}
