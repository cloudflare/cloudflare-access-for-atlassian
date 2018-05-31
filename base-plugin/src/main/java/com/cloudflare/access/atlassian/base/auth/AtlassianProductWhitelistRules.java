package com.cloudflare.access.atlassian.base.auth;

import static org.apache.commons.lang3.StringUtils.*;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public interface AtlassianProductWhitelistRules {

	static final Logger log = LoggerFactory.getLogger(AtlassianProductWhitelistRules.class);

	boolean isRequestWhitelisted(HttpServletRequest request);

	default boolean isRestWithOauth(HttpServletRequest httpRequest) {
		String uri = httpRequest.getRequestURI();
		String authHeader = httpRequest.getHeader("authorization");

		return uri.matches("^.*/rest/.*$") && contains(lowerCase(authHeader), "oauth");
	}

	default boolean isApplicationLinkRelated(HttpServletRequest httpRequest) {
		String uri = httpRequest.getRequestURI();
		List<String> rules = Lists.newArrayList(
				"^.*/rest/applinks.*$",
				"^.*/rest/capabilities.*$",
				"^.*/servlet/capabilities$",  			/*'rest/capabilities' redirects to here*/
				"^.*/servlet/oauth/.*$"
		);

		return rules.stream()
					.anyMatch(rule -> this.checkRule(uri, rule));
	}

	default boolean checkRule(String uri, String regex) {
		boolean m = uri.matches(regex);
		log.debug("URI '{}' Matches whitelist '{}' ? {}", new Object[] {uri, regex, m});
		System.out.println(String.format("URI '%s' Matches whitelist '%s' ? %s", uri, regex, m));
		return m;
	}
}
