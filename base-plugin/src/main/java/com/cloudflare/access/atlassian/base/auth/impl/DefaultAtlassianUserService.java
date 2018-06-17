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
import com.cloudflare.access.atlassian.common.exception.CloudflareAccessUnauthorizedException;
import com.google.common.collect.Iterators;

@Component
public class DefaultAtlassianUserService implements AtlassianUserService{

	private CrowdService crowdService;

	@Autowired
	public DefaultAtlassianUserService(@ComponentImport CrowdService crowdService) {
		this.crowdService = crowdService;
	}

	@Override
	public User getUser(String userEmail) {
		SearchRestriction userCriteria = Combine.allOf(
				new TermRestriction<>(UserTermKeys.EMAIL, MatchMode.EXACTLY_MATCHES, userEmail),
				new TermRestriction<>(UserTermKeys.ACTIVE, true)
		);
		UserQuery<User> query = new UserQuery<>(User.class, userCriteria, 0, Integer.MAX_VALUE);

		Iterator<User> users = crowdService.search(query).iterator();
		User user  = Iterators.getNext(users, null);

		if(user == null) {
			throw new CloudflareAccessUnauthorizedException(String.format("No user matching '%s'", userEmail));
		}

		if(users.hasNext()) {
			throw new CloudflareAccessUnauthorizedException(String.format("More than one user matching '%s'", userEmail));
		}

		return user;
	}


}
