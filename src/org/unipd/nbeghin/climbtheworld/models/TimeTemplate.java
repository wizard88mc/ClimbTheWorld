package org.unipd.nbeghin.climbtheworld.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "timetemplates")
public class TimeTemplate {

	//si usa questo nome del campo 'id' in modo da considerarlo quando viene costruita una query
    //per ottenere i template orario aventi un certo id
	public final static String ID_FIELD_NAME = "id";
	
	@DatabaseField(id = true, canBeNull = false, columnName = ID_FIELD_NAME) 
    private int id;
	
	@DatabaseField
	public boolean isEnabled;
	 
	/**
	 * Costruttore del template orario, vuoto per ormlite.
	 */
	public TimeTemplate(){		
	}
	
	/**
	 * Costruttore del template orario.
	 * @param id
	 * @param isEnabled
	 */
	public TimeTemplate(int id, boolean isEnabled){
		
		this.id=id;
		this.isEnabled=isEnabled;		
	}
	
	

    public int get_id() {
		return id;
	}

	public void set_id(int id) {
		this.id = id;
	}
	
	
}
