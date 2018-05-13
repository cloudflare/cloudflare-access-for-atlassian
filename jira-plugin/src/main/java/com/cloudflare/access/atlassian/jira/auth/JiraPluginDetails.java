package com.cloudflare.access.atlassian.jira.auth;

import org.springframework.stereotype.Component;

import com.cloudflare.access.atlassian.base.auth.CloudflarePluginDetails;
import com.cloudflare.access.atlassian.base.support.AtlassianApplicationType;

@Component
public class JiraPluginDetails implements CloudflarePluginDetails{

	@Override
	public String getPluginKey() {
		return "com.cloudflare.access.atlassian.jira-plugin";
	}

	@Override
	public AtlassianApplicationType getApplicationType() {
		return AtlassianApplicationType.JIRA;
	}
}
