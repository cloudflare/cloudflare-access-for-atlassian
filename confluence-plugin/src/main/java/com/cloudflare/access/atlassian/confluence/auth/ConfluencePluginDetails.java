package com.cloudflare.access.atlassian.confluence.auth;

import org.springframework.stereotype.Component;

import com.cloudflare.access.atlassian.base.auth.CloudflarePluginDetails;
import com.cloudflare.access.atlassian.base.support.AtlassianApplicationType;

@Component
public class ConfluencePluginDetails implements CloudflarePluginDetails{

	@Override
	public String getPluginKey() {
		return "com.cloudflare.access.atlassian.confluence-plugin";
	}

	@Override
	public AtlassianApplicationType getApplicationType() {
		return AtlassianApplicationType.CONFLUENCE;
	}
}
