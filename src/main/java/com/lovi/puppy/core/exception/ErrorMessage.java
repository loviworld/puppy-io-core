package com.lovi.puppy.core.exception;

public enum ErrorMessage {

	INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR"),
	UNABLE_TO_FOUND_REQUEST_MAPPER("UNABLE_TO_FOUND_REQUEST_MAPPER"),
	RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND"),
	REQUEST_MAP_MUST_START_WITH_SLASH("REQUEST_MAP_MUST_START_WITH_SLASH"), 
	REQUEST_FORWORD_ERROR("REQUEST_FORWORD_ERROR");
	
	
	String message;
	private ErrorMessage(String message){
		this.message = message;
	}
	
	public String getMessage(){
		return message;
	}
}
