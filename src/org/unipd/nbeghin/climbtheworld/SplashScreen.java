package org.unipd.nbeghin.climbtheworld;

import java.io.IOException;
import org.unipd.nbeghin.climbtheworld.weka.WekaClassifier;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;


/**
 * 
 * If your application has a time-consuming initial setup phase, consider showing a splash screen 
 * or rendering the main view as quickly as possible and filling in the information asynchronously. 
 * In either case, you should indicate somehow that progress is being made, 
 * lest the user perceive that the application is frozen
 * @author silvia
 *
 */

@SuppressLint("NewApi")
public class SplashScreen extends ActionBarActivity{
	   @Override
	   public void onCreate(Bundle savedInstanceState){
	      super.onCreate(savedInstanceState);
	      // set the content view for your splash screen you defined in an xml file
	      if (Build.VERSION.SDK_INT < 16) {
	            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
	                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
	       }else{
	    	   View decorView = getWindow().getDecorView();
	    	// Hide the status bar.
	    	int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
	    	decorView.setSystemUiVisibility(uiOptions);
	    	// Remember that you should never show the action bar if the
	    	// status bar is hidden, so hide that too if necessary.
	    	ActionBar actionBar = getSupportActionBar();
	    	actionBar.hide();
	       }
	      
	      setContentView(R.layout.activity_splashscreen);
	      TextView title = (TextView) findViewById(R.id.textTitle);
	      Typeface tf = Typeface.createFromAsset(getAssets(),"fonts/travel_diary.ttf");  
	      title.setTypeface(tf);
	      // perform other stuff you need to do

	      // execute your xml news feed loader
	      new AsyncLoadXMLFeed().execute();

	   }

	   private class AsyncLoadXMLFeed extends AsyncTask<Void, Void, Void>{
	      @Override
	      protected void onPreExecute(){
	            // show your progress dialog

	      }

	      @Override
	      protected Void doInBackground(Void... voids){
	            // load your data asynchronously
	    	  try { 
	  			Log.i("SplashScreen", "Loading game model");
	  			WekaClassifier.initializeParameters(getResources().openRawResource(R.raw.newmodelvsw30osl0));
	  		} catch (IOException exc) {
	  			//finish();
	  		}
	    	  return null;
	      }

	      @Override
	      protected void onPostExecute(Void params){
	            // dismiss your dialog
	            // launch your News activity
	            Intent intent = new Intent(SplashScreen.this, MainActivity.class);
	            startActivity(intent);

	            // close this activity
	            finish();
	      }

	   }
	}
