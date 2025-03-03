package com.cloudflare.access.atlassian.base.utils;

import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SessionUtils {
	private static final Logger log = LoggerFactory.getLogger(SessionUtils.class);

	public static final String ATLASSIAN_FLOW_FLAG = "ATLASSIAN_FLOW_FLAG";
	private static final String CF_USER_EMAIL = "CF_USER_EMAIL";
	private static final String CLOUDFLARE_UPDATE_NOTIFIED_FLAG = "CLOUDFLARE_UPDATE_NOTIFIED";

	public static void clearSession(HttpServletRequest request) {
		final HttpSession httpSession = request.getSession(false);
		if(httpSession != null) {
			try {
				httpSession.invalidate();
			}catch (IllegalStateException e) {
				log.debug("Session was already invalid");
			}
		}
	}

	public static void enableAtlassianFlowSession(HttpServletRequest request) {
		log.debug("adding atlassian flow flag in current session");
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

	public static void removeAtlassianFlowFlagFromSession(HttpServletRequest request) {
		log.debug("removing atlassian flow flag from current session");
		final HttpSession httpSession = request.getSession(false);
		httpSession.removeAttribute(ATLASSIAN_FLOW_FLAG);
	}

	public static boolean sessionAlreadyContainsAuthenticatedUser(HttpServletRequest request, String email) {
		HttpSession session = request.getSession(false);
		if(session != null) {
			return Objects.equals(session.getAttribute(CF_USER_EMAIL), email);
		}
		return false;
	}

	public static void storeUserEmailInSession(HttpServletRequest request, String email) {
		HttpSession session = request.getSession(false);
		if(session != null) {
			session.setAttribute(CF_USER_EMAIL, email);
		}
	}

	public static boolean isUpdateCheckAlreadyNotified(HttpServletRequest req) {
		HttpSession session = req.getSession(false);
		if(session != null) {
			return Objects.equals(true, session.getAttribute(CLOUDFLARE_UPDATE_NOTIFIED_FLAG));
		}
		return false;
	}

	public static void markUpdateCheckNotified(HttpServletRequest req) {
		HttpSession session = req.getSession(false);
		if(session != null) {
			session.setAttribute(CLOUDFLARE_UPDATE_NOTIFIED_FLAG, true);
		}
	}


}
