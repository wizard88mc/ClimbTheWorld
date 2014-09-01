package org.unipd.nbeghin.climbtheworld.models;

import java.util.Date;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "day_stairs")
public class DayStairs {

	//si usa questo nome del campo 'date' in modo da considerarlo quando viene costruita una query
    //per ottenere il numero di scalini fatti in un certo giorno
	public final static String DATE_FIELD_NAME = "date";
		
	@DatabaseField(id = true, canBeNull = false, columnName = DATE_FIELD_NAME, 
			dataType = DataType.DATE_STRING, format = "yyyy-MM-dd") 
    private Date date;
	
	@DatabaseField
	public int steps;
	 
	/**
	 * Costruttore della storia scalini, vuoto per ormlite.
	 */
	public DayStairs() {
	}
	
	/**
	 * Costruttore della storia scalini.
	 * @param id
	 * @param isEnabled
	 */
	public DayStairs(Date date, int steps){
		
		this.date=date;
		this.steps=steps;		
	}
	
	

    public Date get_date() {
		return date;
	}

	public void set_date(Date date) {
		this.date = date;
	}
	
	
	public int get_steps() {
		return steps;
	}

	public void set_steps(int steps) {
		this.steps = steps;
	}	
}