package com.cloudflare.access.atlassian.common.context;

import static com.cloudflare.access.atlassian.common.config.EnvUtils.getEnvValueOrThrow;

import com.cloudflare.access.atlassian.common.CertificateProvider;
import com.cloudflare.access.atlassian.common.http.SimpleHttp;

public class EnvironmentAuthenticationContext implements AuthenticationContext{

	private static final String CF_ACCESS_ATLASSIAN_AUDIENCE = "CF_ACCESS_ATLASSIAN_AUDIENCE";
	private static final String CF_ACCESS_ATLASSIAN_ISSUER = "CF_ACCESS_ATLASSIAN_ISSUER";
	private static final String CF_ACCESS_ATLASSIAN_CERTS_URL = "CF_ACCESS_ATLASSIAN_CERTS_URL";
	private static final String CF_ACCESS_ATLASSIAN_LOGOUT_URL = "CF_ACCESS_ATLASSIAN_LOGOUT_URL";

	private CertificateProvider certificateProvider;

	public EnvironmentAuthenticationContext() {
		this.certificateProvider = new CertificateProvider(new SimpleHttp());
	}

	@Override
	public String getAudience() {
		return getEnvValueOrThrow(CF_ACCESS_ATLASSIAN_AUDIENCE);
	}

	@Override
	public String getIssuer() {
		return getEnvValueOrThrow(CF_ACCESS_ATLASSIAN_ISSUER);
	}

	@Override
	public String getSigningKeyAsJson() {
		String url = getEnvValueOrThrow(CF_ACCESS_ATLASSIAN_CERTS_URL);
		return this.certificateProvider.getCerticatesAsJson(url).get(0);
	}

	@Override
	public String getLogoutUrl() {
		return getEnvValueOrThrow(CF_ACCESS_ATLASSIAN_LOGOUT_URL);
	}

}
