package com.cloudflare.access.atlassian.confluence.auth;

import com.atlassian.crowd.embedded.api.User;

public class CloudflareAuthenticationResult {

	private User user;

	private Throwable error;

	public CloudflareAuthenticationResult(User user) {
		super();
		this.user = user;
	}

	public CloudflareAuthenticationResult(Throwable error) {
		super();
		this.error = error;
	}

	public boolean isAuthenticated() {
		return user != null;
	}

	public User getUser() {
		return user;
	}

	public Throwable getError() {
		return error;
	}

}
