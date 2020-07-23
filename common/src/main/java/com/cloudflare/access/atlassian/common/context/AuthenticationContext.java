package com.cloudflare.access.atlassian.common.context;

import java.time.Clock;

import org.apache.cxf.rs.security.jose.jwk.JsonWebKey;

public interface AuthenticationContext {
	String getAudience();
	String getIssuer();
	JsonWebKey getJwk(String kid);
	String getLogoutUrl();

	default Clock getClock() {
		return Clock.systemUTC();
	}
}
