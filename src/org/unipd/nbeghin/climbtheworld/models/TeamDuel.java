package org.unipd.nbeghin.climbtheworld.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "team_duel")
public class TeamDuel {
	@DatabaseField(generatedId = true)
	private int			_id;
	@DatabaseField
	private String challenger_name;
	@DatabaseField
	private int steps_my_group;
	@DatabaseField
	private int steps_other_group;
	@DatabaseField
	private int saved;
	@DatabaseField
	private String id_online;
	@DatabaseField
	private int creator;
	@DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true, columnName = "building_id")
	private Building building;
	@DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true, columnName = "user_id")
	private User user;
	@DatabaseField
	private int my_steps;
	
	
	public TeamDuel(){}
	
	public int get_id() {
		return _id;
	}
	public void set_id(int _id) {
		this._id = _id;
	}
	public int getSteps_my_group() {
		return steps_my_group;
	}
	public void setSteps_my_group(int steps_my_group) {
		this.steps_my_group = steps_my_group;
	}
	public int getSteps_other_group() {
		return steps_other_group;
	}
	public void setSteps_other_group(int steps_other_group) {
		this.steps_other_group = steps_other_group;
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

	public String getId_online() {
		return id_online;
	}

	public void setId_online(String id_online) {
		this.id_online = id_online;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Building getBuilding() {
		return building;
	}

	public void setBuilding(Building building) {
		this.building = building;
	}

	public void setCreator(boolean creator){
		if(creator){
			this.creator = 1;
		}else{
			this.creator = 0;
		}
	}
	
	public boolean isCreator(){
		if(creator == 1)
			return true;
		else
			return false;
	}

	public String getChallenger_name() {
		return challenger_name;
	}

	public void setChallenger_name(String challenger_name) {
		this.challenger_name = challenger_name;
	}

	public int getMy_steps() {
		return my_steps;
	}

	public void setMy_steps(int my_steps) {
		this.my_steps = my_steps;
	}
	
	
}
