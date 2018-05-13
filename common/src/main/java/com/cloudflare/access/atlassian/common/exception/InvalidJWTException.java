package com.cloudflare.access.atlassian.common.exception;

public class InvalidJWTException extends RuntimeException {

	private static final long serialVersionUID = 2608865350569604759L;

	public InvalidJWTException(String msg) {
		super(msg);
	}

	public InvalidJWTException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
