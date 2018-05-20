package com.cloudflare.access.atlassian.jira.auth;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.BooleanUtils;

import com.atlassian.extras.common.log.Logger;
import com.atlassian.extras.common.log.Logger.Log;
import com.cloudflare.access.atlassian.jira.util.WhitelistRule;
import com.google.common.collect.Lists;

public class JiraWhitelistRules {

	private static final Log log = Logger.getInstance(JiraWhitelistRules.class);

	/**
	 * Matches requests to gadgets specs and bundles
	 *
	 * Example: /jira/rest/gadgets/1.0/g/feed
	 *
	 */
	private static final WhitelistRule GADGETS = (request) -> {
		String uri = request.getRequestURI();
		boolean matches = uri.matches("^.*/rest/gadgets/.*$");
		log.debug(String.format("Whitelist '%s'? %s", uri, matches));
		return matches;
	};

	/**
	 * Matches requests to CSS files
	 *
	 */
	private static final WhitelistRule CSS = (request) -> {
		String uri = request.getRequestURI();
		boolean matches = uri.matches("^.*css$");
		log.debug(String.format("Whitelist '%s'? %s", uri, matches));
		return matches;
	};

	private static final List<WhitelistRule> rules = Lists.newArrayList(GADGETS, CSS);


	public static final boolean matchesWhitelist(HttpServletRequest request) {
		return rules.stream()
				.map(rule -> rule.match(request))
				.anyMatch(BooleanUtils::isTrue);
	}

}
