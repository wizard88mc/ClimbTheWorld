package org.unipd.nbeghin.climbtheworld.util;

import java.sql.SQLException;
import java.util.Date;

import org.unipd.nbeghin.climbtheworld.db.DbHelper;
import org.unipd.nbeghin.climbtheworld.models.Alarm;
import org.unipd.nbeghin.climbtheworld.models.DayStairs;

import android.content.Context;

import com.j256.ormlite.android.AndroidConnectionSource;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class StairsHistoryUtils {
	
	private StairsHistoryUtils(){
		
	}
	
	/*
	private static DbHelper getHelper(Context context) {
	    if (dbh == null) {
	    	
	    	System.out.println("DB helper null");
	    	
	        dbh = (DbHelper)OpenHelperManager.getHelper(context, DbHelper.class);
	    }
	    else{
	    	System.out.println("DB helper NOT null");
	    }
	    return dbh;
	}
	*/
	
	public static void setupStairsHistoryDB(Context context){
	    	
		ConnectionSource connectionSource = new AndroidConnectionSource(DbHelper.getInstance(context));
	    	    	
		try {
			
			DaoManager.createDao(connectionSource, DayStairs.class);
				
			TableUtils.createTableIfNotExists(connectionSource, DayStairs.class);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	

	
	  public static boolean dayStairsExists(Context context, Date day){ 
	    	
	    	//return DbHelper.getInstance(context).getStairsHistoryDao();
		  
		  return false;
	    }
	
	
	
	
	
	public static void createOrUpdateSteps(Context context, Date day, int steps){
		
		
		DbHelper dbh = DbHelper.getInstance(context);
		
		
		
		
		
		
	}
	
	
}
