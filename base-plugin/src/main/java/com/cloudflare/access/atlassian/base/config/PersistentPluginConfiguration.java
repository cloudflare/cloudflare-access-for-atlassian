package com.cloudflare.access.atlassian.base.config;

import com.cloudflare.access.atlassian.common.CertificateProvider;
import com.cloudflare.access.atlassian.common.config.PluginConfiguration;
import com.cloudflare.access.atlassian.common.context.AuthenticationContext;
import com.cloudflare.access.atlassian.common.http.SimpleHttp;

public class PersistentPluginConfiguration implements PluginConfiguration{

	private AuthenticationContext authContext;

	public PersistentPluginConfiguration(ConfigurationVariables variables) {
		this.authContext = new PersistentAuthenticationContext(variables);
	}

	@Override
	public AuthenticationContext getAuthenticationContext() {
		return authContext;
	}

	public static final class PersistentAuthenticationContext implements AuthenticationContext{

		private ConfigurationVariables variables;
		private CertificateProvider certificateProvider;

		public PersistentAuthenticationContext(ConfigurationVariables variables) {
			this.variables = variables;
			this.certificateProvider = new CertificateProvider(new SimpleHttp());
		}

		@Override
		public String getAudience() {
			return variables.getTokenAudience();
		}

		@Override
		public String getIssuer() {
			return String.format("https://%s", variables.getAuthDomain());
		}

		@Override
		public String getSigningKeyAsJson() {
			String url = String.format("https://%s/cdn-cgi/access/certs", variables.getAuthDomain());
			return this.certificateProvider.getCerticatesAsJson(url).get(0);
		}

		@Override
		public String getLogoutUrl() {
			return String.format("https://%s/cdn-cgi/access/logout", variables.getAuthDomain());
		}

	}

}
