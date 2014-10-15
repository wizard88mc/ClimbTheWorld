package org.unipd.nbeghin.climbtheworld.activity.recognition;

import java.util.ArrayList;

import org.unipd.nbeghin.climbtheworld.MainActivity;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class ActivityRecognitionIntentService extends IntentService {

	
	private static int values_number = 0;	
	private static int activities_number = 0;
		
	//si utilizzano due liste: una per contenere i livelli di confidenza
	//e l'altra i pesi delle varie attività fisiche rilevate
	
	//nel caso in cui si calcoli la funzione di valutazione in un thread separato:
	//si hanno due coppie di liste in quanto il calcolo della funzione di
	//valutazione, che utilizza confidenze e pesi, può richiedere parecchio tempo
	//(più valori di attività vengono rilevati, più tempo è necessario);
	//per non bloccare il passaggio al prossimo alarm (che può essere immediatamente
	//successivo) il calcolo viene fatto in un thread separato che usa la coppia di
	//liste corrente;
	//l'ascolto del prossimo intervallo utilizza quindi l'altra coppia di liste
	
	//tale soluzione può non andar bene in quanto il set del prossimo alarm può
	//considerare l'array dell'alarm che il thread sta modificando
	
	
	
	
	private static ArrayList<Float> confidences_1 = new ArrayList<Float>();
	private static ArrayList<Integer> weights_1 = new ArrayList<Integer>();
	
	/*
	private static ArrayList<Float> confidences_2 = new ArrayList<Float>();
	private static ArrayList<Integer> weights_2 = new ArrayList<Integer>();
	
	//liste che vengono usate in un certo intervallo (sono semplici alias di una coppia delle
	//liste precedenti, così da non dover fare il controllo ad ogni valore ricevuto)
	private ArrayList<Float> current_confidences_list;
	private ArrayList<Integer> current_weights_list;
	
	private static boolean usedList = true; //indica quale coppia di liste è usata
	                                       //(false: 1a, true: 2a)
	*/
	
	public ActivityRecognitionIntentService() {
		 super("ActivityRecognitionIntentService");
	}

	
	/*
	@Override
	public void onCreate() {
		
		if(!usedList){
			current_confidences_list=confidences_1;
			current_weights_list=weights_1;
		}
		else{
			current_confidences_list=confidences_2;
			current_weights_list=weights_2;
		}
		
		
		super.onCreate();
	}
	*/
	
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
	
	
	
	public static float getActivityAmount(){	
		
		float amount = 0f;
		
		if(values_number>0){
			amount = (float) activities_number/values_number;			
			if(amount>0.9f)
				amount=0.9f;
		}
		
		return amount;
	}
	
	
	
	private void handleActivity(int activityType, int confidence){
		
		switch(activityType) { //nel caso di due liste mettere current_weights_list e
		  					   //current_confidences_list
		  case DetectedActivity.ON_FOOT:
              weights_1.add(30);
              confidences_1.add((float)confidence/100);
              activities_number++;
              break;
		  case DetectedActivity.WALKING:    
			  weights_1.add(60);
			  confidences_1.add((float)confidence/100);
              activities_number++;
              break;
		  case DetectedActivity.RUNNING:    
			  weights_1.add(100);
			  confidences_1.add((float)confidence/100);
              activities_number++;
              break;
		  case DetectedActivity.ON_BICYCLE:    
			  weights_1.add(80);
			  confidences_1.add((float)confidence/100);
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
	
	
	
	public static void clearLists(){
		/*
		if(!usedList){
			confidences_1.clear();
			weights_1.clear();
			
			Log.d(MainActivity.AppName,"ActivityRecognitionIntentService - clear 1");
		}
		else{
			confidences_2.clear();
			weights_2.clear();
			
			Log.d(MainActivity.AppName,"ActivityRecognitionIntentService - clear 2");
		}
		*/
		
		confidences_1.clear();
		weights_1.clear();
	}
	
	
	
	public static void clearValuesCount(){
		values_number=0;
		activities_number=0;
	}
	
	
	public static int getValuesNumber(){
		return values_number;
	}
	
	public static int getActivitiesNumber(){
		return activities_number;
	}
	
	/*
	public static boolean getUsedList(){
		return usedList;
	}

	public static void setUsedList(boolean used){
		usedList=used;
	}
	*/
	
	
	
	public static ArrayList<Float> getConfidencesList(){
		/*
		if(!usedList){
			return confidences_1;
		}
		else{
			return confidences_2;
		}
		
		*/
		
		return confidences_1;
	}
	
	
	public static ArrayList<Integer> getWeightsList(){
		
		/*
		if(!usedList){
			return weights_1;
		}
		else{
			return weights_2;
		}
		*/
		
		return weights_1;
		
	}
	
}
