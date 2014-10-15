package org.unipd.nbeghin.climbtheworld.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.unipd.nbeghin.climbtheworld.MainActivity;
import org.unipd.nbeghin.climbtheworld.receivers.TimeBatteryWatcher;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * 
 * Classe che contiene alcuni metodi di utilità.
 *
 */
public class GeneralUtils {
	
	//numero di giorni di cui è composta una settimana
	public static int daysOfWeek = 2;
	private static AlarmManager alarmMgr;

	
	/**
	 * Costruttore della classe.
	 */
	private GeneralUtils(){
		
	}
	
	
	/**
     * Il metodo <code>isInternetConnectionUp</code> permette di controllare se 
     * è disponibile o meno una connessione dati.
     * 
     * @param context
     *            contesto dell'activity che chiama il metodo
     * @return 'true' se è disponibile una connessione dati, 'false' altrimenti
     */
    public static boolean isInternetConnectionUp(Context context) {
    	
    	ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    	NetworkInfo netInfo = connMgr.getActiveNetworkInfo();
       
    	if (netInfo!=null && netInfo.isConnected()) {
    		return true;
    	}
    	return false;
    }
	
    
    public static boolean isActivityRecognitionServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("org.unipd.nbeghin.climbtheworld.services.ActivityRecognitionRecordService".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
        
    /*
    public static int isServiceInRestartPhase(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("org.unipd.nbeghin.climbtheworld.services.SamplingClassifyService".equals(service.service.getClassName())) {                
            	if(service.restarting!=0){
            		return 1;
            	}
            	else{
            		return 0;	
            	}
            }
        }
        return -1;
    }
    */
    
    public static boolean isGameServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("org.unipd.nbeghin.climbtheworld.services.SamplingClassifyService".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    
       
    /**
     * Performs the alarms setup and initializes the related shared preferences.
     * @param context context of the application.
     * @param prefs reference to android shared preferences. 
     */
    public static void initializeAlarmsAndPrefs(Context context, SharedPreferences prefs) {
    	    	
    	//SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);//context.getSharedPreferences("appPrefs", 0);
    	
    	Editor editor = prefs.edit();    	
    	editor.putBoolean("firstRun", false); // si memorizza che non è il primo run dell'app
    	//editor.putInt("current_template", 1); // il template orario che si usa è il primo    	
       	//si salvano le credenziali
    	editor.commit();  
    	
    	/////////		
    	//PER TEST ALGORITMO
    	editor.putInt("artificialDayIndex", 0);    	
    	Calendar cal = Calendar.getInstance();
    	SimpleDateFormat calFormat = new SimpleDateFormat("yyyy-MM-dd");
    	String dateFormatted = calFormat.format(cal.getTime());
    	editor.putString("dateOfIndex", dateFormatted);
    	editor.commit();    	
    	
    	//si imposta l'alarm per aggiornare l'indice artificiale che rappresenta il giorno
    	//all'interno della settimana corta
    	alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
    	Intent intent = new Intent(context, TimeBatteryWatcher.class);
    	intent.setAction("UPDATE_DAY_INDEX_TESTING");    	
    	Calendar calendar = Calendar.getInstance();
    	//si imposta a partire dalla mezzanotte del giorno successivo
    	calendar.add(Calendar.DATE, 1); 
    	calendar.set(Calendar.HOUR_OF_DAY, 0);
    	calendar.set(Calendar.MINUTE, 0);
    	calendar.set(Calendar.SECOND, 0); 
    	//si ripete l'alarm ogni giorno a mezzanotte
    	alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
    			AlarmManager.INTERVAL_DAY, PendingIntent.getBroadcast(context, 0, intent, 0));
    	    	
    	if(MainActivity.logEnabled){
    		Log.d(MainActivity.AppName + " - TEST","GeneralUtils - init index: 0, init date: " + dateFormatted);	
    		Log.d(MainActivity.AppName + " - TEST","GeneralUtils - set update day index alarm");
    		int month =calendar.get(Calendar.MONTH)+1;    	
        	Log.d(MainActivity.AppName + " - TEST", "GeneralUtils - UPDATE DAY INDEX ALARM: h:m:s=" 
    				+ calendar.get(Calendar.HOUR_OF_DAY)+":"+ calendar.get(Calendar.MINUTE)+":"+ calendar.get(Calendar.SECOND) +
    				"  "+calendar.get(Calendar.DATE)+"/"+month+"/"+calendar.get(Calendar.YEAR));        	
        	Log.d(MainActivity.AppName + " - TEST", "GeneralUtils - milliseconds of the update day index alarm: " + calendar.getTimeInMillis());
    	}
    	/////////
    	
    	
    	//si fa il setup del db per gli alarm
    	AlarmUtils.setupAlarmsDB(context); 
    	//si creano gli alarm
		AlarmUtils.createAlarms(context);     	    	
    	//si imposta e si lancia il prossimo alarm
    	AlarmUtils.setNextAlarm(context,AlarmUtils.getAllAlarms(context)); //AlarmUtils.lookupAlarmsForTemplate(context,AlarmUtils.getTemplate(context,1))
    
    }   
    
    
    
    
    public static float evaluateInterval(float qn, ArrayList<Float> confidences,
    		ArrayList<Integer> weights){
    	
    	
    	int weights_sum = 0;
    	for(Integer i : weights){
    	    weights_sum += i;
    	}
    	
    	//le due liste hanno la stessa size
    	
    	float n = 0f;    	
    	for(int i=0; i<weights.size(); i++){
    		n += weights.get(i)*confidences.get(i);
    	}
    	
    	
    	float ql = n/weights_sum;
    	
    	Log.d(MainActivity.AppName,"Quality of physical activity: " + ql);
    	
    	
    	return qn*ql;
    }
    
    
}