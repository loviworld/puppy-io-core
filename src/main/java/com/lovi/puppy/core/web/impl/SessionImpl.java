package com.lovi.puppy.core.web.impl;

import com.lovi.puppy.core.web.Session;
import com.lovi.puppy.core.web.SessionStore;

public class SessionImpl implements Session{
	
	private SessionStore sessionStore;
	private String sessionId;
	
	public SessionImpl(SessionStore sessionStore, String sessionId) {
		this.sessionStore = sessionStore;
		this.sessionId = sessionId;
	}
	
	@Override
	public void put(String key, Object value){
		sessionStore.getUsers().get(sessionId).put(key, value);
	}
	
	@Override
	public Object get(String key){
		return sessionStore.getUsers().get(sessionId).get(key);
	}
	
	@Override
	public void remove(String key){
		sessionStore.getUsers().get(sessionId).remove(key);
	}

}
