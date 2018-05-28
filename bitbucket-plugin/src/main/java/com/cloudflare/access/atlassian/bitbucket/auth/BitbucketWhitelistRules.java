package com.cloudflare.access.atlassian.bitbucket.auth;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.cloudflare.access.atlassian.base.auth.AtlassianProductWhitelistRules;
import com.google.common.collect.Lists;

@Component
public class BitbucketWhitelistRules implements AtlassianProductWhitelistRules{

	private static final Logger log = LoggerFactory.getLogger(BitbucketWhitelistRules.class);

	@Override
	public boolean isRequestWhitelisted(HttpServletRequest httpRequest) {
		String uri = httpRequest.getRequestURI();
		List<String> rules = Lists.newArrayList(
				"^.*/rest/gadgets/.*$",
				"^.*(css|woff|ttf)$"
		);
		return rules
				.stream()
				.anyMatch(rule -> this.checkRule(uri, rule));
	}

	private boolean checkRule(String uri, String regex) {
		boolean m = uri.matches(regex);
		log.debug("URI '{}' Matches whitelist '{}' ? {}", new Object[] {uri, regex, m});
		return m;
	}


}
