package com.cloudflare.access.atlassian.base.auth.impl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.cloudflare.access.atlassian.base.auth.FailedAuthenticationRequestHandler;

@Component
public class DefaultFailedAuthHandler implements FailedAuthenticationRequestHandler{

	private static final Logger log = LoggerFactory.getLogger(DefaultFailedAuthHandler.class);

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response, Throwable e) {
		try {
			response.addHeader("WWW-Authenticate", "bearer realm=" + request.getServerName());
			response.sendError(401, e.getMessage());
		}catch (Exception e2) {
			log.error("Unable to send 401 error with message " + e.getMessage(), e2);
			throw new RuntimeException(e2);
		}
	}

}
