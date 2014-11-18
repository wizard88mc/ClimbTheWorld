package org.unipd.nbeghin.climbtheworld;

import org.unipd.nbeghin.climbtheworld.adapters.RectangleShapeAdapter;
import org.unipd.nbeghin.climbtheworld.util.AlarmUtils;

import com.etsy.android.grid.StaggeredGridView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.DisplayMetrics;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class AlgorithmConfigFragment extends Fragment {

	//questo codice fino a 'onDetach()' è cio' che serve per lavorare con i
	//callbacks dell'Activity
	private Callbacks mCallbacks = sDummyCallbacks;

	
	private static int screen_width;
	private static int screen_height;
	
	//intero che indica il colore selezionato (-1 se non è ancora stato selezionato alcun colore)
	private int current_color=-1;
	//map che contiene le coppie di valori <posizione_item, colore>
	private static SparseIntArray positions_colors;
	
	
	
	
	/**
	 * 
	 * Interfaccia contenente un metodo che permette a questo fragment di informare la relativa
	 * activity che il task eseguito è finito. Tale interfaccia è implementata dall'activity.
	 *
	 */
	public interface Callbacks
	{
		/**
		 * Metodo usato dal fragment per informare la relativa activity che il task eseguito è finito.
		 * @param context contesto in cui viene chiamato il metodo
		 * @param intent intent con il quale viene lanciata un'opportuna activity in cui mostrare 
		 *               il risultato del task
		 */
		public void onTaskFinished(Context context, Intent intent);
	}
	    
	//si inizializza il campo per il callback
	private static Callbacks sDummyCallbacks = new Callbacks()
	{
		@Override
		public void onTaskFinished(Context context, Intent intent) { }
	};
	
	
	public void onAttach(Activity activity) {
		
		super.onAttach(activity);    	
    	
		if (!(activity instanceof Callbacks))
			{
			throw new IllegalStateException("Activity must implement fragment's callbacks.");
			}
		mCallbacks = (Callbacks) activity;
	};
	
	
	@Override
	public void onDetach() {
		super.onDetach();
		mCallbacks = sDummyCallbacks;
	}
	
	
	//si salva un riferimento al fragment manager. Questo è inizializzato in 'onCreate()'
    private FragmentManager mFM;

    //intero per identificare il fragment che chiama 'onActivityResult()'; non se ne ha
    //veramente il bisogno visto che in tal caso si ha un solo fragment da gestire
    static final int TASK_FRAGMENT = 0;

    //stringa che indica un tag per fare in modo di ritrovare il task fragment, in un'altra
    //istanza di questo fragment dopo una rotazione
    static final String TASK_FRAGMENT_TAG = "task";
	
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	   
    	/*
    	//a tal punto il fragment può essere stato ricreato a causa di una rotazione,
        //e possiamo trovare il TaskFragment
        mFM = getFragmentManager();
        TaskFragment taskFragment = (TaskFragment)mFM.findFragmentByTag(TASK_FRAGMENT_TAG);

        if (taskFragment != null)
        {
         //si aggiorna il target fragment, per considerare il nuovo fragment invece del vecchio
         taskFragment.setTargetFragment(this, TASK_FRAGMENT);
        }
        */
    }
    
    
    
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
    		Bundle savedInstanceState) {
    	return inflater.inflate(R.layout.fragment_algorithm_config, container, false);
    }
    
    
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
    	super.onViewCreated(view, savedInstanceState);
    	
    }
    
    
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);
    	
    	//ExpandableHeightGridView gview= (ExpandableHeightGridView) getActivity().findViewById(R.id.alg_conf_gridview);
    	//GridView gridview = (GridView) getActivity().findViewById(R.id.alg_conf_gridview);
    	//gridview.setAdapter(new RectangleShapeAdapter(this.getActivity()));
    	//gview.setExpanded(true);
    	//gview.setAdapter(new RectangleShapeAdapter(this.getActivity()));
    	
    	if(positions_colors==null){
    		positions_colors=new SparseIntArray(24);
    		initializeMap();
    	}
    	
    	
    	DisplayMetrics metrics = new DisplayMetrics();
    	getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

    	screen_height = metrics.widthPixels;
    	screen_width = metrics.heightPixels;
    	
    	
    	GradientDrawable rectangle_shape = (GradientDrawable) getResources().getDrawable(R.drawable.rectangle_shape);
    	((GradientDrawable)rectangle_shape.mutate()).setColor(Color.GREEN);    	
    	TextView green_picker = (TextView) getActivity().findViewById(R.id.green_picker);    	
    	green_picker.setBackground(rectangle_shape);
    	
    	rectangle_shape=((GradientDrawable)rectangle_shape.getConstantState().newDrawable());
    	rectangle_shape.setColor(Color.RED);  
    	TextView red_picker = (TextView) getActivity().findViewById(R.id.red_picker);    	
    	red_picker.setBackground(rectangle_shape);
    	
    	rectangle_shape=((GradientDrawable)rectangle_shape.getConstantState().newDrawable());
    	rectangle_shape.setColor(Color.YELLOW);  
    	TextView yellow_picker = (TextView) getActivity().findViewById(R.id.yellow_picker);    	
    	yellow_picker.setBackground(rectangle_shape);
    	
    	
    	StaggeredGridView gridView = (StaggeredGridView) getActivity().findViewById(R.id.alg_conf_gridview);

    	
    	ListView lstV = new ListView(getActivity());
    	
    	Button config_btt = new Button(getActivity());
    	config_btt.setText(R.string.config_button_text);
    	config_btt.setId(R.string.config_button_id);
    	config_btt.setBackgroundResource(R.drawable.blue_button_style);
    	config_btt.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				if(!allTimeSlotsSetted()){
					Toast.makeText(getActivity(), getActivity().getResources().getText(R.string.set_timeslots_btt), Toast.LENGTH_SHORT).show();
				}
				else{
					AlarmUtils.createIntervals(getActivity(), positions_colors);
				}
				
			}
		});
    	lstV.addFooterView(config_btt);
    	
    	
    	lstV.setAdapter(new RectangleShapeAdapter(this.getActivity()));
    	    	
    	gridView.setAdapter(lstV.getAdapter());
    	gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				if(current_color!=-1){ //è stato selezionato uno dei tre colori
					positions_colors.put(position, current_color);
					GradientDrawable rect_shape_view = (GradientDrawable) view.getBackground();
					rect_shape_view.setColor(current_color);	
					((TextView)view).setTypeface(null, Typeface.BOLD_ITALIC);
				}
				else{ //non è stato ancora selezionato alcun colore
					Toast.makeText(getActivity(), getActivity().getResources().getText(R.string.select_color_toast), Toast.LENGTH_SHORT).show();
				}
			}
		});
    }
    
    
    
    //metodo chiamato quando il task è concluso che, a sua volta, chiama il metodo 'onTaskFinished' 
  	//dell'oggetto Callbacks per informare l'activity dell'avvenuta conclusione del processo
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	
    	if (requestCode == TASK_FRAGMENT && resultCode == Activity.RESULT_OK)
        {
            //si informa l'activity che è finito il task
            mCallbacks.onTaskFinished(getActivity(),data);
        }
    }
    
    
    //il seguente metodo è chiamato quando tutti gli stati salvati sono stati ripristinati nel
  	//fragment. E' qui usato per fare un'inizializzazione basata sullo stato salvato che si lascia
  	//in gestione alla vista stessa (ad esempio le label).
  	//Tale metodo è chiamato dopo onActivityCreated(Bundle) e prima di onStart()
    @Override
    public void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
		
		//si salva l'intero che indica il colore selezionato (-1 se ancora nessun colore selezionato)
		outState.putInt("current_color", current_color);
    }
    
    //il seguente metodo è chiamato quando tutti gli stati salvati sono stati ripristinati nel
  	//fragment. E' qui usato per fare un'inizializzazione basata sullo stato salvato che si lascia
  	//in gestione alla vista stessa (ad esempio le label).
  	//Tale metodo è chiamato dopo onActivityCreated(Bundle) e prima di onStart()
    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
    	super.onViewStateRestored(savedInstanceState);
    	
    	if(savedInstanceState!=null){    		
    		//si recupera lo stato salvato che indica il colore selezionato
    		current_color=savedInstanceState.getInt("current_color");
    	}
    }
    
    
    
   
    public void pickColor(View v){
    	
    	switch(v.getId()) {
    		case R.id.red_picker:
    			current_color=Color.RED;
    			break;
    		case R.id.green_picker:
    			current_color=Color.GREEN;
    			break;
    		case R.id.yellow_picker: 
    			current_color=Color.YELLOW;
    			break;
    	}
    }
    
    
    private void initializeMap(){ 
    	
    	for(int i = 0; i < 24; i++) {       		
    		positions_colors.put(i,-1);
    	}
    }
    
    
    private boolean allTimeSlotsSetted(){ 
    	
    	for(int i = 0; i < positions_colors.size(); i++) { 
    		
    		if(positions_colors.get(i)==-1)
    			return false;
    	}    	
    	return true;
    }
    
    
    
    public static int getPositionColor(int position){
    	return positions_colors.get(position, -1);
    }
    
    
    
    public static int getScreenWidth(){
    	return screen_width;
    }
    
    public static int getScreenHeight(){
    	return screen_height;
    }
}