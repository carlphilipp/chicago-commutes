package fr.cph.chicago.entity.enumeration;

public enum BusDirection {
	NORTHBOUND("Northbound"), WESTBOUND("Westbound"), SOUTHBOUND("Southbound"), EASTBOUND("Eastbound");
	
	private String text;
	
	BusDirection(String text) {
		this.text = text;
	}
	
	public static BusDirection fromString(String text) {
		if (text != null) {
			for (BusDirection b : BusDirection.values()) {
				if (text.equalsIgnoreCase(b.text)) {
					return b;
				}
			}
		}
		return null;
	}
	
	@Override
	public String toString(){
		return text;
	}
	
}
