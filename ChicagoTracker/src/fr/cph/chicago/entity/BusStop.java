package fr.cph.chicago.entity;

public class BusStop {
	private Integer id;
	private String name;
	private Position position;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

	@Override
	public String toString() {
		return "[id:" + getId() + ";name:" + getName() + ";position:" + getPosition() + "]";
	}

}
