package org.unipd.nbeghin.climbtheworld.ui.card;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.SimpleTimeZone;

import org.json.JSONException;
import org.json.JSONObject;
import org.unipd.nbeghin.climbtheworld.ClimbApplication;
import org.unipd.nbeghin.climbtheworld.MainActivity;
import org.unipd.nbeghin.climbtheworld.R;
import org.unipd.nbeghin.climbtheworld.models.AskCollaborationNotification;
import org.unipd.nbeghin.climbtheworld.models.AskCompetitionNotification;
import org.unipd.nbeghin.climbtheworld.models.Building;
import org.unipd.nbeghin.climbtheworld.models.Climbing;
import org.unipd.nbeghin.climbtheworld.models.Collaboration;
import org.unipd.nbeghin.climbtheworld.models.Competition;
import org.unipd.nbeghin.climbtheworld.models.Notification;
import org.unipd.nbeghin.climbtheworld.models.NotificationType;
import org.unipd.nbeghin.climbtheworld.models.User;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.fima.cardsui.objects.Card;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class NotificationCard extends Card {

	SharedPreferences pref = MainActivity.getContext().getSharedPreferences("UserSession", 0);
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
		case ASK_COMPETITION:
			return notification.getSender() + " asks help to " + notification.getGroupName() + " to climb " + ((AskCompetitionNotification) notification).getBuilding_name();
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

					final AskCollaborationNotification current = ((AskCollaborationNotification) notification);

					//prendi collaborazione da parse
					ParseQuery<ParseObject> queryColl = ParseQuery.getQuery("Collaboration");
					queryColl.whereEqualTo("objectId", current.getCollaborationId());
					queryColl.findInBackground(new FindCallback<ParseObject>() {

						@Override
						public void done(List<ParseObject> collabs, ParseException e) {
							if(e == null){
								if(collabs.size() == 0){
									Toast.makeText(MainActivity.getContext(), "This collaboration does not exists anymore", Toast.LENGTH_SHORT).show();
									text.setText("This collaboration not exists");
								}else{
									//se c'è la collaborazione
									ParseObject collaborationParse = collabs.get(0);
									JSONObject collaborators = collaborationParse.getJSONObject("collaborators");
									JSONObject stairs = collaborationParse.getJSONObject("stairs");
									
									boolean meIn = collaborators.has(pref.getString("FBid", ""));
									
									if(!meIn){
									
									int n_collaborators = collaborators.length();
									if(n_collaborators < ClimbApplication.N_MEMBERS_PER_GROUP){
									
										
										
									try {// mi aggiungo alla collaborazione
										collaborators.put(pref.getString("FBid", ""), pref.getString("username", ""));
										stairs.put(pref.getString("FBid", ""), 0);
									} catch (JSONException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
									collaborationParse.put("collaborators", collaborators);
									collaborationParse.put("stairs", stairs);
									collaborationParse.saveEventually();
									
									User me = MainActivity.getUserById(pref.getInt("local_id", -1));
									Building building = MainActivity.getBuildingById(current.getBuilding_id());
									int my_stairs = 0;
									final Climbing climb = MainActivity.getClimbingForBuilding(building.get_id());
									if(climb == null){
										//creo climbing e salvo in locale e non
										Climbing climbing = new Climbing();
										climbing.setBuilding(building);
										climbing.setCompleted(0);
										climbing.setCompleted_steps(0);
										climbing.setCreated(new Date().getTime());
										climbing.setGame_mode(1);
										climbing.setModified(new Date().getTime());
										climbing.setPercentage(0);
										climbing.setRemaining_steps(building.getSteps());
										climbing.setUser(me);
										climbing.setSaved(true);
										MainActivity.climbingDao.create(climbing);
										
										ParseObject climbingParse = new ParseObject("Climbing");
										DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
										df.setTimeZone(new SimpleTimeZone(0, "GMT"));
										climbingParse.put("building", climbing.getBuilding().get_id());
										try {
											climbingParse.put("created", df.parse(df.format(climbing.getCreated())));
											climbingParse.put("modified", df.parse(df.format(climbing.getModified())));		
											climbingParse.put("completedAt", df.parse(df.format(climbing.getCompleted())));
										} catch (java.text.ParseException ex) {
											// TODO Auto-generated catch block
											ex.printStackTrace();
										}
										climbingParse.put("completed_steps", climbing.getCompleted_steps());
										climbingParse.put("remaining_steps", climbing.getRemaining_steps());
										climbingParse.put("percentage", String.valueOf(climbing.getPercentage()));
										climbingParse.put("users_id", climbing.getUser().getFBid());
										climbingParse.put("game_mode", climbing.getGame_mode());
										climbingParse.saveEventually();
									}else{
										climb.setGame_mode(1);
										my_stairs = climb.getCompleted_steps();
										ParseQuery<ParseObject> query = ParseQuery.getQuery("Climbing");
										query.whereEqualTo("building", building.get_id());
										query.whereEqualTo("users_id", pref.getString("FBid", ""));
										query.findInBackground(new FindCallback<ParseObject>() {

											@Override
											public void done(List<ParseObject> climbs, ParseException e) {
												if(e == null){
													ParseObject climbParse = climbs.get(0);
													climbParse.put("game_mode", 1);
													climbParse.saveEventually();
													MainActivity.climbingDao.update(climb);
												}else{
													Toast.makeText(MainActivity.getContext(), "Connection problem", Toast.LENGTH_SHORT).show();
													Log.d("Connection problem", "Error: " + e.getMessage());
												}
											}
										});
									}
									
									//salvo collaboration in locale
									Collaboration collaborationLocal = new Collaboration();
									collaborationLocal.setId(collaborationParse.getObjectId());
									collaborationLocal.setBuilding(MainActivity.getBuildingById(current.getBuilding_id()));
									collaborationLocal.setMy_stairs(my_stairs);
									collaborationLocal.setOthers_stairs(0);
									collaborationLocal.setSaved(true);
									collaborationLocal.setLeaved(false);
									collaborationLocal.setUser(me);
									MainActivity.collaborationDao.create(collaborationLocal);
									
									
									
									
									text.setText("Request Accepted");}
									else{
										text.setText("Collaboration completed");
										Toast.makeText(MainActivity.getContext(), "Too Late: collaboration completed", Toast.LENGTH_SHORT).show();
									}
									}else{
										text.setText("You are already in this collaboration");
									}
									
									deleteRequest(String.valueOf(notification.getId()));

									cancelBtn.setEnabled(false);
									acceptBtn.setEnabled(false);
									notification.setRead(true);
								}
							}else{
								Toast.makeText(MainActivity.getContext(), "Connection problem", Toast.LENGTH_SHORT).show();
								Log.d("Connection problem", "Error: " + e.getMessage());
							}
							
						}
					});
		
					break;
				case ASK_COMPETITION:
					final AskCompetitionNotification current1 = ((AskCompetitionNotification) notification);

					//prendi competizione da parse
					ParseQuery<ParseObject> queryComp = ParseQuery.getQuery("Competition");
					queryComp.whereEqualTo("objectId", current1.getCompetitionId());
					queryComp.findInBackground(new FindCallback<ParseObject>() {

						@Override
						public void done(List<ParseObject> compets, ParseException e) {
							if(e == null){
								if(compets.size() == 0){
									Toast.makeText(MainActivity.getContext(), "This competition does not exists anymore", Toast.LENGTH_SHORT).show();
									text.setText("This competition not exists");
								}else{
									//se c'è la competizione
									ParseObject collaborationParse = compets.get(0);
									JSONObject collaborators = collaborationParse.getJSONObject("competitors");
									JSONObject stairs = collaborationParse.getJSONObject("stairs");
									
									boolean meIn = collaborators.has(pref.getString("FBid", ""));
									
									if(!meIn){
									
									int n_collaborators = collaborators.length();
									if(n_collaborators < ClimbApplication.N_MEMBERS_PER_GROUP){
									
										
										
									try {// mi aggiungo alla collaborazione
										collaborators.put(pref.getString("FBid", ""), pref.getString("username", ""));
										stairs.put(pref.getString("FBid", ""), 0);
									} catch (JSONException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
									collaborationParse.put("competitors", collaborators);
									collaborationParse.put("stairs", stairs);
									collaborationParse.saveEventually();
									
									User me = MainActivity.getUserById(pref.getInt("local_id", -1));
									Building building = MainActivity.getBuildingById(current1.getBuilding_id());
									int my_stairs = 0;
									final Climbing climb = MainActivity.getClimbingForBuilding(building.get_id());
									if(climb == null){
										//creo climbing e salvo in locale e non
										Climbing climbing = new Climbing();
										climbing.setBuilding(building);
										climbing.setCompleted(0);
										climbing.setCompleted_steps(0);
										climbing.setCreated(new Date().getTime());
										climbing.setGame_mode(2);
										climbing.setModified(new Date().getTime());
										climbing.setPercentage(0);
										climbing.setRemaining_steps(building.getSteps());
										climbing.setUser(me);
										climbing.setSaved(true);
										MainActivity.climbingDao.create(climbing);
										
										ParseObject climbingParse = new ParseObject("Climbing");
										DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
										df.setTimeZone(new SimpleTimeZone(0, "GMT"));
										climbingParse.put("building", climbing.getBuilding().get_id());
										try {
											climbingParse.put("created", df.parse(df.format(climbing.getCreated())));
											climbingParse.put("modified", df.parse(df.format(climbing.getModified())));		
											climbingParse.put("completedAt", df.parse(df.format(climbing.getCompleted())));
										} catch (java.text.ParseException ex) {
											// TODO Auto-generated catch block
											ex.printStackTrace();
										}
										climbingParse.put("completed_steps", climbing.getCompleted_steps());
										climbingParse.put("remaining_steps", climbing.getRemaining_steps());
										climbingParse.put("percentage", String.valueOf(climbing.getPercentage()));
										climbingParse.put("users_id", climbing.getUser().getFBid());
										climbingParse.put("game_mode", climbing.getGame_mode());
										climbingParse.saveEventually();
									}else{
										climb.setGame_mode(2);
										my_stairs = climb.getCompleted_steps();
										ParseQuery<ParseObject> query = ParseQuery.getQuery("Climbing");
										query.whereEqualTo("building", building.get_id());
										query.whereEqualTo("users_id", pref.getString("FBid", ""));
										query.findInBackground(new FindCallback<ParseObject>() {

											@Override
											public void done(List<ParseObject> climbs, ParseException e) {
												if(e == null){
													ParseObject climbParse = climbs.get(0);
													climbParse.put("game_mode", 2);
													climbParse.saveEventually();
													MainActivity.climbingDao.update(climb);
												}else{
													Toast.makeText(MainActivity.getContext(), "Connection problem", Toast.LENGTH_SHORT).show();
													Log.d("Connection problem", "Error: " + e.getMessage());
												}
											}
										});
									}
									
									//salvo collaboration in locale
									Competition competitionLocal = new Competition();
									competitionLocal.setId_online(collaborationParse.getObjectId());
									competitionLocal.setBuilding(MainActivity.getBuildingById(current1.getBuilding_id()));
									competitionLocal.setMy_stairs(my_stairs);
									competitionLocal.setCurrent_position(0);
									competitionLocal.setSaved(true);
									competitionLocal.setLeaved(false);
									competitionLocal.setUser(me);
									MainActivity.competitionDao.create(competitionLocal);
									
									
									
									
									text.setText("Request Accepted");}
									else{
										text.setText("Collaboration completed");
										Toast.makeText(MainActivity.getContext(), "Too Late: collaboration completed", Toast.LENGTH_SHORT).show();
									}
									}else{
										text.setText("You are already in this collaboration");
									}
									
									deleteRequest(String.valueOf(notification.getId()));

									cancelBtn.setEnabled(false);
									acceptBtn.setEnabled(false);
									notification.setRead(true);
								}
							}else{
								Toast.makeText(MainActivity.getContext(), "Connection problem", Toast.LENGTH_SHORT).show();
								Log.d("Connection problem", "Error: " + e.getMessage());
							}
							
						}
					});
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
