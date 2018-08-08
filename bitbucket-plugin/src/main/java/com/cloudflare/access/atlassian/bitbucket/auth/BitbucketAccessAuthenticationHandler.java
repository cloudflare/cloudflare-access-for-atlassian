package com.cloudflare.access.atlassian.bitbucket.auth;

import static com.cloudflare.access.atlassian.bitbucket.auth.BitbucketPluginDetails.*;

import java.io.IOException;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.bitbucket.auth.HttpAuthenticationContext;
import com.atlassian.bitbucket.auth.HttpAuthenticationHandler;
import com.atlassian.bitbucket.auth.HttpAuthenticationSuccessContext;
import com.atlassian.bitbucket.auth.HttpAuthenticationSuccessHandler;
import com.atlassian.bitbucket.user.ApplicationUser;
import com.atlassian.bitbucket.user.UserService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;

@Named("cloudflareAccessAuthenticationHandler")
public class BitbucketAccessAuthenticationHandler implements HttpAuthenticationHandler, HttpAuthenticationSuccessHandler {

	private static final Logger log = LoggerFactory.getLogger(BitbucketAccessAuthenticationHandler.class);

	private UserService userService;

	@Inject
	public BitbucketAccessAuthenticationHandler(@ComponentImport UserService userService) {
		this.userService = userService;
	}

	@Override
	public ApplicationUser authenticate(HttpAuthenticationContext httpAuthenticationContext) {
        HttpServletRequest httpRequest = httpAuthenticationContext.getRequest();

        String userName = (String) httpRequest.getAttribute(AUTHENTICATED_USER_NAME_ATTRIBUTE);
        log.debug("Attempting authentication, username: {}", userName);
        if(userName == null) {
        	log.debug("No username in the request, opt-out auth");
        	return null;
        }
        log.debug("Loading user from user sevice...");
		ApplicationUser user = userService.getUserByName(userName);
		log.debug("User: " + user);
		return user;
	}

	@Override
	public void validateAuthentication(HttpAuthenticationContext httpAuthenticationContext) {
		log.debug("Passing auth validation...");
		HttpServletRequest httpRequest = httpAuthenticationContext.getRequest();
		HttpSession session = httpRequest.getSession(false);
        if (session == null) {
        	log.debug("No session to validate, all fine...");
            return;
        }


        Boolean isWhitelisted = (Boolean) httpRequest.getAttribute(WHITELISTED_REQUEST_FLAG_ATTRIBUTE);
        if(isWhitelisted != null && isWhitelisted) {
        	log.debug("Request is whitelisted, all fine...");
            return;
        }

        String requestUserName = (String) httpRequest.getAttribute(AUTHENTICATED_USER_NAME_ATTRIBUTE);
        String sessionUserName = (String) session.getAttribute(AUTHENTICATED_USER_NAME_ATTRIBUTE);

        if(requestUserName == null) {
        	log.debug("Request does not contains a username, moving on...");
        	return;
        }

        if(sessionUserName != null && !Objects.equals(sessionUserName, requestUserName)) {
        	log.debug("Request user does not match session user...");
        	throw new ExpiredAuthException(sessionUserName, requestUserName);
        }

        log.debug("Auth is valid");
	}

	/**
	 * @return False as we do not complete the request handling.
	 */
	@Override
	public boolean onAuthenticationSuccess(HttpAuthenticationSuccessContext context) throws ServletException, IOException {
        String authenticatedUserName = (String) context.getRequest().getAttribute(AUTHENTICATED_USER_NAME_ATTRIBUTE);
        if (authenticatedUserName != null) {
        	log.debug("Found value for attribute '{}', successful auth, lets store it in the session!", AUTHENTICATED_USER_NAME_ATTRIBUTE);
            context.getRequest().getSession().setAttribute(AUTHENTICATED_USER_NAME_ATTRIBUTE, authenticatedUserName);
        }
        log.debug("No '{}' attribute in the request, unsucessful auth!", AUTHENTICATED_USER_NAME_ATTRIBUTE);
        return false;
	}



	public static class ExpiredAuthException extends RuntimeException{

		private static final long serialVersionUID = -3844887726771335053L;
		public ExpiredAuthException(String sessionUsername, String requestUsername) {
			super(String.format("Session username '%s' does not match request username '%s'!", sessionUsername, requestUsername));
		}

	}
}
