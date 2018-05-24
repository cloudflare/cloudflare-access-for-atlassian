package com.cloudflare.access.atlassian.base.auth;

import javax.servlet.http.HttpServletRequest;

public interface AtlassianProductWhitelistRules {

	boolean isRequestWhitelisted(HttpServletRequest request);

}
