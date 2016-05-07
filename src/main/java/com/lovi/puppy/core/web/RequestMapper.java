package com.lovi.puppy.core.web;

import java.util.Set;

import com.lovi.puppy.core.handlers.FailureHandler;
import com.lovi.puppy.core.web.enums.HttpMethod;
import com.lovi.puppy.core.web.impl.RequestMapperImpl;

public interface RequestMapper {

	static RequestMapper create(){
		return new RequestMapperImpl();
	}
	
	Set<RequestMap> getRequestMaps();

	RequestMap map(String path, HttpMethod method);

	FailureHandler<ServerContext> getFailureHandler();

	RequestMapperImpl setFailureHandler(FailureHandler<ServerContext> failureHandler);

}
