package org.unipd.nbeghin.climbtheworld.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "collaborations")
public class Collaboration {
	@DatabaseField
	private String id;	
	@DatabaseField
	private String groupId;
	@DatabaseField
	private int my_stairs;
	@DatabaseField
	private int others_stairs;
	@DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true)
	private Building	 building;
	@DatabaseField
	private String group_name;
	@DatabaseField
	private int saved;

	public Collaboration(){}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public int getMy_stairs() {
		return my_stairs;
	}

	public void setMy_stairs(int my_stairs) {
		this.my_stairs = my_stairs;
	}

	public int getOthers_stairs() {
		return others_stairs;
	}

	public void setOthers_stairs(int others_stairs) {
		this.others_stairs = others_stairs;
	}

	public Building getBuilding() {
		return building;
	}

	public void setBuilding(Building building) {
		this.building = building;
	}

	public String getGroup_name() {
		return group_name;
	}

	public void setGroup_name(String group_name) {
		this.group_name = group_name;
	}
	
	public void setSaved(boolean saved){
		if(saved){
			this.saved = 1;
		}else{
			this.saved = 0;
		}
	}
	
	public boolean isSaved(){
		if(saved == 1)
			return true;
		else
			return false;
	}
	
}
