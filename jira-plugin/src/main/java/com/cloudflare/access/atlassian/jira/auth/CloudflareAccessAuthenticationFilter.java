package com.cloudflare.access.atlassian.jira.auth;

import java.io.IOException;
import java.util.Iterator;
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
import com.atlassian.crowd.embedded.api.SearchRestriction;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.search.builder.Combine;
import com.atlassian.crowd.search.query.entity.UserQuery;
import com.atlassian.crowd.search.query.entity.restriction.MatchMode;
import com.atlassian.crowd.search.query.entity.restriction.TermRestriction;
import com.atlassian.crowd.search.query.entity.restriction.constants.UserTermKeys;
import com.atlassian.extras.common.log.Logger;
import com.atlassian.extras.common.log.Logger.Log;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.login.LoginManager;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import com.atlassian.seraph.service.rememberme.RememberMeService;
import com.cloudflare.access.atlassian.common.http.AtlassianInternalHttpProxy;
import com.cloudflare.access.atlassian.jira.util.PluginUtils;
import com.cloudflare.access.atlassian.jira.util.RequestInspector;
import com.google.common.collect.Iterators;

@Named("CloudflareAccessAuthenticationFilter")
public class CloudflareAccessAuthenticationFilter implements Filter{

	private static final Log log = Logger.getInstance(CloudflareAccessAuthenticationFilter.class);

	@ComponentImport
	private CrowdService crowdService;

	@ComponentImport
	private PluginAccessor pluginAcessor;

	@Inject
	public CloudflareAccessAuthenticationFilter(CrowdService crowdService, PluginAccessor pluginAcessor) {
		this.crowdService = Objects.requireNonNull(crowdService, "CrowdService instance not injected by DI container");
		this.pluginAcessor = Objects.requireNonNull(pluginAcessor, "PluginAccessor instance not injected by DI container");
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		//TODO we may need to find host and port
		//TODO add a config to set the url and port, this way if our approach fail we are always able to forward to the right place
		AtlassianInternalHttpProxy.INSTANCE.init("localhost", 2990);
	}

	@Override
	public void destroy() {}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		final HttpServletRequest httpRequest = (HttpServletRequest) request;
		final HttpServletResponse httpResponse = (HttpServletResponse) response;

		if(pluginAcessor.isPluginEnabled(PluginUtils.getPluginKey()) == false) {
			chain.doFilter(request, response);
			return;
		}

		if(JiraWhitelistRules.matchesWhitelist(httpRequest)) {
			chain.doFilter(request, response);
			return;
		}

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
			log.debug(RequestInspector.getHeadersAndCookies(httpRequest));
			log.debug(RequestInspector.getSessionContents(httpRequest));
			log.debug(RequestInspector.getRequestedResourceInfo(httpRequest));
			httpResponse.sendError(401, authResult.getError().getMessage());
			httpResponse.addHeader("WWW-Authenticate", "bearer realm=" + httpRequest.getServerName());
		}
	}

	private CloudflareAuthenticationResult authenticate(HttpServletRequest httpRequest) {
		try {
			CloudflareToken cloudflareToken = new CloudflareToken(httpRequest);

			String userEmail = cloudflareToken.getUserEmail();
			SearchRestriction userCriteria = Combine.allOf(
					new TermRestriction<>(UserTermKeys.EMAIL, MatchMode.EXACTLY_MATCHES, userEmail),
					new TermRestriction<>(UserTermKeys.ACTIVE, true)
			);
			UserQuery<User> query = new UserQuery<>(User.class, userCriteria, 0, Integer.MAX_VALUE);

			Iterator<User> users = crowdService.search(query).iterator();
			User user  = Iterators.getNext(users, null);

			if(user == null) {
				throw new IllegalArgumentException(String.format("No user matching '%s'", userEmail));
			}

			if(users.hasNext()) {
				throw new IllegalArgumentException(String.format("More than one user matching '%s'", userEmail));
			}

			return new CloudflareAuthenticationResult(user);
		}catch (Throwable e) {
			log.error("Error processing token:" + e.getMessage(), e);
			return new CloudflareAuthenticationResult(e);
		}
	}

}
