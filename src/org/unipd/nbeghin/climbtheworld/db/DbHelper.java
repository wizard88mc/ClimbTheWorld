package org.unipd.nbeghin.climbtheworld.db;

import org.unipd.nbeghin.climbtheworld.models.Alarm;
import org.unipd.nbeghin.climbtheworld.models.AlarmTimeTemplate;
import org.unipd.nbeghin.climbtheworld.models.Building;
import org.unipd.nbeghin.climbtheworld.models.BuildingTour;
import org.unipd.nbeghin.climbtheworld.models.Climbing;
import org.unipd.nbeghin.climbtheworld.models.Photo;
import org.unipd.nbeghin.climbtheworld.models.TimeTemplate;
import org.unipd.nbeghin.climbtheworld.models.Tour;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;

public class DbHelper extends OrmLiteSqliteOpenHelper {
	
	private static DbHelper mInstance = null;
	private Context context;
	
	
	public static final String							DATABASE_NAME			= "ClimbTheWorld";
	public static final int							DATABASE_VERSION		= 1;
	private RuntimeExceptionDao<Building, Integer>		buildingRuntimeDao		= null;
	private RuntimeExceptionDao<Tour, Integer>			tourRuntimeDao			= null;
	private RuntimeExceptionDao<Climbing, Integer>		climbingRuntimeDao		= null;
	private RuntimeExceptionDao<BuildingTour, Integer>	buildingTourRuntimeDao	= null;
	private RuntimeExceptionDao<Photo, Integer>			photoRuntimeDao			= null;
	
	
	private RuntimeExceptionDao<Alarm, Integer> alarmRuntimeDao = null;
	private RuntimeExceptionDao<TimeTemplate, Integer> timeTemplateRuntimeDao = null;
	private RuntimeExceptionDao<AlarmTimeTemplate, Integer> alarmTimeTemplateRuntimeDao = null;
	

	private DbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;
	}
	
	
	  public static DbHelper getInstance(Context ctx) {
	        /** 
	         * si usa l'application context per far sì di non fuoriuscire
	         * dal contesto di un'activity
	         * http://android-developers.blogspot.nl/2009/01/avoiding-memory-leaks.html)
	         */
	        if (mInstance == null) {
	        	System.out.println("Istanza dbHelper null");
	            mInstance = new DbHelper(ctx.getApplicationContext());
	        }
	        else{
	        	System.out.println("Istanza dbHelper NOT null");
	        }
	        return mInstance;
	    }
	

	@Override
	public void onCreate(SQLiteDatabase arg0, ConnectionSource arg1) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, ConnectionSource arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
	}

	public RuntimeExceptionDao<Building, Integer> getBuildingDao() {
		if (buildingRuntimeDao == null) {
			buildingRuntimeDao = getRuntimeExceptionDao(Building.class);
		}
		return buildingRuntimeDao;
	}

	public RuntimeExceptionDao<Tour, Integer> getTourDao() {
		if (tourRuntimeDao == null) {
			tourRuntimeDao = getRuntimeExceptionDao(Tour.class);
		}
		return tourRuntimeDao;
	}

	public RuntimeExceptionDao<BuildingTour, Integer> getBuildingTourDao() {
		if (buildingTourRuntimeDao == null) {
			buildingTourRuntimeDao = getRuntimeExceptionDao(BuildingTour.class);
		}
		return buildingTourRuntimeDao;
	}

	public RuntimeExceptionDao<Climbing, Integer> getClimbingDao() {
		if (climbingRuntimeDao == null) {
			climbingRuntimeDao = getRuntimeExceptionDao(Climbing.class);
		}
		return climbingRuntimeDao;
	}

	public RuntimeExceptionDao<Photo, Integer> getPhotoDao() {
		if (photoRuntimeDao == null) {
			photoRuntimeDao = getRuntimeExceptionDao(Photo.class);
		}
		return photoRuntimeDao;
	}
	
	
	public RuntimeExceptionDao<Alarm, Integer> getAlarmDao() {
		if (alarmRuntimeDao == null) {
			System.out.println("alarm dao null");
			alarmRuntimeDao = getRuntimeExceptionDao(Alarm.class);
		}
		else{
			System.out.println("alarm dao NOT null");
		}
		return alarmRuntimeDao;
	}
	
	public RuntimeExceptionDao<TimeTemplate, Integer> getTimeTemplateDao() {
		if (timeTemplateRuntimeDao == null) {
				timeTemplateRuntimeDao = getRuntimeExceptionDao(TimeTemplate.class);
		}
		return timeTemplateRuntimeDao;
	}
	
	public RuntimeExceptionDao<AlarmTimeTemplate, Integer> getAlarmTimeTemplateDao() {
		if (alarmTimeTemplateRuntimeDao == null) {
				alarmTimeTemplateRuntimeDao = getRuntimeExceptionDao(AlarmTimeTemplate.class);
		}
		return alarmTimeTemplateRuntimeDao;
	}
		
	
    public String getDbPath() {
        return this.getReadableDatabase().getPath();
    }
    
	/**
	 * Close the database connections and clear any cached DAOs.
	 */
	@Override
	public void close() {
		super.close();
		buildingRuntimeDao = null;
		tourRuntimeDao = null;
		climbingRuntimeDao = null;
		buildingTourRuntimeDao = null;
		photoRuntimeDao = null;
				
		alarmRuntimeDao=null;
		timeTemplateRuntimeDao=null;
		alarmTimeTemplateRuntimeDao=null;
	}
}
