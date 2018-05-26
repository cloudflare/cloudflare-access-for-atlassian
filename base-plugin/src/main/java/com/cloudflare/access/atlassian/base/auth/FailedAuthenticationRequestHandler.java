package com.cloudflare.access.atlassian.base.auth;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface FailedAuthenticationRequestHandler {

	void handle(HttpServletRequest httpRequest, HttpServletResponse httpResponse, Throwable e);

	default void send401(HttpServletResponse httpResponse, String reason, String realm) throws IOException {
		//Disable cloudflare cache for 401 result
		httpResponse.addHeader("Cache-Control", "no-cache");
		httpResponse.addHeader("WWW-Authenticate", "bearer realm=" + realm);
		httpResponse.sendError(401, reason);
	}

}
