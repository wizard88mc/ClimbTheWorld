package org.unipd.nbeghin.climbtheworld.util;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.unipd.nbeghin.climbtheworld.R;
import org.unipd.nbeghin.climbtheworld.ClimbApplication;
import org.unipd.nbeghin.climbtheworld.MainActivity;
import org.unipd.nbeghin.climbtheworld.exceptions.NoStatFound;
import org.unipd.nbeghin.climbtheworld.models.Building;
import org.unipd.nbeghin.climbtheworld.models.Stat;


import android.util.Log;

import com.j256.ormlite.dao.GenericRawResults;

public class StatUtils {
	/**
	 * Check if a climbing has been performed yesterday
	 * 
	 * @return boolean
	 */
	public static boolean climbedYesterday(int climbing_id) {
//		try {
			return true; // for testing purposes
//			String max_modified = execQuery("SELECT MAX(modified) FROM climbings WHERE id!="+climbing_id);
//			long diff = new Date().getTime() - Long.parseLong(max_modified);
//			Log.i(MainActivity.AppName, "Last climbing diff: "+diff);
//			return diff <= 86400000;
//		} catch (SQLException e) {
//			return false;
//		} catch (NoStatFound e) {
//			return false;
//		}
	}

	public static List<Stat> calculateStats() {
		List<Stat> stats = new ArrayList<Stat>();
		String statName = ClimbApplication.getContext().getString(R.string.fastest);
		String sql = "SELECT building_id,MIN(completed-created) FROM climbings WHERE completed>created";
		try {
			String building_id = execQuery(sql);
			Building building = ClimbApplication.buildingDao.queryForId(Integer.valueOf(building_id));
			stats.add(new Stat(statName, building.getName()));
		} catch (NoStatFound e) {
			stats.add(new Stat(statName, ClimbApplication.getContext().getString(R.string.no_completed_yet)));
		} catch (Exception e) {
			Log.e(MainActivity.AppName, "SQL exception: " + e.getMessage());
		}
		statName = ClimbApplication.getContext().getString(R.string.climbed_buildings);
		sql = "SELECT COUNT(*) FROM climbings WHERE completed>0";
		try {
			String count = execQuery(sql);
			stats.add(new Stat(statName, ClimbApplication.getContext().getString(R.string.n_climbed_buildings, count) ));
		} catch (NoStatFound e) {
			stats.add(new Stat(statName, ClimbApplication.getContext().getString(R.string.no_completed_yet)));
		} catch (Exception e) {
			Log.e(MainActivity.AppName, "SQL exception: " + e.getMessage());
		}
		statName = ClimbApplication.getContext().getString(R.string.in_progress);
		sql = "SELECT COUNT(*) FROM climbings WHERE completed=0";
		try {
			String count = execQuery(sql);
			stats.add(new Stat(statName, ClimbApplication.getContext().getString(R.string.n_in_progress, count)));
		} catch (NoStatFound e) {
			stats.add(new Stat(statName, ClimbApplication.getContext().getString(R.string.no_completed_yet)));
		} catch (Exception e) {
			Log.e(MainActivity.AppName, "SQL exception: " + e.getMessage());
		}
		statName = ClimbApplication.getContext().getString(R.string.climbings);
		sql = "SELECT COUNT(*) FROM climbings";
		try {
			String count = execQuery(sql);
			stats.add(new Stat(statName, ClimbApplication.getContext().getString(R.string.n_climbings, count)));
		} catch (NoStatFound e) {
			stats.add(new Stat(statName, ClimbApplication.getContext().getString(R.string.no_climbing)));
		} catch (Exception e) {
			Log.e(MainActivity.AppName, "SQL exception: " + e.getMessage());
		}
		statName = ClimbApplication.getContext().getString(R.string.available_buildings);
		sql = "SELECT COUNT(*) FROM buildings";
		try {
			String count = execQuery(sql);
			stats.add(new Stat(statName, ClimbApplication.getContext().getString(R.string.n_buildings,count)));
		} catch (NoStatFound e) {
			stats.add(new Stat(statName, ClimbApplication.getContext().getString(R.string.no_building)));
		} catch (Exception e) {
			Log.e(MainActivity.AppName, "SQL exception: " + e.getMessage());
		}
		statName = ClimbApplication.getContext().getString(R.string.available_tours);
		sql = "SELECT COUNT(*) FROM tours";
		try {
			String count = execQuery(sql);
			stats.add(new Stat(statName, ClimbApplication.getContext().getString(R.string.n_tours, count)));
		} catch (NoStatFound e) {
			stats.add(new Stat(statName, ClimbApplication.getContext().getString(R.string.no_tour)));
		} catch (Exception e) {
			Log.e(MainActivity.AppName, "SQL exception: " + e.getMessage());
		}
		return stats;
	}

	private static String execQuery(String sql) throws SQLException, NoStatFound {
		GenericRawResults<String[]> rawResults = ClimbApplication.climbingDao.queryRaw(sql);
		List<String[]> results = rawResults.getResults();
		if (results.isEmpty()) throw new NoStatFound();
		String[] resultArray = results.get(0);
		return resultArray[0];
	}
}
