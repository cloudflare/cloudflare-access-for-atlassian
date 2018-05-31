package com.cloudflare.access.atlassian.bitbucket.auth;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

import com.cloudflare.access.atlassian.base.auth.AtlassianProductWhitelistRules;
import com.google.common.collect.Lists;

@Component
public class BitbucketWhitelistRules implements AtlassianProductWhitelistRules{

	@Override
	public boolean isRequestWhitelisted(HttpServletRequest httpRequest) {
		String uri = httpRequest.getRequestURI();
		List<String> rules = Lists.newArrayList(
				"^.*/rest/gadgets/.*$",
				"^.*\\.(css|png|woff|ttf)$",
				"^.*/rest/analytics/1.0/publish/bulk$", /*Prevent leaking cookies as this is a usage tracking and should not attempt auth*/
				"^.*/rest/api/latest/logs/.*$",
				"^.*/scm/.*$"
		);
		return isRestWithOauth(httpRequest) ||
				isApplicationLinkRelated(httpRequest) ||
				rules.stream()
					.anyMatch(rule -> this.checkRule(uri, rule));
	}

}
