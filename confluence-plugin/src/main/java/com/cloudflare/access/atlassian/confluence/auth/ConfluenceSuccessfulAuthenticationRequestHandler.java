package com.cloudflare.access.atlassian.confluence.auth;

import static org.apache.commons.lang3.StringUtils.stripToEmpty;

import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;
import java.util.stream.StreamSupport;

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
import com.atlassian.user.search.page.Pager;
import com.cloudflare.access.atlassian.base.auth.SuccessfulAuthenticationRequestHandler;
import com.cloudflare.access.atlassian.base.utils.RequestInspector;
import com.cloudflare.access.atlassian.confluence.auth.exception.ConfluenceUserNotFoundException;

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

			internalHandle(httpRequest, httpResponse, user, httpSession);

			log.debug("Returning to filter chain...");
	        chain.doFilter(httpRequest, httpResponse);
		}catch (IllegalStateException e) {

			try {
				httpResponse.sendError(401, "Session is invalid");
			} catch (IOException io) {
				io.printStackTrace();
				log.error("Unable to send error for invalid session: " + e.getMessage(), e);
			}

		}catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	private void internalHandle(HttpServletRequest httpRequest, HttpServletResponse httpResponse, User user, final HttpSession httpSession) {
		try {
			if(httpSession.getAttribute(DefaultAuthenticator.LOGGED_IN_KEY) == null) {
				log.debug("Logged in key not found in session, updating...");
				httpSession.setAttribute(DefaultAuthenticator.LOGGED_IN_KEY, new ConfluenceUserPrincipal(crowdUserToConfluenceUser(user)));
				httpSession.setAttribute(DefaultAuthenticator.LOGGED_OUT_KEY, null);

				log.debug("Principal defined in session, setting sucessful login...");
				ComponentLocator.getComponent(LoginManager.class).onSuccessfulLoginAttempt(user.getName(), httpRequest);

				log.debug("Setting remember me cookie...");
				ComponentLocator.getComponent(RememberMeService.class).addRememberMeCookie(httpRequest, httpResponse, user.getName());
			}
		}catch (Throwable e) {
			log.error("Unable to handle successful authentication: " + e.getMessage(), e);

			if(log.isDebugEnabled()) {
				log.debug(RequestInspector.getHeadersAndCookies(httpRequest));
				log.debug(RequestInspector.getRequestedResourceInfo(httpRequest));
				log.debug(RequestInspector.getSessionContents(httpRequest));
			}

			throw e;
		}
	}

	private ConfluenceUser crowdUserToConfluenceUser(User user){
		Objects.requireNonNull(user, "Atlassian User should not be null");

		@SuppressWarnings("unchecked")
		Pager<User> usersByEmailPager = userAcessor.getUsersByEmail(stripToEmpty(user.getEmailAddress())).pager();

		shouldNotBeEmpty(user, usersByEmailPager);

		Iterator<User> users = usersByEmailPager.iterator();
		ConfluenceUser confluenceUser = userAcessor.getUserByName(users.next().getName());

		shouldNotHaveMoreUsers(user, usersByEmailPager, users);

		return confluenceUser;
	}

	private void shouldNotBeEmpty(User user, Pager<User> usersByEmailPager) {
		if(usersByEmailPager.isEmpty()) {
			log.error("Search for Confluence User by email returned empty: " + user.getEmailAddress());
			throw new ConfluenceUserNotFoundException("Search for Confluence User by email returned empty: " + user.getEmailAddress());
		}
	}

	private void shouldNotHaveMoreUsers(User user, Pager<User> usersByEmailPager, Iterator<User> users) {
		if(users.hasNext()) {
			Iterable<User> usersIterable = () -> usersByEmailPager.iterator();
			long matchingUsers = StreamSupport.stream(usersIterable.spliterator(), false).count();
			final String errorMsg = "Search for Confluence User by email returned more than one result: " + user.getEmailAddress() + " => " + matchingUsers + " users";
			log.error(errorMsg);
			throw new ConfluenceUserNotFoundException(errorMsg);
		}
	}

}
