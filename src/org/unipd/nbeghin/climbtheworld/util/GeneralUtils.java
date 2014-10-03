package org.unipd.nbeghin.climbtheworld.util;

import java.text.SimpleDateFormat;
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
    
    /**
     * Metodo per inizializzare le shared preferences dell'app e per eseguire il
     * setup degli alarm e dei template.  
     * @param context
     * @param dbHelper
     */
    public static void initializePrefsAndAlarms(Context context) {
    	
    	SharedPreferences prefs = context.getSharedPreferences("appPrefs", 0);
    	
    	Editor editor = prefs.edit();    	
    	editor.putBoolean("firstRun", false); // si memorizza che non è il primo run dell'app
    	editor.putInt("current_template", 1); // il template orario che si usa è il primo    	
       	//si salvano le credenziali
    	editor.commit();  
    	
    	/////////		
    	//per test algoritmo
    	editor.putInt("artificialDayIndex", 0).commit();    	
    	Calendar cal = Calendar.getInstance();
    	SimpleDateFormat calFormat = new SimpleDateFormat("yyyy-MM-dd");
    	String dateFormatted = calFormat.format(cal.getTime());
    	editor.putString("dateOfIndex", dateFormatted).commit();
    	Log.d(MainActivity.AppName,"GeneralUtils - init index: 0, init date: " + dateFormatted);	
    	
    	alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
    	Intent intent = new Intent(context, TimeBatteryWatcher.class);
    	intent.setAction("UPDATE_DAY_INDEX_TESTING");    	
    	Calendar calendar = Calendar.getInstance();
    	calendar.setTimeInMillis(System.currentTimeMillis());
    	calendar.set(Calendar.HOUR_OF_DAY, 0);
    	calendar.set(Calendar.MINUTE, 0);
    	calendar.set(Calendar.SECOND, 0); 
    	alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
    			AlarmManager.INTERVAL_DAY, PendingIntent.getBroadcast(context, 0, intent, 0));
    	Log.d(MainActivity.AppName,"GeneralUtils - set update index alarm");	
    	/////////
    	
    	
    	//si fa il setup del db per gli alarm e i template
    	AlarmUtils.setupAlarmTemplatesDB(context); 
    	//si creano gli alarm e i template, gli alarm vengono associati ai template
		AlarmUtils.createAlarmsAndTemplates(context);     	    	
    	//si imposta e si lancia il prossimo alarm
    	AlarmUtils.setNextAlarm(context,AlarmUtils.lookupAlarmsForTemplate(context,AlarmUtils.getTemplate(context,1)));    	 
    }   
}