package com.cloudflare.access.atlassian.base.auth;

import static org.apache.commons.lang3.StringUtils.*;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

@Deprecated()
public interface AtlassianProductWhitelistRules {

	static final Logger log = LoggerFactory.getLogger(AtlassianProductWhitelistRules.class);

	boolean isRequestWhitelisted(HttpServletRequest request);

	default boolean isOauthAuthorizationHeaderPresent(HttpServletRequest httpRequest) {
		String authHeader = httpRequest.getHeader("authorization");
		boolean containsOauth = contains(lowerCase(authHeader), "oauth");
		log.debug("Request contains oauth ? {}", containsOauth);
		if(containsOauth) {
			log.debug("Oauth header: {}", authHeader);
		}
		return containsOauth;
	}

	default boolean isApplicationLinkRelated(HttpServletRequest httpRequest) {
		String uri = httpRequest.getRequestURI();
		List<String> rules = Lists.newArrayList(
				"^.*/rest/applinks.*$",
				"^.*/rest/capabilities.*$",
				"^.*/servlet/capabilities.*$",  			/*'rest/capabilities' redirects to here*/
				"^.*/servlet/oauth/.*$",
				"^.*/rest/remote-link-aggregation.*$",
				"^.*/servlet/remote-link-aggregation.*$"
		);

		return rules.stream()
					.anyMatch(rule -> this.checkRule(uri, rule));
	}

	default boolean checkRule(String uri, String regex) {
		boolean m = uri.matches(regex);
		log.debug("URI '{}' Matches whitelist '{}' ? {}", new Object[] {uri, regex, m});
		return m;
	}
}
