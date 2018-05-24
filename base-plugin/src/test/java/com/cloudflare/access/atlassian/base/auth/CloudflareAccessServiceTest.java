package com.cloudflare.access.atlassian.base.auth;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Ignore;
import org.junit.Test;

import com.atlassian.plugin.PluginAccessor;


public class CloudflareAccessServiceTest {


	private PluginAccessor pluginAcessor;
	private CloudflarePluginDetails pluginDetails;
	private AtlassianUserService userService;
	private AtlassianProductWhitelistRules whitelistRules;
	private SuccessfulAuthenticationRequestHandler successHandler;
	private FailedAuthenticationRequestHandler failureHandler;

	@Test
	@Ignore
	public void testAuthenticationSuccess() {
		HttpServletRequest httpRequest = null;
		HttpServletResponse httpResponse = null;
		FilterChain chain = null;

		CloudflareAccessService cloudflareAccessService = new CloudflareAccessService(pluginAcessor, pluginDetails, userService, whitelistRules, successHandler, failureHandler);

		cloudflareAccessService.processAuthRequest(httpRequest, httpResponse, chain);
	}

	@Test
	@Ignore
	public void testAuthenticationFailure() {

	}
}
