package com.cloudflare.access.atlassian.jira.auth;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.BooleanUtils;

import com.cloudflare.access.atlassian.jira.util.WhitelistRule;
import com.google.common.collect.Lists;

public class JiraWhitelistRules {

	/**
	 * Matches requests to gadgets specs and bundles
	 *
	 * Example: /jira/rest/gadgets/1.0/g/feed
	 *
	 */
	private static final WhitelistRule GADGETS = (request) -> {
		String uri = request.getRequestURI();
		return uri.matches("^.*/rest/gadgets/.*$");
	};

	/**
	 * Matches requests to CSS files
	 *
	 */
	private static final WhitelistRule CSS = (request) -> {
		String uri = request.getRequestURI();
		return uri.matches("^.*css$");
	};

	private static final List<WhitelistRule> rules = Lists.newArrayList(GADGETS, CSS);


	public static final boolean matchesWhitelist(HttpServletRequest request) {
		return rules.stream()
				.map(rule -> rule.match(request))
				.anyMatch(BooleanUtils::isTrue);
	}

}
