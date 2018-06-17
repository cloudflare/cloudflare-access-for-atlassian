package com.cloudflare.access.atlassian.base.auth;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import com.cloudflare.access.atlassian.base.config.ConfigurationService;
import com.cloudflare.access.atlassian.base.utils.EnvironmentFlags;
import com.cloudflare.access.atlassian.base.utils.RequestInspector;
import com.cloudflare.access.atlassian.common.context.AuthenticationContext;

@Component
public class CloudflareAccessService {

	private static final Logger log = LoggerFactory.getLogger(CloudflareAccessService.class);

	private PluginAccessor pluginAcessor;
	private CloudflarePluginDetails pluginDetails;
	private AtlassianUserService userService;
	private AtlassianProductWhitelistRules whitelistRules;
	private SuccessfulAuthenticationRequestHandler successHandler;
	private FailedAuthenticationRequestHandler failureHandler;
	private ConfigurationService configurationService;
	private final boolean filteringDisabled;

	@Autowired
	public CloudflareAccessService(@ComponentImport PluginAccessor pluginAcessor,
									CloudflarePluginDetails pluginDetails,
									ConfigurationService configurationService,
									AtlassianUserService userService,
									AtlassianProductWhitelistRules whitelistRules,
									SuccessfulAuthenticationRequestHandler successHandler,
									FailedAuthenticationRequestHandler failureHandler,
									Environment env) {
		this.pluginAcessor = pluginAcessor;
		this.pluginDetails = pluginDetails;
		this.configurationService = configurationService;
		this.userService = userService;
		this.whitelistRules = whitelistRules;
		this.successHandler = successHandler;
		this.failureHandler = failureHandler;
		this.filteringDisabled = EnvironmentFlags.isFiltersDisabled(env);
	}

	public void processAuthRequest(HttpServletRequest request, HttpServletResponse response, FilterChain chain) {
		try {
			if(isRequestFilteringDisabled()) {
				log.debug("Plugin filters are disabled or not configured yet, bypassing auth process");
				chain.doFilter(request, response);
				return;
			}

			if(whitelistRules.isRequestWhitelisted(request)) {
				log.debug("Request is whitelisted: {}", request.getRequestURI());
				chain.doFilter(request, response);
				return;
			}

			CloudflareToken token = getValidTokenFromRequest(request);
			User user = userService.getUser(token.getUserEmail());
			successHandler.handle(request, response, chain, user);
		}catch (Throwable e) {
			log.error("Error processing authentication: " + e.getMessage(), e);
			log.debug(RequestInspector.getRequestedResourceInfo(request));
			log.debug(RequestInspector.getHeadersAndCookies(request));
			clearCookies(request, response);
			failureHandler.handle(request, response, e);
		}
	}

	public void processLogoutRequest(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
		if(isRequestFilteringDisabled()) {
			log.debug("Plugin filters are disabled or no configured yet, bypassing logout redirect");
			chain.doFilter(request, response);
			return;
		}

		final HttpSession httpSession = request.getSession(false);
		if(httpSession != null) {
			httpSession.setAttribute(DefaultAuthenticator.LOGGED_IN_KEY, null);
			httpSession.setAttribute(DefaultAuthenticator.LOGGED_OUT_KEY, true);
			try {
				httpSession.invalidate();
			}catch (IllegalStateException e) {
				log.debug("Session was already invalid");
			}
		}

		clearCookies(request, response);

		AuthenticationContext authContext = getAuthContext();
		log.debug("Redirecting user to cloudflare logout at " + authContext.getLogoutUrl());
		response.sendRedirect(authContext.getLogoutUrl());
	}

	private void clearCookies(HttpServletRequest request, HttpServletResponse response) {
		Cookie[] cookies = request.getCookies();
		if(cookies == null) {
			return;
		}
		for (Cookie cookie : cookies) {
			if(shouldDeleteCookie(cookie)) {
				cookie.setMaxAge(0);
				log.debug("Deleting cookie '{}'", cookie.getName());
				response.addCookie(cookie);
			}
		}
	}

	private boolean shouldDeleteCookie(Cookie c) {
		String name = c.getName().toLowerCase();
		return name.equalsIgnoreCase("JSESSIONID") ||
				name.startsWith("seraph");
	}

	private boolean isRequestFilteringDisabled() {
		return isPluginDisabled() || filteringDisabled || (isPluginConfigured() == false);
	}

	private boolean isPluginDisabled() {
		return pluginAcessor.isPluginEnabled(pluginDetails.getPluginKey()) == false;
	}

	private CloudflareToken getValidTokenFromRequest(HttpServletRequest request) {
		return new CloudflareToken(request, getAuthContext());
	}

	private boolean isPluginConfigured() {
		return configurationService.getPluginConfiguration().isPresent();
	}

	private AuthenticationContext getAuthContext() {
		return configurationService.getPluginConfiguration().get().getAuthenticationContext();
	}

}
