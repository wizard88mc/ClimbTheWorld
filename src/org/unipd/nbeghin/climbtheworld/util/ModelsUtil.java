package org.unipd.nbeghin.climbtheworld.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.json.JSONException;
import org.json.JSONObject;
import org.unipd.nbeghin.climbtheworld.models.Building;
import org.unipd.nbeghin.climbtheworld.models.ChartMember;

public class ModelsUtil {

	public static int getIndexByProperty(int id, List<Building> buildings) {
        for (int i = 0; i < buildings.size(); i++) {
            if (buildings.get(i).get_id() == id) {
                return i;
            }
        }
        return -1;// not there is list
    }
	
	public static List<ChartMember> fromJsonToChart(JSONObject json){
		Iterator keys = json.keys();
		List<ChartMember> chart = new ArrayList<ChartMember>();
		while(keys.hasNext()){
			String key;
			try {
				key = (String) keys.next();
				ChartMember m = new ChartMember(key, json.getInt(key));
				chart.add(m);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Collections.sort(chart, new Comparator<ChartMember>(){
			public int compare(ChartMember m1, ChartMember m2){
				if(m1.getScore() == m2.getScore())
					return 0;
				else
					return m1.getScore() < m2.getScore() ? -1 : 1;
			}
		});
		return chart;
	}
	
	public static SortedMap<Integer, String> fromJsonToSortedMap(JSONObject json){
		Iterator keys = json.keys();
		SortedMap<Integer, String> map = new TreeMap<Integer, String>();
		while(keys.hasNext()){
			String key = (String) keys.next();
			try {
				map.put(json.getInt(key), key);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return map;
	}
	
	public static int getMyPosition(String me, SortedMap<Integer, String> map){


		for (Entry<Integer, String> entry : map.entrySet())
		{
			if(entry.getValue().equalsIgnoreCase(me)){
				return map.headMap(entry.getKey()).entrySet().size() + 1;
			}
		}
		return 0;
	}
	
}
