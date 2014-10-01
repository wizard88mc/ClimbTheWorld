package org.unipd.nbeghin.climbtheworld.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;


@DatabaseTable(tableName = "competitions")
public class Competition {
	@DatabaseField(generatedId = true)
	private int _id;
	@DatabaseField
	private String id_online;	
	@DatabaseField
	private int my_stairs;
	@DatabaseField
	private int current_position;
	@DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true, columnName = "building_id")
	private Building	 building;
	@DatabaseField
	private int saved;
	@DatabaseField
	private int leaved;
	@DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true, columnName = "user_id")
	private User	 user;
	@DatabaseField
	private int completed;


	public Competition(){}


	public int get_id() {
		return _id;
	}


	public void set_id(int _id) {
		this._id = _id;
	}


	public String getId_online() {
		return id_online;
	}


	public void setId_online(String id_online) {
		this.id_online = id_online;
	}


	public int getMy_stairs() {
		return my_stairs;
	}


	public void setMy_stairs(int my_stairs) {
		this.my_stairs = my_stairs;
	}


	public int getCurrent_position() {
		return current_position;
	}


	public void setCurrent_position(int current_position) {
		this.current_position = current_position;
	}


	public Building getBuilding() {
		return building;
	}


	public void setBuilding(Building building) {
		this.building = building;
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
	
	public void setLeaved(boolean leaved){
		if(leaved){
			this.leaved = 1;
		}else{
			this.leaved = 0;
		}
	}
	
	public boolean isLeaved(){
		if(leaved == 1)
			return true;
		else
			return false;
	}

	public void setCompleted(boolean completed){
		if(completed){
			this.completed = 1;
		}else{
			this.completed = 0;
		}
	}
	
	public boolean isCompleted(){
		if(completed == 1)
			return true;
		else
			return false;
	}
	
	public User getUser() {
		return user;
	}


	public void setUser(User user) {
		this.user = user;
	}
	
	

}
