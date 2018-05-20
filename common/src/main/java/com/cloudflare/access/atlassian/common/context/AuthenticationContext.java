package com.cloudflare.access.atlassian.common.context;

import java.time.Clock;

public interface AuthenticationContext {
	public String getAudience();
	public String getIssuer();
	public String getSigningKeyAsJson();
	public String getLogoutUrl();

	default public Clock getClock() {
		return Clock.systemUTC();
	}
}
