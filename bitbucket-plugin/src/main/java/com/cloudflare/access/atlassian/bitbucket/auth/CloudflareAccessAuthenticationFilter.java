package com.cloudflare.access.atlassian.bitbucket.auth;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.annotations.security.UnrestrictedAccess;
import com.cloudflare.access.atlassian.base.auth.CloudflareAccessService;

@Named("CloudflareAccessAuthenticationFilter")
@UnrestrictedAccess
public class CloudflareAccessAuthenticationFilter implements Filter{

	//private static final Logger log = LoggerFactory.getLogger(CloudflareAccessAuthenticationFilter.class);

	@Inject
	private CloudflareAccessService cloudflareAccess;

	@Inject
	public CloudflareAccessAuthenticationFilter(CloudflareAccessService cloudflareAccess) {
		this.cloudflareAccess = cloudflareAccess;
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		final HttpServletRequest httpRequest = (HttpServletRequest) request;
		final HttpServletResponse httpResponse = (HttpServletResponse) response;

		cloudflareAccess.processAuthRequest(httpRequest, httpResponse, chain);
	}

}
