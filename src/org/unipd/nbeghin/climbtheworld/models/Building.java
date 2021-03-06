package org.unipd.nbeghin.climbtheworld.models;

import java.io.Serializable;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "buildings")
public class Building implements Serializable {
	private static final long	serialVersionUID	= 1L;
	@DatabaseField(generatedId = true)
	private int				_id;
	@DatabaseField
	private String			name;
	@DatabaseField
	private String			description;
	@DatabaseField
	private String			location;
	@DatabaseField
	private String			photo;
	@DatabaseField
	private String			url;
	@DatabaseField
	private int				height;
	@DatabaseField
	private int				steps;
	public static final int	average_step_height	= 17;	// in cm

	Building() {} // needed by ormlite

	public int get_id() {
		return _id;
	}

	public void set_id(int _id) {
		this._id = _id;
	}

	public String getName() {
		return name;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public String getPhoto() {
		return photo;
	}

	public void setPhoto(String photo) {
		this.photo = photo;
	}

	public int getSteps() {
		return this.height * 100 / average_step_height;
	}

	public void setSteps(int steps) {
		this.steps = steps;
	}
}
