package org.unipd.nbeghin.climbtheworld.models;

import java.io.Serializable;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "users")
public class User implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@DatabaseField(generatedId = true)
	private int				_id;
	@DatabaseField
	private String			FBid;
	@DatabaseField
	private String			name;
	@DatabaseField
	private int				XP;
	@DatabaseField
	private int				level;
	@DatabaseField
	private int 				owner;
	
	
	
	public User(){} //needed by ormlite

	public String getFBid() {
		return FBid;
	}

	public void setFBid(String fBid) {
		FBid = fBid;
	}

	public int getXP() {
		return XP;
	}

	public void setXP(int xP) {
		XP = xP;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int get_id() {
		return _id;
	}

	public void set_id(int _id) {
		this._id = _id;
	}
	
	public boolean isOwner(){
		if(owner ==  1)
			return true;
		else return false;
	}
	
	public void setOwner(boolean isOwner){
		if(isOwner)
			owner = 1;
		else owner = 0 ;
	}

	
}
