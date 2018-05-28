package com.cloudflare.access.atlassian.jira.auth;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class JiraFailedAuthenticationRequestHandlerTest {

	@Test
	public void testThatSends401WhenNoCookieIsAvailable() throws IOException {
		JiraFailedAuthenticationRequestHandler handler = new JiraFailedAuthenticationRequestHandler();

		HttpServletRequest httpRequest = mock(HttpServletRequest.class);
		HttpServletResponse httpResponse = mock(HttpServletResponse.class);

		when(httpRequest.getRequestURI()).thenReturn("/secure/website");

		handler.handle(httpRequest, httpResponse, new Exception("testing exception"));

		verify(httpResponse, never()).sendRedirect(anyString());
		verify(httpResponse, times(1)).sendError(401, "testing exception");
		verify(httpResponse, times(1)).addHeader(eq("WWW-Authenticate"), anyString());
	}

	@Test
	public void testThatSendsRedirectOnTheFirstFailure() throws IOException {
		JiraFailedAuthenticationRequestHandler handler = new JiraFailedAuthenticationRequestHandler();

		HttpServletRequest httpRequest = mock(HttpServletRequest.class);
		HttpServletResponse httpResponse = mock(HttpServletResponse.class);

		when(httpRequest.getRequestURI()).thenReturn("/secure/website");
		when(httpRequest.getCookies()).thenReturn(new Cookie[] {new Cookie("JSESSIONID","somehashvalue")});

		handler.handle(httpRequest, httpResponse, new Exception("testing exception"));

		verify(httpResponse, times(1)).sendRedirect(httpRequest.getRequestURI() + "?" + JiraFailedAuthenticationRequestHandler.CF_PLUGIN_REQUEST_IDENTIFIER_PARAM + "=somehashvalue" );
		verify(httpResponse, never()).sendError(401);
	}

	@Test
	public void testThatSends401OnTheSubsequentFailure() throws IOException {
		JiraFailedAuthenticationRequestHandler handler = new JiraFailedAuthenticationRequestHandler();

		HttpServletRequest httpRequest = mock(HttpServletRequest.class);
		HttpServletResponse httpResponse = mock(HttpServletResponse.class);

		when(httpRequest.getRequestURI()).thenReturn("/secure/website");
		when(httpRequest.getCookies()).thenReturn(new Cookie[] {new Cookie("JSESSIONID","somehashvalue2")});

		handler.handle(httpRequest, httpResponse, new Exception("testing exception"));

		ArgumentCaptor<String> redirectUrlCaptor = ArgumentCaptor.forClass(String.class);
		verify(httpResponse, times(1)).sendRedirect(redirectUrlCaptor.capture());
		verify(httpResponse, never()).sendError(401);

		//Create a new response
		httpResponse = mock(HttpServletResponse.class);
		Exception secondException = new Exception("another testing exception");

		when(httpRequest.getRequestURI()).thenReturn(redirectUrlCaptor.getValue());
		handler.handle(httpRequest, httpResponse, secondException);
		verify(httpResponse, never()).sendRedirect(redirectUrlCaptor.capture());
		verify(httpResponse, times(1)).sendError(401, secondException.getMessage());
		verify(httpResponse, times(1)).addHeader(eq("WWW-Authenticate"), anyString());
	}

}
