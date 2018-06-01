package com.cloudflare.access.atlassian.confluence.auth;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.cloudflare.access.atlassian.base.auth.AtlassianProductWhitelistRules;
import com.google.common.collect.Lists;

@Component
public class ConfluenceWhitelistRules implements AtlassianProductWhitelistRules{

	private static final Logger log = LoggerFactory.getLogger(ConfluenceWhitelistRules.class);


	@Override
	public boolean isRequestWhitelisted(HttpServletRequest httpRequest) {
		String uri = httpRequest.getRequestURI();
		List<String> rules = Lists.newArrayList(
				"^.*"+AuthenticationErrorServlet.PATH+"$",
				"^.*(css|woff|ttf|svg|png|gif|jpg|jpeg)$",
				"^.*/rest/analytics/1.0/publish/bulk$", /*Prevent leaking cookies as this is a usage tracking and should not attempt auth*/
				"^.*/rest/gadgets/.*$"
		);
		return rules
				.stream()
				.anyMatch(rule -> this.checkRule(uri, rule));
	}

}
