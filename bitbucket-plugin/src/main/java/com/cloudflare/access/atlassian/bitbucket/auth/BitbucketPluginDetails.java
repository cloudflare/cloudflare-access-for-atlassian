package com.cloudflare.access.atlassian.bitbucket.auth;

import org.springframework.stereotype.Component;

import com.cloudflare.access.atlassian.base.auth.CloudflarePluginDetails;

@Component
public class BitbucketPluginDetails implements CloudflarePluginDetails{

	public static final String KEY_CONTAINER_AUTH_NAME = "auth.container.cloudflare-access-user";

	@Override
	public String getPluginKey() {
		return "com.cloudflare.access.atlassian.bitbucket-plugin";
	}

}
