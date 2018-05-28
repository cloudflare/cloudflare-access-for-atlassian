package com.cloudflare.access.atlassian.bitbucket.auth;

import static com.cloudflare.access.atlassian.bitbucket.auth.BitbucketPluginDetails.KEY_CONTAINER_AUTH_NAME;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.bitbucket.auth.HttpAuthenticationContext;
import com.atlassian.bitbucket.auth.HttpAuthenticationHandler;
import com.atlassian.bitbucket.auth.HttpAuthenticationSuccessContext;
import com.atlassian.bitbucket.auth.HttpAuthenticationSuccessHandler;
import com.atlassian.bitbucket.user.ApplicationUser;
import com.cloudflare.access.atlassian.base.auth.CloudflareAccessService;

@Named("cloudflareAccessAuthenticationHandler")
public class CloudflareAccessAuthenticationHandler implements HttpAuthenticationHandler, HttpAuthenticationSuccessHandler {

	private static final Logger log = LoggerFactory.getLogger(CloudflareAccessAuthenticationHandler.class);

	@Inject
	private CloudflareAccessService cloudflareAccess;

	@Inject
	public CloudflareAccessAuthenticationHandler(CloudflareAccessService cloudflareAccess) {
		this.cloudflareAccess = cloudflareAccess;
	}

	@Override
	public ApplicationUser authenticate(HttpAuthenticationContext httpAuthenticationContext) {
        HttpServletRequest httpRequest = httpAuthenticationContext.getRequest();
        HttpServletResponse httpResponse = httpAuthenticationContext.getResponse();
        FilterChain chain = httpAuthenticationContext.getFilterChain();

        log.info("Attempting authentication........");

		return (ApplicationUser) cloudflareAccess.processAuthRequest(httpRequest, httpResponse, chain);
	}

	@Override
	public void validateAuthentication(HttpAuthenticationContext httpAuthenticationContext) {
        HttpSession session = httpAuthenticationContext.getRequest().getSession(false);
        if (session == null) {
            // nothing to validate - the user wasn't authenticated by this authentication handler
            return;
        }
        log.info("Passing a chance of checking the auth............");
	}

	@Override
	public boolean onAuthenticationSuccess(HttpAuthenticationSuccessContext context) throws ServletException, IOException {
        String authenticationUser = (String) context.getRequest().getAttribute(KEY_CONTAINER_AUTH_NAME);
        if (authenticationUser != null) {
            context.getRequest().getSession().setAttribute(KEY_CONTAINER_AUTH_NAME, authenticationUser);
            return true;
        }

        return false;
	}




}
