package com.cloudflare.access.atlassian.confluence.auth.exception;

public class ConfluenceUserNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1684736042699694558L;

	public ConfluenceUserNotFoundException(String msg) {
		super(msg);
	}
}
