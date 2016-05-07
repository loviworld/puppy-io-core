package com.lovi.puppy.core.web.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import org.apache.log4j.Logger;
import com.lovi.puppy.core.web.HttpRequst;

/**
 * 
 * @author Tharanga Thennakoon
 *
 */
public class HttpRequstImpl implements HttpRequst{
	
	private final String raw;
    private String method;
    private String location;
    private String requestBody;
    private String version;
    private Map<String, String> headers = new HashMap<String, String>();
    private Map<String, String> parameters = new HashMap<String, String>();
    final static Logger logger = Logger.getLogger(HttpRequst.class);
    
    public HttpRequstImpl(String raw) {
		this.raw = raw;
		parse();
	}
    
    private void parse() {
    	try{
	        StringTokenizer tokenizer = new StringTokenizer(raw);
	        
	        method = tokenizer.nextToken().toUpperCase();
	        location = tokenizer.nextToken();
	        version = tokenizer.nextToken();
	        
	        boolean processRequestBody = false;
	        
	        String[] lines = raw.split("\r\n");
	        for (int i = 1; i < lines.length; i++) {
	        	
	        	if(lines[i].equals("")){
	        		//headers are finish.next start request body
	        		processRequestBody = true;
	        		continue;
	        	}
	        	
	        	if(!processRequestBody){
	        		String[] keyVal = lines[i].split(":", 2);
	        		headers.put(keyVal[0], keyVal[1]);
	        	}else{
	        		requestBody = lines[i];
	        	}
	        }
	        prepareRequestParameters();
    	}catch(Exception e){
    		logger.error("Http request parse fail " + raw);
    	}
    }
    
    private void prepareRequestParameters(){
    	//prepare requestBody parameters
    	if(requestBody != null){
    		
    		String[] strParms = requestBody.split("&");
    	
    		for(String str : strParms){
	    		String[] s = str.split("=");
	    		try{
	    			parameters.put(s[0], s[1]);
	    		}catch(IndexOutOfBoundsException e){
	    			
	    		}
	    		
	    	}
    	}
    	
    	//prepare query parameters
    	String[] locationSplit = location.split("\\?");
    	if(locationSplit.length > 1){
    		
    		String[] strParms = locationSplit[1].split("&");
        	
    		for(String str : strParms){
	    		String[] s = str.split("=");
	    		try{
	    			parameters.put(s[0], s[1]);
	    		}catch(IndexOutOfBoundsException e){
	    			
	    		}
	    	}
		}
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public String getLocation() {
        return location;
    }

    @Override
    public Map<String,String> getHeaders() {
        return headers;
    }
    
    @Override
    public String getHeader(String key) {
        return headers.get(key);
    }
    
    @Override
    public String getRequestBody(){
    	return requestBody;
    }
    
    @Override
    public Map<String,String> getParameters(){
    	return parameters;
    }
    
    @Override
    public String getParameter(String key){
    	return parameters.get(key);
    }

    @Override
    public String getVersion(){
    	return version;
    }
    
    @Override
    public String getRawRequest(){
    	return raw;
    }

	@Override
	public String toString() {
		return "HttpRequstImpl [method=" + method
				+ ", location=" + location + ", requestBody=" + requestBody
				+ ", version=" + version + ", headers=" + headers + "]";
	}
    
}
