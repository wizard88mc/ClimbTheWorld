package org.unipd.nbeghin.climbtheworld.models;

import org.unipd.nbeghin.climbtheworld.util.GeneralUtils;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * 
 * Classe che definisce un oggetto Alarm che rappresenta un evento di start/stop del processo di
 * classificazione oppure un evento di lancio di un trigger.
 *
 */
@DatabaseTable(tableName = "alarms")
public class Alarm {

	//campi interi che rappresentano i giorni della settimana, utili a controllare se un certo
	//alarm è attivo in un determinato giorno della settimana
	public static final int SUNDAY = 0;
    public static final int MONDAY = 1;
    public static final int TUESDAY = 2;
    public static final int WEDNESDAY = 3;
    public static final int THURSDAY = 4;
    public static final int FRIDAY = 5;
    public static final int SATURDAY = 6;
     
	//si usa questo nome del campo 'id' in modo da considerarlo quando viene costruita una query
    //per ottenere gli alarm aventi un certo id
    public final static String ID_FIELD_NAME = "id";
    //si usa questo nome del campo 'actionType' in modo da considerarlo quando viene costruita una query
    //per ottenere gli alarm che fanno partire o fermano il servizio di classificazione
    public final static String ACTION_TYPE_FIELD_NAME = "actionType";
    //si usa questo nome del campo 'classificatorType' in modo da considerarlo quando viene costruita una query
    //per ottenere gli alarm relativi ad un certo classificatore (Google o scalini/non_scalini)
    public final static String CLASSIF_TYPE_FIELD_NAME = "classificatorType";
    
    @DatabaseField(generatedId = true, canBeNull = false, columnName = ID_FIELD_NAME) 
    private int id;
    @DatabaseField
    private int hour;
    @DatabaseField
	private int minute;
    @DatabaseField
	private int second;
    @DatabaseField(columnName = ACTION_TYPE_FIELD_NAME) 
    private boolean actionType;    //per ora boolean (con lancio trigger: int, 0,1,-1)
   // @DatabaseField(columnName = CLASSIF_TYPE_FIELD_NAME) 
    //private boolean classificatorType; 
        
    @DatabaseField(dataType = DataType.SERIALIZABLE)
    private boolean repeatingDays[] = new boolean[GeneralUtils.daysOfWeek]; // =  new boolean[7];
    //la dimensione è decisa all'inizio a seconda del numero di giorni della settimana
    //(per test algoritmo meno di 7 giorni, es. 1,2)
    
    
          
    //i valori del seguente array indicano le varie probabilità di riconsiderare
    //gli alarm scartati in precedenza; ogni valore dell'array indica la probabilità 
    //di lanciare il relativo alarm (se scartato) in un certo giorno della settimana;
    //tale array viene usato solo per gli alarm di start in quanto un alarm di stop
    //se non è attivo non viene ripescato per essere lanciato 
    //tale array per un certo alarm viene popolato durante la settimana: in 
    //corrispondenza dell'indice relativo ad un certo giorno in cui l'alarm viene
    //considerato viene posto il valore di probabilità calcolato in base alla fitness
    @DatabaseField(dataType = DataType.SERIALIZABLE)
    private float evaluations[] = new float[GeneralUtils.daysOfWeek]; // =  new float[7];
    
     
    //i valori di tale array indicano se l'alarm definisce un "intervallo di gioco",
    //cioè un intervallo in cui l'utente la settimana precedente ha fatto scalini;
    //quindi non si fa partire il servizio di activity recognition, ma ci si aspetta che 
    //l'utente faccia scalini; se non li fa può essere un momento buono per lanciare un 
    //trigger    
    @DatabaseField(dataType = DataType.SERIALIZABLE)
    private boolean gameInterval[] = new boolean[GeneralUtils.daysOfWeek]; // =  new float[7];
    
    
    
    
    /*NO
    // [ aggiungere altri due array di 7 elementi: per probabilità e numero
    //   scalini fatti in quel giorno (array da considerare solo se day=false
    //   per vedere se ri-considerare l'alarm in quel giorno) ] FORSE NO
    */
    
    
    /**
     * Costruttore di un oggetto Alarm (costruttore vuoto per ormlite).
     */
    public Alarm() {
    }
 
    /**
     * Costruttore di un oggetto Alarm 
     * @param hour
     * @param minute
     * @param second
     * @param type
     * @param repeatingDays
     * @param repeatWeekly
     * @param isEnabled
     */
    public Alarm(int hour, int minute, int second, boolean actionType, boolean[] repeatingDays, float[] evaluations) {
		
    	//this.id=id;
    	this.hour=hour;
    	this.minute=minute;
    	this.second=second;
    	this.actionType=actionType;
    	//this.classificatorType= classificatorType;
    	    	
    	for(int i=0; i<repeatingDays.length; i++)
    		this.repeatingDays[i]=repeatingDays[i];
    	
    	for(int i=0; i<evaluations.length; i++)
    		this.evaluations[i]=evaluations[i];
    	
    	for(int i=0; i<gameInterval.length; i++)
    		this.gameInterval[i]=false;
    	
	}

    /**
     * Costruttore
     * @param hour
     * @param minute
     * @param second
     * @param type
     */
    
    public Alarm(int hour, int minute, int second, boolean actionType) {
		
    	//this.id=id;
    	this.hour=hour;
    	this.minute=minute;
    	this.second=second;
    	this.actionType=actionType;
    	//this.classificatorType= classificatorType;
    	
    	for(int i=0; i<repeatingDays.length; i++)
    		this.repeatingDays[i]=true;    	
    
    	//si pone la probabilità di riconsiderare un alarm scartato ad un valore
    	//di default pari a 0,25 in ogni giorno della settimana    	
    	for(int i=0; i<evaluations.length; i++)
    		this.evaluations[i]=0.25f;
    	
    	for(int i=0; i<gameInterval.length; i++)
    		this.gameInterval[i]=false;    	
	}
    
    
	
    public int get_id() {
		return id;
	}

	public void set_id(int id) {
		this.id = id;
	}
	
	public int get_hour() {
		return hour;
	}

	public void set_hour(int hour) {
		this.hour = hour;
	}
    
	public int get_minute() {
		return minute;
	}

	public void set_minute(int minute) {
		this.minute = minute;
	}
	
	
	public int get_second() {
		return second;
	}

	public void set_second(int second) {
		this.second = second;
	}
	
	public boolean get_actionType() {
		return actionType;
	}

	public void set_actionType(boolean type) {
		actionType = type;
	}

	/*
	public boolean get_classificatorType() {
		return classificatorType;
	}

	public void set_classificatorType(boolean type) {
		classificatorType = type;
	}
	*/
	
	/*
	public boolean[] get_repeatingDays(){
		return repeatingDays;
	}
	
	public void set_repeatingDays(boolean[] repeatingDays) {
	
		for(int i=0; i<repeatingDays.length; i++)
    		this.repeatingDays[i]=repeatingDays[i];		
	}
	*/
	
	public void setRepeatingDay(int dayOfWeek, boolean value) {
        repeatingDays[dayOfWeek] = value;
    }
 
    public boolean getRepeatingDay(int dayOfWeek) {
        return repeatingDays[dayOfWeek];
    }

    public void setEvaluation(int dayOfWeek, float value) {
    	
    	//il valore non può mai essere > 1 
    	//if(value>1.0f){
    	//	value=1.0f;
    	//}    	
    	evaluations[dayOfWeek] = value;
    }
 
    public float getEvaluation(int dayOfWeek) {
        return evaluations[dayOfWeek];
    }
    
    
    public void setGameInterval(int dayOfWeek, boolean value) {
    	
    	gameInterval[dayOfWeek] = value;
    }
    
    public boolean isGameInterval(int dayOfWeek) {
        return gameInterval[dayOfWeek];
    }
    
}