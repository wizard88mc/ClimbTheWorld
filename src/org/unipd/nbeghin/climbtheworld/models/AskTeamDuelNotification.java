package org.unipd.nbeghin.climbtheworld.models;

public class AskTeamDuelNotification extends Notification{
	private String teamDuelId;
	private int building_id;
	private String building_name;
	
	public AskTeamDuelNotification(String _id, String _sender, String _collabIb, int _type) {
		id = _id;
		sender = _sender;
		teamDuelId = _collabIb;
		type = NotificationType.values()[_type];
		read = false;
		
	}
	
	
}
