package com.cloudflare.access.atlassian.jira.auth;

import org.springframework.stereotype.Component;

import com.cloudflare.access.atlassian.base.auth.CloudflarePluginDetails;

@Component
public class JiraPluginDetails implements CloudflarePluginDetails{

	@Override
	public String getPluginKey() {
		return "com.cloudflare.access.atlassian.jira-plugin";
	}

}
