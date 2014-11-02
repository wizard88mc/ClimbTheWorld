package org.unipd.nbeghin.climbtheworld.util;

import java.util.Calendar;

import org.unipd.nbeghin.climbtheworld.MainActivity;
import org.unipd.nbeghin.climbtheworld.activity.recognition.ActivityRecognitionIntentService;
import org.unipd.nbeghin.climbtheworld.db.DbHelper;
import org.unipd.nbeghin.climbtheworld.models.Alarm;
import org.unipd.nbeghin.climbtheworld.receivers.StairsClassifierReceiver;

import com.j256.ormlite.dao.RuntimeExceptionDao;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class IntervalEvaluationUtils {

	//soglia per il valore di valutazione
	private static float eval_threshold = 0.5f;
	
	
	
	public static void evaluateAndUpdateInterval(Context context, boolean stepsInterval, boolean withSteps, int stop_alarm_id){
				
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		//si recupera il DAO associato alla tabella degli alarm attraverso il gestore del DB
		RuntimeExceptionDao<Alarm, Integer> alarmDao = DbHelper.getInstance(context).getAlarmDao();
						
		//dal DB si ottengono l'alarm di start e di stop che definiscono questo intervallo
		//di esplorazione/con scalini (gli alarm vengono salvati in un modo da avere un alarm
		//di start seguito dal relativo alarm di stop così da definire un intervallo)				
		Alarm previous_start_alarm = AlarmUtils.getAlarm(context, stop_alarm_id-1);
		Alarm this_stop_alarm = AlarmUtils.getAlarm(context, stop_alarm_id);
						
		//si recupera l'indice del giorno corrente all'interno della settimana
		/////////		
		//PER TEST ALGORITMO
		int current_day_index = PreferenceManager.getDefaultSharedPreferences(context).getInt("artificialDayIndex", 0);
		///////// altrimenti l'indice del giorno è (Calendar.getInstance().get(Calendar.DAY_OF_WEEK))-1;

		////////////////////////////
		//LOG
		Calendar now = Calendar.getInstance();
		if(stop_alarm_id-1==1){
    		int month = now.get(Calendar.MONTH)+1;
    		LogUtils.writeLogFile(context,"Indice giorno: "+current_day_index+" - "+now.get(Calendar.DATE)+"/"+month+"/"+now.get(Calendar.YEAR));
    	}
		
		
		String log_string="";
		String status=" attivo";
		
		if(PreferenceManager.getDefaultSharedPreferences(context).getBoolean("next_alarm_mutated", false)){
			//questo intervallo è stato mutato, da non attivo ad attivo
			
			status=" dopo mutazione";
		}
		
		status = status +": "+ previous_start_alarm.get_hour()+":"+previous_start_alarm.get_minute()+":"
				+previous_start_alarm.get_second()+" - "+this_stop_alarm.get_hour()+":"
				+this_stop_alarm.get_minute()+":"+this_stop_alarm.get_second()+" | ";
		////////////////////////////
		
				
		Log.d(MainActivity.AppName,"alarm start prima: " + previous_start_alarm.getRepeatingDay(current_day_index));
		
		//si valuta l'intervallo di esplorazione/con scalini e, a seconda della valutazione,
		//lo si attiva o meno per la prossima settimana (un intervallo non attivo può essere
		//poi attivato con la mutazione)
				
		float evaluation = 0f;
		
		//è un "intervallo con scalini"
		if(stepsInterval){
			
			////////////////////////////
			//LOG
			log_string="Intervallo con scalini"+status;
			////////////////////////////
			
			Log.d(MainActivity.AppName,"EVALUATION - It is a 'interval with steps'");
			
			int steps_number =  StairsClassifierReceiver.getStepsNumber(prefs);
			
			//se nell'intervallo l'utente fa scalini, è confermato come "intervallo con scalini"
			if(steps_number>=1){
				
				Log.d(MainActivity.AppName,"EVALUATION - steps>=1: OK");
				
				previous_start_alarm.setStepsInterval(current_day_index, true);	
				this_stop_alarm.setStepsInterval(current_day_index, true);
				evaluation = 1.0f;
				
				////////////////////////////
				//LOG
				if(steps_number==1){
					log_string=log_string+"Valutazione: 1 (1 scalino) ";
				}
				else{
					log_string=log_string+"Valutazione: 1 (" + steps_number +" scalini) ";
				}
				
				log_string=log_string+" | Rimane un intervallo con scalini, ATTIVO la prossima settimana";
				////////////////////////////
			}
			else{ 
				
				Log.d(MainActivity.AppName,"EVALUATION - steps=0: back to an EXPLORATION INTERVAL");
				
				//l'utente non fa scalini e, quindi, l'intervallo ritorna ad essere un
				//semplice intervallo di esplorazione
				previous_start_alarm.setStepsInterval(current_day_index, false);	
				this_stop_alarm.setStepsInterval(current_day_index, false);			
				
				//all'intervallo viene data una valutazione pari a 0.5 in modo da attivarlo
				//comunque la prossima settimana
				evaluation = 0.5f;
				
				////////////////////////////
				//LOG
				log_string=log_string+"Valutazione 0 (0 scalini) | Ritorna ad essere un intervallo di esplorazione, ATTIVO la prossima settimana";
				////////////////////////////
			}			
			
						
		}
		else{ //è un "intervallo di esplorazione"
			
			////////////////////////////
			//LOG
			log_string="Intervallo di esplorazione"+status;
			////////////////////////////
			
			Log.d(MainActivity.AppName,"EVALUATION - It is a 'exploration interval'");
			
			//se l'intervallo è interessato da un periodo di gioco dove finora l'utente ha fatto
			//almeno uno scalino allora si aggiorna la sua valutazione: si pone valutazione=1 e
			//tale intervallo diventa un "intervallo con scalini"	
			if(withSteps){			
				
				Log.d(MainActivity.AppName,"EVALUATION - 'exploration interval' with steps");
				
				//all'intervallo si assegna una valutazione=1
				evaluation=1.0f;	
				//diventa un "intervallo con scalini"
				previous_start_alarm.setStepsInterval(current_day_index, true);	
				this_stop_alarm.setStepsInterval(current_day_index, true);	
				
				////////////////////////////
				//LOG
				log_string=log_string+"Valutazione: 1 (>=1 scalino) | Diventa un intervallo con scalini, ATTIVO la prossima settimana";
				////////////////////////////
			}
			else{
				//si calcola la valutazione che considera quantità e qualità
				//dell'attività fisica svolta
				evaluation = 0f;
				
				float qn = activityAmountValue(prefs);
				
				Log.d(MainActivity.AppName,"EVALUATION - Amount of physical activity: " + qn);
				
							
				if(qn > 0){
					evaluation = qn * activityQualityValue(prefs);
					Log.d(MainActivity.AppName,"EVALUATION - qn*ql: " + evaluation);
				}
				
				//non è un "intervallo con scalini"
				previous_start_alarm.setStepsInterval(current_day_index, false);	
				this_stop_alarm.setStepsInterval(current_day_index, false);		
				
				////////////////////////////
				//LOG
				log_string=log_string+"Valutazione: "+evaluation+" | ";
				////////////////////////////
			}
			
			
		}
		
		previous_start_alarm.setEvaluation(current_day_index, evaluation);
		this_stop_alarm.setEvaluation(current_day_index, evaluation);
		
		
		//se la valutazione dell'intervallo è superiore o uguale alla soglia
		//allora lo si attiva (1) anche la prossima settimana, altrimenti lo si disattiva
		//(0) (si attiva/disattiva sia l'alarm di start che l'alarm di stop)
		//in pratica, si calcola la funzione di fitness a pezzi facendo ogni volta la
		//scelta migliore (alla fine si porta avanti l'individuo che presenta la 
		//fitness maggiore tra i diversi individui possibili)
		 
		////////////////////////////
		//LOG
		String str_eval=" Rimane un intervallo di esplorazione, ";
		////////////////////////////
		
		if(evaluation>=eval_threshold){	
			//l'intervallo viene attivato per la prossima settimana
			Log.d(MainActivity.AppName,"FITNESS - intervallo buono, lo tengo per la prossima settimana");						
			previous_start_alarm.setRepeatingDay(current_day_index, true);
			this_stop_alarm.setRepeatingDay(current_day_index, true);	
			
			////////////////////////////
			//LOG
			str_eval=str_eval+"ATTIVO la prossima settimana";
			////////////////////////////
		}
		else{
			Log.d(MainActivity.AppName,"FITNESS - intervallo non buono, lo disattivo per la prossima settimana");
			previous_start_alarm.setRepeatingDay(current_day_index, false);
			this_stop_alarm.setRepeatingDay(current_day_index, false);
			
			////////////////////////////
			//LOG
			str_eval=str_eval+"NON ATTIVO la prossima settimana";
			////////////////////////////
		}
			
		
		//si salvano queste modifiche anche nel database
		alarmDao.update(previous_start_alarm);
		alarmDao.update(this_stop_alarm);		
		
		Log.d(MainActivity.AppName,"alarm start dopo: " + AlarmUtils.getAlarm(context, stop_alarm_id-1).getRepeatingDay(current_day_index));
	
		/*
		//forse rivedere casi tra gioco senza scalini e niente gioco
		float evaluation = 0f;			
		if(qn>0){
			evaluation=(GeneralUtils.evaluateInterval(qn, ActivityRecognitionIntentService.getConfidencesList(), ActivityRecognitionIntentService.getWeightsList()) 
					+0.5f) / 2;
		}
		else{
			evaluation=0.5f;
		}
		*/
		
		////////////////////////////
		//LOG
		if(!stepsInterval && !withSteps){
			log_string=log_string+str_eval;
		}
		
		//si scrive sul file di log la valutazione dell'intervallo	
		LogUtils.writeLogFile(context, log_string);
		////////////////////////////
	}
	
	
	
	private static float activityAmountValue(SharedPreferences prefs){	
		
		int values_number = ActivityRecognitionIntentService.getValuesNumber(prefs);
		float amount = 0f;
		
		if(values_number>0){
			amount = (float) ActivityRecognitionIntentService.getActivitiesNumber(prefs)/values_number;			
			if(amount>0.9f)
				amount=0.9f;
		}
		
		return amount;
	}
	
	
	
	private static float activityQualityValue(SharedPreferences prefs){
    	
    	Log.d(MainActivity.AppName,"EVALUATION - Quality of physical activity: " + 
    			ActivityRecognitionIntentService.getConfidencesWeightsSum(prefs)/ActivityRecognitionIntentService.getWeightsSum(prefs));
    	
    	return ActivityRecognitionIntentService.getConfidencesWeightsSum(prefs)/ActivityRecognitionIntentService.getWeightsSum(prefs);
	}
	
	
	
}
