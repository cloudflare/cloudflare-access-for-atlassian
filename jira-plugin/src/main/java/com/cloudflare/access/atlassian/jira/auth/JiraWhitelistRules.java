package com.cloudflare.access.atlassian.jira.auth;

import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.BooleanUtils;

import com.cloudflare.access.atlassian.jira.util.WhitelistRule;

public class JiraWhitelistRules {

	/**
	 * Matches requests to gadgets specs and bundles
	 *
	 * Example: /jira/rest/gadgets/1.0/g/feed
	 *
	 */
	private static final WhitelistRule GADGETS = (request) -> {
		String uri = request.getRequestURI();
		System.out.println("Matching uri: "+uri);
		return uri.matches("^.*/rest/gadgets/.*$");
	};

	private static final List<WhitelistRule> rules = Collections.singletonList(GADGETS);


	public static final boolean matchesWhitelist(HttpServletRequest request) {
		return rules.stream()
				.map(rule -> rule.match(request))
				.anyMatch(BooleanUtils::isTrue);
	}

}
