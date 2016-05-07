package com.lovi.puppy.core.web.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.lovi.puppy.core.web.SessionStore;

public class SessionStoreImpl implements SessionStore {
	
	private Map<String, Map<String, Object>> users = new HashMap<>();
	
	@Override
	public String addNewUser(){
		
		String sessionId = genarateSessionId();
		while(users.containsKey(sessionId)){
			sessionId = genarateSessionId();
		}
		
		users.put(sessionId, new HashMap<>());
		return sessionId;
	}
	
	@Override
	public void removeUser(String sessionId){
		users.remove(sessionId);
	}
	
	@Override
	public boolean checkSessionIdExists(String sessionId){
		if(users.containsKey(sessionId))
			return true;
		else
			return false;
	}

	@Override
	public Map<String, Map<String, Object>> getUsers() {
		return users;
	}
	
	private String genarateSessionId(){
		String sessionId = UUID.randomUUID().toString();
		return sessionId;
	}
	
}
