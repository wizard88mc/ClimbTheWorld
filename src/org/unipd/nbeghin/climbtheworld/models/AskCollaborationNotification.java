package org.unipd.nbeghin.climbtheworld.models;

public class AskCollaborationNotification extends Notification {
	
	private String groupId;
	private int building_id;
	private String building_name;
	
	public AskCollaborationNotification(String _id, String _sender, String _groupName, int _type) {
		id = _id;
		sender = _sender;
		groupName = _groupName;
		type = NotificationType.values()[_type];
		read = false;
		
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public int getBuilding_id() {
		return building_id;
	}

	public void setBuilding_id(int building_id) {
		this.building_id = building_id;
	}

	public String getBuilding_name() {
		return building_name;
	}

	public void setBuilding_name(String building_name) {
		this.building_name = building_name;
	}
	
	
	
	
}
