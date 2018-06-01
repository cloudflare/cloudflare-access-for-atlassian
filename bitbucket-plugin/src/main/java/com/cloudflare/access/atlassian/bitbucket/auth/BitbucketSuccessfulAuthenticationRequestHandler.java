package com.cloudflare.access.atlassian.bitbucket.auth;

import static com.cloudflare.access.atlassian.bitbucket.auth.BitbucketPluginDetails.AUTHENTICATED_USER_NAME_ATTRIBUTE;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.atlassian.crowd.embedded.api.User;
import com.cloudflare.access.atlassian.base.auth.SuccessfulAuthenticationRequestHandler;

@Component
public class BitbucketSuccessfulAuthenticationRequestHandler implements SuccessfulAuthenticationRequestHandler{

	private static final Logger log = LoggerFactory.getLogger(BitbucketSuccessfulAuthenticationRequestHandler.class);

	@Override
	public void handle(HttpServletRequest httpRequest, HttpServletResponse httpResponse, FilterChain chain, User user) {
		log.info("Handling successful authentication for user {}", user.getName());
		httpRequest.setAttribute(AUTHENTICATED_USER_NAME_ATTRIBUTE, user.getName());
		try {
			chain.doFilter(httpRequest, httpResponse);
		} catch (IOException | ServletException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

}
