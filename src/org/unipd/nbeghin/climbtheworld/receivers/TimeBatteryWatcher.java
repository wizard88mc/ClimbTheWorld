package org.unipd.nbeghin.climbtheworld.receivers;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.unipd.nbeghin.climbtheworld.ClimbActivity;
import org.unipd.nbeghin.climbtheworld.MainActivity;
import org.unipd.nbeghin.climbtheworld.activity.recognition.ActivityRecognitionIntentService;
import org.unipd.nbeghin.climbtheworld.activity.recognition.ActivityRecognitionUtils;
import org.unipd.nbeghin.climbtheworld.models.Alarm;
import org.unipd.nbeghin.climbtheworld.services.ActivityRecognitionRecordService;
import org.unipd.nbeghin.climbtheworld.services.SamplingClassifyService;
import org.unipd.nbeghin.climbtheworld.services.SetNextAlarmIntentService;
import org.unipd.nbeghin.climbtheworld.util.AlarmUtils;
import org.unipd.nbeghin.climbtheworld.util.GeneralUtils;
import org.unipd.nbeghin.climbtheworld.util.IntervalEvaluationUtils;
import org.unipd.nbeghin.climbtheworld.util.LogUtils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.preference.PreferenceManager;
import android.util.Log;

public class TimeBatteryWatcher extends BroadcastReceiver {
	
	//stringhe che identificano le varie azioni per gli intent che può ricevere questo receiver
	//(questo receiver potrà quindi ricevere gli intent mandati da sendBroadcast() che hanno
	//impostato queste azioni)	
	//private final String BOOT_ACTION = "android.intent.action.BOOT_COMPLETED";
	private final String INTERVAL_START_ACTION = "org.unipd.nbeghin.climbtheworld.INTERVAL_START";
	private final String INTERVAL_STOP_ACTION = "org.unipd.nbeghin.climbtheworld.INTERVAL_STOP";	
	private final String ENERGY_BALANCING = "org.unipd.nbeghin.climbtheworld.BATTERY_ENERGY_BALANCING";
	/////////	
	//PER TEST ALGORITMO
	private final String UPDATE_DAY_INDEX_FOR_TESTING = "org.unipd.nbeghin.climbtheworld.UPDATE_DAY_INDEX_TESTING";
	/////////	
	
	//private BroadcastReceiver stairsReceiver = StairsClassifierReceiver.getInstance();
	//private IntentFilter stairsActionFilter = new IntentFilter(ClassifierCircularBuffer.CLASSIFIER_ACTION);	
	
	//campi utili a registrare il receiver che "ascolta" l'attività utente; quest'ultimo sarà
	//chiamato con qualsiasi broadcast intent che matcha con l'azione descritta dal seguente
	//IntentFilter
	//NB: è importante porre campo receiver 'static' per poi annullare la registrazione dello
	//stesso dopo aver stoppato il servizio di sampling
	//private static BroadcastReceiver userMotionReceiver = new UserMotionReceiver();
	//private IntentFilter userMotionFilter = new IntentFilter(ClassifierCircularBuffer.CLASSIFIER_ACTION);	

	//private static Context context;	
	//private DbHelper dbHelper = new DbHelper(context); 
	/*
	private static ActivityDetectionRequester requester = new ActivityDetectionRequester(context);
	private static ActivityDetectionRemover remover = new ActivityDetectionRemover(context);
		*/
	
	@Override
	public void onReceive(final Context context, Intent intent) {
		
		Log.d(MainActivity.AppName, "TimeBatteryWatcher - ON RECEIVE");
		
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		
		//si recupera l'oggetto delle shared preferences
		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);//context.getSharedPreferences("appPrefs", 0);
		
		//si ottiene la stringa che descrive l'azione dell'intent
		String action = intent.getAction();
		
		if(action.equalsIgnoreCase(Intent.ACTION_BATTERY_LOW)){
			//se il livello di batteria è critico si sospende l'algoritmo (ascolto e trigger)
			
			//nelle shared preferences si salva l'informazione che indica il livello di batteria 
			//pref.edit().putBoolean("low_battery_status", true).commit();
			
			Log.d(MainActivity.AppName, "TimeBatteryWatcher - BATTERY LOW");
			
			IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
			Intent batteryStatus = context.getApplicationContext().registerReceiver(null, ifilter);
			int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
			int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

			float batteryPct = level / (float)scale;
			
			LogUtils.writeLogFile(context, "TimeBatteryWatcher - BATTERY LOW, "+dateFormat.format((Calendar.getInstance()).getTime())+" level: "+level+", scale: "+scale+", BATTERY: "+batteryPct);
			
			/*
			int alarm_id=pref.getInt("alarm_id", -1);
						
			if(pref.getBoolean("algorithm_configured", false) && alarm_id!=-1){	
				
				//si recupera il prossimo alarm impostato in precedenza
				Alarm current_next_alarm = AlarmUtils.getAlarm(context, alarm_id);
				//se è di stop significa che si è all'interno di un intervallo attivo e, quindi,
				//si ferma il classificatore eventualmente in esecuzione
				if(!current_next_alarm.get_actionType()){
				 					
					if(!current_next_alarm.isStepsInterval(pref.getInt("artificialDayIndex", 0))){
						if(GeneralUtils.isActivityRecognitionServiceRunning(context)){
							Log.d(MainActivity.AppName,"BATTERY LOW - Stop activity recognition");
						   	context.stopService(new Intent(context, ActivityRecognitionRecordService.class));
						}
					}
					else{
						
						if(!ClimbActivity.samplingEnabled){
							Log.d(MainActivity.AppName,"BATTERY LOW - Gioco non attivo, si ferma il classificatore scalini");
							
							context.getApplicationContext().stopService(new Intent(context, SamplingClassifyService.class));
							//si disabilita anche il receiver
							//context.getApplicationContext().unregisterReceiver(stairsReceiver);
							context.getPackageManager().setComponentEnabledSetting(new ComponentName(context, StairsClassifierReceiver.class), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
						}	
					}
				}
				//in ogni caso si cancella dall'alarm manager l'alarm precedentemente impostato 
		    	//AlarmUtils.cancelAlarm(context, );	
				
				
				
				
			}
			
			*/	
			
			
			
			
			
		}
		else if(action.equalsIgnoreCase(Intent.ACTION_BATTERY_OKAY)){
			
			Log.d(MainActivity.AppName, "TimeBatteryWatcher - BATTERY OKAY");

			IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
			Intent batteryStatus = context.getApplicationContext().registerReceiver(null, ifilter);
			int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
			int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

			float batteryPct = level / (float)scale;
			
			LogUtils.writeLogFile(context, "TimeBatteryWatcher - BATTERY OKAY, "+dateFormat.format((Calendar.getInstance()).getTime())+" level: "+level+", scale: "+scale+", BATTERY: "+batteryPct);
			
		}		
		//se il device ha completato il boot
		else if (action.equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
			
			Log.d(MainActivity.AppName, "TimeBatteryWatcher - BOOT ACTION");
			
			LogUtils.writeLogFile(context, "TimeBatteryWatcher - BOOT ACTION");
			
			//riferimento all'alarm manager
			final AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			
			//si setta il prossimo alarm prendendo quello salvato nelle shared preferences
			//se non c'è non si fa nulla			
			
			/*
			//se si vede che si tratta del primo run dell'applicazione
			if(pref.getBoolean("firstRun", true)){	
				System.out.println("init");
				//si inizializzano gli alarm e le relative shared preferences
				GeneralUtils.initializeAlarmsAndPrefs(context,pref);	
			}
			else{
			*/
			
			//dalle preferences salvate si ottiene l'id e gli altri dati relativi
			//al prossimo alarm che era stato settato in precedenza	(-1 se non è ancora
			//stato impostato un alarm)
			final int alarm_id = pref.getInt("alarm_id", -1);
			
			if(pref.getBoolean("algorithm_configured", false) && alarm_id!=-1){	
				/////////				
				//PER TEST ALGORITMO 
				//si aggiorna l'indice artificiale che rappresenta il giorno corrente della 
				//settimana (utile per testare l'algoritmo con una settimana corta, composta ad 
				//esempio da 1,2 giorni); tale indice viene opportunamente aggiornato considerando
				//i giorni passati dall'ultimo spegnimento del device
				if(MainActivity.logEnabled){
					Log.d(MainActivity.AppName + " - TEST","TimeBatteryWatcher - day index update, on boot event");
				}
									
				//si aggiorna l'indice per indicare il giorno all'interno della settimana 
				//corta salvandolo nelle shared preferences
				int currentDayIndex=pref.getInt("artificialDayIndex", 0);
				
				Calendar now = Calendar.getInstance();
				now.set(Calendar.HOUR_OF_DAY, 0);
				now.set(Calendar.MINUTE, 0);
				now.set(Calendar.SECOND, 0);
				
				Calendar before = (Calendar) now.clone();
				/*before.set(Calendar.HOUR_OF_DAY, 0);
				before.set(Calendar.MINUTE, 0);
				before.set(Calendar.SECOND, 0);*/
				DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");	
				Date date = null;
				try {
					date = formatter.parse(pref.getString("dateOfIndex", ""));
				} catch (ParseException e) {
					e.printStackTrace();
				}
				before.setTime(date);
				
				double diff = now.getTimeInMillis() - before.getTimeInMillis();
				diff = diff / (24 * 60 * 60 * 1000); //hours in a day, minutes in a hour,
				                                     //seconds in a minute, millis in a second
				int day_diff = (int) Math.round(diff);
								
				//si aggiorna l'indice artificiale che rappresenta il giorno corrente
				//considerando i giorni dall'ultimo spegnimento del device
				for(int i=0; i<day_diff; i++){										
					if(currentDayIndex==GeneralUtils.daysOfWeek-1)
						currentDayIndex=0;
					else
						currentDayIndex++;
				}
				
				final int curr_day_index=currentDayIndex;
				
				//l'indice che risulta alla fine del ciclo è l'indice associato 
				//alla nuova data
				//si salvano nuovo indice e data nelle shared preferences
				pref.edit().putInt("artificialDayIndex", currentDayIndex).commit();
				
				SimpleDateFormat calFormat = new SimpleDateFormat("yyyy-MM-dd");
		    	String dateFormatted = calFormat.format(now.getTime());
		    	pref.edit().putString("dateOfIndex", dateFormatted).commit();
		    	
		    	if(MainActivity.logEnabled){
		    		Log.d(MainActivity.AppName + " - TEST","TimeBatteryWatcher - on boot, days since the last device shutdown: " + day_diff);	
		    		Log.d(MainActivity.AppName + " - TEST","TimeBatteryWatcher - on boot, new index: "+currentDayIndex+", new date: " + dateFormatted);	
		    	}	
		    	
		    	//si reimposta il repeating alarm per l'update dell'indice artificiale, in quanto
		    	//dopo un reboot del device quello impostato in precedenza non viene lanciato; 
		    	//quest'ultimo viene prima cancellato attraverso l'alarm manager		    	
		    	Intent update_index_intent = new Intent(context, TimeBatteryWatcher.class);
		    	update_index_intent.setAction("org.unipd.nbeghin.climbtheworld.UPDATE_DAY_INDEX_TESTING");  
				alarmManager.cancel(PendingIntent.getBroadcast(context, 0, update_index_intent, 0));
		    	//si reimposta l'alarm per l'update dell'indice artificiale
				Calendar calendar = Calendar.getInstance();
		    	//si imposta a partire dalla mezzanotte del giorno successivo
		    	calendar.add(Calendar.DATE, 1); 
		    	calendar.set(Calendar.HOUR_OF_DAY, 0);
		    	calendar.set(Calendar.MINUTE, 0);
		    	calendar.set(Calendar.SECOND, 0); 
		    	//si ripete l'alarm ogni giorno a mezzanotte
		    	alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
		    			AlarmManager.INTERVAL_DAY, PendingIntent.getBroadcast(context, 0, update_index_intent, 0));
				
		    	if(MainActivity.logEnabled){
		    		Log.d(MainActivity.AppName + " - TEST","TimeBatteryWatcher - set update day index alarm");
		    		int month =calendar.get(Calendar.MONTH)+1;    	
		        	Log.d(MainActivity.AppName + " - TEST", "TimeBatteryWatcher - UPDATE DAY INDEX ALARM: h:m:s=" 
		    				+ calendar.get(Calendar.HOUR_OF_DAY)+":"+ calendar.get(Calendar.MINUTE)+":"+ calendar.get(Calendar.SECOND) +
		    				"  "+calendar.get(Calendar.DATE)+"/"+month+"/"+calendar.get(Calendar.YEAR));        	
		        	Log.d(MainActivity.AppName + " - TEST", "TimeBatteryWatcher - milliseconds of the update day index alarm: " + calendar.getTimeInMillis());
		    	}
		    	/////////
		    	
		    	//if(alarm_id!=-1){ //si mette il controllo nel caso il valore salvato non esista
					
		    	System.out.println("C'è un alarm nelle preferences id=" + alarm_id);
										
		    	Alarm current_next_alarm = AlarmUtils.getAlarm(context,alarm_id);
		    	
		    	
		    	//quando il device completa il boot si deve controllare se l'alarm 
		    	//impostato è ancora valido o meno; se è ancora valido, non viene 
		    	//settato automaticamente nell'alarm manager, quindi bisogna impostarlo
		    	//manualmente; se non è valido (orario alarm già passato) allora si
		    	//si cerca un nuovo alarm
		    	
		    	
		    	Calendar alarmTime = Calendar.getInstance();
					
		    	if(MainActivity.logEnabled){
		    		int month=alarmTime.get(Calendar.MONTH)+1;	
		    		Log.d(MainActivity.AppName, "On Boot - NOW: h:m:s=" 
		    				+ alarmTime.get(Calendar.HOUR_OF_DAY)+":"+ alarmTime.get(Calendar.MINUTE)+":"+ alarmTime.get(Calendar.SECOND) +
		    				"  "+alarmTime.get(Calendar.DATE)+"/"+month+"/"+alarmTime.get(Calendar.YEAR));		
		    		Log.d(MainActivity.AppName, "On Boot - NOW MILLISECONDS: " + alarmTime.getTimeInMillis());		
		    	}							
										
		    	alarmTime.set(pref.getInt("alarm_year", -1), 
		    			pref.getInt("alarm_month", -1), pref.getInt("alarm_date", -1), 
		    			current_next_alarm.get_hour(), current_next_alarm.get_minute(),
		    			current_next_alarm.get_second());
										
		    	if(MainActivity.logEnabled){
		    		int month=alarmTime.get(Calendar.MONTH)+1;	
		    		Log.d(MainActivity.AppName, "On Boot - PREVIOUS ALARM: h:m:s=" 
		    				+ alarmTime.get(Calendar.HOUR_OF_DAY)+":"+ alarmTime.get(Calendar.MINUTE)+":"+ alarmTime.get(Calendar.SECOND) +
		    				"  "+alarmTime.get(Calendar.DATE)+"/"+month+"/"+alarmTime.get(Calendar.YEAR));		
		    		Log.d(MainActivity.AppName, "On Boot - PREVIOUS ALARM MILLISECONDS: " + alarmTime.getTimeInMillis());		
		    	}					
		    	
		    	
		    	//se il prossimo alarm che era stato impostato ha un istante di inizio
		    	//già passato, allora si cerca e si setta un altro alarm
		    	
		    	//in ogni caso si cancella dall'alarm manager l'alarm precedentemente impostato 
		    	AlarmUtils.cancelAlarm(context, current_next_alarm);	
					
					
		    	//////////////////////////////////////////////
		    	//SI TRACCIANO GLI INTERVALLI NON VALUTATI A CAUSA DEL DEVICE SPENTO
		    	LogUtils.offIntervalsTracking(context, pref, alarm_id);					
		    	//////////////////////////////////////////////
					
					
		    	//l'alarm ha un istante di inizio già passato e, quindi, non è più valido
		    	if(alarmTime.before(Calendar.getInstance())){
		    		
		    		if(MainActivity.logEnabled){
		    			Log.d(MainActivity.AppName,"On boot - the previous alarm is not valid; we set another alarm");		
		    		}
						
		    		
		    		//si setta un nuovo alarm solo se il livello di batteria è accettabile
		    		if(!pref.getBoolean("low_battery_status", false)){	
		    			
		    			//si fa partire l'intent service che imposta e lancia un nuovo alarm	 
			    		//(se il nuovo alarm è di stop, si fa ripartire subito il classificatore opportuno)
			    		context.getApplicationContext().startService(new Intent(context, SetNextAlarmIntentService.class)
			    			.putExtra("takeAllAlarms", true)
			    			.putExtra("prevAlarmNotAvailable", true)
			    			.putExtra("current_alarm_id", alarm_id)
			    			.putExtra("low_battery", pref.getBoolean("low_battery_status", false)));			    		
		    		}
		    		
		    		/*		    		
		    		Thread thread = new Thread(){					    	
		    			@Override
		    			public void run() {
		    				AlarmUtils.setNextAlarm(context,AlarmUtils.getAllAlarms(context),true,true,alarm_id); 
		    			}					    	
		    		};		
		    		//si fa partire il thread che imposta e lancia un nuovo alarm
		    		//(se il nuovo alarm è di stop, si fa ripartire subito il 
		    		//classificatore opportuno)
		    		thread.start();			
		    		*/			
		    	}			
		    	else{	
		    		//se l'alarm è ancora valido, lo si re-imposta solamente se non si ha
		    		//un livello di batteria critico
		    		if(!pref.getBoolean("low_battery_status", false)){		    			
		    			
		    			//se il next alarm settato è un evento di stop significa che si è
			    		//all'interno di un intervallo attivo iniziato in precedenza (infatti
			    		//gli alarm sono preordinati per fare in modo che dopo un evento di
			    		//start ci sia un evento di stop): se tale intervallo è un "intervallo
			    		//di esplorazione", allora significa che il service di activity
			    		//recognition dovrebbe essere già in esecuzione e, quindi, lo si
			    		//attiva; invece, se è un "intervallo con scalini", si attiva il
			    		//classificatore scalini/non_scalini (il cl. Google/scalini viene
			    		//fatto ripartire solo nel caso in cui i valori già ottenuti in questo
			    		//intervallo (se ce ne sono) non bastano per definire/confermare un
			    		//'intervallo con scalini')						
			    		if(!current_next_alarm.get_actionType()){
			    			
			    			//è un "intervallo di esplorazione"
			    			if(!current_next_alarm.isStepsInterval(curr_day_index)){ 
			    				
			    				//si fa ripartire il service di activity recognition solo se
			    				//l'intervallo non è stato interessato da un periodo di gioco
			    				//con scalini
			    				if(StairsClassifierReceiver.getStepsNumber(pref)<1){
			    					context.getApplicationContext().startService(new Intent(context, ActivityRecognitionRecordService.class));
			    					//si registra anche il receiver per la registrazione dell'attività utente
			    					//context.getApplicationContext().registerReceiver(userMotionReceiver, userMotionFilter);
			    					//context.getPackageManager().setComponentEnabledSetting(new ComponentName(context, UserMotionReceiver.class), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
			    				}
			    			}				
			    			else{ //è un "intervallo con scalini"
			    				
			    				//si fa ripartire il classificatore scalini solo se il numero
			    				//di scalini contati fino ad ora nell'intervallo è inferiore
			    				//alla soglia (soglia=1 per ora)
			    				if(StairsClassifierReceiver.getStepsNumber(pref)<1){
			    					context.getApplicationContext().startService(new Intent(context, SamplingClassifyService.class));
			    					//si registra anche il receiver
			    					context.getPackageManager().setComponentEnabledSetting(new ComponentName(context, StairsClassifierReceiver.class), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);					
			    				}
			    			}
			    		}						
			    		
			    		//si re-imposta l'alarm precedente
			    		//si crea il pending intent creando dapprima un intent con tutti i
			    		//dati dell'alarm per identificarlo in modo univoco
			    		PendingIntent pi = AlarmUtils.createPendingIntent(context, current_next_alarm, new int[]{alarmTime.get(Calendar.DATE), alarmTime.get(Calendar.MONTH), alarmTime.get(Calendar.YEAR)});
			    		alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime.getTimeInMillis(), pi);
			    		
			    		if(MainActivity.logEnabled){
			    			Log.d(MainActivity.AppName,"On boot - the previous alarm is valid; we set the same alarm");		
			    		}
		    		}
		    		else{
		    			if(MainActivity.logEnabled){
			    			Log.d(MainActivity.AppName,"On boot - the previous alarm is valid, but the battery level is very low; stop algorithm");		
			    		}
		    		}
		    		
		    	}		    	
		    	//}	
		    	
		    	//si re-imposta l'alarm che serve per recuperare il livello di carica della batteria;
		    	//è utile per attuare il bilanciamento energetico        	    	
    	    	Intent battery_intent = new Intent(context, TimeBatteryWatcher.class);
    	    	battery_intent.setAction("org.unipd.nbeghin.climbtheworld.BATTERY_ENERGY_BALANCING");    	
    	    	//si ripete l'alarm circa ogni ora (il primo lancio avviene entro 10 minuti dal boot)
    	    	alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 600000,
    	    			3600000, PendingIntent.getBroadcast(context, 0, battery_intent, 0));		    	
			}
		}
		else if(action.equalsIgnoreCase(ENERGY_BALANCING)){ //bilanciamento energetico
			
			//per attuare il bilanciamento energetico non si considerano gli eventi ACTION_BATTERY_LOW
			//e ACTION_BATTERY_OKAY (a causa di varie problematiche: vengono lanciati solo se il
			//device è attivo, la soglia del livello critico è specificata nella ROM e non si può
			//cambiare, ecc.; se si spegne con livello basso e si ricarica da spento superando la
			//soglia, l'evento non viene lanciato al boot non facendo riavviare l'algoritmo) né si
			//monitora costantemente il livello di batteria; infatti, il livello viene controllato
			//solamente ogni ora circa, operando eventualmente le opportune azioni correttive
			
			int alarm_id = pref.getInt("alarm_id", -1);
			//se l'algoritmo è stato configurato e c'è un prossimo alarm impostato
			if(pref.getBoolean("algorithm_configured", false) && alarm_id!=-1){	
				
				//si recupera il livello della batteria
				IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
				Intent batteryStatus = context.getApplicationContext().registerReceiver(null, ifilter);
				int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
				int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
				//percentuale di batteria
				float batteryPct = level / (float)scale;
				
				LogUtils.writeLogFile(context, "\nTimeBatteryWatcher - ENERGY BALANCING, "+dateFormat.format((Calendar.getInstance()).getTime())+" level: "+level+", scale: "+scale+", BATTERY: "+batteryPct);
				
				String toLog="";
				
				//se il livello di batteria è critico (<=10%) e non si sono già fatte le opportune
				//correzioni, si sospende l'algoritmo (ascolto e trigger)
				if(batteryPct<=0.1){
				
					toLog+="TimeBatteryWatcher - ENERGY BALANCING, LEVEL <=10%";
					
					if(!pref.getBoolean("low_battery_status", false)){
						
						//nelle shared preferences si salva il booleano che indica il livello critico della batteria 
						pref.edit().putBoolean("low_battery_status", true).commit();
						
						//si recupera il prossimo alarm impostato in precedenza
						Alarm current_next_alarm = AlarmUtils.getAlarm(context, alarm_id);
						//se è di stop significa che si è all'interno di un intervallo attivo e, quindi,
						//si ferma il classificatore eventualmente in esecuzione
						if(!current_next_alarm.get_actionType()){
							
							if(!current_next_alarm.isStepsInterval(pref.getInt("artificialDayIndex", 0))){
								if(GeneralUtils.isActivityRecognitionServiceRunning(context)){
									Log.d(MainActivity.AppName,"BATTERY LOW - Stop activity recognition");
									toLog+=", stop activity recognition";
									context.getApplicationContext().stopService(new Intent(context, ActivityRecognitionRecordService.class));
								}
							}
							else{
								//se l'intervallo è un "intervallo con scalini" e il gioco non è in esecuzione, allora
								//si ferma il classificatore scalini/non_scalini
								if(!ClimbActivity.samplingEnabled){
									Log.d(MainActivity.AppName,"BATTERY LOW - Gioco non attivo, si ferma il classificatore scalini");
									toLog+=", game not active, stop stairs classifier";
									context.getApplicationContext().stopService(new Intent(context, SamplingClassifyService.class));
									//si disabilita anche il receiver
									//context.getApplicationContext().unregisterReceiver(stairsReceiver);
									context.getPackageManager().setComponentEnabledSetting(new ComponentName(context, StairsClassifierReceiver.class), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
								}	
							}
						}
							
						//si fa partire l'intent service del setNextAlarm che, in tal caso, cancella
						//solamente il prossimo alarm, precedentemente impostato (in tal modo si
						//ferma l'algoritmo)
						context.getApplicationContext().startService(new Intent(context, SetNextAlarmIntentService.class)
						.putExtra("current_alarm_id", pref.getInt("alarm_id",-1))
						.putExtra("low_battery", true));		
						
						toLog+=", STOP ALGORITHM (cancel next alarm)";
					}
				}
				else {
					
					toLog+="TimeBatteryWatcher - ENERGY BALANCING, LEVEL >10%";
					
					//se l'ultima volta è stato rilevato un livello di batteria critico, ora quest'ultimo
					//si è alzato e, quindi, si fa ripartire l'algoritmo, impostando opportunamente il
					//prossimo alarm
					if(pref.getBoolean("low_battery_status", false)){
						
						//il livello della batteria non è più critico
						pref.edit().putBoolean("low_battery_status", false).commit();
						
						//si fa partire l'intent service del setNextAlarm che imposta il prossimo
						//alarm valido, facendo ripartire l'algoritmo
						context.getApplicationContext().startService(new Intent(context, SetNextAlarmIntentService.class)
							.putExtra("takeAllAlarms", true)
							.putExtra("prevAlarmNotAvailable", true)
							.putExtra("current_alarm_id", pref.getInt("alarm_id",-1))
							.putExtra("low_battery", false));			
						
						toLog+=", low before, now ok, RESTART ALGORITHM (set new next alarm)";
					}		
					
					boolean restart=false;
					
					//se il livello di batteria L è <=30% si abbassa la frequenza di aggiornamento
					//del servizio di activity recognition:
					//se 10%<L<=20%: ogni 20 secondi, se 20%<L<=30%: ogni 10 secondi, 
					//se L>30% ogni 5 secondi (impostazione di default)
					if(batteryPct<=0.2){						
						if(ActivityRecognitionUtils.getDetectionIntervalMilliseconds(context)!=20000){							
							//si imposta la frequenza di aggiornamento a 20 secondi
							ActivityRecognitionUtils.setDetectionIntervalMilliseconds(context, 20000);
							restart=true;
						}
					}
					else if(batteryPct<=0.3){					
						if(ActivityRecognitionUtils.getDetectionIntervalMilliseconds(context)!=10000){
							//si imposta la frequenza di aggiornamento a 10 secondi
							ActivityRecognitionUtils.setDetectionIntervalMilliseconds(context, 10000);	
							restart=true;
						}						
					}
					else{ //batteryPct>0.3						
						if(ActivityRecognitionUtils.getDetectionIntervalMilliseconds(context)!=5000){
							//si imposta la frequenza di aggiornamento a 5 secondi
							ActivityRecognitionUtils.setDetectionIntervalMilliseconds(context, 5000);
							restart=true;
						}						
					}
					//se si è cambiata la frequenza di aggiornamento e se il servizio di activity
					//recognition è in esecuzione, si riavvia
					if(restart && GeneralUtils.isActivityRecognitionServiceRunning(context)){
						context.getApplicationContext().stopService(new Intent(context, ActivityRecognitionRecordService.class));
						context.getApplicationContext().startService(new Intent(context, ActivityRecognitionRecordService.class));
						toLog+=", restart activity recognition service with "+ActivityRecognitionUtils.getDetectionIntervalMilliseconds(context)/100+"-seconds update interval";
					}
				}
				LogUtils.writeLogFile(context, toLog);
			}
		}
		/////////
		//PER TEST ALGORITMO
		else if(action.equalsIgnoreCase(UPDATE_DAY_INDEX_FOR_TESTING)){
			//serve per incrementare l'indice artificiale che rappresenta il giorno corrente della
			//settimana (utile per testare l'algoritmo con una settimana corta, composta ad esempio
			//da 1,2 giorni); tale indice viene opportunamente aggiornato ogni giorno a mezzanotte
					   	
			if(MainActivity.logEnabled){
				Log.d(MainActivity.AppName + " - TEST","TimeBatteryWatcher - day index update, midnight event");
			}
												
			//si aggiorna l'indice per indicare il giorno all'interno della settimana corta
			//salvandolo nelle shared preferences
			int currentDayIndex=pref.getInt("artificialDayIndex", 0);
			
			if(currentDayIndex==GeneralUtils.daysOfWeek-1){
				pref.edit().putInt("artificialDayIndex", 0).commit();
			}
			else{
				pref.edit().putInt("artificialDayIndex", currentDayIndex+1).commit();
			}
			
			//si aggiorna la data corrente salvandola nelle shared preferences
			Calendar cal = Calendar.getInstance();
	    	SimpleDateFormat calFormat = new SimpleDateFormat("yyyy-MM-dd");
	    	String dateFormatted = calFormat.format(cal.getTime());
	    	pref.edit().putString("dateOfIndex", dateFormatted).commit();
			
	    	if(MainActivity.logEnabled){
	    		Log.d(MainActivity.AppName + " - TEST","TimeBatteryWatcher - on update index, new index: "+pref.getInt("artificialDayIndex", 0)+", new date: " + dateFormatted);
	    	}
	    			   	
	    	/*
	    	//utile settare il prossimo alarm se si porta avanti l'ora manualmente, saltando
	    	//degli alarm in mezzo; prima si cerca di fermare il servizio di activity recognition,
	    	//se questo è attivo, in quanto può partire per un alarm di start settato in precedenza,
	    	//non ancora consumato, il cui tempo di inizio è già passato per lo spostamento 
	    	//d'orario (quindi, si avvia subito)
	    	
	    	Intent activityRecognitionIntent = new Intent(context, ActivityRecognitionRecordService.class);
		    context.stopService(activityRecognitionIntent);
	    	
			//si cancella il next alarm settato in precedenza
	    	int aa_id = pref.getInt("alarm_id", -1);
			System.out.println("ID da cancellare " + aa_id);
			AlarmUtils.cancelAlarm(context, AlarmUtils.getAlarm(context,aa_id));			
			//si imposta e si lancia il prossimo alarm
	    	AlarmUtils.setNextAlarm(context,AlarmUtils.lookupAlarmsForTemplate(context,AlarmUtils.getTemplate(context,pref.getInt("current_template", -1))));	
			*/
		}
		/////////
		else{				
			
			//id dell'alarm che era stato impostato, cioè questo
			final int this_alarm_id = pref.getInt("alarm_id",-1);			
			
			//si recupera l'indice del giorno corrente all'interno della settimana
			/////////		
	    	//PER TEST ALGORITMO
			int current_day_index = pref.getInt("artificialDayIndex", 0);
			///////// altrimenti l'indice del giorno è (Calendar.getInstance().get(Calendar.DAY_OF_WEEK))-1;
			
			
			//tale receiver riceve un intent che rappresenta un'azione di start di un intervallo:
			//si controlla se quello che inizia è un "intervallo di esplorazione" o un "intervallo
			//con scalini"; nel primo caso si attiva il classificatore Google di activity
			//recognition, mentre nel secondo il classificatore scalini/non_scalini;
			//in entrambi i casi, se il gioco è in esecuzione non viene fatto partire alcun
			//classificatore (il cl. scalini/non_scalini è già attivo in questo caso)
			if(action.equalsIgnoreCase(INTERVAL_START_ACTION)){				
						
				//si resetta il numero totale di attività rilevate, il numero di valori che
				//indicano un'attività fisica e le variabili per la somma dei pesi e per la somma
				//dei prodotti confidenze-pesi
				ActivityRecognitionIntentService.clearValuesCount(pref);
								
				Log.d(MainActivity.AppName,"START ACTION - Reset total number of values: " + ActivityRecognitionIntentService.getValuesNumber(pref));
			   	Log.d(MainActivity.AppName,"START ACTION - Reset number of activities: " + ActivityRecognitionIntentService.getActivitiesNumber(pref));
							   	
			   	//si resetta il numero di scalini rilevati con il classificatore 
			   	//scalini/non_scalini (si tiene comunque il fatto che in un periodo di gioco
			   	//possono essere stati fatti degli scalini)
			   	StairsClassifierReceiver.clearStepsNumber(pref);
			   	Log.d(MainActivity.AppName,"START ACTION - Reset number of steps: " + StairsClassifierReceiver.getStepsNumber(pref));
			   			   	
			   	//si resetta il valore salvato nelle preferences che indica se l'ultimo
			   	//intervallo considerato ha avuto almeno un periodo di gioco con scalini
				pref.edit().putBoolean("last_interval_with_steps", false).commit();
				Log.d(MainActivity.AppName,"START ACTION - Reset 'last_interval_with_steps' value: " + pref.getBoolean("last_interval_with_steps", false));
			   	
				//se è attivo il gioco non si fa partire il servizio di activity recognition/
			   	//il cl. scalini/non_scalini
				if(!ClimbActivity.samplingEnabled){//il gioco non è attivo
					
					Log.d(MainActivity.AppName,"START ACTION - Gioco non attivo");
					
					//si controlla se questo alarm definisce un "intervallo con scalini";
					//se è un "intervallo con scalini", allora non si attiva il servizio di 
					//activity recognition, ma il classificatore scalini/non_scalini
					if(!AlarmUtils.getAlarm(context, this_alarm_id).isStepsInterval(current_day_index)){
						
						//non è un "intervallo con scalini"
						
						if(!GeneralUtils.isActivityRecognitionServiceRunning(context)){
						   	context.getApplicationContext().startService(new Intent(context, ActivityRecognitionRecordService.class));
						   	//si abilita anche il receiver per la registrazione dell'attività utente
							//context.getApplicationContext().registerReceiver(userMotionReceiver, userMotionFilter);
							//context.getPackageManager().setComponentEnabledSetting(new ComponentName(context, UserMotionReceiver.class), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);					
						   	Log.d(MainActivity.AppName,"START ACTION - Service di activity recognition NON in esecuzione");
						}
						else{
							Log.d(MainActivity.AppName,"START ACTION - Service di activity recognition già in esecuzione");
						}
						
					}					
					else{ 
						//è un "intervallo con scalini"			
						Log.d(MainActivity.AppName,"START ACTION - 'Intervallo con scalini'");
						
						context.getApplicationContext().startService(new Intent(context, SamplingClassifyService.class));
						//si registra anche il receiver
						//context.getApplicationContext().registerReceiver(stairsReceiver, stairsActionFilter);
						context.getPackageManager().setComponentEnabledSetting(new ComponentName(context, StairsClassifierReceiver.class), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);					
					}	
				} 
				//se il gioco è attivo, si continua con esso
				
			}
			//tale receiver riceve un intent che rappresenta un'azione di stop di un intervallo:
			//si controlla se quello appena concluso è un "intervallo di esplorazione" o un
			//"intervallo con scalini"; nel primo caso si ferma il classificatore Google di
			//activity recognition e si valuta l'intervallo tenendo conto dell'attività
			//svolta o degli scalini fatti (valutazione massima in quest'ultimo caso); nel
			//secondo caso si ferma il classificatore scalini/non_scalini (se il gioco non è
			//attivo) e la valutazione dell'intervallo è data dalla quantità di scalini fatti
			else if(action.equalsIgnoreCase(INTERVAL_STOP_ACTION)){
				
				//l'intervallo appena concluso attualmente è un "intervallo di esplorazione"
				if(!AlarmUtils.getAlarm(context, this_alarm_id).isStepsInterval(current_day_index)){
					
					Log.d(MainActivity.AppName,"STOP ACTION - 'Intervallo di esplorazione'");
					
					//innanzitutto si ferma il servizio di activity recognition, se questo
					//è attivo
					if(GeneralUtils.isActivityRecognitionServiceRunning(context)){
						Log.d(MainActivity.AppName,"STOP ACTION - Stop activity recognition");
					   	context.getApplicationContext().stopService(new Intent(context, ActivityRecognitionRecordService.class));
						//si disabilita anche il receiver per la registrazione dell'attività utente
						//context.getApplicationContext().unregisterReceiver(userMotionReceiver);
						//context.getPackageManager().setComponentEnabledSetting(new ComponentName(context, UserMotionReceiver.class), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
					}					
					
					//quando viene lanciato questo evento di stop (fine di un intervallo di esplorazione
					//attivo) si controlla se in questo intervallo l'utente ha fatto almeno 1 scalino:
					//può averlo fatto in uno dei precedenti periodi di gioco all'interno dell'intervallo
					//oppure nel periodo di gioco corrente che è ancora in esecuzione (in entrambi i casi
					//il periodo di gioco può essere iniziato in un precedente intervallo e finire in
					//questo)					
										
					System.out.println("LAST INTERVAL WITH STEPS: " +pref.getBoolean("last_interval_with_steps", false));
					System.out.println("STEPS IN CURRENT GAME: " +ClimbActivity.stepsInCurrentGamePeriod());
					
					if(pref.getBoolean("last_interval_with_steps", false) || ClimbActivity.stepsInCurrentGamePeriod()
							|| StairsClassifierReceiver.getStepsNumber(pref)>=1){ 
						
						//l'intervallo appena concluso ha presentato un periodo di gioco in cui 
						//l'utente ha fatto almeno 1 scalino; l'intervallo diventa quindi un
						//"intervallo con scalini": sarà sicuramente attivo la prossima settimana
						
						Log.d(MainActivity.AppName,"STOP ACTION - L'intervallo ha un periodo di gioco con >= 1 scalino");
						
						IntervalEvaluationUtils.evaluateAndUpdateInterval(context, false, true, this_alarm_id);
					}
					else{
						
						Log.d(MainActivity.AppName,"STOP ACTION - L'intervallo non ha un periodo di gioco con scalini");
					
						//l'intervallo viene valutato con i dati del classificatore Google;				
						//si calcola la valutazione che determina la sua attivazione o meno per la
						//prossima settimana
						
						Log.d(MainActivity.AppName,"STOP ACTION - Total number of values: " + ActivityRecognitionIntentService.getValuesNumber(pref));
						Log.d(MainActivity.AppName,"STOP ACTION - Number of activities: " + ActivityRecognitionIntentService.getActivitiesNumber(pref));
						Log.d(MainActivity.AppName,"STOP ACTION - Sum of weights: " + ActivityRecognitionIntentService.getWeightsSum(pref));
						Log.d(MainActivity.AppName,"STOP ACTION - Sum of confidences-weights products: " + ActivityRecognitionIntentService.getConfidencesWeightsSum(pref));
						
						
						IntervalEvaluationUtils.evaluateAndUpdateInterval(context, false, false, this_alarm_id);
					}	
				}
				else{ //l'intervallo appena concluso è un "intervallo con scalini"
					
					Log.d(MainActivity.AppName,"STOP ACTION - 'Intervallo con scalini'");
					
					//se il gioco non è attivo allora si ferma il classificatore
					//scalini/non_scalini, disabilitando anche il relativo receiver
					if(!ClimbActivity.samplingEnabled){
						
						Log.d(MainActivity.AppName,"STOP ACTION - Gioco non attivo, si ferma il classificatore scalini");
						
						context.getApplicationContext().stopService(new Intent(context, SamplingClassifyService.class));
						//si disabilita anche il receiver
						//context.getApplicationContext().unregisterReceiver(stairsReceiver);
						context.getPackageManager().setComponentEnabledSetting(new ComponentName(context, StairsClassifierReceiver.class), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);					
					
					}	
					//in ogni caso si valuta l'intervallo di gioco per confermarlo o meno
					//per la prossima settimana
					
					Log.d(MainActivity.AppName,"STOP ACTION - Steps in interval: " + StairsClassifierReceiver.getStepsNumber(pref));
					
					IntervalEvaluationUtils.evaluateAndUpdateInterval(context, true, false, this_alarm_id);					
				}
				
				////////////////////////////
				//utile per scrivere il LOG
				pref.edit().putBoolean("next_alarm_mutated", false).commit();
				////////////////////////////				
				
				
				//nelle SharedPreferences si memorizza l'informazione che questo appena concluso è l'ultimo
				//intervallo valutato (informazione completa con data per essere precisi su quando è stato
				//valutato); questa informazione è utile per vedere qual è stato l'ultimo intervallo 
				//ad essere valutato ed autorizza o meno l'algoritmo ad ascoltare un certo intervallo
				//considerato (serve per attuare il bilanciamento energetico)				
				Calendar now_eval = Calendar.getInstance();				
				Editor editor = pref.edit();	
				//si imposta l'id dell'alarm di stop dell'intervallo valutato
				editor.putInt("last_evaluated_interval_stop_id", this_alarm_id);
				//si impostano giorno, mese e anno dell'ultimo intervallo valutato
				editor.putInt("last_evaluated_interval_alarm_date", now_eval.get(Calendar.DATE));
				editor.putInt("last_evaluated_interval_alarm_month", now_eval.get(Calendar.MONTH));
				editor.putInt("last_evaluated_interval_alarm_year", now_eval.get(Calendar.YEAR));   
				editor.commit();    				
			}
			
			//int aa_id = pref.getInt("alarm_id", -1);
			System.out.println("ID da cancellare " + this_alarm_id);
			 
			//AlarmUtils.cancelAlarm(context, AlarmUtils.getAlarm(context,this_alarm_id));
			
			//si fa partire l'intent service che cancella l'alarm "consumato" da questo on receive e
			//che imposta e lancia il prossimo alarm
			context.getApplicationContext().startService(new Intent(context, SetNextAlarmIntentService.class)
				.putExtra("takeAllAlarms", false)
				.putExtra("prevAlarmNotAvailable", false)
				.putExtra("current_alarm_id", this_alarm_id)
				.putExtra("low_battery", pref.getBoolean("low_battery_status", false)));
			
			/*
			Thread thread = new Thread(){				
				@Override
				public void run() {					
					//si imposta e si lancia il prossimo alarm
			    	AlarmUtils.setNextAlarm(context,AlarmUtils.getAllAlarms(context),false,false,this_alarm_id); //AlarmUtils.lookupAlarmsForTemplate(context,AlarmUtils.getTemplate(context,pref.getInt("current_template", -1)))	
				}
			};	
			//si fa partire il thread che imposta il prossimo alarm
			thread.start();	
			*/		
		}
	}
}