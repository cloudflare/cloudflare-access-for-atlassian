package com.cloudflare.access.atlassian.confluence.auth;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.atlassian.confluence.security.login.LoginManager;
import com.atlassian.confluence.security.seraph.ConfluenceUserPrincipal;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.component.ComponentLocator;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import com.atlassian.seraph.service.rememberme.RememberMeService;
import com.cloudflare.access.atlassian.base.auth.SuccessfulAuthenticationRequestHandler;

@Component
public class ConfluenceSuccessfulAuthenticationRequestHandler implements SuccessfulAuthenticationRequestHandler{

	private static final Logger log = LoggerFactory.getLogger(ConfluenceSuccessfulAuthenticationRequestHandler.class);

	private UserAccessor userAcessor;

	@Autowired
	public ConfluenceSuccessfulAuthenticationRequestHandler(@ComponentImport UserAccessor userAcessor) {
		this.userAcessor = userAcessor;
	}

	@Override
	public void handle(HttpServletRequest httpRequest, HttpServletResponse httpResponse, FilterChain chain, User user) {
		try {

			final HttpSession httpSession = httpRequest.getSession();

			if(httpSession.getAttribute(DefaultAuthenticator.LOGGED_IN_KEY) == null) {
				ConfluenceUser confluenceUser = userAcessor.getUserByName(user.getName());
				httpSession.setAttribute(DefaultAuthenticator.LOGGED_IN_KEY, new ConfluenceUserPrincipal(confluenceUser));
				httpSession.setAttribute(DefaultAuthenticator.LOGGED_OUT_KEY, null);
				ComponentLocator.getComponent(LoginManager.class).onSuccessfulLoginAttempt(user.getName(), httpRequest);
				ComponentLocator.getComponent(RememberMeService.class).addRememberMeCookie(httpRequest, httpResponse, user.getName());
			}

	        chain.doFilter(httpRequest, httpResponse);

		}catch (IllegalStateException e) {

			try {
				httpResponse.sendError(401, "Session is invalid");
			} catch (IOException io) {
				io.printStackTrace();
				log.error("Unable to send error for invalid session: " + e.getMessage(), e);
			}

		}catch (Throwable e) {

			log.error("Unable to handle successful authentication: " + e.getMessage(), e);
			throw new RuntimeException(e);

		}
	}


}
