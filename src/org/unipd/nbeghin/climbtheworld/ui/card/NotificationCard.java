package org.unipd.nbeghin.climbtheworld.ui.card;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.SimpleTimeZone;

import org.json.JSONException;
import org.json.JSONObject;
import org.unipd.nbeghin.climbtheworld.ClimbApplication;
import org.unipd.nbeghin.climbtheworld.MainActivity;
import org.unipd.nbeghin.climbtheworld.R;
import org.unipd.nbeghin.climbtheworld.models.AskCollaborationNotification;
import org.unipd.nbeghin.climbtheworld.models.AskCompetitionNotification;
import org.unipd.nbeghin.climbtheworld.models.AskTeamDuelNotification;
import org.unipd.nbeghin.climbtheworld.models.Building;
import org.unipd.nbeghin.climbtheworld.models.Climbing;
import org.unipd.nbeghin.climbtheworld.models.Collaboration;
import org.unipd.nbeghin.climbtheworld.models.Competition;
import org.unipd.nbeghin.climbtheworld.models.Group;
import org.unipd.nbeghin.climbtheworld.models.Notification;
import org.unipd.nbeghin.climbtheworld.models.NotificationType;
import org.unipd.nbeghin.climbtheworld.models.TeamDuel;
import org.unipd.nbeghin.climbtheworld.models.User;
import org.unipd.nbeghin.climbtheworld.util.FacebookUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import com.fima.cardsui.SwipeDismissTouchListener;
import com.fima.cardsui.SwipeDismissTouchListener.OnDismissCallback;
import com.fima.cardsui.objects.Card;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

public class NotificationCard extends Card {

	SharedPreferences pref = MainActivity.getContext().getSharedPreferences("UserSession", 0);
	final Notification notification;
	ImageButton acceptBtn;
	ImageButton cancelBtn;
	TextView text;
	boolean enabled;

	public NotificationCard(Notification notification, boolean enabled) {
		super(notification.getId());
		this.notification = notification;
		this.enabled = enabled;
	}

	private String setMessage() {
		switch (notification.getType()) {
		case INVITE_IN_GROUP:
			return notification.getSender() + " asked you to Join new group: " + notification.getGroupName();
		case ASK_COLLABORATION:
			return notification.getSender() + " asks help to climb " + ((AskCollaborationNotification) notification).getBuilding_name();
		case ASK_COMPETITION:
			return notification.getSender() + " challenges you to climb " + ((AskCompetitionNotification) notification).getBuilding_name();
		case ASK_TEAM_COMPETITION_CHALLENGER:
			return notification.getSender() + " challenges you in a team duel to climb " + ((AskTeamDuelNotification) notification).getBuilding_name();
		case ASK_TEAM_COMPETITION_TEAM:
			return notification.getSender() + " asks help to win the duel with his team in climbing " + ((AskTeamDuelNotification) notification).getBuilding_name();

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
		/*if (enabled){
			acceptBtn.setVisibility(View.VISIBLE);
			acceptBtn.setEnabled(true);
			cancelBtn.setVisibility(View.VISIBLE);
			cancelBtn.setEnabled(true);
			view.setBackgroundColor(Color.parseColor("#fffb94"));
		}else{
			acceptBtn.setVisibility(View.INVISIBLE);
			acceptBtn.setEnabled(false);
			cancelBtn.setVisibility(View.INVISIBLE);
			cancelBtn.setEnabled(false);
			view.setBackgroundColor(Color.parseColor("#ffffff"));
		}*/
		
		view.setOnTouchListener(new SwipeDismissTouchListener(view, null, new OnDismissCallback() {
			
			@Override
			public void onDismiss(View view, Object token) {
				System.out.println("swipe");
			}
		}));
			 
		
		acceptBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				boolean busy = ClimbApplication.BUSY;
				if (FacebookUtils.isOnline(context) && !busy) {
					ClimbApplication.BUSY = true;
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
								ClimbApplication.BUSY = false;
							}

						});
						break;
					case ASK_COLLABORATION:

						final AskCollaborationNotification current = ((AskCollaborationNotification) notification);

						Collaboration collabs = MainActivity.getCollaborationByBuildingAndUser(current.getBuilding_id(), pref.getInt("local_id",-1));
						Competition compet = MainActivity.getCompetitionByBuildingAndUser(current.getBuilding_id(), pref.getInt("local_id",-1));
						TeamDuel duel = MainActivity.getTeamDuelByBuildingAndUser(current.getBuilding_id(), pref.getInt("local_id",-1));
						// stessa per team
						if (collabs == null && compet == null && duel == null) {
							
							//pick collaboration from Parse (use the id in the notification)
							ParseQuery<ParseObject> queryColl = ParseQuery.getQuery("Collaboration");
							queryColl.whereEqualTo("objectId", current.getCollaborationId());
							queryColl.findInBackground(new FindCallback<ParseObject>() {

								@Override
								public void done(List<ParseObject> collabs, ParseException e) {
									if (e == null) {
										if (collabs.size() == 0) {//this collaboration has been deleted by someone else
											Toast.makeText(MainActivity.getContext(), "This collaboration does not exists anymore", Toast.LENGTH_SHORT).show();
											text.setText("This collaboration not exists");
											deleteRequest(String.valueOf(notification.getId()));

											cancelBtn.setEnabled(false);
											acceptBtn.setEnabled(false);
											notification.setRead(true);
											ClimbApplication.BUSY = false;
										} else {
											//collaboration object found in parse
											final ParseObject collaborationParse = collabs.get(0);
											JSONObject collaborators = collaborationParse.getJSONObject("collaborators");
											JSONObject stairs = collaborationParse.getJSONObject("stairs");

											boolean meIn = collaborators.has(pref.getString("FBid", ""));

											if (!meIn) {//I am not already part of the collaboration

												int n_collaborators = collaborators.length();
												if (n_collaborators < ClimbApplication.N_MEMBERS_PER_GROUP) {
													
													final User me = MainActivity.getUserById(pref.getInt("local_id", -1));
													final Building building = MainActivity.getBuildingById(current.getBuilding_id());
													final Climbing climb = MainActivity.getClimbingForBuilding(building.get_id());
													if(climb != null && (climb.getGame_mode() != 0 /*|| climb.getId_mode().equalsIgnoreCase("paused")*/)){
														Toast.makeText(context, "Building occupied", Toast.LENGTH_SHORT).show();
														text.setText("Building occupied");
													}else{	
													
													

													try {//Collaboration  is not full, I can add myself				
														collaborators.put(pref.getString("FBid", ""), pref.getString("username", ""));
														stairs.put(pref.getString("FBid", ""), 0);
													} catch (JSONException e1) {
														// TODO Auto-generated
														// catch block
														e1.printStackTrace();
													}
													collaborationParse.put("collaborators", collaborators);
													collaborationParse.put("stairs", stairs);
													collaborationParse.saveInBackground(new SaveCallback() {
														
														@Override
														public void done(ParseException e) {
															if(e == null){
																int my_stairs = 0;	
																if (climb == null) {
																	
																	final Climbing climbing = new Climbing();
																	climbing.setBuilding(building);
																	climbing.setCompleted(0);
																	climbing.setCompleted_steps(0);
																	climbing.setCreated(new Date().getTime());
																	climbing.setGame_mode(1);
																	climbing.setId_mode(collaborationParse.getObjectId());
																	climbing.setModified(new Date().getTime());
																	climbing.setPercentage(0);
																	climbing.setRemaining_steps(building.getSteps());
																	climbing.setUser(me);
																	climbing.setSaved(true);
																	MainActivity.climbingDao.create(climbing);
																	my_stairs = climbing.getCompleted_steps();
																	final ParseObject climbingParse = new ParseObject("Climbing");
																	DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
																	df.setTimeZone(new SimpleTimeZone(0, "GMT"));
																	climbingParse.put("building", climbing.getBuilding().get_id());
																	try {
																		climbingParse.put("created", df.parse(df.format(climbing.getCreated())));
																		climbingParse.put("modified", df.parse(df.format(climbing.getModified())));
																		climbingParse.put("completedAt", df.parse(df.format(climbing.getCompleted())));
																	} catch (java.text.ParseException ex) {
																		// TODO
																		// Auto-generated
																		// catch block
																		ex.printStackTrace();
																	}
																	climbingParse.put("completed_steps", climbing.getCompleted_steps());
																	climbingParse.put("remaining_steps", climbing.getRemaining_steps());
																	climbingParse.put("percentage", String.valueOf(climbing.getPercentage()));
																	climbingParse.put("users_id", climbing.getUser().getFBid());
																	climbingParse.put("game_mode", climbing.getGame_mode());
																	climbingParse.put("id_mode", climbing.getId_mode());
																	climbingParse.saveInBackground(new SaveCallback() {
																		
																		@Override
																		public void done(ParseException e) {
																			if(e == null){
																				climbing.setId_online(climbingParse.getObjectId());
																				climbing.setSaved(true);
																				MainActivity.climbingDao.update(climbing);
																			}else{
																				climbing.setSaved(false);
																				MainActivity.climbingDao.update(climbing);
																				Toast.makeText(MainActivity.getContext(), "Connection problem", Toast.LENGTH_SHORT).show();
																				Log.d("Connection problem", "Error: " + e.getMessage());
																			}
																			
																		}
																	});
																} else {
																	climb.setGame_mode(1);
																	climb.setId_mode(collaborationParse.getObjectId());
																	if (climb.getPercentage() >= 1.00) {
																		climb.setPercentage(0);
																		climb.setCompleted_steps(0);
																		climb.setRemaining_steps(building.getSteps());
																	}
																	my_stairs = climb.getCompleted_steps();
																	ParseQuery<ParseObject> query = ParseQuery.getQuery("Climbing");
																	query.whereEqualTo("objectId", climb.getId_online());
//																	query.whereEqualTo("building", building.get_id());
//																	query.whereEqualTo("users_id", pref.getString("FBid", ""));
																	query.findInBackground(new FindCallback<ParseObject>() {

																		@Override
																		public void done(List<ParseObject> climbs, ParseException e) {
																			if (e == null) {
																				ParseObject climbParse = climbs.get(0);
																				climbParse.put("game_mode", 1);
																				climbParse.put("id_mode", climb.getId_mode());
																				climbParse.put("percentage", String.valueOf(climb.getPercentage()));
																				climbParse.put("completed_steps", climb.getCompleted_steps());
																				climbParse.put("remaining_steps", climb.getRemaining_steps());
																				climbParse.saveEventually();
//																				climbParse.saveInBackground(new SaveCallback() {
//																					
//																					@Override
//																					public void done(ParseException e) {
//																						if(e == null){
//																							climbing.setId_online(climbingParse.getObjectId());
//																							climbing.setSaved(true);
//																							MainActivity.climbingDao.update(climbing);
//																						}else{
//																							climbing.setSaved(false);
//																							MainActivity.climbingDao.update(climbing);
//																							Toast.makeText(MainActivity.getContext(), "Connection problem", Toast.LENGTH_SHORT).show();
//																							Log.d("Connection problem", "Error: " + e.getMessage());
//																						}
//																						
//																					}
//																				});
																			} else {
																				climb.setSaved(false);
																				MainActivity.climbingDao.update(climb);
																				Toast.makeText(MainActivity.getContext(), "Connection problem", Toast.LENGTH_SHORT).show();
																				Log.d("Connection problem", "Error: " + e.getMessage());
																			}
																		}
																	});
																}

																// salvo collaboration in
																// locale
																Collaboration collaborationLocal = new Collaboration();
																collaborationLocal.setId(collaborationParse.getObjectId());
																collaborationLocal.setBuilding(MainActivity.getBuildingById(current.getBuilding_id()));
																collaborationLocal.setMy_stairs(my_stairs);
																collaborationLocal.setOthers_stairs(0);
																collaborationLocal.setSaved(true);
																collaborationLocal.setLeaved(false);
																collaborationLocal.setUser(me);
																collaborationLocal.setCompleted(false);
																MainActivity.collaborationDao.create(collaborationLocal);

																text.setText("Request Accepted");
																
																deleteRequest(String.valueOf(notification.getId()));

																cancelBtn.setEnabled(false);
																acceptBtn.setEnabled(false);
																notification.setRead(true);
																ClimbApplication.BUSY = false;

															}else{
																Toast.makeText(MainActivity.getContext(), "Connection problem", Toast.LENGTH_SHORT).show();
																Log.d("Connection problem", "Error: " + e.getMessage());
																ClimbApplication.BUSY = false;
															}
														}
													});
													}
													
												} else {
													text.setText("Collaboration completed");
													Toast.makeText(MainActivity.getContext(), "Too Late: collaboration completed", Toast.LENGTH_SHORT).show();
													deleteRequest(String.valueOf(notification.getId()));

													cancelBtn.setEnabled(false);
													acceptBtn.setEnabled(false);
													notification.setRead(true);
													ClimbApplication.BUSY = false;

												}
											} else {
												text.setText("You are already in this collaboration");
												deleteRequest(String.valueOf(notification.getId()));

												cancelBtn.setEnabled(false);
												acceptBtn.setEnabled(false);
												notification.setRead(true);
												ClimbApplication.BUSY = false;

											}

											
										}
//										deleteRequest(String.valueOf(notification.getId()));
//
//										cancelBtn.setEnabled(false);
//										acceptBtn.setEnabled(false);
//										notification.setRead(true);
									} else {
										Toast.makeText(MainActivity.getContext(), "Connection problem", Toast.LENGTH_SHORT).show();
										Log.d("Connection problem", "Error: " + e.getMessage());
										ClimbApplication.BUSY = false;
									}
								}
							});
							
						} else {
							//I'm already part of another collaboration/competition/team duel
							text.setText("Unable to take part");
							deleteRequest(String.valueOf(notification.getId()));

							cancelBtn.setEnabled(false);
							acceptBtn.setEnabled(false);
							notification.setRead(true);
							ClimbApplication.BUSY = false;

						}
						break;
					case ASK_COMPETITION:
						final AskCompetitionNotification current1 = ((AskCompetitionNotification) notification);

						Collaboration collabs1 = MainActivity.getCollaborationByBuildingAndUser(current1.getBuilding_id(), pref.getInt("local_id",-1));
						Competition compet1 = MainActivity.getCompetitionByBuildingAndUser(current1.getBuilding_id(), pref.getInt("local_id",-1));
						TeamDuel duel1 = MainActivity.getTeamDuelByBuildingAndUser(current1.getBuilding_id(), pref.getInt("local_id",-1));
						if (collabs1 == null && compet1 == null && duel1 == null) {

							//look for competition object in Parse
							ParseQuery<ParseObject> queryComp = ParseQuery.getQuery("Competition");
							queryComp.whereEqualTo("objectId", current1.getCompetitionId());
							queryComp.findInBackground(new FindCallback<ParseObject>() {

								@Override
								public void done(List<ParseObject> compets, ParseException e) {
									if (e == null) {
										if (compets.size() == 0) {
											Toast.makeText(MainActivity.getContext(), "This competition does not exists anymore", Toast.LENGTH_SHORT).show();
											text.setText("This competition not exists");
											deleteRequest(String.valueOf(notification.getId()));
											cancelBtn.setEnabled(false);
											acceptBtn.setEnabled(false);
											notification.setRead(true);
											ClimbApplication.BUSY = false;

										} else {
											// se c'è la competizione
											final ParseObject collaborationParse = compets.get(0);
											JSONObject collaborators = collaborationParse.getJSONObject("competitors");
											JSONObject stairs = collaborationParse.getJSONObject("stairs");

											boolean meIn = collaborators.has(pref.getString("FBid", ""));

											if (!meIn) {
												

												int n_collaborators = collaborators.length();
												if (n_collaborators < ClimbApplication.N_MEMBERS_PER_GROUP) {
													
												final User me = MainActivity.getUserById(pref.getInt("local_id", -1));
												final Building building = MainActivity.getBuildingById(current1.getBuilding_id());
												final Climbing climb = MainActivity.getClimbingForBuilding(building.get_id());

												if(climb != null && (climb.getGame_mode() != 0 /*|| climb.getId_mode().equalsIgnoreCase("paused")*/)){
													Toast.makeText(context, "Building occupied", Toast.LENGTH_SHORT).show();
													text.setText("Building occupied");
												}else{	
												

													try {// mi aggiungo alla
															// competizione
														collaborators.put(pref.getString("FBid", ""), pref.getString("username", ""));
														stairs.put(pref.getString("FBid", ""), 0);
													} catch (JSONException e1) {
														// TODO Auto-generated
														// catch block
														e1.printStackTrace();
													}
													collaborationParse.put("competitors", collaborators);
													collaborationParse.put("stairs", stairs);
													collaborationParse.saveInBackground(new SaveCallback() {
														
														@Override
														public void done(ParseException e) {
															if(e == null){
																int my_stairs = 0;
																/*if (climb != null) {
																	climb.setId_mode("paused");
																	MainActivity.climbingDao.update(climb);
																	ParseQuery<ParseObject> query = ParseQuery.getQuery("Climbing");
																	if(climb.getId_online() != null && !climb.getId_online().equals("")){
																		query.whereEqualTo("objectId", climb.getId_online());
																	}else{
																		query.whereEqualTo("building", building.get_id());
																		query.whereEqualTo("users_id", pref.getString("FBid", ""));
																		query.whereEqualTo("game_mode", 0);
																	}
																	query.findInBackground(new FindCallback<ParseObject>() {

																		@Override
																		public void done(List<ParseObject> climbs, ParseException e) {
																			if (e == null) {
																				ParseObject c = climbs.get(0);
																				c.put("id_mode", climb.getId_mode());
																				c.saveEventually();
																			} else {
																				climb.setSaved(false);
																				MainActivity.climbingDao.update(climb);
																				Toast.makeText(MainActivity.getContext(), "Connection problem", Toast.LENGTH_SHORT).show();
																				Log.d("Connection problem", "Error: " + e.getMessage());
																			}
																		}
																	});
																}*/

																
																final Climbing climbing = new Climbing();
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
																climbing.setId_mode(collaborationParse.getObjectId());
																MainActivity.climbingDao.create(climbing);

																final ParseObject climbingParse = new ParseObject("Climbing");
																DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
																df.setTimeZone(new SimpleTimeZone(0, "GMT"));
																climbingParse.put("building", climbing.getBuilding().get_id());
																try {
																	climbingParse.put("created", df.parse(df.format(climbing.getCreated())));
																	climbingParse.put("modified", df.parse(df.format(climbing.getModified())));
																	climbingParse.put("completedAt", df.parse(df.format(climbing.getCompleted())));
																} catch (java.text.ParseException ex) {
																	// TODO Auto-generated
																	// catch block
																	ex.printStackTrace();
																}
																climbingParse.put("completed_steps", climbing.getCompleted_steps());
																climbingParse.put("remaining_steps", climbing.getRemaining_steps());
																climbingParse.put("percentage", String.valueOf(climbing.getPercentage()));
																climbingParse.put("users_id", climbing.getUser().getFBid());
																climbingParse.put("game_mode", climbing.getGame_mode());
																climbingParse.put("id_mode", climbing.getId_mode());
																climbingParse.saveInBackground(new SaveCallback() {
																	
																	@Override
																	public void done(ParseException e) {
																		if(e == null){
																			climbing.setId_online(climbingParse.getObjectId());
																			climbing.setSaved(true);
																			MainActivity.climbingDao.update(climbing);
																			
																		}else{
																			climbing.setSaved(false);
																			MainActivity.climbingDao.update(climbing);
																			Toast.makeText(context, "Connection problem", Toast.LENGTH_SHORT).show();
																			Log.e("1 Connection Problem", e.getMessage());
																		}
																	}
																});
																

																// salvo collaboration in
																// locale
																Competition competitionLocal = new Competition();
																competitionLocal.setId_online(collaborationParse.getObjectId());
																competitionLocal.setBuilding(MainActivity.getBuildingById(current1.getBuilding_id()));
																competitionLocal.setMy_stairs(0);
																competitionLocal.setCurrent_position(0);
																competitionLocal.setSaved(true);
																competitionLocal.setLeaved(false);
																competitionLocal.setUser(me);
																competitionLocal.setCompleted(false);
																MainActivity.competitionDao.create(competitionLocal);

																text.setText("Request Accepted");
																deleteRequest(String.valueOf(notification.getId()));

																cancelBtn.setEnabled(false);
																acceptBtn.setEnabled(false);
																notification.setRead(true);
																ClimbApplication.BUSY = false;

															}else{
																Competition competitionLocal = new Competition();
																competitionLocal.setBuilding(MainActivity.getBuildingById(current1.getBuilding_id()));
																competitionLocal.setSaved(false);
																competitionLocal.setLeaved(true);
																competitionLocal.setUser(me);
																competitionLocal.setCompleted(false);
																MainActivity.competitionDao.create(competitionLocal);
																Toast.makeText(context, "Connection Problems", Toast.LENGTH_SHORT).show();
																Log.e("2 Connection Problem adding me in competition", "Error: " + e.getMessage());
																ClimbApplication.BUSY = false;

															}
														}
															
															
														
													});
												
												}	
													
												} else {
													text.setText("Competition completed");
													Toast.makeText(MainActivity.getContext(), "Too Late: competition completed", Toast.LENGTH_SHORT).show();

													deleteRequest(String.valueOf(notification.getId()));

													cancelBtn.setEnabled(false);
													acceptBtn.setEnabled(false);
													notification.setRead(true);
													ClimbApplication.BUSY = false;

												}
											} else {
												text.setText("You are already in this competition");
												deleteRequest(String.valueOf(notification.getId()));

												cancelBtn.setEnabled(false);
												acceptBtn.setEnabled(false);
												notification.setRead(true);
												ClimbApplication.BUSY = false;
											}

										}
									} else {
										Toast.makeText(MainActivity.getContext(), "Connection problem", Toast.LENGTH_SHORT).show();
										Log.d("Connection problem", "Error: " + e.getMessage());
										ClimbApplication.BUSY = false;

									}

								}
							});
							
						} else {
							text.setText("Unable to take part");
							deleteRequest(String.valueOf(notification.getId()));

							cancelBtn.setEnabled(false);
							acceptBtn.setEnabled(false);
							notification.setRead(true);
							ClimbApplication.BUSY = false;

						}
						break;
					case ASK_TEAM_COMPETITION_CHALLENGER:
						final AskTeamDuelNotification current2 = ((AskTeamDuelNotification) notification);

						Collaboration collabs2 = MainActivity.getCollaborationByBuildingAndUser(current2.getBuilding_id(), pref.getInt("local_id",-1));
						Competition compet2 = MainActivity.getCompetitionByBuildingAndUser(current2.getBuilding_id(), pref.getInt("local_id",-1));
						TeamDuel duel2 = MainActivity.getTeamDuelByBuildingAndUser(current2.getBuilding_id(), pref.getInt("local_id",-1));
						if (collabs2 == null && compet2 == null && duel2 == null) {

							//look for team duel object in Parse
							ParseQuery<ParseObject> queryComp = ParseQuery.getQuery("TeamDuel");
							queryComp.whereEqualTo("objectId", current2.getTeamDuelId());
							queryComp.findInBackground(new FindCallback<ParseObject>() {

								@Override
								public void done(List<ParseObject> duels, ParseException e) {
									if (e == null) {
										if (duels.size() == 0) {
											Toast.makeText(MainActivity.getContext(), "This team duel does not exists anymore", Toast.LENGTH_SHORT).show();
											text.setText("This team duel not exists");
											deleteRequest(String.valueOf(notification.getId()));
											cancelBtn.setEnabled(false);
											acceptBtn.setEnabled(false);
											notification.setRead(true);
											ClimbApplication.BUSY = false;

										} else {
											// if team duel exists
											final ParseObject teamDuelParse = duels.get(0);
											JSONObject challenger = teamDuelParse.getJSONObject("challenger");
											JSONObject challenger_stairs = teamDuelParse.getJSONObject("challenger_stairs");
											JSONObject challenger_team = teamDuelParse.getJSONObject("challenger_team");
											JSONObject creator_stairs = teamDuelParse.getJSONObject("creator_stairs");
											
											boolean meIn = challenger_stairs.has(pref.getString("FBid", "")) && creator_stairs.has(pref.getString("FBid", ""));
											
											if (!meIn) {
												

												int n_collaborators = challenger.length();
												if (n_collaborators < 1) {
													
												final User me = MainActivity.getUserById(pref.getInt("local_id", -1));
												final Building building = MainActivity.getBuildingById(current2.getBuilding_id());
												final Climbing climb = MainActivity.getClimbingForBuilding(building.get_id());

												if(climb != null && (climb.getGame_mode() != 0 /*|| climb.getId_mode().equalsIgnoreCase("paused")*/)){
													Toast.makeText(context, "Building occupied", Toast.LENGTH_SHORT).show();
													text.setText("Building occupied");
												}else{	
												

													try {//add myself as challenger
														challenger.put(pref.getString("FBid", ""), pref.getString("username", ""));
														challenger_stairs.put(pref.getString("FBid", ""), 0);
													} catch (JSONException e1) {
														// TODO Auto-generated
														// catch block
														e1.printStackTrace();
													}
													teamDuelParse.put("challenger", challenger);
													teamDuelParse.put("challenger_stairs", challenger_stairs);
													//teamDuelParse.put("challenger_team", challenger_team);
													teamDuelParse.saveInBackground(new SaveCallback() {
														
														@Override
														public void done(ParseException e) {
															if(e == null){
																int my_stairs = 0;
															
																final Climbing climbing = new Climbing();
																climbing.setBuilding(building);
																climbing.setCompleted(0);
																climbing.setCompleted_steps(0);
																climbing.setCreated(new Date().getTime());
																climbing.setGame_mode(3);
																climbing.setModified(new Date().getTime());
																climbing.setPercentage(0);
																climbing.setRemaining_steps(building.getSteps());
																climbing.setUser(me);
																climbing.setSaved(true);
																climbing.setId_mode(teamDuelParse.getObjectId());
																MainActivity.climbingDao.create(climbing);

																final ParseObject climbingParse = new ParseObject("Climbing");
																DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
																df.setTimeZone(new SimpleTimeZone(0, "GMT"));
																climbingParse.put("building", climbing.getBuilding().get_id());
																try {
																	climbingParse.put("created", df.parse(df.format(climbing.getCreated())));
																	climbingParse.put("modified", df.parse(df.format(climbing.getModified())));
																	climbingParse.put("completedAt", df.parse(df.format(climbing.getCompleted())));
																} catch (java.text.ParseException ex) {
																	// TODO Auto-generated
																	// catch block
																	ex.printStackTrace();
																}
																climbingParse.put("completed_steps", climbing.getCompleted_steps());
																climbingParse.put("remaining_steps", climbing.getRemaining_steps());
																climbingParse.put("percentage", String.valueOf(climbing.getPercentage()));
																climbingParse.put("users_id", climbing.getUser().getFBid());
																climbingParse.put("game_mode", climbing.getGame_mode());
																climbingParse.put("id_mode", climbing.getId_mode());
																climbingParse.saveInBackground(new SaveCallback() {
																	
																	@Override
																	public void done(ParseException e) {
																		if(e == null){
																			climbing.setId_online(climbingParse.getObjectId());
																			climbing.setSaved(true);
																			MainActivity.climbingDao.update(climbing);
																			
																		}else{
																			climbing.setSaved(false);
																			MainActivity.climbingDao.update(climbing);
																			Toast.makeText(context, "Connection problem", Toast.LENGTH_SHORT).show();
																			Log.e("1 Connection Problem", e.getMessage());
																		}
																	}
																});
																JSONObject creator = teamDuelParse.getJSONObject("creator");
																Iterator<String> it = creator.keys();
																String creator_name = "";
																try {
																	if(it.hasNext()) creator_name = creator.getString(it.next());
																} catch (JSONException e1) {
																	// TODO Auto-generated catch block
																	e1.printStackTrace();
																}

																// save team duel locally
																TeamDuel teamDuelLocal = new TeamDuel();
																teamDuelLocal.setId_online(teamDuelParse.getObjectId());
																teamDuelLocal.setBuilding(MainActivity.getBuildingById(current2.getBuilding_id()));
																teamDuelLocal.setMy_steps(0);
																teamDuelLocal.setSaved(true);
																teamDuelLocal.setUser(me);
																teamDuelLocal.setCreator_name(creator_name);
																teamDuelLocal.setChallenger_name(pref.getString("username", ""));
																teamDuelLocal.setMygroup(Group.CHALLENGER);
																teamDuelLocal.setCompleted(false);
																teamDuelLocal.setCreator(false);
																teamDuelLocal.setDeleted(false);
																teamDuelLocal.setReadyToPlay(false);
																teamDuelLocal.setSteps_my_group(0);
																teamDuelLocal.setSteps_other_group(0);
																MainActivity.teamDuelDao.create(teamDuelLocal);

																text.setText("Request Accepted");
																deleteRequest(String.valueOf(notification.getId()));

																cancelBtn.setEnabled(false);
																acceptBtn.setEnabled(false);
																notification.setRead(true);
																ClimbApplication.BUSY = false;

															}else{
																TeamDuel teamDuelLocal = new TeamDuel();
																teamDuelLocal.setBuilding(MainActivity.getBuildingById(current2.getBuilding_id()));
																teamDuelLocal.setCompleted(true);
																teamDuelLocal.setSaved(false);
																teamDuelLocal.setDeleted(true);
																teamDuelLocal.setUser(me);
																MainActivity.teamDuelDao.create(teamDuelLocal);
																Toast.makeText(context, "Connection Problems", Toast.LENGTH_SHORT).show();
																Log.e("2 Connection Problem adding me in team duel", "Error: " + e.getMessage());
																ClimbApplication.BUSY = false;
															}
														}
															
															
														
													});
												
												}	
													
												} else {
													text.setText("Challenger already chosen");
													Toast.makeText(MainActivity.getContext(), "Too Late: Challenger already chosen", Toast.LENGTH_SHORT).show();

													deleteRequest(String.valueOf(notification.getId()));

													cancelBtn.setEnabled(false);
													acceptBtn.setEnabled(false);
													notification.setRead(true);
													ClimbApplication.BUSY = false;

												}
											} else {
												text.setText("You are already the challenger");
												deleteRequest(String.valueOf(notification.getId()));

												cancelBtn.setEnabled(false);
												acceptBtn.setEnabled(false);
												notification.setRead(true);
												ClimbApplication.BUSY = false;

											}

										}
									} else {
										Toast.makeText(MainActivity.getContext(), "Connection problem", Toast.LENGTH_SHORT).show();
										Log.d("Connection problem", "Error: " + e.getMessage());
										ClimbApplication.BUSY = false;
									}

								}
							});
							
						} else {
							text.setText("Unable to take part");
							deleteRequest(String.valueOf(notification.getId()));

							cancelBtn.setEnabled(false);
							acceptBtn.setEnabled(false);
							notification.setRead(true);
							ClimbApplication.BUSY = false;

						}
						break;
					case ASK_TEAM_COMPETITION_TEAM:
						final AskTeamDuelNotification current3 = ((AskTeamDuelNotification) notification);

						Collaboration collabs3 = MainActivity.getCollaborationByBuildingAndUser(current3.getBuilding_id(), pref.getInt("local_id",-1));
						Competition compet3 = MainActivity.getCompetitionByBuildingAndUser(current3.getBuilding_id(), pref.getInt("local_id",-1));
						TeamDuel duel3 = MainActivity.getTeamDuelByBuildingAndUser(current3.getBuilding_id(), pref.getInt("local_id",-1));
						if (collabs3 == null && compet3 == null && duel3 == null) {

							//look for team duel object in Parse
							ParseQuery<ParseObject> queryComp = ParseQuery.getQuery("TeamDuel");
							queryComp.whereEqualTo("objectId", current3.getTeamDuelId());
							queryComp.findInBackground(new FindCallback<ParseObject>() {

								@Override
								public void done(List<ParseObject> duels, ParseException e) {
									if (e == null) {
										if (duels.size() == 0) {
											Toast.makeText(MainActivity.getContext(), "This team duel does not exists anymore", Toast.LENGTH_SHORT).show();
											text.setText("This team duel not exists");
											deleteRequest(String.valueOf(notification.getId()));
											cancelBtn.setEnabled(false);
											acceptBtn.setEnabled(false);
											notification.setRead(true);
											ClimbApplication.BUSY = false;

										} else {
											// if team duel exists
											final ParseObject teamDuelParse = duels.get(0);
											JSONObject stairs;
											JSONObject team;
											JSONObject challenger_stairs;
											if(current3.isSenderCreator()){
												team = teamDuelParse.getJSONObject("creator_team");
												stairs = teamDuelParse.getJSONObject("creator_stairs");
												challenger_stairs = teamDuelParse.getJSONObject("challenger_stairs");
											}else{
												team = teamDuelParse.getJSONObject("challenger_team");
												stairs = teamDuelParse.getJSONObject("challenger_stairs");
												challenger_stairs = teamDuelParse.getJSONObject("creator_stairs");
											}

											boolean meIn = stairs.has(pref.getString("FBid", "")) && challenger_stairs.has(pref.getString("FBid", ""));

											if (!meIn) {
												

												int n_collaborators = team.length();
												if (n_collaborators < ClimbApplication.N_MEMBERS_PER_GROUP - 1) {
													
												final User me = MainActivity.getUserById(pref.getInt("local_id", -1));
												final Building building = MainActivity.getBuildingById(current3.getBuilding_id());
												final Climbing climb = MainActivity.getClimbingForBuilding(building.get_id());

												if(climb != null && (climb.getGame_mode() != 0 /*|| climb.getId_mode().equalsIgnoreCase("paused")*/)){
													Toast.makeText(context, "Building occupied", Toast.LENGTH_SHORT).show();
													text.setText("Building occupied");
													

												}else{	
												

													try {//add myself as challenger
														team.put(pref.getString("FBid", ""), pref.getString("username", ""));
														stairs.put(pref.getString("FBid", ""), 0);
													} catch (JSONException e1) {
														// TODO Auto-generated
														// catch block
														e1.printStackTrace();
													}
													if(current3.isSenderCreator()){
														teamDuelParse.put("creator_team", team);
														teamDuelParse.put("creator_stairs", stairs);
													}else{
														teamDuelParse.put("challenger_team", team);
														teamDuelParse.put("challenger_stairs", stairs);
													}
													teamDuelParse.saveInBackground(new SaveCallback() {
														
														@Override
														public void done(ParseException e) {
															if(e == null){
																int my_stairs = 0;
															
																final Climbing climbing = new Climbing();
																climbing.setBuilding(building);
																climbing.setCompleted(0);
																climbing.setCompleted_steps(0);
																climbing.setCreated(new Date().getTime());
																climbing.setGame_mode(3);
																climbing.setModified(new Date().getTime());
																climbing.setPercentage(0);
																climbing.setRemaining_steps(building.getSteps());
																climbing.setUser(me);
																climbing.setSaved(true);
																climbing.setId_mode(teamDuelParse.getObjectId());
																MainActivity.climbingDao.create(climbing);

																final ParseObject climbingParse = new ParseObject("Climbing");
																DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
																df.setTimeZone(new SimpleTimeZone(0, "GMT"));
																climbingParse.put("building", climbing.getBuilding().get_id());
																try {
																	climbingParse.put("created", df.parse(df.format(climbing.getCreated())));
																	climbingParse.put("modified", df.parse(df.format(climbing.getModified())));
																	climbingParse.put("completedAt", df.parse(df.format(climbing.getCompleted())));
																} catch (java.text.ParseException ex) {
																	// TODO Auto-generated
																	// catch block
																	ex.printStackTrace();
																}
																climbingParse.put("completed_steps", climbing.getCompleted_steps());
																climbingParse.put("remaining_steps", climbing.getRemaining_steps());
																climbingParse.put("percentage", String.valueOf(climbing.getPercentage()));
																climbingParse.put("users_id", climbing.getUser().getFBid());
																climbingParse.put("game_mode", climbing.getGame_mode());
																climbingParse.put("id_mode", climbing.getId_mode());
																climbingParse.saveInBackground(new SaveCallback() {
																	
																	@Override
																	public void done(ParseException e) {
																		if(e == null){
																			climbing.setId_online(climbingParse.getObjectId());
																			climbing.setSaved(true);
																			MainActivity.climbingDao.update(climbing);
																			
																		}else{
																			climbing.setSaved(false);
																			MainActivity.climbingDao.update(climbing);
																			Toast.makeText(context, "Connection problem", Toast.LENGTH_SHORT).show();
																			Log.e("1 Connection Problem", e.getMessage());
																		}
																	}
																});
																//salva creator nome localm
																JSONObject challenger = teamDuelParse.getJSONObject("challenger");
																Iterator<String> it = challenger.keys();
																String challenger_name = "";
																try {
																	if(it.hasNext()) challenger_name = challenger.getString(it.next());
																} catch (JSONException e1) {
																	// TODO Auto-generated catch block
																	e1.printStackTrace();
																}
																JSONObject creator = teamDuelParse.getJSONObject("creator");
																Iterator<String> it1 = creator.keys();
																String creator_name = "";
																try {
																	if(it1.hasNext()) creator_name = creator.getString(it1.next());
																} catch (JSONException e1) {
																	// TODO Auto-generated catch block
																	e1.printStackTrace();
																}

																// save team duel locally
																TeamDuel teamDuelLocal = new TeamDuel();
																teamDuelLocal.setId_online(teamDuelParse.getObjectId());
																teamDuelLocal.setBuilding(MainActivity.getBuildingById(current3.getBuilding_id()));
																teamDuelLocal.setMy_steps(0);
																teamDuelLocal.setSaved(true);
																teamDuelLocal.setUser(me);
																teamDuelLocal.setReadyToPlay(false);
																teamDuelLocal.setCreator_name(creator_name);
																teamDuelLocal.setChallenger_name(challenger_name);
																teamDuelLocal.setCreator(false);
																if(current3.isSenderCreator())
																	teamDuelLocal.setMygroup(Group.CREATOR);
																else
																	teamDuelLocal.setMygroup(Group.CHALLENGER);
																teamDuelLocal.setCompleted(false);
																teamDuelLocal.setDeleted(false);
																teamDuelLocal.setSteps_my_group(0);
																teamDuelLocal.setSteps_other_group(0);
																MainActivity.teamDuelDao.create(teamDuelLocal);

																text.setText("Request Accepted");
																deleteRequest(String.valueOf(notification.getId()));

																cancelBtn.setEnabled(false);
																acceptBtn.setEnabled(false);
																notification.setRead(true);
																ClimbApplication.BUSY = false;

															}else{
																TeamDuel teamDuelLocal = new TeamDuel();
																teamDuelLocal.setBuilding(MainActivity.getBuildingById(current3.getBuilding_id()));
																teamDuelLocal.setCompleted(true);
																teamDuelLocal.setSaved(false);
																teamDuelLocal.setDeleted(true);
																teamDuelLocal.setUser(me);
																MainActivity.teamDuelDao.create(teamDuelLocal);
																Toast.makeText(context, "Connection Problems", Toast.LENGTH_SHORT).show();
																Log.e("2 Connection Problem adding me in team duel", "Error: " + e.getMessage());
																ClimbApplication.BUSY = false;
															}
														}
															
															
														
													});
												
												}	
													
												} else {
													text.setText("Challenger already chosen");
													Toast.makeText(MainActivity.getContext(), "Too Late: Challenger already chosen", Toast.LENGTH_SHORT).show();

													deleteRequest(String.valueOf(notification.getId()));

													cancelBtn.setEnabled(false);
													acceptBtn.setEnabled(false);
													notification.setRead(true);
													ClimbApplication.BUSY = false;

												}
											} else {
												text.setText("You are already the challenger");
												deleteRequest(String.valueOf(notification.getId()));

												cancelBtn.setEnabled(false);
												acceptBtn.setEnabled(false);
												notification.setRead(true);
												ClimbApplication.BUSY = false;

											}

										}
									} else {
										Toast.makeText(MainActivity.getContext(), "Connection problem", Toast.LENGTH_SHORT).show();
										Log.d("Connection problem", "Error: " + e.getMessage());
										ClimbApplication.BUSY = false;
									}

								}
							});
							
						} else {
							text.setText("Unable to take part");
							deleteRequest(String.valueOf(notification.getId()));

							cancelBtn.setEnabled(false);
							acceptBtn.setEnabled(false);
							notification.setRead(true);
							ClimbApplication.BUSY = false;

						}
						break;
						

					}

				}if(busy){
					Toast.makeText(context, "Wait for notification to be saved", Toast.LENGTH_SHORT).show();
				}
			}
		});

		cancelBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				boolean busy = ClimbApplication.BUSY;
				if (FacebookUtils.isOnline(context) && !busy) {									
					ClimbApplication.BUSY = true;

					deleteRequest(String.valueOf(notification.getId()));
					text.setText("Request Deleted");
					cancelBtn.setEnabled(false);
					acceptBtn.setEnabled(false);
					notification.setRead(true);
					ClimbApplication.BUSY = false;

				}if(busy){
					Toast.makeText(context, "Wait for notification to be saved", Toast.LENGTH_SHORT).show();
				}
				

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
