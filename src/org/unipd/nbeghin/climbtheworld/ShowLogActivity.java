package org.unipd.nbeghin.climbtheworld;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.unipd.nbeghin.climbtheworld.util.GeneralUtils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ShowLogActivity extends Activity {

    //holds the ListView object in the UI
    private ListView logListView;
    
    //Holds activity recognition data, in the form of strings that can contain markup
    private ArrayAdapter<Spanned> logAdapter;

    private Context context;
	
   /* 
    //Intent filter for incoming broadcasts from the IntentService
    IntentFilter mBroadcastFilter;

    // Instance of a local broadcast manager
    private LocalBroadcastManager mBroadcastManager;
    */
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_show_log);
		context = getApplicationContext();
		

        //get a handle to the activity update list
		logListView = (ListView) findViewById(R.id.log_listview);

        //instantiate an adapter to store update data from the log
        logAdapter = new ArrayAdapter<Spanned>(
                this,
                R.layout.log_item,
                R.id.log_text
        );

        // Bind the adapter to the status list
        logListView.setAdapter(logAdapter);
		

        // Set the broadcast receiver intent filer
       // mBroadcastManager = LocalBroadcastManager.getInstance(this);

        // Create a new Intent filter for the broadcast receiver
        //mBroadcastFilter = new IntentFilter(ActivityUtils.ACTION_REFRESH_STATUS_LIST);
        //mBroadcastFilter.addCategory(ActivityUtils.CATEGORY_LOCATION_SERVICES);

        
		
	}
	
	
	@Override
	protected void onResume() {
		super.onResume();
		
		// Register the broadcast receiver
       /* mBroadcastManager.registerReceiver(
                updateListReceiver,
                mBroadcastFilter);*/
		
		refreshLogData();
		
	}
	
	@Override
	protected void onPause() {
        // Stop listening to broadcasts when the Activity isn't visible.
       // mBroadcastManager.unregisterReceiver(updateListReceiver);
        
		super.onPause();
	}
	
	
	/**
     * Display the algorithm history stored in the
     * log file
     */
    private void refreshLogData() {
    	    	
    	 //try to load data from the log file
        try {              	
        	File logFile = new File(context.getDir("climbTheWorld_dir", Context.MODE_PRIVATE), "algorithm_log");
        	
            //load log file records into the list
            List<Spanned> lines = GeneralUtils.loadLogFile(logFile);

            //clear the adapter of existing data
            logAdapter.clear();

            //add each element of the log to the adapter
            for (Spanned line : lines) {
                logAdapter.add(line);
            }

            //trigger the adapter to update the display
            logAdapter.notifyDataSetChanged();

        // If an error occurs while reading the history file
        } catch (IOException e) {
            Log.e(MainActivity.AppName, e.getMessage(), e);
        }
    }
    
    
    public void onRefreshLogData(View view){
    	
    	refreshLogData();
    }
    
    
    /*
    //Broadcast receiver that receives activity update intents
    //It checks to see if the ListView contains items. If it doesn't, it pulls in history.
    //This receiver is local only. It can't read broadcast Intents from other apps
    BroadcastReceiver updateListReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            
            //When an Intent is received from the update listener IntentService, update
            //the displayed log
        	refreshLogData();
        }
    };
	*/
    
    
    
}
