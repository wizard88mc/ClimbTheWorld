package org.unipd.nbeghin.climbtheworld.receivers;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.unipd.nbeghin.climbtheworld.ClimbActivity;
import org.unipd.nbeghin.climbtheworld.MainActivity;
import org.unipd.nbeghin.climbtheworld.activity.recognition.ActivityRecognitionIntentService;
import org.unipd.nbeghin.climbtheworld.models.Alarm;
import org.unipd.nbeghin.climbtheworld.services.ActivityRecognitionRecordService;
import org.unipd.nbeghin.climbtheworld.util.AlarmUtils;
import org.unipd.nbeghin.climbtheworld.util.GeneralUtils;
import org.unipd.nbeghin.climbtheworld.util.IntervalEvaluationUtils;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class TimeBatteryWatcher extends BroadcastReceiver {
	
	//stringhe che identificano le varie azioni per gli intent che può ricevere questo receiver
	//(questo receiver potrà quindi ricevere gli intent mandati da sendBroadcast() che hanno
	//impostato queste azioni)	
	private final String BOOT_ACTION = "android.intent.action.BOOT_COMPLETED";
	private final String ACTIVITY_RECOGNITION_START_ACTION = "ACTIVITY_RECOGNITION_START";
	private final String ACTIVITY_RECOGNITION_STOP_ACTION = "ACTIVITY_RECOGNITION_STOP";
	
	/////////	
	//PER TEST ALGORITMO
	private final String UPDATE_DAY_INDEX_FOR_TESTING = "UPDATE_DAY_INDEX_TESTING";
	/////////	
	
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
	public void onReceive(Context context, Intent intent) {
		
		Log.d(MainActivity.AppName, "TimeBatteryWatcher - ON RECEIVE");
		
		//TimeBatteryWatcher.context=context;
		
		//si recupera l'oggetto delle shared preferences
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);//context.getSharedPreferences("appPrefs", 0);
		
		//si ottiene la stringa che descrive l'azione dell'intent
		String action = intent.getAction();
		
		//se il device ha completato il boot
		if (action.equalsIgnoreCase(BOOT_ACTION)) {
			
			Log.d(MainActivity.AppName, "TimeBatteryWatcher - BOOT ACTION");
			
			//si setta il prossimo alarm prendendo quello salvato nelle shared preferences
			//se non c'è non si fa nulla			
			
			//se si vede che si tratta del primo run dell'applicazione
			if(pref.getBoolean("firstRun", true)){	
				System.out.println("init");
				//si inizializzano gli alarm e le relative shared preferences
				GeneralUtils.initializeAlarmsAndPrefs(context,pref);	
			}
			else{	
				/////////				
				//PER TEST ALGORITMO 
				//si aggiorna l'indice artificiale che rappresenta il giorno corrente della 
				//settimana (utile per testare l'algoritmo con una settimana corta, composta ad 
				//esempio da 1,2 giorni); tale indice viene opportunamente aggiornato considerando
				//i giorni passati dall'ultimo spegnimento del device
				if(MainActivity.logEnabled){
					Log.d(MainActivity.AppName + " - TEST","TimeBatteryWatcher - day index update, on boot event");
				}
									
				//si aggiorna l'indice per indicare il giorno all'interno della settimana corta
				//salvandolo nelle shared preferences
				int currentDayIndex=pref.getInt("artificialDayIndex", 0);
								
				Calendar now = Calendar.getInstance();
				now.set(Calendar.HOUR_OF_DAY, 0);
				now.set(Calendar.MINUTE, 0);
				now.set(Calendar.SECOND, 0);
				
				Calendar before = Calendar.getInstance();
				before.set(Calendar.HOUR_OF_DAY, 0);
				before.set(Calendar.MINUTE, 0);
				before.set(Calendar.SECOND, 0);
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
		    	update_index_intent.setAction("UPDATE_DAY_INDEX_TESTING");  		    	
		    	AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
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
		    	
		    	
		    	//dalle preferences salvate si ottiene l'id e gli altri dati relativi
				//al prossimo alarm che era stato settato in precedenza				
				int alarm_id = pref.getInt("alarm_id", -1);
				
				if(alarm_id!=-1){ //si mette il controllo nel caso il valore salvato non esista
					
					System.out.println("C'è un alarm nelle preferences id=" + alarm_id);
					
					Alarm current_next_alarm = AlarmUtils.getAlarm(context,alarm_id);
										
					//quando il device completa il boot non setta automaticamente l'alarm impostato
					//precedentemente tramite l'alarm manager, quindi bisogna settarlo nuovamente
					
					//prima si cancella l'alarm che era stato settato in precedenza					
					AlarmUtils.cancelAlarm(context, current_next_alarm);	
					
					//poi si imposta il prossimo alarm
			    	AlarmUtils.setNextAlarm(context,AlarmUtils.getAllAlarms(context)); //AlarmUtils.lookupAlarmsForTemplate(context,AlarmUtils.getTemplate(context,pref.getInt("current_template", -1)))
					
					
			    	
			    	/* DA USARE QUESTO, CANCELLANDO LE DUE PRECEDENTI ISTRUZIONI di cancel e setnext
			    	
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
					//già passato, allora si cancella l'alarm in questione tramite l'alarm 
					//manager e poi si cerca e si setta un altro alarm
					
					if(alarmTime.before(Calendar.getInstance())){
						
						System.out.println("alarm time già passato - lo cancello e ne imposto un altro");
						
						//si cancella l'alarm già passato
						AlarmUtils.cancelAlarm(context, current_next_alarm);							
						//si imposta e si lancia il prossimo alarm
				    	AlarmUtils.setNextAlarm(context,AlarmUtils.getAllAlarms(context)); 
					}				
					*/
					
					//se il next alarm settato è un evento di stop e l'intervallo che
			    	//definisce non è un intervallo di gioco, allora significa che 
					//il service di activity recognition dovrebbe essere già attivo
			    	//(infatti gli alarm sono preordinati per fare in modo che dopo un evento
			    	//di attivazione ci sia un evento di stop); in tal caso si attiva il
			    	//servizio			    	
			    	Alarm next_alarm = AlarmUtils.getAlarm(context,pref.getInt("alarm_id", -1));
			    	
					if(!next_alarm.get_actionType()){
						
						if(!next_alarm.isGameInterval(currentDayIndex)){
							context.startService(new Intent(context, ActivityRecognitionRecordService.class));
						   	//si registra anche il receiver per la registrazione dell'attività utente
							//context.getApplicationContext().registerReceiver(userMotionReceiver, userMotionFilter);
						   	//context.getPackageManager().setComponentEnabledSetting(new ComponentName(context, UserMotionReceiver.class), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
						}					   
					}
					
				}			    	
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
			
			//id del prossimo alarm impostato
			int this_alarm_id = pref.getInt("alarm_id",-1);			
			
			//si recupera l'indice del giorno corrente all'interno della settimana
			/////////		
	    	//PER TEST ALGORITMO
			int current_day_index = pref.getInt("artificialDayIndex", 0);
			///////// altrimenti l'indice del giorno è (Calendar.getInstance().get(Calendar.DAY_OF_WEEK))-1;
			
			
			//se tale receiver riceve un intent che rappresenta un'azione di start del processo
			//di registrazione dell'attività utente allora si controlla se questo processo è
			//già in esecuzione o meno; se non è attivo e se il gioco non è in esecuzione, allora
			//si fa partire il processo si activity recognition
			if(action.equalsIgnoreCase(ACTIVITY_RECOGNITION_START_ACTION)){				
						
				//si resetta il numero totale di attività rilevate e quello che conta
				//quanti valori indicano un'attività fisica
				ActivityRecognitionIntentService.clearValuesCount();
				
				//si cambia la coppia di liste da utilizzare per contenere i livelli di
				//confidenza e i pesi
				//ActivityRecognitionIntentService.setUsedList(!ActivityRecognitionIntentService.getUsedList());
			
				//si fa il clear delle due liste, così da svuotarle
				ActivityRecognitionIntentService.clearLists();
								
				Log.d(MainActivity.AppName,"START ACTION - Reset total number of values: " + ActivityRecognitionIntentService.getValuesNumber());
			   	Log.d(MainActivity.AppName,"START ACTION - Reset number of activities: " + ActivityRecognitionIntentService.getActivitiesNumber());
				
				//se è attivo il gioco non si fa partire il servizio di activity recognition
				
				if(!ClimbActivity.samplingEnabled){//il gioco non è attivo
					
					Log.d(MainActivity.AppName,"START ACTION - Gioco non attivo");
					
					//si controlla se questo alarm definisce un intervallo di gioco
					//se è un intervallo di gioco, allora non si attiva il servizio di 
					//activity recognition
					if(!AlarmUtils.getAlarm(context, this_alarm_id).isGameInterval(current_day_index)){
						
						//non è un intervallo di gioco
						
						if(!GeneralUtils.isActivityRecognitionServiceRunning(context)){							
						    Intent activityRecognitionIntent = new Intent(context, ActivityRecognitionRecordService.class);
						   	context.startService(activityRecognitionIntent);
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
						//è un intervallo di gioco						
						Log.d(MainActivity.AppName,"START ACTION - Intervallo di gioco");
					}	
				}
				
			}
			//se tale receiver riceve un intent che rappresenta un'azione di stop del processo
			//di registrazione dell'attività utente allora il servizio viene fermato
			else if(action.equalsIgnoreCase(ACTIVITY_RECOGNITION_STOP_ACTION)){
								
				//innanzitutto si ferma il servizio di activity recognition, se questo
				//è attivo
				if(GeneralUtils.isActivityRecognitionServiceRunning(context)){
					Log.d(MainActivity.AppName,"STOP ACTION - Stop activity recognition");
				   	context.stopService(new Intent(context, ActivityRecognitionRecordService.class));
					//si disabilita anche il receiver per la registrazione dell'attività utente
					//context.getApplicationContext().unregisterReceiver(userMotionReceiver);
					//context.getPackageManager().setComponentEnabledSetting(new ComponentName(context, UserMotionReceiver.class), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
				}
									
				//quando viene lanciato questo evento di stop (fine di un intervallo di esplorazione
				//attivo) si controlla se in questo intervallo l'utente ha fatto almeno 1 scalino:
				//può averlo fatto in uno dei precedenti periodi di gioco all'interno dell'intervallo
				// oppure nel periodo di gioco corrente che è ancora in esecuzione (in entrambi i casi
				//il periodo di gioco può essere iniziato in un precedente intervallo e finire in
				//questo)
				
				if(pref.getInt("last_interval_with_steps", -1)==this_alarm_id || ClimbActivity.stepsInCurrentGamePeriod()){
						
					//l'intervallo appena concluso ha presentato un periodo di gioco in cui 
					//l'utente ha fatto almeno 1 scalino; l'intervallo diventa quindi un
					//"intervallo di gioco": sarà sicuramente attivo la prossima settimana
					
					Log.d(MainActivity.AppName,"STOP ACTION - L'intervallo ha un periodo di gioco con >= 1 scalino");
					
					IntervalEvaluationUtils.evaluateAndUpdateInterval(context, true, this_alarm_id);
				}
				else{
					
					Log.d(MainActivity.AppName,"STOP ACTION - L'intervallo non ha un periodo di gioco con scalini");
				
					//l'intervallo viene valutato con i dati del classificatore Google;				
					//si calcola la valutazione che determina la sua attivazione o meno per la
					//prossima settimana
					
					Log.d(MainActivity.AppName,"STOP ACTION - List conf size: " + ActivityRecognitionIntentService.getConfidencesList().size());
					Log.d(MainActivity.AppName,"STOP ACTION - List weight size: " + ActivityRecognitionIntentService.getWeightsList().size());
					Log.d(MainActivity.AppName,"STOP ACTION - Total number of values: " + ActivityRecognitionIntentService.getValuesNumber());
					Log.d(MainActivity.AppName,"STOP ACTION - Number of activities: " + ActivityRecognitionIntentService.getActivitiesNumber());
					
					
					IntervalEvaluationUtils.evaluateAndUpdateInterval(context, false, this_alarm_id);
				}		
			}
			
			int aa_id = pref.getInt("alarm_id", -1);
			System.out.println("ID da cancellare " + aa_id);
			
			//si cancella l'alarm che è stato "consumato" da questo on receive
			AlarmUtils.cancelAlarm(context, AlarmUtils.getAlarm(context,aa_id));							
			//si imposta e si lancia il prossimo alarm
	    	AlarmUtils.setNextAlarm(context,AlarmUtils.getAllAlarms(context)); //AlarmUtils.lookupAlarmsForTemplate(context,AlarmUtils.getTemplate(context,pref.getInt("current_template", -1)))	
		}
	}
	
}