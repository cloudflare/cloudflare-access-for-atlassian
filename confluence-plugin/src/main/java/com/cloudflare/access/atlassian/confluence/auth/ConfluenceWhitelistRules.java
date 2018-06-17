package com.cloudflare.access.atlassian.confluence.auth;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

import com.cloudflare.access.atlassian.base.auth.AtlassianProductWhitelistRules;
import com.cloudflare.access.atlassian.base.auth.AuthenticationErrorServlet;
import com.google.common.collect.Lists;

@Component
public class ConfluenceWhitelistRules implements AtlassianProductWhitelistRules{

	@Override
	public boolean isRequestWhitelisted(HttpServletRequest httpRequest) {
		String uri = httpRequest.getRequestURI();
		List<String> rules = Lists.newArrayList(
				"^.*"+AuthenticationErrorServlet.PATH+"$",
				"^.*(css|woff|ttf|svg|png|gif|jpg|jpeg)$",
				"^.*/rest/analytics/1.0/publish/bulk$", /*Prevent leaking cookies as this is a usage tracking and should not attempt auth*/
				"^.*/rest/gadgets/.*$",
				"^.*/rpc/xmlrpc.*$",  					/*Related to applinks*/
				"^.*/rest/jira-metadata/.*$"			/*Related with jira link*/
		);

		//JIRA may use many url patterns with oauth authorization
		return isOauthAuthorizationHeaderPresent(httpRequest) ||
				isApplicationLinkRelated(httpRequest) ||
				rules.stream()
				.anyMatch(rule -> this.checkRule(uri, rule));
	}

}
