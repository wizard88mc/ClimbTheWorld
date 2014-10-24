package org.unipd.nbeghin.climbtheworld.util;

import org.unipd.nbeghin.climbtheworld.MainActivity;
import org.unipd.nbeghin.climbtheworld.activity.recognition.ActivityRecognitionIntentService;
import org.unipd.nbeghin.climbtheworld.db.DbHelper;
import org.unipd.nbeghin.climbtheworld.models.Alarm;
import org.unipd.nbeghin.climbtheworld.receivers.StairsClassifierReceiver;

import com.j256.ormlite.dao.RuntimeExceptionDao;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;

public class IntervalEvaluationUtils {

	//soglia per il valore di valutazione
	private static float eval_threshold = 0.5f;
	
	
	
	public static void evaluateAndUpdateInterval(Context context, boolean stepsInterval, boolean withSteps, int stop_alarm_id){
				
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

				
		Log.d(MainActivity.AppName,"alarm start prima: " + previous_start_alarm.getRepeatingDay(current_day_index));
		
		//si valuta l'intervallo di esplorazione/con scalini e, a seconda della valutazione,
		//lo si attiva o meno per la prossima settimana (un intervallo non attivo può essere
		//poi attivato con la mutazione)
				
		float evaluation = 0f;
		
		//è un "intervallo con scalini"
		if(stepsInterval){
			
			Log.d(MainActivity.AppName,"EVALUATION - It is a 'interval with steps'");
			
			//se nell'intervallo l'utente fa scalini, è confermato come "intervallo con scalini"
			if(StairsClassifierReceiver.getStepsNumber()>=1){
				
				Log.d(MainActivity.AppName,"EVALUATION - steps>=1: OK");
				
				previous_start_alarm.setStepsInterval(current_day_index, true);	
				this_stop_alarm.setStepsInterval(current_day_index, true);
				evaluation = 1.0f;
			}
			else{ 
				
				Log.d(MainActivity.AppName,"EVALUATION - steps=0: back to an EXPLORATION INTERVAL");
				
				//l'utente non fa scalinie, quindi, l'intervallo ritorna ad essere un
				//semplice intervallo di esplorazione
				previous_start_alarm.setStepsInterval(current_day_index, false);	
				this_stop_alarm.setStepsInterval(current_day_index, false);			
				
				//all'intervallo viene data una valutazione pari a 0.5 in modo da attivarlo
				//comunque la prossima settimana
				evaluation = 0.5f;
			}			
		}
		else{ //è un "intervallo di esplorazione"
			
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
			}
			else{
				//si calcola la valutazione che considera quantità e qualità
				//dell'attività fisica svolta
				evaluation = 0f;
				
				float qn = activityAmountValue();
				
				Log.d(MainActivity.AppName,"EVALUATION - Amount of physical activity: " + qn);
				
							
				if(qn > 0){
					evaluation = qn * activityQualityValue();
					Log.d(MainActivity.AppName,"EVALUATION - qn*ql: " + evaluation);
				}
				
				//non è un "intervallo con scalini"
				previous_start_alarm.setStepsInterval(current_day_index, false);	
				this_stop_alarm.setStepsInterval(current_day_index, false);				
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
		 
		if(evaluation>=eval_threshold){	
			//l'intervallo viene attivato per la prossima settimana
			Log.d(MainActivity.AppName,"FITNESS - intervallo buono, lo tengo per la prossima settimana");						
			previous_start_alarm.setRepeatingDay(current_day_index, true);
			this_stop_alarm.setRepeatingDay(current_day_index, true);						
		}
		else{
			Log.d(MainActivity.AppName,"FITNESS - intervallo non buono, lo disattivo per la prossima settimana");
			previous_start_alarm.setRepeatingDay(current_day_index, false);
			this_stop_alarm.setRepeatingDay(current_day_index, false);
		}
			
		
		//si salvano queste modifiche anche nel database
		alarmDao.update(previous_start_alarm);
		alarmDao.update(this_stop_alarm);		
		
		Log.d(MainActivity.AppName,"alarm start dopo: " + AlarmUtils.getAlarm(context, stop_alarm_id-1).getRepeatingDay(current_day_index));
	
		/*
		//forse divedere casi tra gioco senza scalini e niente gioco
		float evaluation = 0f;			
		if(qn>0){
			evaluation=(GeneralUtils.evaluateInterval(qn, ActivityRecognitionIntentService.getConfidencesList(), ActivityRecognitionIntentService.getWeightsList()) 
					+0.5f) / 2;
		}
		else{
			evaluation=0.5f;
		}
		*/
	
	}
	
	
	
	private static float activityAmountValue(){	
		
		int values_number = ActivityRecognitionIntentService.getValuesNumber();
		float amount = 0f;
		
		if(values_number>0){
			amount = (float) ActivityRecognitionIntentService.getActivitiesNumber()/values_number;			
			if(amount>0.9f)
				amount=0.9f;
		}
		
		return amount;
	}
	
	
	
	private static float activityQualityValue(){
    	
    	Log.d(MainActivity.AppName,"EVALUATION - Quality of physical activity: " + 
    			ActivityRecognitionIntentService.getConfidencesWeightsSum()/ActivityRecognitionIntentService.getWeightsSum());
    	
    	return ActivityRecognitionIntentService.getConfidencesWeightsSum()/ActivityRecognitionIntentService.getWeightsSum();
	}
	
	
	
}
