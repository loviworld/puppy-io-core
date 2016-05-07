package com.lovi.puppy.core.web;

import com.lovi.puppy.core.handlers.Handler;
import com.lovi.puppy.core.web.enums.HttpMethod;
import com.lovi.puppy.core.web.impl.HttpServerImpl;

/**
 * 
 * @author Tharanga Thennakoon
 *
 */
public interface HttpServer {

	static HttpServer create(){
		return new HttpServerImpl();
	}
	
	void setRequestMapper(RequestMapper requestMapper);

	void run(int port, RequestMapper requestMapper);

	void run(String hostname, int port, RequestMapper requestMapper);

	void run(int port, RequestMapper requestMapper, Handler<Object> successHandler, Handler<Throwable> failureHandler);

	void run(String hostname, int port, RequestMapper requestMapper, Handler<Object> successHandler,
			Handler<Throwable> failureHandler);
	
	/**
	 * this method is used for request forwarding. HTTP Method -> GET
	 * @param serverContext
	 * @param newLocation
	 * @throws Exception
	 */
	void requestProcess(ServerContext serverContext, String newLocation)throws Exception;

	/**
	 * this method is used for request forwarding
	 * @param serverContext
	 * @throws Exception
	 */
	void requestProcess(ServerContext serverContext, String newLocation, HttpMethod httpMethod) throws Exception;

	

}
