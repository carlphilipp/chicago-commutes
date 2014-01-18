package fr.cph.chicago.entity;

import java.util.Date;
import java.util.List;

public class Follow {
	private Date timeStamp;
	private Integer errorCode;
	private String errorMessage;
	private Position position;
	private List<Eta> etas;
}
