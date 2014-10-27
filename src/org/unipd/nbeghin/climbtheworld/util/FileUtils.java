package org.unipd.nbeghin.climbtheworld.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.unipd.nbeghin.climbtheworld.MainActivity;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

public class FileUtils {

	
	public static String readFromFile(Context context) {

	    String ret = "";
	    AssetManager assetManager = context.getResources().getAssets();
	    	    
	    try {
	    	
	        InputStream inputStream = assetManager.open("intervals.txt");

	        if (inputStream!=null) {
	            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
	            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
	            String receiveString = "";
	            StringBuilder stringBuilder = new StringBuilder();

	            while ((receiveString = bufferedReader.readLine())!=null) {
	            
	            	//stringBuilder.append(receiveString);
	            
	            	
	            
	            }

	            inputStream.close();
	            ret = stringBuilder.toString();
	        }
	    }
	    catch (FileNotFoundException e) {
	        Log.e(MainActivity.AppName, " - File not found: " + e.toString());
	    } catch (IOException e) {
	        Log.e(MainActivity.AppName, " - Can not read file: " + e.toString());
	    }

	    return ret;
	}
	
	
	
}
