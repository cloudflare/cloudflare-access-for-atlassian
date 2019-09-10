package com.cloudflare.access.atlassian.base.config;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.cloudflare.access.atlassian.common.CertificateProvider;
import com.cloudflare.access.atlassian.common.config.PluginConfiguration;
import com.cloudflare.access.atlassian.common.context.AuthenticationContext;

public class PersistentPluginConfiguration implements PluginConfiguration{

	private AuthenticationContext authContext;
	private ConfigurationVariables variables;

	public PersistentPluginConfiguration(ConfigurationVariables variables, CertificateProvider certificateProvider) {
		this.authContext = new PersistentAuthenticationContext(variables, certificateProvider);
		this.variables = variables;
	}

	@Override
	public AuthenticationContext getAuthenticationContext() {
		return authContext;
	}

	@Override
	public Optional<String> getAllowedEmailDomain() {
		return Optional.ofNullable(StringUtils.defaultIfEmpty(variables.getAllowedEmailDomain(), null));
	}

	public static final class PersistentAuthenticationContext implements AuthenticationContext{

		private ConfigurationVariables variables;
		private CertificateProvider certificateProvider;

		public PersistentAuthenticationContext(ConfigurationVariables variables, CertificateProvider certificateProvider) {
			this.variables = variables;
			this.certificateProvider = certificateProvider;
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
		public List<String> getSigningKeyAsJson() {
			String url = String.format("https://%s/cdn-cgi/access/certs", variables.getAuthDomain());
			return this.certificateProvider.getCerticatesAsJson(url);
		}

		@Override
		public String getLogoutUrl() {
			return String.format("https://%s/cdn-cgi/access/logout", variables.getAuthDomain());
		}

	}

}
