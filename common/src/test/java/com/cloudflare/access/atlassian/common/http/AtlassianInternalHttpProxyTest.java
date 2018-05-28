package com.cloudflare.access.atlassian.common.http;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Test;
import org.littleshoot.proxy.mitm.RootCertificateException;

public class AtlassianInternalHttpProxyTest {

	@After
	public void ensureProxyShutdown() {
		AtlassianInternalHttpProxy.INSTANCE.shutdown();
	}

	@Test
	public void testLocalForward() throws MalformedURLException, IOException {
		AtlassianInternalHttpProxyConfig config = new AtlassianInternalHttpProxyConfig("mockbin.com", 80, false);
		AtlassianInternalHttpProxy.INSTANCE.init(config);
		sendGetAndAssert("http://example.com", 200, null, false);
		sendGetAndAssert("http://example.com/jira/rest/gadgets/1.0/g/feed", 302, "http://mockbin.com:80/jira/rest/gadgets/1.0/g/feed", false);

	}

	@Test
	public void testHttpsForwardToHttp() throws RootCertificateException, MalformedURLException, IOException, KeyManagementException, NoSuchAlgorithmException {
		AtlassianInternalHttpProxyConfig config = new AtlassianInternalHttpProxyConfig("mockbin.com", 80, false);
		AtlassianInternalHttpProxy.INSTANCE.init(config);

		String httpsNonProxiedContent = assertCodeAndReturnContent("https://mockbin.com/request/non/proxied", 200);
		assertThat(StringEscapeUtils.unescapeHtml4(httpsNonProxiedContent), Matchers.containsString("\"url\": \"https://mockbin.com/request/non/proxied\""));

		String httpsProxiedContent = assertCodeAndReturnContent("https://mockbin.com/request/rest/gadgets/1.0/g/feed", 200);
		assertThat(httpsProxiedContent, Matchers.containsString("\"url\": \"http://mockbin.com/request/rest/gadgets/1.0/g/feed\""));
	}

	@Test
	public void testHttpsForwardToHttps() throws RootCertificateException, MalformedURLException, IOException, KeyManagementException, NoSuchAlgorithmException {
		AtlassianInternalHttpProxyConfig config = new AtlassianInternalHttpProxyConfig("mockbin.com", 443, true);
		AtlassianInternalHttpProxy.INSTANCE.init(config);

		String httpsNonProxiedContent = assertCodeAndReturnContent("https://mockbin.com/request/non/proxied", 200);
		assertThat(StringEscapeUtils.unescapeHtml4(httpsNonProxiedContent), Matchers.containsString("\"url\": \"https://mockbin.com/request/non/proxied\""));

		String httpsProxiedContent = assertCodeAndReturnContent("https://mockbin.com/request/rest/gadgets/1.0/g/feed", 200);
		assertThat(httpsProxiedContent, Matchers.containsString("\"url\": \"https://mockbin.com/request/rest/gadgets/1.0/g/feed\""));
	}

	private void sendGetAndAssert(String url, int expectedCode, String expectedLocation, boolean follow) throws MalformedURLException, IOException {
		HttpURLConnection con = (HttpURLConnection)new URL(url).openConnection();
		con.setInstanceFollowRedirects(follow);

		int responseCode = con.getResponseCode();

		assertThat(responseCode, is(expectedCode));
		assertThat(con.getHeaderField("Location"), is(expectedLocation));
	}

	private String assertCodeAndReturnContent(String url, int expectedCode) throws MalformedURLException, IOException {
		HttpURLConnection con = (HttpURLConnection)new URL(url).openConnection();

		int responseCode = con.getResponseCode();
		assertThat(responseCode, is(expectedCode));

		String contents = read(con);
		return contents;
	}

    private String read(URLConnection con) throws IOException {
        try(InputStream is = con.getInputStream(); ByteArrayOutputStream os = new ByteArrayOutputStream();){
            IOUtils.copy(is, os);
            return new String( os.toByteArray(), StandardCharsets.UTF_8 );
        }
    }

}
