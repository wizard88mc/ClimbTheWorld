package org.unipd.nbeghin.climbtheworld.receivers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.unipd.nbeghin.climbtheworld.models.ClassifierCircularBuffer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class UserMotionReceiver extends BroadcastReceiver {

	/*
	private List<Double> list = new ArrayList<Double>();
	private static ArrayList<Double> clonedList;
	
	private static boolean newInterval = true;
	private int move = 0;
	private Calendar startDate;
	private static long startTime; //importante mettere static
	private final static long interval = 1000000L * 1000 * 600; //10 minuti
	*/
	
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Sistemare in base alla nuova funzione di fitness e in base a quale classificatore si usa
		
		/*
		//qui ricevo cio' che riceve anche il receiver dentro ClimbActivity; in tal caso
		//però devo registrare e analizzare i dati che mi arrivano per fare in modo di
		//cambiare il template o comunque "aggiustare" gli alarm salvati nel db quando necessario 
				
		Double result = intent.getExtras().getDouble(ClassifierCircularBuffer.CLASSIFIER_NOTIFICATION_STATUS);
		
		//System.out.println("USER MOTION RECEIVER - double result: " + result);
		
		//if learning==true (booleano che indica se il processo di apprendimento dell'attività utente è
		//attivo)
		if(newInterval){
			startDate = Calendar.getInstance();
			startTime= System.nanoTime();
			newInterval=false;	
			move=0;
			System.out.println("Start time: " + startTime);
		}
		
		list.add(result);
		
		//TODO: considerare un range di valori che definisca dei movimenti dell'utente che prenda sia scalini che non escludendo i piccoli movimenti (result >= di un certo valore) 
		if(result!=-0.001) 
			move++;
		
		long elapsedTime = System.nanoTime() - startTime;
		
		//System.out.println("Int: " + elapsedTime);
	
		 if( elapsedTime > interval){
			 
			 System.out.println("INTERVALLO 10 MINUTI");
			 
			 if(move >= (int)(list.size()*(60.0f/100.0f))){
				 
				 
				 //TODO creare Alarm di start e di stop se non esistono già, altrimenti mettere giorno corrente 
				 //all'interno di quegli alarm per renderli attivi in quel giorno
				 //usare startDate per lo start dell'alarm e data corrente per lo stop
			 }
			
			 list.clear();
			 newInterval=true; //pronto a considerare un altro intervallo
		 }		
		*/		
	}
}