package com.cloudflare.access.atlassian.base.auth;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.plugin.PluginAccessor;

@RunWith(MockitoJUnitRunner.class)
public class CloudflareAccessServiceTest {

	@Mock
	private PluginAccessor pluginAcessor;
	@Mock
	private CloudflarePluginDetails pluginDetails;
	@Mock
	private AtlassianUserService userService;
	@Mock
	private AtlassianProductWhitelistRules whitelistRules;
	@Mock
	private SuccessfulAuthenticationRequestHandler successHandler;
	@Mock
	private FailedAuthenticationRequestHandler failureHandler;

	private TestAuthenticationContext authContext = new TestAuthenticationContext();

	@Before
	public void setupDefaults() {
		when(pluginAcessor.isPluginEnabled(anyString())).thenReturn(true);
	}

	@Test
	public void testAuthenticationSuccess() {
		HttpServletRequest httpRequest = mock(HttpServletRequest.class);
		when(httpRequest.getHeader(CloudflareToken.CF_ACCESS_JWT_HEADER)).thenReturn(authContext.getValidToken());

		HttpServletResponse httpResponse = mock(HttpServletResponse.class);
		FilterChain chain = mock(FilterChain.class);

		User user = mock(User.class);
		when(userService.getUser(authContext.getTokenOwnerEmail())).thenReturn(user);

		CloudflareAccessService cloudflareAccessService = new CloudflareAccessService(pluginAcessor, pluginDetails, userService, whitelistRules, successHandler, failureHandler);
		cloudflareAccessService.setAuthContext(authContext);
		cloudflareAccessService.processAuthRequest(httpRequest, httpResponse, chain);

		verify(successHandler,times(1)).handle(httpRequest, httpResponse, chain, user);
		verifyZeroInteractions(failureHandler);
		verifyZeroInteractions(httpResponse);
		verifyZeroInteractions(chain);
	}

	@Test
	public void testAuthenticationFailure() {
		HttpServletRequest httpRequest = mock(HttpServletRequest.class);
		when(httpRequest.getHeader(CloudflareToken.CF_ACCESS_JWT_HEADER)).thenReturn(authContext.getValidToken());

		HttpServletResponse httpResponse = mock(HttpServletResponse.class);
		FilterChain chain = mock(FilterChain.class);

		RuntimeException userRetrievalExcpetion = new RuntimeException("two users with same email");
		when(userService.getUser(authContext.getTokenOwnerEmail())).thenThrow(userRetrievalExcpetion);

		CloudflareAccessService cloudflareAccessService = new CloudflareAccessService(pluginAcessor, pluginDetails, userService, whitelistRules, successHandler, failureHandler);
		cloudflareAccessService.setAuthContext(authContext);
		cloudflareAccessService.processAuthRequest(httpRequest, httpResponse, chain);

		verify(failureHandler,times(1)).handle(httpRequest, httpResponse, userRetrievalExcpetion);
		verifyZeroInteractions(successHandler);
		verifyZeroInteractions(httpResponse);
		verifyZeroInteractions(chain);
	}

	@Test
	public void testNoTokenValidationIfPluginDisabled() throws IOException, ServletException {
		when(pluginAcessor.isPluginEnabled(anyString())).thenReturn(false);

		HttpServletRequest httpRequest = mock(HttpServletRequest.class);
		HttpServletResponse httpResponse = mock(HttpServletResponse.class);
		FilterChain chain = mock(FilterChain.class);

		CloudflareAccessService cloudflareAccessService = new CloudflareAccessService(pluginAcessor, pluginDetails, userService, whitelistRules, successHandler, failureHandler);
		cloudflareAccessService.setAuthContext(authContext);
		cloudflareAccessService.processAuthRequest(httpRequest, httpResponse, chain);

		verify(chain,times(1)).doFilter(httpRequest, httpResponse);
		verifyZeroInteractions(successHandler);
		verifyZeroInteractions(userService);
		verifyZeroInteractions(httpRequest);
		verifyZeroInteractions(httpResponse);
	}

	@Test
	public void testNoTokenValidationIfRequestIsWhitelisted() throws IOException, ServletException {
		HttpServletRequest httpRequest = mock(HttpServletRequest.class);
		HttpServletResponse httpResponse = mock(HttpServletResponse.class);
		FilterChain chain = mock(FilterChain.class);

		when(whitelistRules.isRequestWhitelisted(httpRequest)).thenReturn(true);

		CloudflareAccessService cloudflareAccessService = new CloudflareAccessService(pluginAcessor, pluginDetails, userService, whitelistRules, successHandler, failureHandler);
		cloudflareAccessService.setAuthContext(authContext);
		cloudflareAccessService.processAuthRequest(httpRequest, httpResponse, chain);

		verify(whitelistRules,times(1)).isRequestWhitelisted(httpRequest);
		verify(chain,times(1)).doFilter(httpRequest, httpResponse);
		verifyZeroInteractions(successHandler);
		verifyZeroInteractions(userService);
		verifyZeroInteractions(httpResponse);
	}
}
