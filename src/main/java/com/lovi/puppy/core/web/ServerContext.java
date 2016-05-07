package com.lovi.puppy.core.web;

import com.lovi.puppy.core.web.enums.HttpMethod;

public interface ServerContext {
	
	HttpRequst getHttpRequst();

	HttpResponse getHttpResponse();

	Session getSession();

	/**
	 * this method is used for request forwarding. HTTP Method -> GET
	 * @param path
	 */
	void forward(String path);
	
	/**
	 * this method is used for request forwarding.
	 * @param path
	 * @param method
	 */
	void forward(String path, HttpMethod method);

	/**
	 * this method redirect the request to new location. only support for GET request
	 * @param path
	 */
	void redirect(String path);

}
