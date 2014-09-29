package org.unipd.nbeghin.climbtheworld.ui.card;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.unipd.nbeghin.climbtheworld.MainActivity;
import org.unipd.nbeghin.climbtheworld.R;
import org.unipd.nbeghin.climbtheworld.models.AskCollaborationNotification;
import org.unipd.nbeghin.climbtheworld.models.Building;
import org.unipd.nbeghin.climbtheworld.models.Climbing;
import org.unipd.nbeghin.climbtheworld.models.Collaboration;
import org.unipd.nbeghin.climbtheworld.models.Notification;
import org.unipd.nbeghin.climbtheworld.models.NotificationType;
import org.unipd.nbeghin.climbtheworld.util.ModelsUtil;

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

public class NotificationCard extends Card {

	final Notification notification;
	ImageButton acceptBtn;
	ImageButton cancelBtn;
	TextView text;

	public NotificationCard(Notification notification) {
		super(notification.getId());
		this.notification = notification;
	}

	private String setMessage() {
		switch (notification.getType()) {
		case INVITE_IN_GROUP:
			return notification.getSender() + " asked you to Join new group: " + notification.getGroupName();
		case ASK_COLLABORATION:
			return notification.getSender() + " asks help to " + notification.getGroupName() + " to climb " + ((AskCollaborationNotification) notification).getBuilding_name();
		default:
			return "";
		}
	}

	@Override
	public View getCardContent(final Context context) {
		View view = LayoutInflater.from(context).inflate(R.layout.notification_card, null);
		cancelBtn = ((ImageButton) view.findViewById(R.id.cancelButton));
		acceptBtn = (ImageButton) view.findViewById(R.id.acceptButton);
		text = ((TextView) view.findViewById(R.id.text));
		text.setText(setMessage());
		acceptBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {

				NotificationType type = notification.getType();
				switch (type) {
				case INVITE_IN_GROUP:
					ParseQuery<ParseObject> query = ParseQuery.getQuery("Group");
					query.whereEqualTo("name", notification.getGroupName());
					query.findInBackground(new FindCallback<ParseObject>() {
						public void done(List<ParseObject> group, ParseException e) {
							if (e == null) {
								if (group.size() == 0) {
									Toast.makeText(MainActivity.getContext(), "The group does not exists anymore", Toast.LENGTH_SHORT).show();
									text.setText("Request expired");

								} else {
									SharedPreferences pref = context.getSharedPreferences("UserSession", 0);
									JSONObject members = group.get(0).getJSONObject("members");
									if (members.has(pref.getString("FBid", ""))) {
										Toast.makeText(MainActivity.getContext(), "You are already part of " + notification.getGroupName(), Toast.LENGTH_SHORT).show();
										text.setText("Already a member");
									} else {
										updateGroup(group.get(0));
										text.setText("Request Accepted");
									}

								}
								deleteRequest(String.valueOf(notification.getId()));

								cancelBtn.setEnabled(false);
								acceptBtn.setEnabled(false);
								notification.setRead(true);
							} else {
								Toast.makeText(MainActivity.getContext(), "Connection problem", Toast.LENGTH_SHORT).show();

								Log.d("Connection problem", "Error: " + e.getMessage());

							}
						}

					});
					break;
				case ASK_COLLABORATION:
					//TODO
					//prendi oggetto collab con l'id ricevuto
					//aggiungiti in stairs e collaborators
					//crea climbing con nuova modalita

					final AskCollaborationNotification current = ((AskCollaborationNotification) notification);

		
									//CODICE DA SISTEMARE CON TODO
					
									ParseObject collab = new ParseObject("Collaboration");
									collab.put("building", current.getBuilding_id());
									JSONObject stairs = new JSONObject();
									//Iterator ids = members.keys();
									/*while (ids.hasNext()) {
										try {
											stairs.put(((String) ids.next()), 0);
										} catch (JSONException e1) {
											// TODO Auto-generated catch block
											e1.printStackTrace();
										}
									}*/

									collab.put("stairs", stairs);
									collab.saveEventually();

									String idgroup = current.getGroupId();
									Building building = MainActivity.buildings.get(ModelsUtil.getIndexByProperty(current.getBuilding_id(), MainActivity.buildings));

									Collaboration coll = new Collaboration();
									coll.setBuilding(building);
									coll.setGroupId(idgroup);
									coll.setMy_stairs(0);
									coll.setOthers_stairs(0);
									coll.setId(collab.getObjectId());
									MainActivity.collaborationDao.create(coll);
									// creare un climbing con game mode
									// modificato
								
							
					

					break;

				}

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
		JSONObject members = group.getJSONObject("members");
		// final String groupName = group.getString("name");

		// List<String> members = group.getList("members");

		if (members.length() < 5) {

			try {
				members.put(pref.getString("FBid", ""), pref.getString("username", ""));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			group.put("members", members);
			group.saveEventually();

			/*
			 * JSONObject member = new JSONObject(); try { member.put("FBid",
			 * pref.getString("FBid", "")); member.put("name",
			 * pref.getString("username", "")); } catch (JSONException e) { //
			 * TODO Auto-generated catch block e.printStackTrace(); }
			 * members.put(member); group.saveEventually();
			 * 
			 * ParseQuery<ParseUser> user = ParseUser.getQuery();
			 * user.whereEqualTo("FBid", pref.getString("FBid", ""));
			 * Log.d("FBid da cercare", "cerco " + pref.getString("FBid", ""));
			 * user.findInBackground(new FindCallback<ParseUser>() { public void
			 * done(List<ParseUser> users, ParseException e) { if (e == null) {
			 * System.out.println(users.size()); ParseUser me = users.get(0);
			 * me.addAllUnique("Groups", Arrays.asList(groupName));
			 * me.saveEventually(); } else {
			 * 
			 * } } });
			 */

		} else {
			Toast.makeText(MainActivity.getContext(), "Too Late: group is complete", Toast.LENGTH_SHORT).show();
		}

	}

	private void deleteRequest(String inRequestId) {
		// Create a new request for an HTTP delete with the
		// request ID as the Graph path.
		Request request = new Request(Session.getActiveSession(), inRequestId, null, HttpMethod.DELETE, new Request.Callback() {

			@Override
			public void onCompleted(Response response) {
				// Show a confirmation of the deletion
				// when the API call completes successfully.
				Toast.makeText(MainActivity.getContext(), "Request deleted", Toast.LENGTH_SHORT).show();
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
