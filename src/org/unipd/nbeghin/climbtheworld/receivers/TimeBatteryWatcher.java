package org.unipd.nbeghin.climbtheworld.receivers;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.unipd.nbeghin.climbtheworld.ClimbActivity;
import org.unipd.nbeghin.climbtheworld.MainActivity;
import org.unipd.nbeghin.climbtheworld.activity.recognition.ActivityRecognitionIntentService;
import org.unipd.nbeghin.climbtheworld.models.Alarm;
import org.unipd.nbeghin.climbtheworld.services.ActivityRecognitionRecordService;
import org.unipd.nbeghin.climbtheworld.services.SamplingClassifyService;
import org.unipd.nbeghin.climbtheworld.util.AlarmUtils;
import org.unipd.nbeghin.climbtheworld.util.GeneralUtils;
import org.unipd.nbeghin.climbtheworld.util.IntervalEvaluationUtils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.util.Log;

public class TimeBatteryWatcher extends BroadcastReceiver {
	
	//stringhe che identificano le varie azioni per gli intent che può ricevere questo receiver
	//(questo receiver potrà quindi ricevere gli intent mandati da sendBroadcast() che hanno
	//impostato queste azioni)	
	private final String BOOT_ACTION = "android.intent.action.BOOT_COMPLETED";
	private final String INTERVAL_START_ACTION = "INTERVAL_START";
	private final String INTERVAL_STOP_ACTION = "INTERVAL_STOP";
	
	/////////	
	//PER TEST ALGORITMO
	private final String UPDATE_DAY_INDEX_FOR_TESTING = "UPDATE_DAY_INDEX_TESTING";
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
	public void onReceive(Context context, Intent intent) {
		
		Log.d(MainActivity.AppName, "TimeBatteryWatcher - ON RECEIVE");
		
		//si recupera l'oggetto delle shared preferences
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);//context.getSharedPreferences("appPrefs", 0);
		
		//si ottiene la stringa che descrive l'azione dell'intent
		String action = intent.getAction();
		
		//se il device ha completato il boot
		if (action.equalsIgnoreCase(BOOT_ACTION)) {
			
			Log.d(MainActivity.AppName, "TimeBatteryWatcher - BOOT ACTION");
			
			//riferimento all'alarm manager
			AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			
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
								
				int oldDayIndex=currentDayIndex;
				
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
										
					
					
					//////////////////////////////////////////////
					//SI TRACCIANO GLI INTERVALLI NON VALUTATI IN QUANTO IL DEVICE ERA SPENTO
										
					//si considerano gli intervalli che non sono stati valutati durante il
					//periodo di tempo in cui il device era spento (utile per tracciarli
					//nel file di output e,se si vuole, per cambiare la loro valutazione) 

					//si calcola il numero di giorni passati dallo spegnimento
					//se 0 si è nello stesso giorno, se 1 nel giorno successivo, e così via
					
					Calendar time_now = Calendar.getInstance();
					time_now.set(Calendar.HOUR_OF_DAY, 0);
					time_now.set(Calendar.MINUTE, 0);
					time_now.set(Calendar.SECOND, 0);					
					Calendar time_before = Calendar.getInstance();
					time_before.set(Calendar.HOUR_OF_DAY, 0);
					time_before.set(Calendar.MINUTE, 0);
					time_before.set(Calendar.SECOND, 0);
					time_before.set(Calendar.DATE,pref.getInt("alarm_date", -1));
					time_before.set(Calendar.MONTH,pref.getInt("alarm_month", -1));
					time_before.set(Calendar.YEAR,pref.getInt("alarm_year", -1));
					
					double time_diff = time_now.getTimeInMillis() - time_before.getTimeInMillis();					
					time_diff = time_diff / (24 * 60 * 60 * 1000); //hours in a day, minutes in a hour,
                    												//seconds in a minute, millis in a second
					int days_diff = (int) Math.round(time_diff);
					
					List<Alarm> alarms_lst = AlarmUtils.getAllAlarms(context);
					
					
					//se si tratta del primo intervallo (id_start=1 e id_stop=2) si è in
					//presenza di un nuovo giorno; si scrive il suo indice nel file di output
					if(alarm_id==2 && time_before.before(now)){
						//time_before.get(Calendar.DAY_OF_WEEK)-1;
						Log.d(MainActivity.AppName, "On Boot - device spento, giorno: " + oldDayIndex);
					}
					
					boolean stop=false;	
					//prima si considerano gli intervalli saltati nel giorno corrente
					for(int i=alarm_id; i<alarms_lst.size() && !stop; i++){
					
						Alarm e = alarms_lst.get(i);						
						//si impostano ora, minuti e secondi prendendo tali parametri dall'alarm salvato
						time_before.set(Calendar.HOUR_OF_DAY, e.get_hour());
						time_before.set(Calendar.MINUTE, e.get_minute());
						time_before.set(Calendar.SECOND, e.get_second());
						
						//se l'alarm è già passato
						if(time_before.before(now)){
							//ed è un alarm di stop
							if(!e.get_actionType()){								
								int stop_id = e.get_id();
								int start_id = stop_id-1;								
								Log.d(MainActivity.AppName, "On Boot - device spento nell'intervallo: start-stop: " + start_id+"-"+stop_id);
							}
						}
						else{ //l'alarm è valido, per cui l'intervallo potrà essere valutato
							  //(anche parzialmente se è già iniziato, cioè se l'alarm è di stop)
							stop=true;
						}
					}
						
					
					if(stop==false){
						
						/////////
						//PER TEST ALGORITMO: si inizializza l'indice artificiale 
						int ii = oldDayIndex; //time_before.get(Calendar.DAY_OF_WEEK)-1
						
						//indice per scorrere il numero di giorni di differenza
						int day_i = 0;
						//se gli alarm del giorno precedente sono tutti passati, si incrementa
						//il giorno 
						while(day_i<days_diff){			
							
							//si resettano ora, minuti e secondi
							time_before.set(Calendar.HOUR_OF_DAY, 0);
							time_before.set(Calendar.MINUTE, 0);
							time_before.set(Calendar.SECOND, 0);
							
							time_before.add(Calendar.DATE, 1);
							
							/////////	
							//PER TEST ALGORITMO: si aggiorna l'indice artificiale man mano che
							//si incrementa la data
							ii=AlarmUtils.getNextDayIndex(ii);
							/////////
							//time_before.get(Calendar.DAY_OF_WEEK)-1
							
							Log.d(MainActivity.AppName, "On Boot - device spento, indice giorno: " + ii);
							
							
							for(int i=0; i<alarms_lst.size() && !stop; i++){
								Alarm e = alarms_lst.get(i);
								
								time_before.set(Calendar.HOUR_OF_DAY, e.get_hour());
								time_before.set(Calendar.MINUTE, e.get_minute());
								time_before.set(Calendar.SECOND, e.get_second());
																
								//se l'alarm è già passato
								if(time_before.before(now)){
									//ed è un alarm di stop
									if(!e.get_actionType()){								
										int stop_id = e.get_id();
										int start_id = stop_id-1;								
										Log.d(MainActivity.AppName, "On Boot - device spento nell'intervallo: start-stop: " + start_id+"-"+stop_id);
									}
								}
								else{ //l'alarm è valido, per cui l'intervallo potrà essere valutato
									  //(anche parzialmente se è già iniziato, cioè se l'alarm è di stop)
									stop=true;
								}
							}
							
							day_i++;
						}
					}
					
					//////////////////////////////////////////////
					
					
					//quando il device completa il boot non setta automaticamente l'alarm impostato
					//precedentemente tramite l'alarm manager, quindi bisogna settarlo nuovamente
					
					//prima si cancella l'alarm che era stato settato in precedenza					
					//AlarmUtils.cancelAlarm(context, current_next_alarm);	
					
					//poi si imposta il prossimo alarm
			    	//AlarmUtils.setNextAlarm(context,AlarmUtils.getAllAlarms(context)); //AlarmUtils.lookupAlarmsForTemplate(context,AlarmUtils.getTemplate(context,pref.getInt("current_template", -1)))
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
					
					if(alarmTime.before(Calendar.getInstance())){
						
					    if(MainActivity.logEnabled){
				 	    	Log.d(MainActivity.AppName,"On boot - the previous alarm is not valid; we set another alarm");		
				 	    }
												
						//si imposta e si lancia un nuovo alarm
				    	AlarmUtils.setNextAlarm(context,AlarmUtils.getAllAlarms(context)); 
					}			
					else{			
						//si re-imposta l'alarm precedente
						//si crea il pending intent creando dapprima un intent con tutti i dati dell'alarm
				    	//per identificarlo in modo univoco
				    	PendingIntent pi = AlarmUtils.createPendingIntent(context, current_next_alarm, new int[]{alarmTime.get(Calendar.DATE), alarmTime.get(Calendar.MONTH), alarmTime.get(Calendar.YEAR)});
				 	    alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime.getTimeInMillis(), pi);
				 	    
				 	    if(MainActivity.logEnabled){
				 	    	Log.d(MainActivity.AppName,"On boot - the previous alarm is valid; we set the same alarm");		
				 	    }
					}
						
					
					//se il next alarm settato è un evento di stop e l'intervallo che
			    	//definisce non è un intervallo di gioco, allora significa che 
					//il service di activity recognition dovrebbe essere già attivo
			    	//(infatti gli alarm sono preordinati per fare in modo che dopo un evento
			    	//di attivazione ci sia un evento di stop); in tal caso si attiva il
			    	//servizio			    	
			    	Alarm next_alarm = AlarmUtils.getAlarm(context,pref.getInt("alarm_id", -1));
			    	
					if(!next_alarm.get_actionType()){
						
						if(!next_alarm.isStepsInterval(currentDayIndex)){
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
			
			//id dell'alarm che era stato impostato, cioè questo
			int this_alarm_id = pref.getInt("alarm_id",-1);			
			
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
				ActivityRecognitionIntentService.clearValuesCount();
								
				Log.d(MainActivity.AppName,"START ACTION - Reset total number of values: " + ActivityRecognitionIntentService.getValuesNumber());
			   	Log.d(MainActivity.AppName,"START ACTION - Reset number of activities: " + ActivityRecognitionIntentService.getActivitiesNumber());
							   	
			   	//si resetta il numero di scalini rilevati con il classificatore 
			   	//scalini/non_scalini (si tiene comunque il fatto che in un periodo di gioco
			   	//possono essere stati fatti degli scalini)
			   	StairsClassifierReceiver.clearStepsNumber();
			   	Log.d(MainActivity.AppName,"START ACTION - Reset number of steps: " + StairsClassifierReceiver.getStepsNumber());
			   	
			   	
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
					   	context.stopService(new Intent(context, ActivityRecognitionRecordService.class));
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
										
					System.out.println("LAST INTERVAL WITH STEPS: " +pref.getInt("last_interval_with_steps", -1));
					System.out.println("STEPS IN CURRENT GAME: " +ClimbActivity.stepsInCurrentGamePeriod());
					
					if(pref.getInt("last_interval_with_steps", -1)==this_alarm_id || ClimbActivity.stepsInCurrentGamePeriod()){
						
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
						
						Log.d(MainActivity.AppName,"STOP ACTION - Total number of values: " + ActivityRecognitionIntentService.getValuesNumber());
						Log.d(MainActivity.AppName,"STOP ACTION - Number of activities: " + ActivityRecognitionIntentService.getActivitiesNumber());
						Log.d(MainActivity.AppName,"STOP ACTION - Sum of weights: " + ActivityRecognitionIntentService.getWeightsSum());
						Log.d(MainActivity.AppName,"STOP ACTION - Sum of confidences-weights products: " + ActivityRecognitionIntentService.getConfidencesWeightsSum());
						
						
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
					
					Log.d(MainActivity.AppName,"STOP ACTION - Steps in interval: " + StairsClassifierReceiver.getStepsNumber());
					
					IntervalEvaluationUtils.evaluateAndUpdateInterval(context, true, false, this_alarm_id);					
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