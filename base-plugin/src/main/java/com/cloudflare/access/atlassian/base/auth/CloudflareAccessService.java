package com.cloudflare.access.atlassian.base.auth;

import static com.cloudflare.access.atlassian.base.utils.SessionUtils.clearSession;
import static com.cloudflare.access.atlassian.base.utils.SessionUtils.isAtlassianFlowSession;
import static com.cloudflare.access.atlassian.base.utils.SessionUtils.sessionAlreadyContainsAuthenticatedUser;
import static com.cloudflare.access.atlassian.base.utils.SessionUtils.storeUserEmailInSession;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.substringAfterLast;

import java.io.IOException;
import java.util.Set;

import javax.inject.Inject;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.cloudflare.access.atlassian.base.config.ConfigurationService;
import com.cloudflare.access.atlassian.base.support.PluginStateService;
import com.cloudflare.access.atlassian.base.utils.EnvironmentFlags;
import com.cloudflare.access.atlassian.base.utils.RequestInspector;
import com.cloudflare.access.atlassian.base.utils.SessionUtils;
import com.cloudflare.access.atlassian.common.context.AuthenticationContext;
import com.cloudflare.access.atlassian.common.exception.CloudflareAccessUnauthorizedException;
import com.google.common.collect.Sets;

@Component
public class CloudflareAccessService {

	private static final String AUTHORIZATION_IC_HEADER_NAME = "authorization";

	private static final Set<String> STATIC_RESOURCE_EXTENSIONS =  Sets.newHashSet(
			"js", "css", "png", "jpg", "jpeg", "woff", "ttf"
	);

	private static final Logger log = LoggerFactory.getLogger(CloudflareAccessService.class);

	private PluginAccessor pluginAcessor;
	private CloudflarePluginDetails pluginDetails;
	private AtlassianUserService userService;
	private SuccessfulAuthenticationRequestHandler successHandler;
	private FailedAuthenticationRequestHandler failureHandler;
	private ConfigurationService configurationService;
	private PluginStateService pluginStateService;
	private final boolean filteringDisabled;

	@Inject
	public CloudflareAccessService(@ComponentImport PluginAccessor pluginAcessor,
									CloudflarePluginDetails pluginDetails,
									ConfigurationService configurationService,
									AtlassianUserService userService,
									SuccessfulAuthenticationRequestHandler successHandler,
									FailedAuthenticationRequestHandler failureHandler,
									Environment env,
									PluginStateService pluginStateService) {
		this.pluginAcessor = pluginAcessor;
		this.pluginDetails = pluginDetails;
		this.configurationService = configurationService;
		this.userService = userService;
		this.successHandler = successHandler;
		this.failureHandler = failureHandler;
		this.filteringDisabled = EnvironmentFlags.isFiltersDisabled(env);
		this.pluginStateService = pluginStateService;
	}

	public void processAuthRequest(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
		try {
			if(isRequestFilteringDisabled()) {
				log.debug("Plugin filters are disabled or not configured yet, bypassing auth process");
				chain.doFilter(request, response);
				return;
			}

			if(isAtlassianFlowEnabled(request)) {
				log.debug("Atlassian flow is enabled, bypassing");
				chain.doFilter(request, response);
				return;
			}

			if(isWhitelisted(request)) {
				log.debug("Request is whitelisted, bypassing");
				chain.doFilter(request, response);
				return;
			}

			CloudflareToken token = getValidTokenFromRequest(request);
			if(token.isNotPresent()) {
				log.debug("JWT token not present, bypassing auth process: {}", request.getRequestURI());
				chain.doFilter(request, response);
				return;
			}

			if(sessionAlreadyContainsAuthenticatedUser(request, token.getUserEmail())) {
				log.debug("Session already contains user {} , skipping success handler: {}", token.getUserEmail(), request.getRequestURI());
				chain.doFilter(request, response);
				return;
			}

			if(anyAuthorizationHeaderIsPresent(request)) {
				log.debug("Authorization header is present skipping user matching...");
				chain.doFilter(request, response);
				return;
			}

			if(requiresAtlassianAuthentication(token)) {
				log.debug("User required to do atlassian authentication...");
				chain.doFilter(request, response);
				return;
			}

			User user = userService.getUser(token.getUserEmail());
			successHandler.handle(request, response, chain, user);

			storeUserEmailInSession(request, token.getUserEmail());
		}catch (CloudflareAccessUnauthorizedException e) {
			log.error("Error processing authentication: " + e.getMessage(), e);
			log.debug(RequestInspector.getRequestedResourceInfo(request));
			log.debug(RequestInspector.getHeadersAndCookies(request));
			SessionUtils.clearSession(request);
			failureHandler.handle(request, response, e);
		}
	}

	public void processLogoutRequest(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
		if(isRequestFilteringDisabled()) {
			log.debug("Plugin filters are disabled or no configured yet, bypassing logout redirect");
			chain.doFilter(request, response);
			return;
		}

		if(isAtlassianFlowEnabled(request)) {
			log.debug("Atlassian flow is enabled, bypassing");
			chain.doFilter(request, response);
			return;
		}

		if(new CloudflareToken(request).isNotPresent()) {
			log.debug("No token present, bypassing logout redirect");
			chain.doFilter(request, response);
			return;
		}

		clearSession(request);

		AuthenticationContext authContext = getAuthContext();
		log.debug("Redirecting user to cloudflare logout at " + authContext.getLogoutUrl());
		response.sendRedirect(authContext.getLogoutUrl());
	}

	private boolean isRequestFilteringDisabled() {
		return isPluginDisabled() || filteringDisabled || (isPluginConfigured() == false);
	}

	private boolean isAtlassianFlowEnabled(HttpServletRequest request) {
		return isAtlassianFlowSession(request);
	}

	private boolean isPluginDisabled() {
		return pluginAcessor.isPluginEnabled(pluginDetails.getPluginKey()) == false;
	}

	private CloudflareToken getValidTokenFromRequest(HttpServletRequest request) {
		return new CloudflareToken(request, getAuthContext());
	}

	private boolean isWhitelisted(HttpServletRequest request) {
		final String requestPath = defaultIfBlank(request.getRequestURI(), "");
		return isErrorPath(requestPath) || isStaticResource(requestPath);
	}

	private boolean isErrorPath(String requestPath) {
		return requestPath.endsWith(AuthenticationErrorServlet.PATH);
	}

	private boolean isStaticResource(String requestPath) {
		String ext = substringAfterLast(requestPath, ".").toLowerCase();
		return STATIC_RESOURCE_EXTENSIONS.contains(ext);
	}

	private boolean isPluginConfigured() {
		return pluginStateService.isReady() && configurationService.getPluginConfiguration().isPresent();
	}

	private AuthenticationContext getAuthContext() {
		return configurationService.getPluginConfiguration().get().getAuthenticationContext();
	}

	private boolean anyAuthorizationHeaderIsPresent(HttpServletRequest request) {
		String authHeaderValue = request.getHeader(AUTHORIZATION_IC_HEADER_NAME);
		if(isNotBlank(authHeaderValue)) {
			log.debug("Auth header found: " + authHeaderValue);
			return true;
		}
		return false;
	}

	private boolean requiresAtlassianAuthentication(CloudflareToken token) {
		return configurationService.emailDomainRequiresAtlassianAuthentication(token.getUserEmail());
	}

}
