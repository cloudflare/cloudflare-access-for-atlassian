package com.cloudflare.access.atlassian.common.exception;

public class HttpRequestException extends RuntimeException {

	private static final long serialVersionUID = 7578492072131158051L;

	public HttpRequestException(String msg) {
		super(msg);
	}

	public HttpRequestException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
