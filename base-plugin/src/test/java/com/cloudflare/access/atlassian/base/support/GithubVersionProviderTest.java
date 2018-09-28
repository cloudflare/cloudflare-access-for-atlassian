package com.cloudflare.access.atlassian.base.support;

import static org.junit.Assert.assertTrue;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

public class GithubVersionProviderTest {

	@Test
	public void testThatCanRetrieveLatestReleaseInfo() {
		String latestReleaseVersion = new GithubVersionProvider().getLatestReleaseVersion();
		System.out.println("Retrieved latest version: " + latestReleaseVersion);
		assertTrue(StringUtils.isNotBlank(latestReleaseVersion));
		assertTrue(latestReleaseVersion.matches("[0-9]+\\.[0-9]+\\.[0-9]+$"));
	}

}
