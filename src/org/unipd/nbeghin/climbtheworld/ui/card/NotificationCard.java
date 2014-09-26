package org.unipd.nbeghin.climbtheworld.ui.card;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.unipd.nbeghin.climbtheworld.MainActivity;
import org.unipd.nbeghin.climbtheworld.R;
import org.unipd.nbeghin.climbtheworld.models.Building;
import org.unipd.nbeghin.climbtheworld.models.Climbing;
import org.unipd.nbeghin.climbtheworld.models.Notification;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.fima.cardsui.objects.Card;
import com.j256.ormlite.stmt.query.SetValue;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class NotificationCard extends Card{

	final Notification	notification;
	ImageButton acceptBtn;
	ImageButton cancelBtn;
	TextView text;
	public NotificationCard(Notification notification) {
		super(notification.getId());
		this.notification = notification;
	}
	
	private String setMessage(){
		switch (notification.getType()) {
		case INVITE_IN_GROUP:
			return notification.getSender() + " asked you to Join new group: " + notification.getGroupName();
		default:
			return "";
		}
	}

	@Override
	public View getCardContent(Context context) {
		View view = LayoutInflater.from(context).inflate(R.layout.notification_card, null);
		cancelBtn = ((ImageButton) view.findViewById(R.id.cancelButton));
		acceptBtn = (ImageButton) view.findViewById(R.id.acceptButton);
		text = ((TextView) view.findViewById(R.id.text));
		text.setText(setMessage());
		acceptBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				ParseQuery<ParseObject> query = ParseQuery
						.getQuery("Group");
				query.whereEqualTo("name", notification.getGroupName());
				query.findInBackground(new FindCallback<ParseObject>() {
					public void done(List<ParseObject> group,
							ParseException e) {
						if (e == null) {					
							if(group.size() == 0){
								Toast.makeText(MainActivity.getContext(), "The group does not exists anymore", Toast.LENGTH_SHORT).show();
								text.setText("Request expired");

							}else{
								updateGroup(group.get(0));
							deleteRequest(String.valueOf(notification.getId()));	
							text.setText("Request Accepted");
							
							}
							cancelBtn.setEnabled(false);
							acceptBtn.setEnabled(false);
							notification.setRead(true);
						} else {
							Toast.makeText(MainActivity.getContext(), "Connection problem", Toast.LENGTH_SHORT).show();

							Log.d("Connection problem",
									"Error: " + e.getMessage());

							
						}
					}
				
		});
			
			
			}
		});
		
		cancelBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				deleteRequest(String.valueOf(notification.getId()));	
				text.setText("Request Deleted");
				cancelBtn.setEnabled(false);
				acceptBtn.setEnabled(false);
				notification.setRead(true);

			}
		});
			
	
		
		return view;
	}
	
	
	
	private void updateGroup(ParseObject group) {
		SharedPreferences pref = MainActivity.getContext().getSharedPreferences("UserSession", 0);
		//JSONArray members = group.getJSONArray("members");
		//final String groupName = group.getString("name");
		List<String> members = group.getList("members");
		if (members.size() < 5) {
			
			members.add(pref.getString("FBid", ""));
			group.saveEventually();
			
		/*	JSONObject member = new JSONObject();
			try {
				member.put("FBid", pref.getString("FBid", ""));
				member.put("name", pref.getString("username", ""));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			members.put(member);
			group.saveEventually();
			
			ParseQuery<ParseUser> user = ParseUser.getQuery();
			user.whereEqualTo("FBid", pref.getString("FBid", "")); Log.d("FBid da cercare", "cerco " + pref.getString("FBid", "")); 
			user.findInBackground(new FindCallback<ParseUser>() {
			    public void done(List<ParseUser> users, ParseException e) {
			        if (e == null) { System.out.println(users.size());
			        		ParseUser me = users.get(0);
			        		me.addAllUnique("Groups", Arrays.asList(groupName));
			        		me.saveEventually();
			        } else {

			        }
			    }
			});*/
			
		} else {
			Toast.makeText(MainActivity.getContext(),
					"Too Late: group is complete", Toast.LENGTH_SHORT).show();
		}

	}
	
	private void deleteRequest(String inRequestId) {
	    // Create a new request for an HTTP delete with the
	    // request ID as the Graph path.
	    Request request = new Request(Session.getActiveSession(), 
	        inRequestId, null, HttpMethod.DELETE, new Request.Callback() {

	            @Override
	            public void onCompleted(Response response) {
	                // Show a confirmation of the deletion
	                // when the API call completes successfully.
	                Toast.makeText(MainActivity.getContext(), "Request deleted",
	                Toast.LENGTH_SHORT).show();
	            }
	        });
	    // Execute the request asynchronously.
	    Request.executeBatchAsync(request);
	}

	@Override
	public boolean convert(View convertCardView) {
		// TODO Auto-generated method stub
		return false;
	}
}

