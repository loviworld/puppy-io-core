package com.lovi.puppy.core.web.impl;

import org.apache.log4j.Logger;

import com.lovi.puppy.core.exception.ErrorMessage;
import com.lovi.puppy.core.web.HttpRequst;
import com.lovi.puppy.core.web.HttpResponse;
import com.lovi.puppy.core.web.HttpServer;
import com.lovi.puppy.core.web.ServerContext;
import com.lovi.puppy.core.web.Session;
import com.lovi.puppy.core.web.enums.HttpMethod;

public class ServerContextImpl implements ServerContext {

	private HttpServer httpServer;
	private HttpRequst httpRequst;
	private HttpResponse httpResponse;
	private Session session;
	final static Logger logger = Logger.getLogger(ServerContext.class);

	public ServerContextImpl(HttpServer httpServer, HttpRequst httpRequst,
			HttpResponse httpResponse, Session session) {
		this.httpServer = httpServer;
		this.httpRequst = httpRequst;
		this.httpResponse = httpResponse;
		this.session = session;
	}

	@Override
	public void forward(String path) {
		forward(path, HttpMethod.GET);
	}
	
	@Override
	public void forward(String path, HttpMethod method) {
		try {
			httpServer.requestProcess(this, path, method);
		} catch (Exception e) {
			logger.error(ErrorMessage.REQUEST_FORWORD_ERROR.getMessage());
		}

	}
	
	@Override
	public void redirect(String path){
		httpResponse.setResponseCode(301);
		httpResponse.setHeader("Location", path);
		httpResponse.write("");
	}
	
	@Override
	public HttpRequst getHttpRequst() {
		return httpRequst;
	}

	@Override
	public HttpResponse getHttpResponse() {
		return httpResponse;
	}

	@Override
	public Session getSession() {
		return session;
	}

}
