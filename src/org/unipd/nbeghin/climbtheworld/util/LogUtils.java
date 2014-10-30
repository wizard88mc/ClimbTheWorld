package org.unipd.nbeghin.climbtheworld.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.unipd.nbeghin.climbtheworld.MainActivity;
import org.unipd.nbeghin.climbtheworld.models.Alarm;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.Spanned;
import android.text.SpannedString;
import android.util.Log;

public class LogUtils {

	 
    //start-stop, 0/1 attuale, se attivo valutazione, 0/1 la prossima settimana
    public static void writeLogFile(Context context, String text){
    	   	    	
    	File appdir = context.getDir("climbTheWorld_dir", Context.MODE_PRIVATE); 
    	File logFile = new File(appdir, "algorithm_log");
    	
    	try {    	
    		if(!logFile.exists()){    	    			
    			Log.e(MainActivity.AppName, "Log file not exists");
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
    
    
    
    //
    public static void offIntervalsTracking(Context context, SharedPreferences pref, int lastDayIndex, int alarm_id){
    	
    	//si considerano gli intervalli che non sono stati valutati durante il
		//periodo di tempo in cui il device era spento (utile per tracciarli
		//nel file di output e, se si vuole, per cambiare la loro valutazione) 

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
    	
		int dd=0; int mm=0; int yyyy=0;
		String not_evaluated_cause="";
		
    	//se ancora non è stato impostato alcun alarm, significa che questo metodo è
		//chiamato al primo avvio per tracciare gli intervalli che hanno orario di inizio
		//già passato
    	if(alarm_id==-1){    		
    		alarm_id=2;    		
    		dd=time_before.get(Calendar.DATE);
    		mm=time_before.get(Calendar.MONTH);
    		yyyy=time_before.get(Calendar.YEAR);  
    		not_evaluated_cause="Non valutato perché app installata dopo";
    	}
    	else{
    		dd=pref.getInt("alarm_date", -1);
    		mm=pref.getInt("alarm_month", -1);
    		yyyy=pref.getInt("alarm_year", -1);		
    		not_evaluated_cause="Non valutato perché device spento";
    	}    	

		time_before.set(Calendar.DATE,dd);
		time_before.set(Calendar.MONTH,mm);
		time_before.set(Calendar.YEAR,yyyy);  
    	
    	Alarm current_next_alarm = AlarmUtils.getAlarm(context, alarm_id);
    	
    	
		//si calcola il numero di giorni passati dallo spegnimento
		//se 0 si è nello stesso giorno, se 1 nel giorno successivo, e così via
		double time_diff = time_now.getTimeInMillis() - time_before.getTimeInMillis();					
		time_diff = time_diff / (24 * 60 * 60 * 1000); //hours in a day, minutes in a hour,
        												//seconds in a minute, millis in a second
		int days_diff = (int) Math.round(time_diff);
		
		Log.d(MainActivity.AppName, "Intervals tracking - device spento, day diff: " + days_diff);
		
		List<Alarm> alarms_lst = AlarmUtils.getAllAlarms(context);
		
		//si resettano ora, minuti e secondi per riportarli all'orario corrente
		time_now = Calendar.getInstance();
		

		if(MainActivity.logEnabled){
			int month=time_now.get(Calendar.MONTH)+1;	
			Log.d(MainActivity.AppName, "TIME NOW: h:m:s=" 
			+ time_now.get(Calendar.HOUR_OF_DAY)+":"+ time_now.get(Calendar.MINUTE)+":"+ time_now.get(Calendar.SECOND) +
			"  "+time_now.get(Calendar.DATE)+"/"+month+"/"+time_now.get(Calendar.YEAR));		
		
			month=time_before.get(Calendar.MONTH)+1;	
			Log.d(MainActivity.AppName, "TIME BEFORE: h:m:s=" 
			+ time_before.get(Calendar.HOUR_OF_DAY)+":"+ time_before.get(Calendar.MINUTE)+":"+ time_before.get(Calendar.SECOND) +
			"  "+time_before.get(Calendar.DATE)+"/"+month+"/"+time_before.get(Calendar.YEAR));		
		}		
				
		time_before.set(Calendar.HOUR_OF_DAY, current_next_alarm.get_hour());
		time_before.set(Calendar.MINUTE, current_next_alarm.get_minute());
		time_before.set(Calendar.SECOND, current_next_alarm.get_minute());
				
		//se si tratta del primo intervallo (id_start=1 e id_stop=2) si è in
    	//presenza di un nuovo giorno; si scrive il suo indice nel file di output
    	if(alarm_id==2 && time_before.before(time_now)){
    		//time_before.get(Calendar.DAY_OF_WEEK)-1;
    		Log.d(MainActivity.AppName, "Intervals tracking - day index: " + lastDayIndex);    		
    		mm=mm+1;
    		writeLogFile(context,"Indice giorno: "+lastDayIndex+" - "+dd+"/"+mm+"/"+yyyy);
    	}
		
    	//stringa per scrivere l'orario di un alarm di start nel file di log
    	String previous_start_time="";   	
    	//se il primo alarm considerato è di stop, si inizializza questa stringa con
    	//l'orario del precedente alarm di start
    	if(alarm_id>1){    		
    		Alarm previous=AlarmUtils.getAlarm(context, alarm_id-1);    		
    		//se il precedente alarm è di start si memorizza la stringa dell'orario
    		if(previous.get_actionType()){
    			previous_start_time=previous.get_hour()+":"+previous.get_minute()+
						":"+previous.get_second();
    		}
    	}    	
    	
		boolean stop=false; 
		//prima si considerano gli intervalli saltati nel giorno corrente
		for(int i=alarm_id; i<=alarms_lst.size() && !stop; i++){
			
			Alarm e = alarms_lst.get(i-1);						
			//si impostano ora, minuti e secondi prendendo tali parametri dall'alarm salvato
			time_before.set(Calendar.HOUR_OF_DAY, e.get_hour());
			time_before.set(Calendar.MINUTE, e.get_minute());
			time_before.set(Calendar.SECOND, e.get_second());
			
			Log.d(MainActivity.AppName, "for - id: " + e.get_id());
			
			
			//se l'alarm è già passato
			if(time_before.before(time_now)){
				Log.d(MainActivity.AppName, "for - before ");
				//ed è un alarm di stop
				if(!e.get_actionType()){								
					int stop_id = e.get_id();
					int start_id = stop_id-1;	
					Log.d(MainActivity.AppName, "On Boot - device spento nell'intervallo: start-stop: " + start_id+"-"+stop_id);
				
					String status="";
					
					if(e.isStepsInterval(lastDayIndex)){
						status="Intervallo con scalini";
					}
					else{
						status="Intervallo di esplorazione";
					}
					
					if(e.getRepeatingDay(lastDayIndex)){
						status=status+" attivo";
					}
					else{
						status=status+" non attivo";
					}					
					
					writeLogFile(context,status+": " + previous_start_time+" - "+e.get_hour()+":"
							+e.get_minute()+":"+e.get_second()+" | "+not_evaluated_cause+" | "+
							status+" la prossima settimana");
				}
				else{
					previous_start_time=e.get_hour()+":"+e.get_minute()+":"+e.get_second();
				}
			}
			else{ //l'alarm è valido, per cui l'intervallo potrà essere valutato
				  //(anche parzialmente se è già iniziato, cioè se l'alarm è di stop)
				Log.d(MainActivity.AppName, "for - after ");
				stop=true;
			}
		}
			
		
		if(stop==false){
			
			Log.d(MainActivity.AppName, "On Boot - device spento; si passa al giorno successivo");
			
			/////////
			//PER TEST ALGORITMO: si inizializza l'indice artificiale 
			int ii = lastDayIndex; //time_before.get(Calendar.DAY_OF_WEEK)-1
			
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
					if(time_before.before(time_now)){
						//ed è un alarm di stop
						if(!e.get_actionType()){								
							int stop_id = e.get_id();
							int start_id = stop_id-1;								
							Log.d(MainActivity.AppName, "On Boot - device spento nell'intervallo: start-stop: " + start_id+"-"+stop_id);
						
							String status="";
							
							if(e.isStepsInterval(lastDayIndex)){
								status="Intervallo con scalini";
							}
							else{
								status="Intervallo di esplorazione";
							}
							
							if(e.getRepeatingDay(lastDayIndex)){
								status=status+" attivo";
							}
							else{
								status=status+" non attivo";
							}					
							
							writeLogFile(context,status+": " + previous_start_time+" - "+e.get_hour()+":"
									+e.get_minute()+":"+e.get_second()+" | "+not_evaluated_cause+" | "+
									status+" la prossima settimana");
						
						}
						else{
							previous_start_time=e.get_hour()+":"+e.get_minute()+":"+e.get_second();
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
    }
    
	
	
}
