package org.unipd.nbeghin.climbtheworld.util;

import java.util.List;

import org.unipd.nbeghin.climbtheworld.models.Building;

public class ModelsUtil {

	public static int getIndexByProperty(int id, List<Building> buildings) {
        for (int i = 0; i < buildings.size(); i++) {
            if (buildings.get(i).get_id() == id) {
                return i;
            }
        }
        return -1;// not there is list
    }
}
