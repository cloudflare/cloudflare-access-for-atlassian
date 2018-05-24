package com.cloudflare.access.atlassian.jira.auth;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.security.login.LoginManager;
import com.atlassian.sal.api.component.ComponentLocator;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import com.atlassian.seraph.service.rememberme.RememberMeService;
import com.cloudflare.access.atlassian.base.auth.SuccessfulAuthenticationRequestHandler;

@Component
public class JiraSuccessfulAuthenticationRequestHandler implements SuccessfulAuthenticationRequestHandler{

	private static final Logger log = LoggerFactory.getLogger(JiraSuccessfulAuthenticationRequestHandler.class);

	@Override
	public void handle(HttpServletRequest httpRequest, HttpServletResponse httpResponse, FilterChain chain, User user) {
		try {
			final HttpSession httpSession = httpRequest.getSession();
	        httpSession.setAttribute(DefaultAuthenticator.LOGGED_IN_KEY, user);
	        httpSession.setAttribute(DefaultAuthenticator.LOGGED_OUT_KEY, null);
	        ComponentLocator.getComponent(LoginManager.class).onLoginAttempt(httpRequest, user.getName(), true);
	        ComponentLocator.getComponent(RememberMeService.class).addRememberMeCookie(httpRequest, httpResponse, user.getName());
	        chain.doFilter(httpRequest, httpResponse);
		}catch (Throwable e) {
			log.error("Unable to handle successful authentication: " + e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

}
