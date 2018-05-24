package com.cloudflare.access.atlassian.base.auth;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface FailedAuthenticationRequestHandler {

	void handle(HttpServletRequest httpRequest, HttpServletResponse httpResponse, Throwable e);

}
