package com.cloudflare.access.atlassian.base.config;

import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.rs.security.jose.jwk.JsonWebKey;

import com.cloudflare.access.atlassian.common.CertificateProvider;
import com.cloudflare.access.atlassian.common.context.AuthenticationContext;
import com.google.common.collect.Sets;

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

	@Override
	public UserMatchingAttribute getUserMatchingAttribute() {
		return variables.getUserMatchingAttribute();
	}

	public static final class PersistentAuthenticationContext implements AuthenticationContext{

		private ConfigurationVariables variables;
		private CertificateProvider certificateProvider;
		private String certificatesUrl;

		public PersistentAuthenticationContext(ConfigurationVariables variables, CertificateProvider certificateProvider) {
			this.variables = variables;
			this.certificateProvider = certificateProvider;
			this.certificatesUrl = String.format("https://%s/cdn-cgi/access/certs", variables.getAuthDomain());
		}

		@Override
		public Set<String> getAudiences() {
			return Sets.newHashSet(variables.getTokenAudience());
		}

		@Override
		public String getIssuer() {
			return String.format("https://%s", variables.getAuthDomain());
		}

		@Override
		public JsonWebKey getJwk(String kid) {
			return certificateProvider.getJwk(certificatesUrl, kid);
		}

		@Override
		public String getLogoutUrl() {
			return "/cdn-cgi/access/logout";
		}

	}

}
