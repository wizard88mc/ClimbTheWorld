package org.unipd.nbeghin.climbtheworld.util;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.unipd.nbeghin.climbtheworld.MainActivity;
import org.unipd.nbeghin.climbtheworld.comparator.AlarmComparator;
import org.unipd.nbeghin.climbtheworld.db.DbHelper;
import org.unipd.nbeghin.climbtheworld.models.Alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

import com.j256.ormlite.android.AndroidConnectionSource;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class AlarmUtils {
	
	//private static PreparedQuery<Alarm> alarmsForTemplateQuery = null;
	private static Random rand = new Random();
	
	/**
	 * Costruttore della classe.
	 */
	private AlarmUtils(){	
	}

	
    public static void setupAlarmsDB(Context context){
    	
    	ConnectionSource connectionSource = new AndroidConnectionSource(DbHelper.getInstance(context));
    	    	
    	try{    		
    		DaoManager.createDao(connectionSource, Alarm.class);
			//DaoManager.createDao(connectionSource, TimeTemplate.class);
			//DaoManager.createDao(connectionSource, AlarmTimeTemplate.class);
			
			TableUtils.createTableIfNotExists(connectionSource, Alarm.class);
			//TableUtils.createTableIfNotExists(connectionSource, TimeTemplate.class);
			//TableUtils.createTableIfNotExists(connectionSource, AlarmTimeTemplate.class);
			
    	} catch (SQLException e) {
			e.printStackTrace();
		}
    }
    
    
    public static void createAlarms(Context context){
    	
    	DbHelper helper = DbHelper.getInstance(context);
    	
    	//esempi    	
    	
    	//creo alarm
    	//boolean bb[] = new boolean[] {true,true,true,true,true,true,true};
    	//boolean bb1[] = new boolean[] {false,false,true,true,true,true,true};
    	//boolean noweekend[] = new boolean[] {false,true,true,true,true,true,false}; 
    	boolean bb[] = new boolean[] {true,true};
    	//float pf[] = new float[] {0.25f,0.25f,0.25f,0.25f,0.25f,0.25f,0.25f};
    	float pf[] = new float[] {0.25f,0.25f};
		Alarm alm1 = new Alarm(9,55,50,true,new boolean[]{false,true},pf);
		Alarm alm2 = new Alarm(9,57,50,false,new boolean[]{false,true},pf);
		Alarm alm3 = new Alarm(11,25,10,true,new boolean[]{true,false},pf); 
		Alarm alm4 = new Alarm(11,27,50,false,new boolean[]{true,false},pf);
		Alarm alm5 = new Alarm(14,52,15,true,bb,pf);
		Alarm alm6 = new Alarm(14,54,50,false,bb,pf);
		Alarm alm7 = new Alarm(14,54,51,true,bb,pf);
		Alarm alm8 = new Alarm(14,55,50,false,bb,pf);
		Alarm alm9 = new Alarm(15,21,10,true,bb,pf);
		Alarm alm10 = new Alarm(15,22,50,false,bb,pf);
		
		alm9.setStepsInterval(PreferenceManager.getDefaultSharedPreferences(context).getInt("artificialDayIndex", 0), true);
		alm10.setStepsInterval(PreferenceManager.getDefaultSharedPreferences(context).getInt("artificialDayIndex", 0), true);
		
		/*
		//creo template
		
		//template usato nella prima settimana
		TimeTemplate tt1 = new TimeTemplate(1,true);		
		//template che viene popolato di alarm durante il processo di learning (prima settimana)
		TimeTemplate tt2 = new TimeTemplate(2,true); 
		//template che viene popolato di alarm durante la seconda settimana
		TimeTemplate tt3 = new TimeTemplate(3,true); 
		
		//metto insieme alarm e relativo template facendo il join delle tabelle
		
		//esempio: il template 1 contiene 2 alarm
		AlarmTimeTemplate att1 = new AlarmTimeTemplate(alm1,tt1);
		AlarmTimeTemplate att2 = new AlarmTimeTemplate(alm2,tt1);
		AlarmTimeTemplate att3 = new AlarmTimeTemplate(alm3,tt1);
		AlarmTimeTemplate att4 = new AlarmTimeTemplate(alm4,tt1);
		AlarmTimeTemplate att5 = new AlarmTimeTemplate(alm5,tt1);
		AlarmTimeTemplate att6 = new AlarmTimeTemplate(alm6,tt1);
				
	    */
				
		// persist the alarm object to the database
		helper.getAlarmDao().createIfNotExists(alm1);
		helper.getAlarmDao().createIfNotExists(alm2);
		helper.getAlarmDao().createIfNotExists(alm3);
		helper.getAlarmDao().createIfNotExists(alm4);
		helper.getAlarmDao().createIfNotExists(alm5);
		helper.getAlarmDao().createIfNotExists(alm6);
		helper.getAlarmDao().createIfNotExists(alm7);
		helper.getAlarmDao().createIfNotExists(alm8);
		helper.getAlarmDao().createIfNotExists(alm9);
		helper.getAlarmDao().createIfNotExists(alm10);
		
		/*
		helper.getTimeTemplateDao().createIfNotExists(tt1);
		helper.getTimeTemplateDao().createIfNotExists(tt2);
		helper.getTimeTemplateDao().createIfNotExists(tt3);
		
		helper.getAlarmTimeTemplateDao().createIfNotExists(att1);
		helper.getAlarmTimeTemplateDao().createIfNotExists(att2);
		helper.getAlarmTimeTemplateDao().createIfNotExists(att3);
		helper.getAlarmTimeTemplateDao().createIfNotExists(att4);
		helper.getAlarmTimeTemplateDao().createIfNotExists(att5);
		helper.getAlarmTimeTemplateDao().createIfNotExists(att6);
		*/
    }
    
   /* 
    //forse non serve
    private static void saveAlarmInDB(Context context, Alarm alarm, int template_id){
    	    	
    	DbHelper helper = DbHelper.getInstance(context);
    	    	
    	//persists the alarm object to the database
    	helper.getAlarmDao().createIfNotExists(alarm);
    		
    	//the AlarmTimeTemplate object is created (by associating the alarm passed as a parameter and the template)
    	//and is saved into the database
    	helper.getAlarmTimeTemplateDao().createIfNotExists(new AlarmTimeTemplate(alarm, getTemplate(context, template_id)));
        	
    }
    */
    
    public static Alarm getAlarm(Context context, int alarm_id){ 
    	
    	return DbHelper.getInstance(context).getAlarmDao().queryForId(alarm_id);
    }
    
    
    public static List<Alarm> getAlarmsByActionType(Context context, boolean type){
    	
    	List<Alarm> alarms = null;
      	
    	RuntimeExceptionDao<Alarm, Integer> alarmDao = DbHelper.getInstance(context).getAlarmDao();    	
    	
    	
    	QueryBuilder<Alarm, Integer> queryBuilder = alarmDao.queryBuilder();

    	// get the WHERE object to build our query; the type field must be equal to 'type' parameter
    	try {
			alarms = queryBuilder.where().eq(Alarm.ACTION_TYPE_FIELD_NAME, type).query();
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	
    	return alarms;
    	
    }
    
    /*
    public static TimeTemplate getTemplate(Context context, int template_id){ 
    	
    	return DbHelper.getInstance(context).getTimeTemplateDao().queryForId(template_id);
    }


    public static List<Alarm> lookupAlarmsForTemplate(Context context, TimeTemplate template) {
    	
    	List<Alarm> alarms = null;
    	
    	DbHelper helper = DbHelper.getInstance(context);
    	    	
    	try{
    		if (alarmsForTemplateQuery == null) {
    			alarmsForTemplateQuery = makeAlarmsForTemplateQuery(helper);
    		}
    		alarmsForTemplateQuery.setArgumentHolderValue(0, template);
    		alarms=helper.getAlarmDao().query(alarmsForTemplateQuery);    		
    	} catch (SQLException e) {
			e.printStackTrace();
		}  
    	
    	return alarms;
    }
    
    
    
	private static PreparedQuery<Alarm> makeAlarmsForTemplateQuery(DbHelper dbh) throws SQLException {
		// build our inner query for AlarmTimeTemplate objects
		QueryBuilder<AlarmTimeTemplate, Integer> alarmTemplateQb = dbh.getAlarmTimeTemplateDao().queryBuilder();
		// just select the alarm-id field
		alarmTemplateQb.selectColumns(AlarmTimeTemplate.ALARM_ID_FIELD_NAME);
		SelectArg timetemplateSelectArg = new SelectArg();

		alarmTemplateQb.where().eq(AlarmTimeTemplate.TIMETEMPLATE_ID_FIELD_NAME, timetemplateSelectArg);

		// build our outer query for Alarm objects
		QueryBuilder<Alarm, Integer> alarmQb = dbh.getAlarmDao().queryBuilder();
		// where the id matches in the alarm-id from the inner query
		alarmQb.where().in(Alarm.ID_FIELD_NAME, alarmTemplateQb);
		return alarmQb.prepare();
	}
	
	*/
	
	
	
    public static List<Alarm> getAllAlarms(Context context) {
    	
    	DbHelper helper = DbHelper.getInstance(context);
    	    	
    	return helper.getAlarmDao().queryForAll();  
    }
	
	
	
	
	
   
    //Metodo per settare il prossimo alarm; chiamato la prima volta per inizializzare il primo alarm,
	//nell'on receive per l'on boot action 
    /**
     * Sets up the next alarm; it is also called to initialize the first alarm.    
     * @param context context of the application.
     * @param alarms list of all alarms saved in the database.
     */
	public static void setNextAlarm(Context context, List<Alarm> alarms){
				
		RuntimeExceptionDao<Alarm, Integer> alarmDao = DbHelper.getInstance(context).getAlarmDao();    	
		
		int artificialIndex = PreferenceManager.getDefaultSharedPreferences(context).getInt("artificialDayIndex", 0);//context.getSharedPreferences("appPrefs", 0).getInt("artificialDayIndex", 0);
				
		if(MainActivity.logEnabled){
			Log.d(MainActivity.AppName, "AlarmUtils - SetNextAlarm: lista prima di collection sort");
			for (Alarm e : alarms) {		    
				Log.d(MainActivity.AppName,"Alarm id: " + e.get_id() + " - hms: " + e.get_hour() + "," + e.get_minute() + "," + e.get_second());			
			}
		}
			
		
		Alarm nextAlarm=null;
				
		//prima si ordina la lista di alarm per orario (perché magari all'inizio 
		//gli alarm non sono stati inseriti in ordine di orario; utile anche per il 
		//fatto di poterne aggiungere/togliere in futuro senza doversi preoccupare
		//del loro ordinamento)
		//FORSE DA TOGLIERE SE SI INSERISCONO GLI ALARM GIA' PRONTI
		Collections.sort(alarms,new AlarmComparator());
		
		
		if(MainActivity.logEnabled){
			Log.d(MainActivity.AppName, "AlarmUtils - SetNextAlarm: lista dopo collection sort");
			for (Alarm e : alarms) {		    
				Log.d(MainActivity.AppName,"Alarm id: " + e.get_id() + " - hms: " + e.get_hour() + "," + e.get_minute() + "," + e.get_second());			
			}
		}
		
		Calendar now = Calendar.getInstance();
		Calendar alarmTime=Calendar.getInstance();	
		//DA RIPRISTINARE CON SETTIMANA DI 7 GIORNI
		//int today = alarmTime.get(Calendar.DAY_OF_WEEK)-1;
				
		
		boolean stop=false;
		
		for(int i=0; i<alarms.size() && !stop; i++){
			
			Alarm e = alarms.get(i);
			
			//Calendar alarmTime=Calendar.getInstance();	
			//si impostano ora, minuti e secondi prendendo tali parametri dall'alarm salvato
			alarmTime.set(Calendar.HOUR_OF_DAY, e.get_hour());
			alarmTime.set(Calendar.MINUTE, e.get_minute());
			alarmTime.set(Calendar.SECOND, e.get_second());
			
			
			/////////
			//PER TEST ALGORITMO: l'indice del giorno corrente è impostato "artificialmente",
			//in quanto lo deve rappresentare all'interno della settimana "corta";
			//normalmente l'indice è dato dalla data corrente: e.getRepeatingDay(today)
			/////////
			
			
			//l'alarm è attivato per questo giorno
			//cio' significa che è attivato anche l'altro alarm a lui associato (perchè una
			//coppia di alarm (start,stop) definisce un intervallo: quando un alarm di start
			//viene attivato (disattivato) viene attivato (disattivato) anche l'alarm di stop
			//successivo)
			if(e.getRepeatingDay(artificialIndex)){
				
				if(alarmTime.after(now)){					
					//se si è arrivati qui vuol dire che l'alarm è attivo in questo
					//giorno della settimana ed è valido per essere lanciato e, quindi, 
					//lo si seleziona per essere il prossimo alarm ad essere lanciato								
					
					nextAlarm=e;
					stop=true;
				}
			}
			else{ //l'alarm non è attivato per questo giorno;
				  //se questo è un alarm di start vuol dire che il relativo intervallo 
				  //non è stato attivato per questo giorno				
				
				//si controlla se è un alarm di start; solo in questo caso si può provare
				//ad eseguire la mutazione (intervallo che da 0 va a 1 con una certa
				//probabilità) in quanto per attivare un intervallo è necessario agire sulla
				//coppia di alarm (start,stop) che lo definisce
				if(e.get_actionType() && alarmTime.after(now)){					
					//è un alarm di start
					
					//si prova ad effettuare la mutazione, attivando l'intervallo
					stop=intervalMutated(e, alarms, i, artificialIndex, alarmDao);
					if(stop){
						nextAlarm=e;
					}
				}
			}
				
		}
		
		//Se nessun alarm scatta nel giorno corrente, allora si cerca un primo alarm che
		//scatta in un giorno successivo a questo (un prossimo alarm lo si trova 
		//sicuramente perché un alarm deve essere attivo in almeno un giorno della settimana,
		//altrimenti sarebbe inutile)				
		
		if(nextAlarm==null)	{
			
			//si resettano ora, minuti e secondi
			alarmTime.set(Calendar.HOUR_OF_DAY, 0);
			alarmTime.set(Calendar.MINUTE, 0);
			alarmTime.set(Calendar.SECOND, 0);
				
			/////////
			//PER TEST ALGORITMO: si inizializza l'indice artificiale 
			int currentIndex = artificialIndex;
			
			if(MainActivity.logEnabled){
				int al_m=alarmTime.get(Calendar.MONTH)+1;    	
				Log.d(MainActivity.AppName, "AlarmUtils - next alarm reset hms: h:m:s=" 
						+ alarmTime.get(Calendar.HOUR_OF_DAY)+":"+ alarmTime.get(Calendar.MINUTE)+":"+ alarmTime.get(Calendar.SECOND) +
						"  "+alarmTime.get(Calendar.DATE)+"/"+al_m+"/"+alarmTime.get(Calendar.YEAR));		    	
				Log.d(MainActivity.AppName, "AlarmUtils - milliseconds of the resetted alarm: " + alarmTime.getTimeInMillis());
			}
			/////////	
			
						
			while(!stop){
				
				//si incrementa il giorno in quanto un opportuno alarm non è stato trovato nel
				//giorno precedente
				alarmTime.add(Calendar.DATE, 1);
				
				if(MainActivity.logEnabled){
					int alr_m=alarmTime.get(Calendar.MONTH)+1;    	
					Log.d(MainActivity.AppName, "AlarmUtils - next alarm add 1 day: h:m:s=" 
							+ alarmTime.get(Calendar.HOUR_OF_DAY)+":"+ alarmTime.get(Calendar.MINUTE)+":"+ alarmTime.get(Calendar.SECOND) +
							"  "+alarmTime.get(Calendar.DATE)+"/"+alr_m+"/"+alarmTime.get(Calendar.YEAR));
			    	Log.d(MainActivity.AppName, "AlarmUtils - milliseconds of the alarm added 1 day: " + alarmTime.getTimeInMillis());
				}
				
				
		    	/////////	
				//PER TEST ALGORITMO: si aggiorna l'indice artificiale man mano che
				//si incrementa la data
				currentIndex=getNextDayIndex(currentIndex);
				/////////
				
				for(int i=0; i<alarms.size() && !stop; i++){
					
					Alarm e = alarms.get(i);
					
					/////////
					//PER TEST ALGORITMO: si fa sempre riferimento all'indice artificiale;
					//normalmente: e.getRepeatingDay(alarmTime.get(Calendar.DAY_OF_WEEK)-1)
					/////////
					
					//gli alarm hanno un istante di inizio sicuramente > di ora in quanto
					//si stanno cercando in un giorno successivo a quello corrente
					
					if(e.getRepeatingDay(currentIndex)){ 
						nextAlarm=e;
						stop=true;
					}
					else{						
						if(e.get_actionType()){					
							//è un alarm di start
							
							//si prova ad effettuare la mutazione, attivando l'intervallo
							stop=intervalMutated(e, alarms, i, artificialIndex, alarmDao);
							if(stop){
								nextAlarm=e;
							}
						}
					}
					
				}
			}
		}
				
		//si impostano ora, minuti e secondi prendendo tali parametri dall'alarm selezionato;
		//quest'ultimo sarà il prossimo alarm che verrà lanciato attraverso l'alarm manager
		alarmTime.set(Calendar.HOUR_OF_DAY, nextAlarm.get_hour());
		alarmTime.set(Calendar.MINUTE, nextAlarm.get_minute());
		alarmTime.set(Calendar.SECOND, nextAlarm.get_second());
					
		if(MainActivity.logEnabled){
			int month=alarmTime.get(Calendar.MONTH)+1;	
			Log.d(MainActivity.AppName, "AlarmUtils - NEXT ALARM: id=" + nextAlarm.get_id() + "  h:m:s=" 
					+ alarmTime.get(Calendar.HOUR_OF_DAY)+":"+ alarmTime.get(Calendar.MINUTE)+":"+ alarmTime.get(Calendar.SECOND) +
					"  "+alarmTime.get(Calendar.DATE)+"/"+month+"/"+alarmTime.get(Calendar.YEAR));		
			Log.d(MainActivity.AppName, "AlarmUtils - MILLISECONDS OF THE NEXT ALARM: " + alarmTime.getTimeInMillis());		
		}		
		
		//nell'oggetto shared preferences si imposta l'id del prossimo alarm e degli interi che indicano il giorno, il mese e 
		//l'anno in cui scatta (ora, minuti e secondi sono all'interno dell'oggetto Alarm)		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);//context.getSharedPreferences("appPrefs", 0);    	
    	Editor editor = prefs.edit();	
    	//si imposta l'id del prossimo alarm nelle preferenze
    	editor.putInt("alarm_id", nextAlarm.get_id());
    	//si impostano giorno, mese e anno dell'alarm
    	editor.putInt("alarm_date", alarmTime.get(Calendar.DATE));
    	editor.putInt("alarm_month", alarmTime.get(Calendar.MONTH));
    	editor.putInt("alarm_year", alarmTime.get(Calendar.YEAR));    	
    	//si salvano le credenziali
    	editor.commit();    	    	
    	
    	//si crea il pending intent creando dapprima un intent con tutti i dati dell'alarm
    	//per identificarlo in modo univoco
    	PendingIntent pi = createPendingIntent(context, nextAlarm, new int[]{alarmTime.get(Calendar.DATE), alarmTime.get(Calendar.MONTH), alarmTime.get(Calendar.YEAR)});
    	 	    
 	    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
 	    alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime.getTimeInMillis(), pi);
 	    
 	   if(MainActivity.logEnabled){
			Log.d(MainActivity.AppName,"AlarmUtils - pending intent for next alarm set in the alarm manager");		
 	   }
	}
	
	//non cancello le preferences perché vengono sovrascritte con i dati del prossimo alarm
	public static void cancelAlarm(Context context, Alarm alarm) {
		 
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);//context.getSharedPreferences("appPrefs", 0);
		 			 
		PendingIntent pIntent = createPendingIntent(context, alarm, new int[]{pref.getInt("alarm_date", -1),pref.getInt("alarm_month", -1),pref.getInt("alarm_year", -1)});	
	    	
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(pIntent);
	}
	 
	 
	public static PendingIntent createPendingIntent(Context context, Alarm alarm, int[] params) {
		 
		Intent intent = new Intent();
	 	    
		//si imposta il tipo di action dell'intent a seconda se è un alarm per far iniziare o
		//finire l'intervallo
		if(alarm.get_actionType()){
			intent.setAction("INTERVAL_START");
		}
		else{
			intent.setAction("INTERVAL_STOP");
		}
	 	    
		intent.putExtra("id", alarm.get_id());
		intent.putExtra("hour", alarm.get_hour());
		intent.putExtra("minute", alarm.get_minute());
		intent.putExtra("second", alarm.get_second());
		intent.putExtra("date", params[0]);
		intent.putExtra("month", params[1]);
		intent.putExtra("year", params[2]);
	 	 
		return PendingIntent.getBroadcast(context, alarm.get_id(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}
	 
	
	
	private static boolean intervalMutated(Alarm a_start, List<Alarm> alarms, int list_index, 
			int current_day_index, RuntimeExceptionDao<Alarm, Integer> alarmDao){
		
		//la probabilità di attivare l'intervallo è data dalla sua valutazione v
		//(0 <= v <= valore_soglia) 
		float probability = a_start.getEvaluation(current_day_index);
						
		//se la valutazione è 0 o quasi, si pone la probabilità a 0.1
		if(probability<0.1f){						
			probability=0.1f;
		}
		
		//con una certa probabilità si attiva l'intervallo (la coppia di alarm
		//start-stop)
		if(rand.nextFloat() <= probability){
			
			a_start.setRepeatingDay(current_day_index, true);
			a_start.setStepsInterval(current_day_index, false);						
			//si recupera dalla lista il relativo alarm di stop 
			//(c'è sicuramente visto che un alarm di start deve essere seguito
			//dal suo alarm di stop)
			Alarm a_stop = alarms.get(list_index+1); 
			a_stop.setRepeatingDay(current_day_index, true);
			a_stop.setStepsInterval(current_day_index, false);
			//la valutazione dell'intervallo verrà aggiornata quando lo si esplorerà
			
			//si salvano le modifiche anche nel db
			alarmDao.update(a_start);
			alarmDao.update(a_stop);
			
			return true;
		}
		//altrimenti l'intervallo (coppia alarm start-stop) rimane disattivato (0)
		return false;		
	}
	
	
	
	/////////
	//PER TEST ALGORITMO
	/**
	 * Metodo che ritorna l'indice "artificiale" che rappresenta il prossimo giorno 
	 * considerando una settimana corta, composta da un numero di giorni inferiore a 7.
	 * @param i indice che rappresenta il giorno corrente
	 * @return indice che rappresenta il prossimo giorno
	 */	
	public static int getNextDayIndex(int i){
		return (i == GeneralUtils.daysOfWeek-1) ? 0 : i+1;
	}
	/////////
	 
	 
	 
	 // fare metodo per considerare una coppia di alarm start-stop consecutivi
	 //      l'idea è che se si seleziona un alarm di start scartato per dargli una 
	 //      probabilità (peso) di attivarsi la prossima volta, allora si deve
	 //      attivare anche il relativo alarm di stop (attivare con day=true)
	 
	 // fare metodo che metta day=true ad un alarm (da usare, ad esempio, dopo
	 //      che si vede che l'intervallo a false per tot volte consecutive in cui
	 //      si è attivato è risultato sempre "buono")
	 
	 // fare metodo che ritorni tutti gli intervalli a false per un certo 
	 //      giorno (utile, ad esempio, per diminuire la probabilità a tutti di
	 //      essere scelti in quanto c'è poca batteria)
	 
	 
	 
	 
	 //I SEGUENTI METODI NON SERVONO se gli intervalli (start-stop alarm) vengono 
	 //già pre-impostati così da non estrarli, crearli e aggiornarli 
	 //SI POSSONO FARE INTERVALLI BREVI (ad es. 10 minuti) separati tra di loro
	 //da pochi secondi (meglio per chiarezza di codice, di spiegazione, più utili
	 //e semplici in un algoritmo genetico)
	 /*
	 public static void setSimilarOrNewAlarm(Context context, Calendar startTime, Calendar stopTime, boolean isConsecutive){
		 
		 
		 if(!isConsecutive){
			 
			 Alarm startAlarm = searchSimilarAlarm(context, startTime, true);
			 
			 if(startAlarm!=null){				 
				startAlarm.setRepeatingDay(startTime.get(Calendar.DAY_OF_WEEK-1), true);				 
			 }
			 else{
				 //si crea un nuovo alarm
				 Alarm newStartAlarm = new Alarm(startTime.get(Calendar.HOUR_OF_DAY), startTime.get(Calendar.MINUTE),
						 startTime.get(Calendar.SECOND), true);
				 //
				 newStartAlarm.setRepeatingDay(startTime.get(Calendar.DAY_OF_WEEK-1), true);
				 
				 saveAlarmInDB(context, newStartAlarm, 2);
			 }
			 
			 
			 Alarm stopAlarm = searchSimilarAlarm(context, stopTime, false);
			 
			 if(stopAlarm!=null){				 
				stopAlarm.setRepeatingDay(stopTime.get(Calendar.DAY_OF_WEEK-1), true);				 
			 }
			 else{
				 //si crea un nuovo alarm
				 Alarm newStopAlarm = new Alarm(stopTime.get(Calendar.HOUR_OF_DAY), stopTime.get(Calendar.MINUTE),
						 stopTime.get(Calendar.SECOND), false);
				 //
				 newStopAlarm.setRepeatingDay(stopTime.get(Calendar.DAY_OF_WEEK-1), true);
				 
				 saveAlarmInDB(context, newStopAlarm, 2);
			 }
			 
			 
		 }
		 else{
			 
		 }
			 
		
		 
	 }
	 
	 
	 
	 
	 private static Alarm searchSimilarAlarm(Context context, Calendar time, boolean alarmType){
		 
		 //si recuperano tutti gli alarm 
		 List<Alarm> sameTypeAlarms = getAlarmsByType(context, alarmType);
		 
		 Collections.sort(sameTypeAlarms,new AlarmComparator());
		 
		 //se c'è almeno un alarm del tipo passato come parametro (tipo che può indicare start/stop classify)
		 if(sameTypeAlarms!=null){
			 
			 Calendar alarmTime=Calendar.getInstance();
				
			 for(int i=0; i<sameTypeAlarms.size(); i++){
					
				 Alarm e = sameTypeAlarms.get(i);
							
				 alarmTime.set(Calendar.HOUR_OF_DAY, e.get_hour());
				 alarmTime.set(Calendar.MINUTE, e.get_minute());
				 alarmTime.set(Calendar.SECOND, e.get_second());
			 			 
				 //se è un alarm che ha un tempo di inizio uguale al tempo di inizio/fine dell'intervallo
				 //considerato o comunque scatta in un tempo più o meno simile (differenza di 2 minuti) allora si 
				 //imposta tale alarm settandolo come attivo nel giorno corrente
				 if(Math.abs(time.getTimeInMillis() - alarmTime.getTimeInMillis()) <= 120000
						 || Math.abs(alarmTime.getTimeInMillis()- time.getTimeInMillis()) <= 120000){
						
					 //e.setRepeatingDay(time.get(Calendar.DAY_OF_WEEK-1), true);						
					 return e;					
				 }
				 
				 
				
				 //per fare un metodo che vada bene sempre bisogna considerare anche l'intorno a cavallo 
				 //di due giorni
				 
				 //if(time.get(Calendar.HOUR_OF_DAY)==23){
							
				//	 time.add(Calendar.DATE, 1);		
				// }
				 
				 
		 
			 }
		 }
		 
	 return null;
		 
	 }
	 */
}