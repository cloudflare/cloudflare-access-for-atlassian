package com.cloudflare.access.atlassian.jira.auth;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.cloudflare.access.atlassian.base.auth.FailedAuthenticationRequestHandler;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

@Component
public class JiraFailedAuthenticationRequestHandler implements FailedAuthenticationRequestHandler{

	static final String CF_PLUGIN_REQUEST_IDENTIFIER_PARAM = "cfPluginRequestIdentifier";
	private static final Logger log = LoggerFactory.getLogger(JiraFailedAuthenticationRequestHandler.class);
	private static final Cache<String, Boolean> REDIRECT_CONTROL = CacheBuilder.newBuilder()
			.concurrencyLevel(4)
			.maximumSize(10000)
			.expireAfterWrite(1, TimeUnit.MINUTES)
			.build();

	private RememberMeHelperService rememberMeService;

	@Inject
	public JiraFailedAuthenticationRequestHandler(RememberMeHelperService rememberMeService) {
		this.rememberMeService = rememberMeService;
	}

	@Override
	public void handle(HttpServletRequest httpRequest, HttpServletResponse httpResponse, Throwable e) {
		try {
			String requestIdentifier = getRequestIdentifier(httpRequest);

			if(acceptsHtml(httpRequest) && shouldSendCookieCleanupRedirect(httpRequest, requestIdentifier)) {
				httpResponse.sendRedirect(String.format("%s?%s=%s",httpRequest.getRequestURI(), CF_PLUGIN_REQUEST_IDENTIFIER_PARAM, requestIdentifier));
			}else {
				rememberMeService.removeRememberMeCookie(httpRequest, httpResponse);
				sendErrorResponse(httpRequest, httpResponse, e);
			}

		}catch (Exception e2) {
			log.error("Unable to send 401 error with message " + e.getMessage(), e2);
			throw new RuntimeException(e2);
		}
	}

	/**
	 * Indicates wheter this request should be answered with a 401 or with a
	 * 302 that subsequently will be answered with a 401.
	 *
	 * @param request The request to check
	 * @return True if should redirect, false if it should send error
	 */
	private boolean shouldSendCookieCleanupRedirect(HttpServletRequest request, String requestIdentifier) {
		try {
			if(isBlank(requestIdentifier)) {
				return false;
			}

			boolean shouldRedirect = REDIRECT_CONTROL.get(requestIdentifier, () -> true);

			if(shouldRedirect == false) {
				REDIRECT_CONTROL.invalidate(requestIdentifier);
			}else {
				REDIRECT_CONTROL.put(requestIdentifier, false);
			}

			return shouldRedirect;
		}catch (Exception e) {
			log.error("This was not expected to ever happen, ignoring exception: " + e.getMessage(), e);
			return false;
		}
	}

	private String getRequestIdentifier(HttpServletRequest request) {
		if(request.getCookies() == null) return null;

		Optional<Cookie> jsessionid = Arrays.stream(request.getCookies()).filter(cookie -> cookie.getName().equalsIgnoreCase("JSESSIONID")).findFirst();
		String requestIdentifier = jsessionid.map(Cookie::getValue).orElseGet(() -> request.getParameter(CF_PLUGIN_REQUEST_IDENTIFIER_PARAM));
		return requestIdentifier;
	}
}
