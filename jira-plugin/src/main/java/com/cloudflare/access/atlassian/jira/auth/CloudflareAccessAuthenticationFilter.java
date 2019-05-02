package com.cloudflare.access.atlassian.jira.auth;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import com.cloudflare.access.atlassian.base.auth.CloudflareAccessService;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

@Named("CloudflareAccessAuthenticationFilter")
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

		final HttpServletRequest httpRequest = new CloudflareProxiedRequest((HttpServletRequest) request);
		final HttpServletResponse httpResponse = (HttpServletResponse) response;

		cloudflareAccess.processAuthRequest(httpRequest, httpResponse, chain);
	}

	private static class CloudflareProxiedRequest extends HttpServletRequestWrapper{
		private final Supplier<String> connectingIpSupplier;

		public CloudflareProxiedRequest(HttpServletRequest request) {
			super(request);
			this.connectingIpSupplier = Suppliers.memoize(() -> {
				Enumeration<?> connectingIpHeader = this.getHeaders("cf-connecting-ip");
				if(connectingIpHeader != null && connectingIpHeader.hasMoreElements()) {
					return Objects.toString(connectingIpHeader.nextElement());
				}
				return super.getRemoteAddr();
			});
		}

		@Override
		public String getRemoteAddr() {
			return connectingIpSupplier.get();
		}
	}
}
