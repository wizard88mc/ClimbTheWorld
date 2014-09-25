package org.unipd.nbeghin.climbtheworld;
//TODO stabilire se nome gruppo è valido
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
import com.facebook.FacebookException;
import com.parse.FindCallback;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.ParseException;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.Toast;

public class GroupsActivity extends ActionBarActivity {
    /** Called when the activity is first created. */
	private ExpandListAdapter ExpAdapter;
	private ArrayList<ExpandListGroup> ExpListItems;
	private ExpandableListView ExpandList;
	private EditText newName;
	private ImageButton sendRequests;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);
        ExpandList = (ExpandableListView) findViewById(R.id.groupsList);
        //ExpListItems = 
        setMyGroups();
        ExpAdapter = new ExpandListAdapter(GroupsActivity.this, ExpListItems);
        ExpandList.setAdapter(ExpAdapter);
        newName = (EditText) findViewById(R.id.insertName);
        sendRequests = (ImageButton) findViewById(R.id.addMembers);
        
        sendRequests.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				onCreateNewGroup();
				
			}
		});
    }
    
    public void setMyGroups(){
    		List<String> myGroups = new ArrayList<String>(); 	
		SharedPreferences pref = getSharedPreferences("UserSession", 0);
		ParseQuery<ParseUser> user = ParseUser.getQuery();
		user.whereEqualTo("FBid", pref.getString("FBid", "")); Log.d("FBid da cercare", "cerco " + pref.getString("FBid", "")); 
		try {
			List<ParseUser> users = user.find();
			List<String> array =	(ArrayList<String>) users.get(0).get("Groups");
	        SetGroups((array));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*user.findInBackground(new FindCallback<ParseUser>() {
		    public void done(List<ParseUser> users, ParseException e) {
		        if (e == null) { 
		        	String[] array =	(String[]) users.get(0).get("groups");
		        SetGroups(Arrays.asList(array));
		        } else {

		        }
		    }
		});*/
    }
    
    public void SetGroups(List<String> groups) {

	 	ArrayList<ExpandListGroup> list = new ArrayList<ExpandListGroup>();
	   	
	 	if(groups.size() == 0){ System.out.println("nessun gruppo");
	 		 ExpandListGroup gru1 = new ExpandListGroup();
	         gru1.setName("No Groups :(");
	         
	         list.add(gru1);
	 	}else{
	 	
    	for(final String group : groups){
        ExpandListGroup gru1 = new ExpandListGroup();
        gru1.setName(group);
        list.add(gru1);
    	}
	 	}
    	
  //  	ArrayList<ExpandListChild> list2 = new ArrayList<ExpandListChild>();
        
   /*     ExpandListChild ch1_1 = new ExpandListChild();
        ch1_1.setName("A movie");
        ch1_1.setTag(null);
        list2.add(ch1_1);
        ExpandListChild ch1_2 = new ExpandListChild();
        ch1_2.setName("An other movie");
        ch1_2.setTag(null);
        list2.add(ch1_2);
        ExpandListChild ch1_3 = new ExpandListChild();
        ch1_3.setName("And an other movie");
        ch1_3.setTag(null);
        list2.add(ch1_3);
        gru1.setItems(list2); */
        //list2 = new ArrayList<ExpandListChild>();
        
        
    /*    ExpandListChild ch2_1 = new ExpandListChild();
        ch2_1.setName("A movie");
        ch2_1.setTag(null);
        list2.add(ch2_1);
        ExpandListChild ch2_2 = new ExpandListChild();
        ch2_2.setName("An other movie");
        ch2_2.setTag(null);
        list2.add(ch2_2);
        ExpandListChild ch2_3 = new ExpandListChild();
        ch2_3.setName("And an other movie");
        ch2_3.setTag(null);
        list2.add(ch2_3);*/
      //  gru2.setItems(list2);
       
        
        ExpListItems = list;
    }
    
    /**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.groups, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				// This ID represents the Home or Up button. In the case of this
				// activity, the Up button is shown. Use NavUtils to allow users
				// to navigate up one level in the application structure. For
				// more details, see the Navigation pattern on Android Design:
				//
				// http://developer.android.com/design/patterns/navigation.html#up-vs-back
				//
				NavUtils.navigateUpFromSameTask(this);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private boolean usableGroupName(String name){
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Group");
		query.whereEqualTo("name", name);
		try {
			int groups = query.count();
			if(groups == 0) return true;
			else return false;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	private void onCreateNewGroup(){
		final String groupName = newName.getText().toString();
		if(groupName.isEmpty() || !usableGroupName(groupName))
			Toast.makeText(getApplicationContext(), "The name must be non empty and not already used", Toast.LENGTH_SHORT).show();
		else {
			//salva gruppo su db con data di creazione (fatta in automatico)
			SharedPreferences pref = getSharedPreferences("UserSession", 0);
			
			
			ParseObject group = new ParseObject("Group");
			group.put("name", groupName);
			JSONArray members = new JSONArray();
			JSONObject member = new JSONObject();
			try {
				member.put("FBid", pref.getString("FBid", ""));
				member.put("name", pref.getString("username", ""));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			members.put(member);
			group.put("members", members);
			
			group.saveInBackground();
			
			ParseQuery<ParseUser> user = ParseUser.getQuery();
			user.whereEqualTo("FBid", pref.getString("FBid", "")); Log.d("FBid da cercare", "cerco " + pref.getString("FBid", "")); 
			user.findInBackground(new FindCallback<ParseUser>() {
			    public void done(List<ParseUser> users, ParseException e) {
			        if (e == null) { System.out.println(users.size());
			        		ParseUser me = users.get(0);
			        		me.addAllUnique("Groups", Arrays.asList(groupName));
			        		me.saveInBackground();
			        } else {

			        }
			    }
			});
			
			
			//invia le richieste per entrare nel gruppo
		Bundle params = new Bundle();
	    params.putString("message", "Hey, let's make a team!!!!");
	    params.putString("data", "{\"team_name\":\"" + groupName + "\", \"type\": \"0\"}");
	    
	    WebDialog requestsDialog = (
	        new WebDialog.RequestsDialogBuilder(this, Session.getActiveSession(), params)).setOnCompleteListener(new OnCompleteListener() {
	                @Override
	                public void onComplete(Bundle values, FacebookException error) {
	                    if (error != null) {
	                        if (error instanceof FacebookOperationCanceledException) {
	                            Toast.makeText(getApplicationContext(), 
	                                "Request cancelled", 
	                                Toast.LENGTH_SHORT).show();
	                        } else {
	                            Toast.makeText(getApplicationContext(), 
	                                "Network Error", 
	                                Toast.LENGTH_SHORT).show();
	                        }
	                    } else {
	                        final String requestId = values.getString("request");
	                        if (requestId != null) {
	                            Toast.makeText(getApplicationContext(), 
	                                "Request sent",  
	                                Toast.LENGTH_SHORT).show();
	                        } else {
	                            Toast.makeText(getApplicationContext(), 
	                                "Request cancelled", 
	                                Toast.LENGTH_SHORT).show();
	                        }
	                    }   
	                }
	            })
	            .build();
	    requestsDialog.show();
	}
	}

}
