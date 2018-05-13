package com.cloudflare.access.atlassian.common.context;

import java.time.Clock;

public interface VerificationContext {
	public String getAudience();
	public String getIssuer();
	public String getSigningKeyAsJson();

	default public Clock getClock() {
		return Clock.systemUTC();
	}
}
