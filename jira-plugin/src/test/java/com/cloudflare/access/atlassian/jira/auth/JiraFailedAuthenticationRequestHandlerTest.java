package com.cloudflare.access.atlassian.jira.auth;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import java.io.IOException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import com.cloudflare.access.atlassian.base.auth.AuthenticationErrorServlet;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JiraFailedAuthenticationRequestHandlerTest {

	@Mock
	private RememberMeHelperService rememberMeService;

	@Test
	public void testThatSends401WhenNoCookieIsAvailable() throws IOException {
		JiraFailedAuthenticationRequestHandler handler = new JiraFailedAuthenticationRequestHandler(rememberMeService);

		HttpServletRequest httpRequest = mock(HttpServletRequest.class);
		HttpServletResponse httpResponse = mock(HttpServletResponse.class);

		handler.handle(httpRequest, httpResponse, new Exception("testing exception"));

		verify(httpResponse, never()).sendRedirect(anyString());
		verify(httpResponse, times(1)).sendError(401, "testing exception");
		verify(httpResponse, times(1)).addHeader(eq("WWW-Authenticate"), anyString());
	}

	@Test
	public void testThatSendsRedirectOnTheFirstFailure() throws IOException {
		JiraFailedAuthenticationRequestHandler handler = new JiraFailedAuthenticationRequestHandler(rememberMeService);

		HttpServletRequest httpRequest = mock(HttpServletRequest.class);
		HttpServletResponse httpResponse = mock(HttpServletResponse.class);

		when(httpRequest.getHeader("Accept")).thenReturn("text/html");
		when(httpRequest.getRequestURI()).thenReturn("/secure/website");
		when(httpRequest.getCookies()).thenReturn(new Cookie[] {new Cookie("JSESSIONID","somehashvalue")});

		handler.handle(httpRequest, httpResponse, new Exception("testing exception"));

		verify(httpResponse, times(1)).sendRedirect(httpRequest.getRequestURI() + "?" + JiraFailedAuthenticationRequestHandler.CF_PLUGIN_REQUEST_IDENTIFIER_PARAM + "=somehashvalue" );
		verify(httpResponse, never()).sendError(401);
	}

	@Test
	public void testThatRedirectsToErrorPageOnTheSubsequentFailure() throws IOException {
		JiraFailedAuthenticationRequestHandler handler = new JiraFailedAuthenticationRequestHandler(rememberMeService);

		HttpServletRequest httpRequest = mock(HttpServletRequest.class);
		HttpServletResponse httpResponse = mock(HttpServletResponse.class);

		when(httpRequest.getHeader("Accept")).thenReturn("text/html");
		when(httpRequest.getRequestURI()).thenReturn("/secure/website");
		when(httpRequest.getCookies()).thenReturn(new Cookie[] {new Cookie("JSESSIONID","somehashvalue2")});

		handler.handle(httpRequest, httpResponse, new Exception("testing exception"));

		ArgumentCaptor<String> redirectUrlCaptor = ArgumentCaptor.forClass(String.class);
		verify(httpResponse, times(1)).sendRedirect(redirectUrlCaptor.capture());
		verify(httpResponse, never()).sendError(401);

		//Create a new response
		httpResponse = mock(HttpServletResponse.class);
		Exception secondException = new Exception("another testing exception");

		handler.handle(httpRequest, httpResponse, secondException);
		verify(httpResponse, times(1)).sendRedirect(redirectUrlCaptor.capture());
		assertTrue(redirectUrlCaptor.getValue().contains(AuthenticationErrorServlet.PATH));
	}

}
