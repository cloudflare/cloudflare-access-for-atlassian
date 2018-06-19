package com.cloudflare.access.atlassian.base.utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.seraph.auth.DefaultAuthenticator;

public abstract class SessionUtils {
	private static final Logger log = LoggerFactory.getLogger(SessionUtils.class);


	public static void clearSession(HttpServletRequest request) {
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
	}

}
