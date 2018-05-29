package com.cloudflare.access.atlassian.bitbucket.auth;

import org.springframework.stereotype.Component;

import com.cloudflare.access.atlassian.base.auth.CloudflarePluginDetails;

@Component
public class BitbucketPluginDetails implements CloudflarePluginDetails{

	public static final String AUTHENTICATED_USER_NAME_ATTRIBUTE = "auth.cloudflare-access-user";

	@Override
	public String getPluginKey() {
		return "com.cloudflare.access.atlassian.bitbucket-plugin";
	}

}
