package org.unipd.nbeghin.climbtheworld.activity.recognition;

import org.unipd.nbeghin.climbtheworld.MainActivity;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class ActivityRecognitionIntentService extends IntentService {

	//variable to hold the total number of values returned by the service 
	private static int values_number = 0;	
	//variable to hold the number of activities
	private static int activities_number = 0;
	//sum of the activities' weights
	private static int weights_sum = 0;
	//sum of the confidences-weights products
	private static float confidences_weights_sum = 0f;
	
	
	public ActivityRecognitionIntentService() {
		 super("ActivityRecognitionIntentService");
	}

		
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		
		Log.d(MainActivity.AppName,"OnCreate activityRec service - n. values " + getValuesNumber());
		
	}
	
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		Log.d(MainActivity.AppName,"OnDestroy activityRec service - n. values " + getValuesNumber());
		
	}
	
	
	@Override
	protected void onHandleIntent(Intent intent) {
		
		// If the incoming intent contains an update
        if (ActivityRecognitionResult.hasResult(intent)) {
        	
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
            
            
            if(MainActivity.logEnabled){
            	Log.d(MainActivity.AppName,"ActivityRecognitionIntentService - the intent contains an update");
	    		
            	Log.d(MainActivity.AppName,"ActivityRecognitionIntentService - activity: "+activityName+" confidence: " + (float)confidence/100);
	    	}
                        
            
            values_number++;
            handleActivity(activityType,confidence);
           
        } else {
           //This implementation ignores intents that don't contain
            //an activity update. If you wish, you can report them as
             //errors.
            
        }

	}
	
	
	
	
	private void handleActivity(int activityType, int confidence){
		
		switch(activityType) { 
		  case DetectedActivity.ON_FOOT:
			  weights_sum += 30;
			  confidences_weights_sum += 30*((float)confidence/100);
              activities_number++;
              break;
		  case DetectedActivity.WALKING:    
			  weights_sum += 60;
			  confidences_weights_sum += 60*((float)confidence/100);
              activities_number++;
              break;
		  case DetectedActivity.RUNNING:    
			  weights_sum += 100;
			  confidences_weights_sum += 100*((float)confidence/100);
              activities_number++;
              break;
		  case DetectedActivity.ON_BICYCLE: 
			  weights_sum += 80;
			  confidences_weights_sum += 80*((float)confidence/100);
              activities_number++;
              break;
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
	
	
	
	
	public static void clearValuesCount(){
		values_number=0;
		activities_number=0;
		weights_sum=0;
		confidences_weights_sum=0f;
	}
	
	
	public static int getValuesNumber(){
		return values_number;
	}
	
	
	public static int getActivitiesNumber(){
		return activities_number;
	}
	
	public static float getConfidencesWeightsSum(){				
		return confidences_weights_sum;
	}
	
	
	public static int getWeightsSum(){	
		return weights_sum;
	}
	
}
