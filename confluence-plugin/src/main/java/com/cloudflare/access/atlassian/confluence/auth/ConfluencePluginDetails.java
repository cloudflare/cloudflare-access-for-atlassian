package com.cloudflare.access.atlassian.confluence.auth;

import org.springframework.stereotype.Component;

import com.cloudflare.access.atlassian.base.auth.CloudflarePluginDetails;

@Component
public class ConfluencePluginDetails implements CloudflarePluginDetails{

	@Override
	public String getPluginKey() {
		return "com.cloudflare.access.atlassian.confluence-plugin";
	}

}
