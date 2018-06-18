package com.cloudflare.access.atlassian.jira.auth;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

import com.cloudflare.access.atlassian.base.auth.AtlassianProductWhitelistRules;
import com.cloudflare.access.atlassian.base.auth.AuthenticationErrorServlet;
import com.google.common.collect.Lists;

@Component
public class JiraWhitelistRules implements AtlassianProductWhitelistRules{

	@Override
	public boolean isRequestWhitelisted(HttpServletRequest httpRequest) {
		String uri = httpRequest.getRequestURI();
		List<String> rules = Lists.newArrayList(
				"^.*"+AuthenticationErrorServlet.PATH+"$",
				"^.*/rest/gadgets/.*$",
				"^.*(css|png|svg|woff|ttf)$"
		);

		return isOauthAuthorizationHeaderPresent(httpRequest) ||
				isApplicationLinkRelated(httpRequest) ||
				rules.stream()
					.anyMatch(rule -> this.checkRule(uri, rule));
	}

}
