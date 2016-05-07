package com.lovi.puppy.core.async;

import com.lovi.puppy.core.async.impl.AsyncExecutorImpl;
import com.lovi.puppy.core.handlers.AsyncHandler;
import com.lovi.puppy.core.handlers.Handler;

/**
 * 
 * @author Tharanga Thennakoon
 *
 */
public interface AsyncExecutor<T>{
	
	static <T> AsyncExecutor<T> create(){
		return new AsyncExecutorImpl<T>();
	}
	void run(AsyncHandler<T> handler, Handler<T> successHandler, Handler<Throwable> failureHandler);
}
