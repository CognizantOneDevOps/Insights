package com.cognizant.devops.platformdal.dal;

public class DataDeleteException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9146118590670177932L;

	public DataDeleteException() {
		super();
	}
	
	public DataDeleteException(String message) {
		super(message);
	}
	
	public DataDeleteException(String message,Throwable t) {
		super(message,t);
	}
}
