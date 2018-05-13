package com.cloudflare.access.atlassian.base.auth;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface FailedAuthenticationRequestHandler {

	void handle(HttpServletRequest httpRequest, HttpServletResponse httpResponse, Throwable e);

	default void sendErrorResponse(HttpServletRequest httpRequest, HttpServletResponse httpResponse, Throwable e)
			throws IOException, UnsupportedEncodingException {
		if(acceptsHtml(httpRequest)) {
			sendToErrorPage(httpRequest, httpResponse, e);
		}else {
			send401(httpResponse, e.getMessage(), httpRequest.getServerName());
		}
	}

	default void sendToErrorPage(HttpServletRequest httpRequest, HttpServletResponse httpResponse, Throwable e)
			throws IOException, UnsupportedEncodingException {
		httpResponse.sendRedirect(httpRequest.getContextPath() + AuthenticationErrorServlet.PATH + String.format("?%s=%s", AuthenticationErrorServlet.ERROR_MSG_PARAM, URLEncoder.encode(e.getMessage(), "UTF-8")));
	}

	default void send401(HttpServletResponse httpResponse, String reason, String realm) throws IOException {
		//Disable cloudflare cache for 401 result
		httpResponse.addHeader("Cache-Control", "no-cache");
		httpResponse.addHeader("WWW-Authenticate", "bearer realm=" + realm);
		httpResponse.sendError(401, reason);
	}

	default  boolean acceptsHtml(HttpServletRequest httpRequest) {
		if(httpRequest.getHeader("Accept") == null) return false;
		return httpRequest.getHeader("Accept").contains("text/html");
	}
}
