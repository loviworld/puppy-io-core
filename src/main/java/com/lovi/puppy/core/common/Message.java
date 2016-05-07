package com.lovi.puppy.core.common;

public enum Message {

	SERVER_START("puppy-io webserver listen : "),
	SERVER_TERMINATE("SERVER_TERMINATE");
	
	private String message;
	private Message(String message){
		this.message = message;
	}
	
	public String getMessage(){
		return message;
	}
}
