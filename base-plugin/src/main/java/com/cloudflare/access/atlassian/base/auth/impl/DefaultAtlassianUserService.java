package com.cloudflare.access.atlassian.base.auth.impl;

import java.util.Iterator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.SearchRestriction;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.search.builder.Combine;
import com.atlassian.crowd.search.query.entity.UserQuery;
import com.atlassian.crowd.search.query.entity.restriction.MatchMode;
import com.atlassian.crowd.search.query.entity.restriction.TermRestriction;
import com.atlassian.crowd.search.query.entity.restriction.constants.UserTermKeys;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.cloudflare.access.atlassian.base.auth.AtlassianUserService;
import com.cloudflare.access.atlassian.base.config.ConfigurationService;
import com.cloudflare.access.atlassian.base.config.UserMatchingAttribute;
import com.cloudflare.access.atlassian.common.exception.CloudflareAccessUnauthorizedException;
import com.google.common.collect.Iterators;

@Component
public class DefaultAtlassianUserService implements AtlassianUserService{

	private CrowdService crowdService;
	private ConfigurationService configurationService;

	@Autowired
	public DefaultAtlassianUserService(@ComponentImport CrowdService crowdService, ConfigurationService configurationService) {
		this.crowdService = crowdService;
		this.configurationService = configurationService;
	}

	@Override
	public User getUser(String userEmail) {
		UserMatchingAttribute userMatchingAttribute = configurationService.getPluginConfiguration()
				.map(c -> c.getUserMatchingAttribute())
				.orElse(UserMatchingAttribute.defaultAttribute());

		SearchRestriction userCriteria = Combine.allOf(
				new TermRestriction<>(userMatchingAttribute.getUserTerm(), MatchMode.EXACTLY_MATCHES, userEmail),
				new TermRestriction<>(UserTermKeys.ACTIVE, true)
		);
		UserQuery<User> query = new UserQuery<>(User.class, userCriteria, 0, Integer.MAX_VALUE);

		Iterator<User> users = crowdService.search(query).iterator();
		User user  = Iterators.getNext(users, null);

		if(user == null) {
			throw new CloudflareAccessUnauthorizedException(String.format("Cloudflare Access authentication was successful, but it appears that no user profile matches the %s '%s'.", userMatchingAttribute.getDisplayName(), userEmail));
		}

		if(users.hasNext()) {
			throw new CloudflareAccessUnauthorizedException(String.format("Cloudflare Access authentication was successful, but it appears that more than one user profile matches the %s '%s'.", userMatchingAttribute.getDisplayName(), userEmail));
		}

		return user;
	}

}
