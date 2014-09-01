package org.unipd.nbeghin.climbtheworld.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Classe che rappresenta la tabella del database che definisce la relazione molti a molti tra le 
 * tabelle degli alarm (classe {@link Alarm}) e dei modelli orario (classe {@link TimeTemplate}). 
 * Infatti, un template orario può avere più alarm che scattano all'interno di un certo periodo e 
 * un alarm (che descrive semplicemente un evento di start o stop del processo di classificazione 
 * che avviene in un orario fissato e può ripetersi anche in diversi giorni della settimana) può 
 * essere presente in uno o più template.
 */
@DatabaseTable(tableName = "alarmtimetemplates")
public class AlarmTimeTemplate {

	//stringhe che definiscono le colonne di questa tabella
	public final static String ALARM_ID_FIELD_NAME = "alarm_id";
	public final static String TIMETEMPLATE_ID_FIELD_NAME = "timetemplate_id";
	
	//campo per l'identificatore (id) di un record di questa tabella
	@DatabaseField(generatedId = true)
	int id;
	
	//chiave esterna che memorizza semplicemente l'id dell'oggetto 'Alarm' in questa tabella
	@DatabaseField(foreign = true, columnName = ALARM_ID_FIELD_NAME)
	Alarm alarm;
	
	//chiave esterna che memorizza semplicemente l'id dell'oggetto 'TimeTemplate' in questa tabella
	@DatabaseField(foreign = true, columnName = TIMETEMPLATE_ID_FIELD_NAME)
	TimeTemplate timeTemplate;
	
	/**
	 * Costruttore vuoto per ormlite.
	 */
	public AlarmTimeTemplate() {		
	}
	
	/**
	 * Costruttore
	 * @param alarm
	 * @param timeTemplate
	 */
	public AlarmTimeTemplate(Alarm alarm, TimeTemplate timeTemplate){
		
		this.alarm=alarm;
		this.timeTemplate=timeTemplate;
	}
}
