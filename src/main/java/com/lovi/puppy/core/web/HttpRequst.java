package com.lovi.puppy.core.web;

import java.util.Map;

/**
 * 
 * @author Tharanga Thennakoon
 *
 */
public interface HttpRequst {

	String getMethod();

	String getLocation();

	Map<String,String> getHeaders();
	
	String getHeader(String key);
	
	String getRequestBody();
	
	Map<String, String> getParameters();
	
	String getParameter(String key);

	String getVersion();

	String getRawRequest();

}
