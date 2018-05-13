package com.cloudflare.access.atlassian.jira.auth;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//TODO the filter logic is a candidate to be reused across plugins!
public class CloudflareAccessAuthenticationFilter implements Filter{

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {}

	@Override
	public void destroy() {}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		httpResponse.setContentType("text/plain;charset=UTF-8");


		CloudflareToken cloudflareToken = new CloudflareToken(httpRequest);

		PrintWriter writer = httpResponse.getWriter();
		writer.write(String.format("\nCF User email: '%s'\n", cloudflareToken.getUserEmail()));

		//chain.doFilter(request, response);
	}

}
