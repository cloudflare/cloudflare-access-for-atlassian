package com.cloudflare.access.atlassian.common.http;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;

public class AtlassianInternalHttpProxyTest {

	@Test
	public void testLocalForward() throws MalformedURLException, IOException {
		AtlassianInternalHttpProxyConfig config = new AtlassianInternalHttpProxyConfig("httpbin.org", 80);
		AtlassianInternalHttpProxy.INSTANCE.init(config);
		sendGet("http://example.com", 200, null);
		sendGet("http://example.com/jira/rest/gadgets/1.0/g/feed", 302, "http://httpbin.org:80/jira/rest/gadgets/1.0/g/feed");
		AtlassianInternalHttpProxy.INSTANCE.shutdown();
	}

	private void sendGet(String url, int expectedCode, String expectedLocation) throws MalformedURLException, IOException {
		HttpURLConnection con = (HttpURLConnection)new URL(url).openConnection();
		con.setInstanceFollowRedirects(false);

		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'GET' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);

		assertThat(responseCode, is(expectedCode));
		assertThat(con.getHeaderField("Location"), is(expectedLocation));
	}
}
