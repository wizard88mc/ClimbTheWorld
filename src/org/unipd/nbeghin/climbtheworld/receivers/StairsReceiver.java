package org.unipd.nbeghin.climbtheworld.receivers;

import java.util.ArrayList;
import java.util.List;

import org.unipd.nbeghin.climbtheworld.ClimbActivity;
import org.unipd.nbeghin.climbtheworld.MainActivity;
import org.unipd.nbeghin.climbtheworld.models.ClassifierCircularBuffer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class StairsReceiver extends BroadcastReceiver {

	private static final double tradeoffG = 0.001;
	private static final double g = tradeoffG / (double)100;
	private List<Double> history = new ArrayList<Double>();
	private static final int historySize = 10;
	
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		//arrivano gli stessi dati ricevuti anche dal receiver in ClimbActivity 
 				
 		Double result = intent.getExtras().getDouble(ClassifierCircularBuffer.CLASSIFIER_NOTIFICATION_STATUS);
 		
 		Log.d(MainActivity.AppName,"STAIRS RECEIVER - result: " + result);
 		
 		
		double correction = 0.0;
		for (int indexHistory = 0; indexHistory < history.size(); indexHistory++) {
			correction += (100 / Math.pow(2, indexHistory + 1)) * (double)history.get(indexHistory) * g;
		}
		
		if (Double.isNaN(correction)) {
			correction = 0.0;
		}
		
		double finalClassification = result + correction;
		
		Log.d(MainActivity.AppName,"STAIRS RECEIVER - final classification: " + finalClassification);
		
		if (result * finalClassification >= 0) {
			if (history.size() == historySize) {
				history.remove(historySize - 1);
				history.add(0, (finalClassification > 0 ? 1.0 : -1.0));
			}
			else {
				history.add(0, (finalClassification > 0 ? 1.0 : -1.0));
			}
		}
		else {
			history.clear();
			history.add(result > 0 ? 1.0 : -1.0);
		}
		
		
		if(finalClassification>0){ //scalino
			
			//se il gioco è attivo si aggiorna la grafica
			//ClimbActivity.updateGUI();
			
			
			
		}
		
		
	}
	
}
