package org.unipd.nbeghin.climbtheworld.util;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * 
 * Classe che contiene alcuni metodi di utilità.
 *
 */
public class GeneralUtils {
	
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
    	
    	//si fa il setup del db per gli alarm e i template
    	AlarmUtils.setupAlarmTemplatesDB(context); 
    	//si creano gli alarm e i template, gli alarm vengono associati ai template
		AlarmUtils.createAlarmsAndTemplates(context);     	    	
    	//si imposta e si lancia il prossimo alarm
    	AlarmUtils.setNextAlarm(context,AlarmUtils.lookupAlarmsForTemplate(context,AlarmUtils.getTemplate(context,1)));    	 
    }   
}