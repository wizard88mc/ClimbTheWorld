package org.unipd.nbeghin.climbtheworld.models;

public class ChartMember {
	String id;
	int score;
	
	public ChartMember(String id, int score){
		this.id = id;
		this.score = score;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}
	
	
}
