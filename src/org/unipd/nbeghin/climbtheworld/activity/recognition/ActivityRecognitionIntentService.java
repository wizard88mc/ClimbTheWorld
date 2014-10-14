package org.unipd.nbeghin.climbtheworld.activity.recognition;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import android.app.IntentService;
import android.content.Intent;

public class ActivityRecognitionIntentService extends IntentService {

	public static int values_number = 0;
	
	
	public ActivityRecognitionIntentService() {
		 super("ActivityRecognitionIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
		
		 System.out.println("ON HANDLE INTENT");
		
		// If the incoming intent contains an update
        if (ActivityRecognitionResult.hasResult(intent)) {
        	
        	 System.out.println("contiene update");
        	
            // Get the update
            ActivityRecognitionResult result =
                    ActivityRecognitionResult.extractResult(intent);
            // Get the most probable activity from the list of activities in the update
            DetectedActivity mostProbableActivity =
                    result.getMostProbableActivity();
           
             //Get the probability that this activity is the
             //the user's actual activity
            // Get the confidence percentage for the most probable activity
             int confidence = mostProbableActivity.getConfidence();
            
            
            //Get an integer describing the type of activity           
            int activityType = mostProbableActivity.getType();
            // Get the type of activity
            String activityName = getNameFromType(activityType);
            
            //At this point, you have retrieved all the information
             //for the current update. You can display this
             //information to the user in a notification, or
              //send it to an Activity or Service in a broadcast
             //Intent.
            
            System.out.println("activity: " + activityName + "  confidence: " + confidence);
            
            values_number++;
           
        } else {
           //This implementation ignores intents that don't contain
            //an activity update. If you wish, you can report them as
             //errors.
            
        }

	}
	
	
	
	private String getNameFromType(int activityType) {
        switch(activityType) {
            case DetectedActivity.IN_VEHICLE:
                return "in_vehicle";
            case DetectedActivity.ON_BICYCLE:
                return "on_bicycle";
            case DetectedActivity.ON_FOOT:
                return "on_foot";
            case DetectedActivity.RUNNING:
                return "running";
            case DetectedActivity.WALKING:
                return "walking";
            case DetectedActivity.STILL:
                return "still";
            case DetectedActivity.UNKNOWN:
                return "unknown";
            case DetectedActivity.TILTING:
                return "tilting";
             
        }
        return "unknown";
    }

}
