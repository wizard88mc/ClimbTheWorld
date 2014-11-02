package org.unipd.nbeghin.climbtheworld;

import android.app.Application;
import android.content.Context;

public class ClimbTheWorldApp extends Application {

	
	private static ClimbTheWorldApp instance;

	public static ClimbTheWorldApp getInstance() {
		return instance;
	}

	public static Context getContext(){
		return instance; //or instance.getApplicationContext();
	}
	
	@Override
	public void onCreate() {
		instance = this;
		super.onCreate();
	}
	
}
