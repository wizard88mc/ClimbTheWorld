package org.unipd.nbeghin.climbtheworld.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.unipd.nbeghin.climbtheworld.MainActivity;
import org.unipd.nbeghin.climbtheworld.db.DbHelper;
import org.unipd.nbeghin.climbtheworld.models.Alarm;
import org.unipd.nbeghin.climbtheworld.receivers.TimeBatteryWatcher;

import com.j256.ormlite.dao.RuntimeExceptionDao;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.Spanned;
import android.text.SpannedString;
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
    
    
    public static boolean isSamplingClassifyServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("org.unipd.nbeghin.climbtheworld.services.SamplingClassifyRecordService".equals(service.service.getClassName())) {
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
    	//readIntervalsFromFile(context);
		
    	//si imposta e si lancia il prossimo alarm
    	AlarmUtils.setNextAlarm(context,AlarmUtils.getAllAlarms(context),true,-1); //AlarmUtils.lookupAlarmsForTemplate(context,AlarmUtils.getTemplate(context,1))    
    }   
    
    
    
    private static void readIntervalsFromFile(Context context) {

    	DbHelper helper = DbHelper.getInstance(context);
    	
    	RuntimeExceptionDao<Alarm, Integer> alarmDao = helper.getAlarmDao();
    	
	    AssetManager assetManager = context.getResources().getAssets();
	    	    
	    try {
	    	
	        InputStream inputStream = assetManager.open("intervals.txt");

	        if (inputStream!=null) {
	        	
	        	InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
	            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
	            String receiveString = "";
	            
	            Calendar cal_start = Calendar.getInstance();
	            Calendar cal_stop = Calendar.getInstance();	            
	        	SimpleDateFormat calFormat = new SimpleDateFormat("HH:mm:ss");
	        	  
	        	int current_day = cal_start.get(Calendar.DATE);
	        	int current_month = cal_start.get(Calendar.MONTH);
	        	int current_year = cal_start.get(Calendar.YEAR);
	        	
	        	Calendar cal_previous_stop = Calendar.getInstance();
	        	//si inizializza questo oggetto Calendar con una data precedente a quella corrente;
	        	//serve per inserire un intervallo solo se il suo inizio viene dopo la fine del 
	        	//precedente intervallo
	        	cal_previous_stop.add(Calendar.YEAR,-1);
	        	
	        	//serve perché si saltano le prime due linee
	        	int line_number=0;
	        	
	            while ((receiveString = bufferedReader.readLine())!=null) {
	            	            	
	            	Log.d(MainActivity.AppName, "Read intervals file: line number=" + line_number);
	            	
	            	if(line_number>1){
	            		/*
	            		if(MainActivity.logEnabled){
	    	    			int month=cal_previous_stop.get(Calendar.MONTH)+1;	
	    	    			Log.d(MainActivity.AppName, "AlarmUtils - PREVIOUS STOP_" + line_number + "  h:m:s=" 
	    	    					+ cal_previous_stop.get(Calendar.HOUR_OF_DAY)+":"+ cal_previous_stop.get(Calendar.MINUTE)+":"+ cal_previous_stop.get(Calendar.SECOND) +
	    	    					"  "+cal_previous_stop.get(Calendar.DATE)+"/"+month+"/"+cal_previous_stop.get(Calendar.YEAR));		    	    			
	    	    		}	
	    	            */
		            	int substring_end = receiveString.indexOf(";",0);
		            		            	
		            	String str_startTime = receiveString.substring(0,substring_end).trim();
		            	Log.d(MainActivity.AppName, "Read intervals file: start time string " + str_startTime);
		            	
		            	String str_stopTime = receiveString.substring(substring_end+1,receiveString.indexOf(";",substring_end+1)).trim();
		            	Log.d(MainActivity.AppName, "Read intervals file: stop time string " + str_stopTime);
		            	
		            	substring_end = receiveString.indexOf(";",substring_end+1);
		            	
		            	cal_start.setTime(calFormat.parse(str_startTime));
		            	cal_start.set(Calendar.DATE, current_day);
		            	cal_start.set(Calendar.MONTH, current_month);
		            	cal_start.set(Calendar.YEAR, current_year);
		            	cal_stop.setTime(calFormat.parse(str_stopTime));
		            	cal_stop.set(Calendar.DATE, current_day);
		            	cal_stop.set(Calendar.MONTH, current_month);
		            	cal_stop.set(Calendar.YEAR, current_year);
					
		            	Log.d(MainActivity.AppName, "Read intervals file: HH:mm:ss START:" + cal_start.get(Calendar.HOUR_OF_DAY)+ ":"
	        					+ cal_start.get(Calendar.MINUTE)+":"+cal_start.get(Calendar.SECOND) + " STOP: " + cal_stop.get(Calendar.HOUR_OF_DAY)+ ":"
	        					+ cal_stop.get(Calendar.MINUTE)+":"+cal_stop.get(Calendar.SECOND));
	        					            	
		            	   
		            	if(cal_start.after(cal_previous_stop)){ 
		            		//l'intervallo ha un orario di inizio successivo alla fine
		            		//dell'intervallo precedente
		            		
		            		Log.d(MainActivity.AppName, "Read intervals file: start time of this interval > end time of the previous one");
		            		
		            		if(cal_start.before(cal_stop)){
		            			//l'intervallo è valido
		            			Log.d(MainActivity.AppName, "Read intervals file: start time < end time");
		            			
		            			
		            			boolean activationState[] = new boolean[daysOfWeek];
		            			
		            			
		            			String str_initialState = receiveString.substring(substring_end+1,receiveString.indexOf(";",substring_end+1)).trim();
		            			//System.out.println("str state: " + str_initialState);
		            			
		            			String[] parts = str_initialState.split("\\:");
		            			//System.out.println("str state lenght: " + parts.length + " 0:" + parts[0] + " 1:" + parts[1]);
		            			
		            			
		            			for(int i=0; i<parts.length; i++){
		            				
		            				if(parts[i].equals("1")){
		            					activationState[i]=true;
		            				}
		            				else{ //parts[i].equals("0")
		            					activationState[i]=false;
		            				}
		            			}		            			
		            			
		            			Alarm start = new Alarm(cal_start.get(Calendar.HOUR_OF_DAY), cal_start.get(Calendar.MINUTE), cal_start.get(Calendar.SECOND), true, activationState, new float[] {0.25f,0.25f});
		            			Alarm stop = new Alarm(cal_stop.get(Calendar.HOUR_OF_DAY), cal_stop.get(Calendar.MINUTE), cal_stop.get(Calendar.SECOND), false, activationState, new float[] {0.25f,0.25f});
		            			
		            			alarmDao.createIfNotExists(start);
		            			alarmDao.createIfNotExists(stop);
		            			
		            			cal_stop=(Calendar) cal_previous_stop.clone();
		            		}
		            	}
	            	}
	            	line_number++;
	            }
	            
	            Log.d(MainActivity.AppName, "Read intervals file: Total number of lines: " + line_number);
	            
	            inputStream.close();
	        }
	    } 
	    catch (ParseException e) {
			e.printStackTrace();
		}
	    catch (FileNotFoundException e) {
	        Log.e(MainActivity.AppName, " - File not found: " + e.toString());
	    } catch (IOException e) {
	        Log.e(MainActivity.AppName, " - Can not read file: " + e.toString());
	    }
	}
    
    
    //start-stop, 0/1 attuale, se attivo valutazione, 0/1 la prossima settimana
    public static void writeLogFile(Context context, String text){
    	   	    	
    	File appdir = context.getDir("climbTheWorld_dir", Context.MODE_PRIVATE); 
    	File logFile = new File(appdir, "algorithm_log");
    	
    	try {    	
    		if(!logFile.exists()){    		
				logFile.createNewFile();
    		}
    	    	
    		//'true' per aggiungere il testo al file esistente
    		BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true)); 
    		buf.append(text);
    		buf.newLine();
    		buf.close();
    	} catch (IOException e) {
			e.printStackTrace();
		}
    }
        
    
    //carica il contenuto del file di log
    public static List<Spanned> loadLogFile(File logFile) throws IOException {

        //get a new list of spanned strings
        List<Spanned> content = new ArrayList<Spanned>();
        
        //if no log file exists yet, return the empty List
        if (!logFile.exists()) {
            return content;
        }

        //create a new buffered file reader based on the log file
        BufferedReader reader = new BufferedReader(new FileReader(logFile));

        //get a string instance to hold input from the log file
        String line;

        //read until end-of-file from the log file, and store the input line as a
        //spanned string in the List
        while ((line = reader.readLine()) != null) {
            content.add(new SpannedString(line));
        }

        //close the file
        reader.close();

        //return the data from the log file
        return content;
    }
    
}