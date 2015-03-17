package org.unipd.nbeghin.climbtheworld.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import android.app.AlarmManager;
import android.content.Context;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

/**
 * 
 * Classe che contiene alcuni metodi di utilità.
 *
 */
public final class GeneralUtils {
	
	//numero di giorni di cui è composta una settimana
	public static int daysOfWeek = 2;
	private static AlarmManager alarmMgr;

	
	/**
	 * Costruttore della classe.
	 */
	private GeneralUtils(){
		
	}
	
	
	/**
     * Il metodo <code>isInternetConnectionUp</code> permette di controllare se 
     * è disponibile o meno una connessione dati.
     * 
     * @param context
     *            contesto dell'activity che chiama il metodo
     * @return 'true' se è disponibile una connessione dati, 'false' altrimenti
     */
    public static boolean isInternetConnectionUp(Context context) {
    	
    	ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    	NetworkInfo netInfo = connMgr.getActiveNetworkInfo();
       
    	if (netInfo!=null && netInfo.isConnected()) {
    		return true;
    	}
    	return false;
    }
	

    private static String getContent(HttpResponse response) throws IOException {
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String body = "";
        String content = "";

        while ((body = rd.readLine()) != null){
            content += body + "\n";
        }
        return content.trim();
    }
    
    
    
    public static void renameLogFile(Context context, int log_file_id){
    	    	
    	//si rinomina il file di log aggiungendo l'id ritornato dal server
    	File from = new File(context.getDir("climbTheWorld_dir", Context.MODE_PRIVATE), "game_log");
    	File to = new File(context.getDir("climbTheWorld_dir", Context.MODE_PRIVATE), "game_log_"+log_file_id);
       	from.renameTo(to);
       	
    	//si salva l'id nelle shared preferences
    	PreferenceManager.getDefaultSharedPreferences(context).edit().putInt("game_log_file_id", log_file_id).commit();
    }
    
    
    @SuppressWarnings("deprecation")
	public static String uploadGameLogFile(Context context) throws IOException{
    	
    	String log_file_name="";
    	int log_file_id = PreferenceManager.getDefaultSharedPreferences(context).getInt("log_game_file_id", -1);
    	    	
    	if(log_file_id==-1){
    		log_file_name="game_log";
    	}
    	else{
    		log_file_name="game_log"+log_file_id;
    	}    	
    	
    	final File logFile = new File(context.getDir("climbTheWorld_dir", Context.MODE_PRIVATE), log_file_name);
    	
    	
    	if(logFile.exists()){
    		
    		//Php script path
        	String uploadServerUri = "http://www.learningquiz.altervista.org/quiz_game_api/uploadGameLogFile.php";
        	    		
        	HttpClient client = new DefaultHttpClient();
        	
        	HttpPost post = new HttpPost(uploadServerUri);
        	MultipartEntityBuilder builder = MultipartEntityBuilder.create();        
        	builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

            FileBody fb = new FileBody(logFile);

            builder.addPart("file", fb);  
            builder.addTextBody("log_file_id", String.valueOf(log_file_id), ContentType.TEXT_PLAIN);
        	
            final HttpEntity entity = builder.build();
        	 
            post.setEntity(entity);
            
            HttpResponse response = null;
            
            try {
    			response = client.execute(post);
    		} catch (ClientProtocolException e) {
    			e.printStackTrace();
    		} catch (IOException e) {
    			e.printStackTrace();
    		}        
            
            
            //response code from the server
            int server_response_code = response.getStatusLine().getStatusCode();
                    
            if(server_response_code==200){ //ok        	
            	return getContent(response);
            }
            else{        	
            	return "server_error";
            }
    	}
    	else{
    		return "logfile_not_exists";
    	}
    }
    
   
}