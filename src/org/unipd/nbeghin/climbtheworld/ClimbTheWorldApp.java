package org.unipd.nbeghin.climbtheworld;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unipd.nbeghin.climbtheworld.db.DbHelper;
import org.unipd.nbeghin.climbtheworld.db.PreExistingDbLoader;
import org.unipd.nbeghin.climbtheworld.models.Building;
import org.unipd.nbeghin.climbtheworld.models.BuildingTour;
import org.unipd.nbeghin.climbtheworld.models.Climbing;
import org.unipd.nbeghin.climbtheworld.models.Photo;
import org.unipd.nbeghin.climbtheworld.models.Tour;
import org.unipd.nbeghin.climbtheworld.weka.WekaClassifier;

import com.j256.ormlite.dao.RuntimeExceptionDao;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;

public class ClimbTheWorldApp extends Application {

	
	private static ClimbTheWorldApp instance;

	private static Context sContext;
	
	public static List<Building>								buildings;
	private static List<Climbing>								climbings;																						// list of loaded climbings
	public static List<Tour>									tours;	// list of loaded tours
	public static RuntimeExceptionDao<Building, Integer>		buildingDao;																					// DAO for buildings
	public static RuntimeExceptionDao<Climbing, Integer>		climbingDao;																					// DAO for climbings
	public static RuntimeExceptionDao<Tour, Integer>			tourDao;																						// DAO for tours
	public static RuntimeExceptionDao<BuildingTour, Integer>	buildingTourDao;																				// DAO for building_tours
	public static RuntimeExceptionDao<Photo, Integer>			photoDao;
	
	private static DbHelper dbHelper;
	
	public static ClimbTheWorldApp getInstance() {
		return instance;
	}

	public static Context getContext(){
		return instance; //or instance.getApplicationContext();
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		instance = this;
		sContext = getApplicationContext();
		
		//load database
		loadDb();
		
		//load classifier parameters
		try {
			WekaClassifier.initializeParameters(getResources().openRawResource(R.raw.modelvsw30osl0));
		}
		catch(IOException exc) {
		}
		
		
	}
	
	
	
	/**
	 * Load db and setup DAOs NB: extracts DB from assets/databases/ClimbTheWorld.zip
	 */
	private void loadDb() {
		PreExistingDbLoader preExistingDbLoader = new PreExistingDbLoader(getApplicationContext()); // extract db from zip
		SQLiteDatabase db = preExistingDbLoader.getReadableDatabase();
		db.close(); // close connection to extracted db
		dbHelper = DbHelper.getInstance(getApplicationContext()); //new DbHelper(getApplicationContext()); // instance new db connection to now-standard db
		buildingDao = dbHelper.getBuildingDao(); // create building DAO
		climbingDao = dbHelper.getClimbingDao(); // create climbing DAO
		tourDao = dbHelper.getTourDao(); // create tour DAO
		buildingTourDao = dbHelper.getBuildingTourDao(); // create building tour DAO
		photoDao = dbHelper.getPhotoDao();
				
		refresh(); // loads all buildings and tours		
	}
	
	
	/**
	 * Check and return if a climbing exists for given building Returns null if no climbing exists yet
	 * 
	 * @param building_id building ID
	 * @return Climbing
	 */
	public static Climbing getClimbingForBuilding(int building_id) {
		Map<String, Object> conditions = new HashMap<String, Object>();
		conditions.put("building_id", building_id); // filter for building ID
		List<Climbing> climbings = climbingDao.queryForFieldValuesArgs(conditions);
		if (climbings.size() == 0) return null;
		return climbings.get(0);
	}

	/**
	 * Reload all buildings
	 */
	public static void refreshBuildings() {
		buildings = buildingDao.queryForAll();
	}

	/**
	 * Reload all tours
	 */
	public static void refreshTours() {
		tours = tourDao.queryForAll();
	}

	/**
	 * Reload buildings and tours
	 */
	public static void refresh() {
		refreshBuildings();
		refreshTours();
	}
	
	
	public void onBtnShowGallery(View v) {
		Intent intent = new Intent(sContext, GalleryActivity.class);
		intent.putExtra("building_id", 1);
		startActivity(intent);
	}

	public static List<BuildingTour> getBuildingsForTour(int tour_id) {
		Map<String, Object> conditions = new HashMap<String, Object>();
		conditions.put("tour_id", tour_id);
		return buildingTourDao.queryForFieldValuesArgs(conditions); // get all buildings associated to a tour
	}

	public static int getBuildingImageResource(Building building) {
		return getContext().getResources().getIdentifier(building.getPhoto(), "drawable", getContext().getPackageName());
	}

	public static List<Integer> getBuildingPhotosForTour(int tour_id) {
		List<Integer> images = new ArrayList<Integer>();
		List<BuildingTour> buildingsTour = getBuildingsForTour(tour_id);
		for (BuildingTour buildingTour : buildingsTour) {
			images.add(getBuildingImageResource(buildingTour.getBuilding()));
		}
		return images;
	}
	
	
}
