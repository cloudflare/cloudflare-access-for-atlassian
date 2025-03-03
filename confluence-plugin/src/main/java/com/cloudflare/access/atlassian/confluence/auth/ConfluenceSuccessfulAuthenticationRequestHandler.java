package com.cloudflare.access.atlassian.confluence.auth;

import static org.apache.commons.lang3.StringUtils.stripToEmpty;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.cloudflare.access.atlassian.confluence.auth.exception.ConfluenceUserNotFoundException;

import javax.inject.Inject;

@Component
public class ConfluenceSuccessfulAuthenticationRequestHandler implements SuccessfulAuthenticationRequestHandler{

	private static final Logger log = LoggerFactory.getLogger(ConfluenceSuccessfulAuthenticationRequestHandler.class);

	private UserAccessor userAcessor;

	@Inject
	public ConfluenceSuccessfulAuthenticationRequestHandler(@ComponentImport UserAccessor userAcessor) {
		this.userAcessor = userAcessor;
	}

	@Override
	public void handle(HttpServletRequest httpRequest, HttpServletResponse httpResponse, FilterChain chain, User user) throws IOException, ServletException {
		try {
			final HttpSession httpSession = httpRequest.getSession();

			internalHandle(httpRequest, httpResponse, user, httpSession);

			log.debug("Returning to filter chain...");
	        chain.doFilter(httpRequest, httpResponse);
		}catch (IllegalStateException e) {
			httpResponse.sendError(401, "Session is invalid");
		}
	}

	private void internalHandle(HttpServletRequest httpRequest, HttpServletResponse httpResponse, User user, final HttpSession httpSession) {
		log.debug("Setting logged in key with current user...");
		httpSession.setAttribute(DefaultAuthenticator.LOGGED_IN_KEY, new ConfluenceUserPrincipal(crowdUserToConfluenceUser(user)));
		httpSession.setAttribute(DefaultAuthenticator.LOGGED_OUT_KEY, null);

		log.debug("Principal defined in session, setting sucessful login...");
		ComponentLocator.getComponent(LoginManager.class).onSuccessfulLoginAttempt(user.getName(), httpRequest);

		log.debug("Setting remember me cookie...");
		ComponentLocator.getComponent(RememberMeService.class).addRememberMeCookie(httpRequest, httpResponse, user.getName());
	}

	private ConfluenceUser crowdUserToConfluenceUser(User user){
		Objects.requireNonNull(user, "Atlassian User should not be null");

		List<User> activeUsers = findActiveMatchingUsers(user);

		shouldContainOnlyOne(user, activeUsers);

		ConfluenceUser confluenceUser = userAcessor.getUserByName(activeUsers.get(0).getName());

		return confluenceUser;
	}

	private List<User> findActiveMatchingUsers(User user) {
		@SuppressWarnings("unchecked")
		Iterable<User> users = userAcessor.getUsersByEmail(stripToEmpty(user.getEmailAddress())).pager();
		return StreamSupport.stream(users.spliterator(), false)
				.filter(u -> u.isActive())
				.collect(Collectors.toList());
	}

	private void shouldContainOnlyOne(User user, List<User> users) {
		String errorMsg;
		if(users.isEmpty()) {
			errorMsg = "Search for Confluence User by email returned empty: " + user.getEmailAddress();
			log.error(errorMsg);
			throw new ConfluenceUserNotFoundException(errorMsg);
		}else if(users.size() > 1) {
			errorMsg = "Search for active Confluence User by email returned more than one result: " + user.getEmailAddress() + " => " + users.size() + " users";
			log.error(errorMsg);
			throw new ConfluenceUserNotFoundException(errorMsg);
		}
	}

}
