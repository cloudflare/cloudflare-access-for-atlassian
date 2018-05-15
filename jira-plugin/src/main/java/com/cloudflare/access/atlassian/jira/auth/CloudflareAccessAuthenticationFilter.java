package com.cloudflare.access.atlassian.jira.auth;

import java.io.IOException;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.search.query.entity.UserQuery;
import com.atlassian.crowd.search.query.entity.restriction.MatchMode;
import com.atlassian.crowd.search.query.entity.restriction.TermRestriction;
import com.atlassian.crowd.search.query.entity.restriction.constants.UserTermKeys;
import com.atlassian.extras.common.log.Logger;
import com.atlassian.extras.common.log.Logger.Log;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.login.LoginManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import com.atlassian.seraph.service.rememberme.RememberMeService;
import com.cloudflare.access.atlassian.jira.util.RequestInspector;
import com.google.common.collect.Iterables;

@Named("CloudflareAccessAuthenticationFilter")
public class CloudflareAccessAuthenticationFilter implements Filter{

	private static final Log log = Logger.getInstance(CloudflareAccessAuthenticationFilter.class);

	@ComponentImport
	private CrowdService crowdService;

	@Inject
	public CloudflareAccessAuthenticationFilter(CrowdService crowdService) {
		this.crowdService = Objects.requireNonNull(crowdService, "CrowdService instance not injected by DI container");
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {}

	@Override
	public void destroy() {}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		final HttpServletRequest httpRequest = (HttpServletRequest) request;
		final HttpServletResponse httpResponse = (HttpServletResponse) response;

		log.info(RequestInspector.getHeadersAndCookies(httpRequest));
		log.info(RequestInspector.getSessionContents(httpRequest));

		final CloudflareAuthenticationResult authResult = authenticate(httpRequest);

		if(authResult.isAuthenticated()) {
			User user = authResult.getUser();
			final HttpSession httpSession = httpRequest.getSession();
            httpSession.setAttribute(DefaultAuthenticator.LOGGED_IN_KEY, user);
            httpSession.setAttribute(DefaultAuthenticator.LOGGED_OUT_KEY, null);
            ComponentAccessor.getComponentOfType(LoginManager.class).onLoginAttempt(httpRequest, user.getName(), true);
            ComponentAccessor.getComponentOfType(RememberMeService.class).addRememberMeCookie(httpRequest, httpResponse, user.getName());
            chain.doFilter(request, response);
		}else {
			httpResponse.sendError(401, authResult.getError().getMessage());
			httpResponse.addHeader("WWW-Authenticate", "bearer realm=" + httpRequest.getServerName());
		}
	}

	private CloudflareAuthenticationResult authenticate(HttpServletRequest httpRequest) {
		User user = null;
		try {
			CloudflareToken cloudflareToken = new CloudflareToken(httpRequest);
			//TODO how to handle more than one user with same email address?
			String userEmail = cloudflareToken.getUserEmail();
			user  = Iterables.getFirst(crowdService.search(new UserQuery<>(User.class, new TermRestriction<>(UserTermKeys.EMAIL, MatchMode.EXACTLY_MATCHES, userEmail), 0, 1)), null);

			if(user == null) {
				throw new IllegalArgumentException(String.format("No user matching '%s'", userEmail));
			}

		}catch (Throwable e) {
			log.error("Error processing token:" + e.getMessage(), e);
			return new CloudflareAuthenticationResult(e);
		}
		return new CloudflareAuthenticationResult(user);
	}


}
