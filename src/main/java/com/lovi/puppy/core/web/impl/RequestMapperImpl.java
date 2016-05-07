package com.lovi.puppy.core.web.impl;

import java.util.HashSet;
import java.util.Set;

import com.lovi.puppy.core.handlers.FailureHandler;
import com.lovi.puppy.core.web.RequestMap;
import com.lovi.puppy.core.web.RequestMapper;
import com.lovi.puppy.core.web.ServerContext;
import com.lovi.puppy.core.web.enums.HttpMethod;

public class RequestMapperImpl implements RequestMapper{

	private Set<RequestMap> requestMaps = new HashSet<>();
	private FailureHandler<ServerContext> failureHandler;

	@Override
	public RequestMap map(String path, HttpMethod method){
		return new RequestMapImpl(this, path, method);
	}

	@Override
	public Set<RequestMap> getRequestMaps() {
		return requestMaps;
	}
	
	@Override
	public FailureHandler<ServerContext> getFailureHandler() {
		return failureHandler;
	}

	@Override
	public RequestMapperImpl setFailureHandler(FailureHandler<ServerContext> failureHandler) {
		this.failureHandler = failureHandler;
		return this;
	}
	
}
