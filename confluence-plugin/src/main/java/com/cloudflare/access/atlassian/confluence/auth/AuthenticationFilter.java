package com.cloudflare.access.atlassian.confluence.auth;

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

import com.atlassian.confluence.security.login.LoginManager;
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
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.component.ComponentLocator;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import com.atlassian.seraph.service.rememberme.RememberMeService;
import com.cloudflare.access.atlassian.common.config.EnvironmentPluginConfiguration;
import com.cloudflare.access.atlassian.common.http.AtlassianInternalHttpProxy;
import com.cloudflare.access.atlassian.confluence.util.PluginUtils;
import com.google.common.collect.Iterators;


@Named("CloudflareAccessAuthenticationFilter")
public class AuthenticationFilter implements Filter{

	private static final Log log = Logger.getInstance(AuthenticationFilter.class);

	@ComponentImport
	private CrowdService crowdService;

	@ComponentImport
	private PluginAccessor pluginAcessor;

	@Inject
	private CloudflareAccessService cloudflareAccess;

	@Inject
	public AuthenticationFilter(CrowdService crowdService, PluginAccessor pluginAcessor, CloudflareAccessService cloudflareAccess) {
		this.crowdService = Objects.requireNonNull(crowdService, "CrowdService instance not injected by DI container");
		this.pluginAcessor = Objects.requireNonNull(pluginAcessor, "PluginAccessor instance not injected by DI container");
		this.cloudflareAccess = Objects.requireNonNull(cloudflareAccess, "CloudflareAccessService instance not injected by DI container");
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		log.debug("Initializing internal proxy...");
		AtlassianInternalHttpProxy.INSTANCE.init(new EnvironmentPluginConfiguration().getInternalProxyConfig());
		log.debug("Filter initialized");
	}

	@Override
	public void destroy() {
		log.debug("Shutting down internal proxy...");
		AtlassianInternalHttpProxy.INSTANCE.shutdown();
		log.debug("Filter destroyed");
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		final HttpServletRequest httpRequest = (HttpServletRequest) request;
		final HttpServletResponse httpResponse = (HttpServletResponse) response;

		if(pluginAcessor.isPluginEnabled(PluginUtils.getPluginKey()) == false) {
			log.debug("Plugin is disabled, bypassing filter");
			chain.doFilter(httpRequest, httpResponse);
			return;
		}

		if(ConfluenceWhitelistRules.matchesWhitelist(httpRequest)) {
			log.debug("Request is whitelisted, bypassing filter");
			chain.doFilter(request, response);
			return;
		}

		final CloudflareAuthenticationResult authResult = authenticate(httpRequest);

		if(authResult.isAuthenticated()) {
			User user = authResult.getUser();
			log.debug("Request authenticated for user: " + user.getName());
			final HttpSession httpSession = httpRequest.getSession();
            httpSession.setAttribute(DefaultAuthenticator.LOGGED_IN_KEY, user);
            httpSession.setAttribute(DefaultAuthenticator.LOGGED_OUT_KEY, null);
            ComponentLocator.getComponent(LoginManager.class).onSuccessfulLoginAttempt(user.getName(), httpRequest);
            ComponentLocator.getComponent(RememberMeService.class).addRememberMeCookie(httpRequest, httpResponse, user.getName());
            chain.doFilter(request, response);
		}else {
			log.debug("Request not authenticated: " + authResult.getError().getMessage());
			/*log.debug(RequestInspector.getHeadersAndCookies(httpRequest));
			log.debug(RequestInspector.getSessionContents(httpRequest));
			log.debug(RequestInspector.getRequestedResourceInfo(httpRequest));*/
			if(acceptsHtml(httpRequest)) {
				httpResponse.addHeader(AuthenticationErrorServlet.ERROR_MSG_HEADER, authResult.getError().getMessage());
				httpResponse.sendRedirect(httpRequest.getContextPath() + AuthenticationErrorServlet.PATH);
			}else {
				httpResponse.sendError(401, authResult.getError().getMessage());
				httpResponse.addHeader("WWW-Authenticate", "bearer realm=" + httpRequest.getServerName());
			}
		}
	}

	private boolean acceptsHtml(HttpServletRequest httpRequest) {
		if(httpRequest.getHeader("Accept") == null) return false;
		return httpRequest.getHeader("Accept").contains("text/html");
	}

	private CloudflareAuthenticationResult authenticate(HttpServletRequest httpRequest) {
		try {
			CloudflareToken cloudflareToken = this.cloudflareAccess.getValidTokenFromRequest(httpRequest);

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
			e.printStackTrace();
			return new CloudflareAuthenticationResult(e);
		}
	}

}
