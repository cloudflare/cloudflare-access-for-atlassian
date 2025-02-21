package com.cloudflare.access.atlassian.base.auth;

import static com.cloudflare.access.atlassian.base.utils.SessionUtils.ATLASSIAN_FLOW_FLAG;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Optional;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.env.Environment;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.plugin.PluginAccessor;
import com.cloudflare.access.atlassian.base.config.ConfigurationService;
import com.cloudflare.access.atlassian.base.config.PluginConfiguration;
import com.cloudflare.access.atlassian.base.support.PluginStateService;
import com.cloudflare.access.atlassian.base.utils.EnvironmentFlags;
import com.cloudflare.access.atlassian.common.context.AuthenticationContext;
import com.cloudflare.access.atlassian.common.exception.CloudflareAccessUnauthorizedException;

@RunWith(MockitoJUnitRunner.class)
public class CloudflareAccessServiceTest {

	@Mock
	private PluginAccessor pluginAcessor;
	@Mock
	private CloudflarePluginDetails pluginDetails;
	@Mock
	private ConfigurationService configurationService;
	@Mock
	private AtlassianUserService userService;
	@Mock
	private SuccessfulAuthenticationRequestHandler successHandler;
	@Mock
	private FailedAuthenticationRequestHandler failureHandler;
	@Mock
	private Environment env;
	@Mock
	private PluginStateService pluginStateService;

	private PluginConfiguration mockPluginConfiguration;
	private TestAuthenticationContext authContext;

	@Before
	public void setupDefaults() {
		when(pluginDetails.getPluginKey()).thenReturn("some.plugin.key");
		when(pluginAcessor.isPluginEnabled(anyString())).thenReturn(true);
		when(pluginStateService.isReady()).thenReturn(true);


		authContext = new TestAuthenticationContext();

		mockPluginConfiguration = mockPluginConfiguration(authContext);
		when(configurationService.getPluginConfiguration()).thenReturn(Optional.of(mockPluginConfiguration));
	}

	private CloudflareAccessService newCloudflareAccessServiceInstance() {
		return new CloudflareAccessService(pluginAcessor, pluginDetails, configurationService, userService, successHandler, failureHandler, env, pluginStateService);
	}

	@Test
	public void testAuthenticationSuccess() throws IOException, ServletException {
		HttpServletRequest httpRequest = mock(HttpServletRequest.class);
		HttpSession httpSession = mock(HttpSession.class);
		when(httpRequest.getSession(anyBoolean())).thenReturn(httpSession);
		when(httpRequest.getHeader(CloudflareToken.CF_ACCESS_JWT_HEADER)).thenReturn(authContext.getValidToken());

		HttpServletResponse httpResponse = mock(HttpServletResponse.class);
		FilterChain chain = mock(FilterChain.class);

		User user = mock(User.class);
		when(userService.getUser(authContext.getTokenOwnerEmail())).thenReturn(user);

		CloudflareAccessService cloudflareAccessService = newCloudflareAccessServiceInstance();
		cloudflareAccessService.processAuthRequest(httpRequest, httpResponse, chain);

		verify(successHandler,times(1)).handle(httpRequest, httpResponse, chain, user);
		verify(httpSession,times(1)).setAttribute("CF_USER_EMAIL", authContext.getTokenOwnerEmail());
		verifyNoInteractions(failureHandler);
		verifyNoInteractions(httpResponse);
		verifyNoInteractions(chain);
	}

	@Test
	public void testAuthenticationFailure() throws IOException, ServletException {
		HttpServletRequest httpRequest = mock(HttpServletRequest.class);
		when(httpRequest.getHeader(CloudflareToken.CF_ACCESS_JWT_HEADER)).thenReturn(authContext.getValidToken());

		HttpServletResponse httpResponse = mock(HttpServletResponse.class);
		FilterChain chain = mock(FilterChain.class);

		CloudflareAccessUnauthorizedException userRetrievalExcpetion = new CloudflareAccessUnauthorizedException("two users with same email");
		when(userService.getUser(authContext.getTokenOwnerEmail())).thenThrow(userRetrievalExcpetion);

		CloudflareAccessService cloudflareAccessService = newCloudflareAccessServiceInstance();
		cloudflareAccessService.processAuthRequest(httpRequest, httpResponse, chain);

		verify(failureHandler,times(1)).handle(httpRequest, httpResponse, userRetrievalExcpetion);
		verifyNoInteractions(successHandler);
		verifyNoInteractions(httpResponse);
		verifyNoInteractions(chain);
	}

	@Test
	public void testNoTokenValidationIfPluginDisabled() throws IOException, ServletException {
		when(pluginAcessor.isPluginEnabled(anyString())).thenReturn(false);

		HttpServletRequest httpRequest = mock(HttpServletRequest.class);
		HttpServletResponse httpResponse = mock(HttpServletResponse.class);
		FilterChain chain = mock(FilterChain.class);

		CloudflareAccessService cloudflareAccessService = newCloudflareAccessServiceInstance();
		cloudflareAccessService.processAuthRequest(httpRequest, httpResponse, chain);

		verify(chain,times(1)).doFilter(httpRequest, httpResponse);
		verifyNoInteractions(successHandler);
		verifyNoInteractions(userService);
		verifyNoInteractions(httpRequest);
		verifyNoInteractions(httpResponse);
	}

	@Test
	public void testNoFilteringIfFilteringIsDisabled() throws IOException, ServletException {
		HttpServletRequest httpRequest = mock(HttpServletRequest.class);
		HttpServletResponse httpResponse = mock(HttpServletResponse.class);
		FilterChain chain = mock(FilterChain.class);

		when(env.getProperty(EnvironmentFlags.FILTERS_DISABLED, "false")).thenReturn("true");

		CloudflareAccessService cloudflareAccessService = newCloudflareAccessServiceInstance();
		cloudflareAccessService.processAuthRequest(httpRequest, httpResponse, chain);

		verify(chain,times(1)).doFilter(httpRequest, httpResponse);
		verifyNoInteractions(successHandler);
		verifyNoInteractions(userService);
		verifyNoInteractions(httpResponse);
	}

	@Test
	public void testLogout() throws IOException, ServletException {
		HttpServletRequest httpRequest = mock(HttpServletRequest.class);
		HttpServletResponse httpResponse = mock(HttpServletResponse.class);
		FilterChain chain = mock(FilterChain.class);
		HttpSession httpSession = mock(HttpSession.class);
		when(httpRequest.getHeader(CloudflareToken.CF_ACCESS_JWT_HEADER)).thenReturn(authContext.getValidToken());
		when(httpRequest.getSession(false)).thenReturn(httpSession);

		CloudflareAccessService cloudflareAccessService = newCloudflareAccessServiceInstance();
		cloudflareAccessService.processLogoutRequest(httpRequest, httpResponse, chain);

		verify(httpResponse, times(1)).sendRedirect(authContext.getLogoutUrl());
		verify(httpSession, times(1)).invalidate();
	}

	@Test
	public void testLogoutWithoutToken() throws IOException, ServletException {
		HttpServletRequest httpRequest = mock(HttpServletRequest.class);
		HttpServletResponse httpResponse = mock(HttpServletResponse.class);
		FilterChain chain = mock(FilterChain.class);
		HttpSession httpSession = mock(HttpSession.class);

		when(httpRequest.getCookies()).thenReturn(new Cookie[] {
				newCookie("seraph.whatever", "test", 999999),
				newCookie("JSESSIONID", "test", 999999),
		});
		when(httpRequest.getSession(false)).thenReturn(httpSession);

		CloudflareAccessService cloudflareAccessService = newCloudflareAccessServiceInstance();
		cloudflareAccessService.processLogoutRequest(httpRequest, httpResponse, chain);

		verify(httpResponse, times(0)).sendRedirect(authContext.getLogoutUrl());
		verify(httpSession, times(0)).invalidate();
		verify(chain, times(1)).doFilter(httpRequest, httpResponse);
	}

	@Test
	public void errorUrlShouldBeWhitelistedWhenTokenIsPresent() throws IOException, ServletException {
		HttpServletRequest httpRequest = mock(HttpServletRequest.class);
		when(httpRequest.getRequestURI()).thenReturn(AuthenticationErrorServlet.PATH);

		HttpServletResponse httpResponse = mock(HttpServletResponse.class);
		FilterChain chain = mock(FilterChain.class);

		CloudflareAccessService cloudflareAccessService = newCloudflareAccessServiceInstance();
		cloudflareAccessService.processAuthRequest(httpRequest, httpResponse, chain);

		verify(chain, times(1)).doFilter(httpRequest, httpResponse);
		verifyNoInteractions(successHandler);
		verifyNoInteractions(failureHandler);
		verifyNoInteractions(httpResponse);
	}

	@Test
	public void shouldSkipSucessHandlerIfSessionAlreadyContainsAuthenticatedUser() throws IOException, ServletException {
		HttpServletRequest httpRequest = mock(HttpServletRequest.class);
		HttpServletResponse httpResponse = mock(HttpServletResponse.class);
		FilterChain chain = mock(FilterChain.class);

		HttpSession httpSession = mock(HttpSession.class);
		when(httpSession.getAttribute("CF_USER_EMAIL")).thenReturn(authContext.getTokenOwnerEmail());

		when(httpRequest.getHeader(CloudflareToken.CF_ACCESS_JWT_HEADER)).thenReturn(authContext.getValidToken());
		when(httpRequest.getSession(anyBoolean())).thenReturn(httpSession);

		CloudflareAccessService cloudflareAccessService = newCloudflareAccessServiceInstance();
		cloudflareAccessService.processAuthRequest(httpRequest, httpResponse, chain);

		verify(chain, times(1)).doFilter(httpRequest, httpResponse);
		verifyNoInteractions(successHandler);
		verifyNoInteractions(failureHandler);
	}

	@Test
	public void shouldForwardUserWithEmailThatIsRequiredToProvideCredentials() throws IOException, ServletException {
		HttpServletRequest httpRequest = mock(HttpServletRequest.class);
		HttpServletResponse httpResponse = mock(HttpServletResponse.class);
		FilterChain chain = mock(FilterChain.class);

		when(configurationService.emailDomainRequiresAtlassianAuthentication(authContext.getTokenOwnerEmail())).thenReturn(true);

		HttpSession httpSession = mock(HttpSession.class);
		when(httpRequest.getSession(anyBoolean())).thenReturn(httpSession);
		when(httpRequest.getHeader(CloudflareToken.CF_ACCESS_JWT_HEADER)).thenReturn(authContext.getValidToken());

		CloudflareAccessService cloudflareAccessService = newCloudflareAccessServiceInstance();
		cloudflareAccessService.processAuthRequest(httpRequest, httpResponse, chain);

		verify(chain, times(1)).doFilter(httpRequest, httpResponse);
		verifyNoInteractions(successHandler);
		verifyNoInteractions(failureHandler);
	}

	@Test
	public void shouldAuthenticateUserWithEmailThatIsAllowedToNotProvideCredentials() throws IOException, ServletException {
		HttpServletRequest httpRequest = mock(HttpServletRequest.class);
		HttpServletResponse httpResponse = mock(HttpServletResponse.class);
		FilterChain chain = mock(FilterChain.class);

		when(configurationService.emailDomainRequiresAtlassianAuthentication(authContext.getTokenOwnerEmail())).thenReturn(true);

		HttpSession httpSession = mock(HttpSession.class);
		when(httpRequest.getSession(anyBoolean())).thenReturn(httpSession);
		when(httpRequest.getHeader(CloudflareToken.CF_ACCESS_JWT_HEADER)).thenReturn(authContext.getValidToken());

		CloudflareAccessService cloudflareAccessService = newCloudflareAccessServiceInstance();
		cloudflareAccessService.processAuthRequest(httpRequest, httpResponse, chain);

		verify(chain, times(1)).doFilter(httpRequest, httpResponse);
		verifyNoInteractions(successHandler);
		verifyNoInteractions(failureHandler);
	}


	@Test
	public void atlassianFlowEnabledShouldBypassAuthProcess() throws IOException, ServletException {
		HttpServletRequest httpRequest = mock(HttpServletRequest.class);
		HttpServletResponse httpResponse = mock(HttpServletResponse.class);
		FilterChain chain = mock(FilterChain.class);

		HttpSession httpSession = mock(HttpSession.class);
		when(httpRequest.getSession(anyBoolean())).thenReturn(httpSession);
		when(httpSession.getAttribute(ATLASSIAN_FLOW_FLAG)).thenReturn(true);

		CloudflareAccessService cloudflareAccessService = newCloudflareAccessServiceInstance();
		cloudflareAccessService.processAuthRequest(httpRequest, httpResponse, chain);

		ArgumentCaptor<HttpServletRequest> requestArgumentCaptor = ArgumentCaptor.forClass(HttpServletRequest.class);
		verify(chain, times(1)).doFilter(requestArgumentCaptor.capture(), eq(httpResponse));
		verifyNoInteractions(successHandler);
		verifyNoInteractions(failureHandler);
		assertEquals(AtlassianLoginFlowRequestWrapper.class, requestArgumentCaptor.getValue().getClass());
	}


	private Cookie newCookie(String name, String value, int maxAge) {
		Cookie cookie = new Cookie(name, value);
		cookie.setMaxAge(maxAge);
		return cookie;
	}

	private PluginConfiguration mockPluginConfiguration(AuthenticationContext authContext) {
		PluginConfiguration mockPluginConfiguration = mock(PluginConfiguration.class);
		when(mockPluginConfiguration.getAuthenticationContext()).thenReturn(authContext);
		return mockPluginConfiguration;
	}
}
