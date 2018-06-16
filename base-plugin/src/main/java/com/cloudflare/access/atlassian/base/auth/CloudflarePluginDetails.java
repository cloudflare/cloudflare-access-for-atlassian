package com.cloudflare.access.atlassian.base.auth;

import com.cloudflare.access.atlassian.base.support.AtlassianApplicationType;

public interface CloudflarePluginDetails {

	public String getPluginKey();

	public AtlassianApplicationType getApplicationType();
}
