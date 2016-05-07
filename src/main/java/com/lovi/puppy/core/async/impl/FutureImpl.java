package com.lovi.puppy.core.async.impl;

import com.lovi.puppy.core.handlers.Handler;
import com.lovi.puppy.core.async.Future;

/**
 * 
 * @author Tharanga Thennakoon
 *
 * @param <T>
 */
public class FutureImpl<T> implements Future<T> {
	
	private Handler<T> successHandler;
	private Handler<Throwable> failureHandler;
	private T result;
	private Throwable failure;
	private boolean isCompleted;

	@Override
	public void setResult(T result) {
		this.result = result;
		this.isCompleted = true;
		if(successHandler != null){
			try {
				successHandler.handle(result);
			} catch (Exception e) {
				setFailure(e);
			}
		}
	}
	
	@Override
	public T getResult() {
		return result;
	}

	@Override
	public void setFailure(Throwable failure) {
		this.failure = failure;
		this.isCompleted = true;
		if(failureHandler != null){
			try {
				failureHandler.handle(failure);
			} catch (Exception e) {
			}
		}
	}
	
	@Override
	public void setSussessHandler(Handler<T> handler){
		successHandler = handler;
		if(isCompleted)
			if(failure == null)
				setResult(result);
	}
	
	@Override
	public void setFailureHandler(Handler<Throwable> handler){
		failureHandler = handler;
		if(isCompleted)
			if(failure != null)
				setFailure(failure);
	}

}
