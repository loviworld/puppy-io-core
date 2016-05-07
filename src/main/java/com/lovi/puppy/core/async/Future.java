package com.lovi.puppy.core.async;

import com.lovi.puppy.core.async.impl.FutureImpl;
import com.lovi.puppy.core.handlers.Handler;

/**
 * 
 * @author Tharanga Thennakoon
 *
 * @param <R>
 */
public interface Future<T> {
	
	static <T> Future<T> create(){
		return new FutureImpl<>();
	}
	
    void setResult(T result);
    
	T getResult();
 
    void setFailure(Throwable failure);

	void setSussessHandler(Handler<T> handler);

	void setFailureHandler(Handler<Throwable> handler);
	
}
