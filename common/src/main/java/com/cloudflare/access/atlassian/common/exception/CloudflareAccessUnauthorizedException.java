package com.cloudflare.access.atlassian.common.exception;

public class CloudflareAccessUnauthorizedException extends RuntimeException {

	private static final long serialVersionUID = -770975204332596573L;

	public CloudflareAccessUnauthorizedException(String msg) {
		super(msg);
	}

	public CloudflareAccessUnauthorizedException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
