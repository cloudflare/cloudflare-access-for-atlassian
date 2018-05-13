package com.cloudflare.access.atlassian.common.context;

import java.time.Clock;
import java.util.List;

public interface AuthenticationContext {
	public String getAudience();
	public String getIssuer();
	public List<String> getSigningKeyAsJson();
	public String getLogoutUrl();

	default public Clock getClock() {
		return Clock.systemUTC();
	}
}
