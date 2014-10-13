package org.unipd.nbeghin.climbtheworld.models;

import org.unipd.nbeghin.climbtheworld.ClimbApplication;

import org.unipd.nbeghin.climbtheworld.R;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "user_badges")
public class UserBadge {
	@DatabaseField(generatedId = true)
	private int _id;
	@DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true, columnName = "user_id")
	private User user;
	@DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true, columnName = "badge_id")
	private Badge badge;
	@DatabaseField
	private int obj_id;
	@DatabaseField
	private double percentage;
	@DatabaseField
	private int saved;
	
	
	public UserBadge(){}


	public int get_id() {
		return _id;
	}


	public void set_id(int _id) {
		this._id = _id;
	}


	public Badge getBadge() {
		return badge;
	}


	public void setBadge(Badge badge) {
		this.badge = badge;
	}


	public int getObj_id() {
		return obj_id;
	}


	public void setObj_id(int obj_id) {
		this.obj_id = obj_id;
	}


	public double getPercentage() {
		return percentage;
	}


	public void setPercentage(double percentage) {
		this.percentage = percentage;
	}


	public User getUser() {
		return user;
	}


	public void setUser(User user) {
		this.user = user;
	}
	
	public void setSaved(boolean save){
		if(save)
			saved = 1;
		else
			saved = 0;
	}
	public boolean getSaved(){
		if(saved == 0)
			return false;
		else
			return true;
	}
	
	public String getDescription(){
		switch(badge.getCategory()){
		case BUILDING_COMPLETED:
			Building building = ClimbApplication.getBuildingById(obj_id);
			return ClimbApplication.getContext().getString(R.string.complete_building, building.getName());
		case TOUR_COMPLETED:
			Tour tour = ClimbApplication.getTourById(obj_id);
			return ClimbApplication.getContext().getString(R.string.complete_tour, tour.getTitle());
		case STEPS_COMPLETED:
			return  ClimbApplication.getContext().getString(R.string.steps_badge, badge.getN_steps());
		default:
			return "";
		}
	}
	
	
}
