package com.cloudflare.access.atlassian.jira.auth;

import static org.apache.commons.lang3.StringUtils.*;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.cloudflare.access.atlassian.base.auth.AtlassianProductWhitelistRules;
import com.google.common.collect.Lists;

@Component
public class JiraWhitelistRules implements AtlassianProductWhitelistRules{

	private static final Logger log = LoggerFactory.getLogger(JiraWhitelistRules.class);

	@Override
	public boolean isRequestWhitelisted(HttpServletRequest httpRequest) {
		String uri = httpRequest.getRequestURI();
		List<String> rules = Lists.newArrayList(
				"^.*/rest/gadgets/.*$",
				"^.*(css|woff|ttf)$",
				"^.*/rest/applinks/.*$",
				"^.*/rest/capabilities/.*$"
		);

		return isRestWithOauth(httpRequest) ||
				rules.stream()
					.anyMatch(rule -> this.checkRule(uri, rule));
	}

	private boolean checkRule(String uri, String regex) {
		boolean m = uri.matches(regex);
		log.debug("URI '{}' Matches whitelist '{}' ? {}", new Object[] {uri, regex, m});
		return m;
	}

	private boolean isRestWithOauth(HttpServletRequest httpRequest) {
		String uri = httpRequest.getRequestURI();
		String authHeader = httpRequest.getHeader("authorization");

		return uri.matches("^.*/rest/.*$") && contains(lowerCase(authHeader), "oauth");
	}
}
