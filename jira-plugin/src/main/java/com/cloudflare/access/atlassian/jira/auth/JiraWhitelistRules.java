package com.cloudflare.access.atlassian.jira.auth;

import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.BooleanUtils;

import com.cloudflare.access.atlassian.jira.util.WhitelistRule;

public class JiraWhitelistRules {

	/**
	 * Matches requests to gadgets XML specs
	 */
	private static final WhitelistRule GADGET_SPEC_RULE = (request) -> {
		String uri = request.getRequestURI();
		return uri.matches("^/rest/gadgets/.*xml$");
	};

	private static final List<WhitelistRule> rules = Collections.singletonList(GADGET_SPEC_RULE);


	public static final boolean matchesWhitelist(HttpServletRequest request) {
		return rules.stream()
				.map(rule -> rule.match(request))
				.anyMatch(BooleanUtils::isTrue);
	}

}
