package com.cloudflare.access.atlassian.confluence.auth;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

import com.cloudflare.access.atlassian.common.context.AuthenticationContext;
import com.cloudflare.access.atlassian.common.context.EnvironmentAuthenticationContext;

@Component
public class CloudflareAccessService {

	private final AuthenticationContext authContext = new EnvironmentAuthenticationContext();

	public CloudflareToken getValidTokenFromRequest(HttpServletRequest request) {
		return new CloudflareToken(request, authContext);
	}


}
