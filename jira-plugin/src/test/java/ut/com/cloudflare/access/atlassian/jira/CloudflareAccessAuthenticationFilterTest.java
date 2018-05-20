package ut.com.cloudflare.access.atlassian.jira;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Collections;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.search.query.entity.UserQuery;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.security.login.LoginManager;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import com.atlassian.seraph.service.rememberme.RememberMeService;
import com.cloudflare.access.atlassian.jira.auth.CloudflareAccessAuthenticationFilter;
import com.cloudflare.access.atlassian.jira.auth.CloudflareAccessService;
import com.cloudflare.access.atlassian.jira.auth.CloudflareToken;
import com.cloudflare.access.atlassian.jira.util.PluginUtils;

@RunWith(MockitoJUnitRunner.class)
public class CloudflareAccessAuthenticationFilterTest {

    @Rule
    public RuleChain mocksInContainer = MockitoMocksInContainer.forTest(this);
    @Mock
    @AvailableInContainer
    private LoginManager loginManager;
    @Mock
    @AvailableInContainer
    private RememberMeService rememberMeService;
	@Mock
	private CrowdService crowdService;
	@Mock
	private PluginAccessor pluginAcessor;
	@Spy
	private CloudflareAccessService cloudflareAccessService;

	@Test
	public void shouldFollowChainIfPluginIsDisabled() throws IOException, ServletException {
		CloudflareAccessAuthenticationFilter filter = new CloudflareAccessAuthenticationFilter(crowdService, pluginAcessor, cloudflareAccessService);
		when(pluginAcessor.isPluginEnabled(PluginUtils.getPluginKey())).thenReturn(false);

		FilterChain chain = mock(FilterChain.class);

		filter.doFilter(null, null, chain);

		verify(chain, times(1)).doFilter(null, null);
		verifyZeroInteractions(crowdService, cloudflareAccessService);
	}

	@Test
	public void shouldFollowChainIfRequestIsWhitelisted() throws IOException, ServletException {
		CloudflareAccessAuthenticationFilter filter = new CloudflareAccessAuthenticationFilter(crowdService, pluginAcessor, cloudflareAccessService);
		when(pluginAcessor.isPluginEnabled(PluginUtils.getPluginKey())).thenReturn(true);

		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getRequestURI()).thenReturn("/jira/rest/gadgets/sample/url");
		FilterChain chain = mock(FilterChain.class);

		filter.doFilter(request, null, chain);

		verify(chain, times(1)).doFilter(request, null);
		verifyZeroInteractions(crowdService, cloudflareAccessService);
	}

	@Test
	public void shouldSend401InCaseOfRequestWithoutTokenOrCookie() throws IOException, ServletException {
		CloudflareAccessAuthenticationFilter filter = new CloudflareAccessAuthenticationFilter(crowdService, pluginAcessor, cloudflareAccessService);
		when(pluginAcessor.isPluginEnabled(PluginUtils.getPluginKey())).thenReturn(true);

		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		FilterChain chain = mock(FilterChain.class);
		when(request.getServerName()).thenReturn("testserver");
		when(request.getRequestURI()).thenReturn("/jira/secure/page");

		filter.doFilter(request, response, chain);

		verify(response).sendError(401, "No Cloudflare Access token available in the request");
		verify(response).addHeader("WWW-Authenticate", "bearer realm=testserver");
		verifyZeroInteractions(chain, crowdService);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldFollowChainInCaseOfAuthSuccess() throws IOException, ServletException {
		CloudflareAccessAuthenticationFilter filter = new CloudflareAccessAuthenticationFilter(crowdService, pluginAcessor, cloudflareAccessService);
		when(pluginAcessor.isPluginEnabled(PluginUtils.getPluginKey())).thenReturn(true);

		HttpSession session = mock(HttpSession.class);
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getRequestURI()).thenReturn("/jira/secure/page");
		when(request.getSession()).thenReturn(session);

		HttpServletResponse response = mock(HttpServletResponse.class);
		FilterChain chain = mock(FilterChain.class);
		CloudflareToken token = mock(CloudflareToken.class);
		User user = mock(User.class);
		when(user.getName()).thenReturn("tester");

		when(token.getUserEmail()).thenReturn("test@test.com");
		doReturn(token).when(cloudflareAccessService).getValidTokenFromRequest(request);
		when(crowdService.search(any(UserQuery.class))).thenReturn(Collections.singleton(user));

		filter.doFilter(request, response, chain);

		verify(chain, times(1)).doFilter(request, response);
		verify(session).setAttribute(DefaultAuthenticator.LOGGED_IN_KEY, user);
		verify(session).setAttribute(DefaultAuthenticator.LOGGED_OUT_KEY, null);
		verify(loginManager).onLoginAttempt(request, "tester", true);
		verify(rememberMeService).addRememberMeCookie(request, response, "tester");
	}
}