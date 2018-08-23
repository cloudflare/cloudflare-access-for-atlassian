package com.cloudflare.access.atlassian.base.utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.seraph.auth.DefaultAuthenticator;

public abstract class SessionUtils {
	private static final Logger log = LoggerFactory.getLogger(SessionUtils.class);

	private static final String ATLASSIAN_FLOW_FLAG = "ATLASSIAN_FLOW_FLAG";

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

	public static void enableAtlassianFlowSession(HttpServletRequest request) {
		final HttpSession httpSession = request.getSession();
		httpSession.setAttribute(ATLASSIAN_FLOW_FLAG, true);
	}

	public static boolean isAtlassianFlowSession(HttpServletRequest request) {
		final HttpSession httpSession = request.getSession(false);
		if(httpSession != null) {
			return BooleanUtils.isTrue((Boolean) httpSession.getAttribute(ATLASSIAN_FLOW_FLAG));
		}
		return false;
	}

}
