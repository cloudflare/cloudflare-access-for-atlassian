package com.cloudflare.access.atlassian.base.config;

import com.atlassian.crowd.search.query.entity.restriction.Property;
import com.atlassian.crowd.search.query.entity.restriction.constants.UserTermKeys;

public enum UserMatchingAttribute {
	EMAIL(UserTermKeys.EMAIL, "Email address"),
	USERNAME(UserTermKeys.USERNAME, "Username");

	private Property<String> userTerm;
	private String displayName;

	private UserMatchingAttribute(Property<String> userTerm, String displayName) {
		this.userTerm = userTerm;
		this.displayName = displayName;
	}

	public Property<String> getUserTerm() {
		return userTerm;
	}

	public String getDisplayName() {
		return displayName;
	}

	public static final UserMatchingAttribute defaultAttribute() {
		return EMAIL;
	}
}
