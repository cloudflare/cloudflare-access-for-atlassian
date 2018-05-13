package com.cloudflare.access.atlassian.common.context;

import com.cloudflare.access.atlassian.common.CertificateProvider;
import com.cloudflare.access.atlassian.common.exception.ConfigurationException;
import com.cloudflare.access.atlassian.common.http.SimpleHttp;

public class EnvironmentVerificationContext implements VerificationContext{

	private static final String CF_ACCESS_ATLASSIAN_AUDIENCE = "CF_ACCESS_ATLASSIAN_AUDIENCE";
	private static final String CF_ACCESS_ATLASSIAN_ISSUER = "CF_ACCESS_ATLASSIAN_ISSUER";
	private static final String CF_ACCESS_ATLASSIAN_CERTS_URL = "CF_ACCESS_ATLASSIAN_CERTS_URL";

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
		return new CertificateProvider(new SimpleHttp()).getCerticatesAsJson(url).get(0);
	}

	private String getEnvValueOrThrow(String key) {
		String value = System.getenv(key);
		if(value == null || value.trim().isEmpty()) {
			throw new ConfigurationException(String.format("Environment variable '%s' not set", key));
		}
		return value;
	}

}
