package com.cloudflare.access.atlassian.confluence.auth;

import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.cloudflare.access.atlassian.base.auth.FailedAuthenticationRequestHandler;

@Component
public class ConfluenceFailedAuthenticationRequestHandler implements FailedAuthenticationRequestHandler{

	private static final Logger log = LoggerFactory.getLogger(ConfluenceFailedAuthenticationRequestHandler.class);

	@Override
	public void handle(HttpServletRequest httpRequest, HttpServletResponse httpResponse, Throwable e) {
		try {
			if(acceptsHtml(httpRequest)) {
				httpResponse.sendRedirect(httpRequest.getContextPath() + AuthenticationErrorServlet.PATH + String.format("?%s=%s", AuthenticationErrorServlet.ERROR_MSG_PARAM, URLEncoder.encode(e.getMessage(), "UTF-8")));
			}else {
				send401(httpResponse, e.getMessage(), httpRequest.getServerName());
			}
		}catch (Throwable e2) {
			log.error("Unable to send 401 error with message " + e.getMessage(), e2);
			throw new RuntimeException("Unable to send response on auth failure", e2);
		}

	}

	private boolean acceptsHtml(HttpServletRequest httpRequest) {
		if(httpRequest.getHeader("Accept") == null) return false;
		return httpRequest.getHeader("Accept").contains("text/html");
	}
}
