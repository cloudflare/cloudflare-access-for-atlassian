package com.cloudflare.access.atlassian.confluence.auth;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudflare.access.atlassian.confluence.util.WhitelistRule;
import com.google.common.collect.Lists;

public class ConfluenceWhitelistRules {

	private static final Logger log = LoggerFactory.getLogger(ConfluenceWhitelistRules.class);

	/**
	 * Matches forward requests to AuthenticationErrorServlet.
	 *
	 */
	private static final WhitelistRule AUTH_ERROR_SERVLET = (request) -> {
		String uri = request.getRequestURI();
		boolean matches = uri.matches("^.*"+AuthenticationErrorServlet.PATH+"$");
		log.debug("Whitelist '{}'? {}", uri, matches);
		return matches;
	};

	/**
	 * Matches requests to static elements
	 *
	 */
	private static final WhitelistRule STATIC_ELEMENTS = (request) -> {
		String uri = request.getRequestURI();
		boolean matches = uri.matches("^.*(css|woff|ttf|svg|png|gif|jpg|jpeg)$");
		log.debug(String.format("Whitelist '%s'? %s", uri, matches));
		return matches;
	};

	private static final List<WhitelistRule> rules = Lists.newArrayList(AUTH_ERROR_SERVLET, STATIC_ELEMENTS);


	public static final boolean matchesWhitelist(HttpServletRequest request) {
		return rules.stream()
				.map(rule -> rule.match(request))
				.anyMatch(BooleanUtils::isTrue);
	}

}
