package com.cloudflare.access.atlassian.bitbucket.auth;

import org.springframework.stereotype.Component;

import com.cloudflare.access.atlassian.base.auth.CloudflarePluginDetails;
import com.cloudflare.access.atlassian.base.support.AtlassianApplicationType;

@Component
public class BitbucketPluginDetails implements CloudflarePluginDetails{

	public static final String AUTHENTICATED_USER_NAME_ATTRIBUTE = "auth.cloudflare-access-user";
	public static final String WHITELISTED_REQUEST_FLAG_ATTRIBUTE = "auth.whitelisted";

	@Override
	public String getPluginKey() {
		return "com.cloudflare.access.atlassian.bitbucket-plugin";
	}

	@Override
	public AtlassianApplicationType getApplicationType() {
		return AtlassianApplicationType.BITBUCKET;
	}
}
