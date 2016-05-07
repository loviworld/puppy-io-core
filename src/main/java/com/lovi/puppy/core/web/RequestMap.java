package com.lovi.puppy.core.web;

import com.lovi.puppy.core.handlers.FailureHandler;
import com.lovi.puppy.core.handlers.Handler;
import com.lovi.puppy.core.web.enums.HttpMethod;

public interface RequestMap{

	Handler<ServerContext> getHandler();

	RequestMap setHandler(Handler<ServerContext> handler);

	String getPath();
	
	String getRegExpPath();
	
	void setRegExpPath(String regExpPath);

	HttpMethod getHttpMethod();

	FailureHandler<ServerContext> getFailureHandler();

	RequestMap setFailureHandler(FailureHandler<ServerContext> failureHandler);

}
