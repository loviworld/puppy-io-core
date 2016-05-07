package com.lovi.puppy.core.web;

public interface Session {

	void put(String key, Object value);
	
	Object get(String key);

	void remove(String key);

}
