package com.cloudflare.access.atlassian.common.http;

import static org.apache.commons.lang3.StringEscapeUtils.unescapeHtml4;
import static org.hamcrest.Matchers.*;
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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Test;
import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersAdapter;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import org.littleshoot.proxy.impl.ProxyUtils;
import org.littleshoot.proxy.mitm.RootCertificateException;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.AttributeKey;

public class AtlassianInternalHttpProxyTest {

	private AtlassianInternalHttpProxy proxy;

	@After
	public void ensureProxyShutdown() {
		this.proxy.shutdown();
	}

	@Test
	public void testLocalForward() throws MalformedURLException, IOException {
		AtlassianInternalHttpProxyConfig config = new AtlassianInternalHttpProxyConfig("mockbin.com", 80, false);
		this.proxy = new AtlassianInternalHttpProxy();
		this.proxy.init(config);
		sendGetAndAssert("http://example.com", 200, null, false);
		sendGetAndAssert("http://example.com/jira/rest/gadgets/1.0/g/feed", 302, "http://mockbin.com:80/jira/rest/gadgets/1.0/g/feed", false);

	}

	@Test
	public void testHttpsForwardToHttp() throws RootCertificateException, MalformedURLException, IOException, KeyManagementException, NoSuchAlgorithmException {
		AtlassianInternalHttpProxyConfig config = new AtlassianInternalHttpProxyConfig("mockbin.com", 80, false);
		this.proxy = new AtlassianInternalHttpProxy();
		this.proxy.init(config);

		String httpsNonProxiedContent = assertCodeAndReturnContent("https://mockbin.com/request/non/proxied", 200);
		assertThat(StringEscapeUtils.unescapeHtml4(httpsNonProxiedContent), Matchers.containsString("\"url\": \"https://mockbin.com/request/non/proxied\""));

		String httpsProxiedContent = assertCodeAndReturnContent("https://mockbin.com/request/rest/gadgets/1.0/g/feed", 200);
		assertThat(httpsProxiedContent, Matchers.containsString("\"url\": \"http://mockbin.com/request/rest/gadgets/1.0/g/feed\""));
	}

	@Test
	public void testHttpsForwardToHttps() throws RootCertificateException, MalformedURLException, IOException, KeyManagementException, NoSuchAlgorithmException {
		AtlassianInternalHttpProxyConfig config = new AtlassianInternalHttpProxyConfig("mockbin.com", 443, true);
		this.proxy = new AtlassianInternalHttpProxy();
		this.proxy.init(config);

		String httpsNonProxiedContent = assertCodeAndReturnContent("https://mockbin.com/request/non/proxied", 200);
		assertThat(unescapeHtml4(httpsNonProxiedContent), Matchers.containsString("\"url\": \"https://mockbin.com/request/non/proxied\""));

		String httpsProxiedContent = assertCodeAndReturnContent("https://mockbin.com/request/rest/gadgets/1.0/g/feed", 200);
		assertThat(httpsProxiedContent, containsString("\"url\": \"https://mockbin.com/request/rest/gadgets/1.0/g/feed\""));
	}

	@Test
	public void testChaininigWithJVMExistingProxy() throws MalformedURLException, IOException {
		List<String> jvmProxyInterceptedURLs = new ArrayList<>();
	    HttpProxyServer jvmProxy = DefaultHttpProxyServer.bootstrap()
	        .withPort(0)
	        .withFiltersSource(new HttpFiltersSourceAdapter() {
	        	@Override
	        	public HttpFilters filterRequest(HttpRequest originalRequest) {
	        		jvmProxyInterceptedURLs.add(originalRequest.getUri());
	        		return super.filterRequest(originalRequest);
	        	}
	        })
	        .start();

	    final String jvmProxyPort = "" + jvmProxy.getListenAddress().getPort();
		System.setProperty("http.proxyHost", "127.0.0.1");
		System.setProperty("http.proxyPort", jvmProxyPort);
		System.setProperty("https.proxyHost", "127.0.0.1");
		System.setProperty("https.proxyPort", jvmProxyPort);


	    AtlassianInternalHttpProxyConfig config = new AtlassianInternalHttpProxyConfig("mockbin.com", 80, false);
	    this.proxy = new AtlassianInternalHttpProxy();
	    this.proxy.init(config);

	    //Assert JVM proxy is used for non filtered request
		String exampleDotCom = "http://example.com/";
		sendGetAndAssert(exampleDotCom, 200, null, false);

		//Reset and assert JVM proxy is used for filtered request
		String contents = assertCodeAndReturnContent("http://nonexistingdomain.foo/request/jira/rest/gadgets/1.0/g/feed", 200, true);
		assertThat(unescapeHtml4(contents), containsString("\"url\": \"http://mockbin.com/request/jira/rest/gadgets/1.0/g/feed\""));

		//Assert that only the external request reaches the JVM proxy
		assertThat(jvmProxyInterceptedURLs, hasSize(1));
		assertThat(jvmProxyInterceptedURLs, contains(exampleDotCom));

		this.proxy.shutdown();

		assertThat(System.getProperty("http.proxyHost"), is("127.0.0.1"));
		assertThat(System.getProperty("http.proxyPort"), is(jvmProxyPort));
		assertThat(System.getProperty("https.proxyHost"), is("127.0.0.1"));
		assertThat(System.getProperty("https.proxyPort"), is(jvmProxyPort));
	}

	@Test
	public void testChaininigWithJVMExistingProxyWithHttpsSupport() throws MalformedURLException, IOException {
		List<String> jvmProxyInterceptedURLs = new ArrayList<>();
	    HttpProxyServer jvmProxy = DefaultHttpProxyServer.bootstrap()
	        .withPort(0)
	        .withFiltersSource(new HttpFiltersSourceAdapter() {
	        	@Override
	        	public HttpFilters filterRequest(HttpRequest originalRequest, ChannelHandlerContext clientCtx) {
	        		jvmProxyInterceptedURLs.add(originalRequest.getUri());
	    			if (ProxyUtils.isCONNECT(originalRequest)) {
	    			    String prefix = "https://" + originalRequest.getUri().replaceFirst(":443$", "");
	    			    clientCtx.channel().attr(AttributeKey.valueOf("connected_url")).set(prefix);
	                    return new HttpFiltersAdapter(originalRequest, clientCtx);
	                }
	        		return super.filterRequest(originalRequest);
	        	}
	        })
	        .start();

	    final String jvmProxyPort = "" + jvmProxy.getListenAddress().getPort();
		System.setProperty("http.proxyHost", "127.0.0.1");
		System.setProperty("http.proxyPort", jvmProxyPort);
		System.setProperty("https.proxyHost", "127.0.0.1");
		System.setProperty("https.proxyPort", jvmProxyPort);

		AtlassianInternalHttpProxyConfig config = new AtlassianInternalHttpProxyConfig("mockbin.com", 80, false);
		this.proxy = new AtlassianInternalHttpProxy();
		this.proxy.init(config);


		String httpsNonProxiedContent = assertCodeAndReturnContent("https://mockbin.com/request/non/proxied", 200);
		assertThat(unescapeHtml4(httpsNonProxiedContent), Matchers.containsString("\"url\": \"https://mockbin.com/request/non/proxied\""));

		String httpsProxiedContent = assertCodeAndReturnContent("https://mockbin.com/request/rest/gadgets/1.0/g/feed", 200);
		assertThat(httpsProxiedContent, containsString("\"url\": \"http://mockbin.com/request/rest/gadgets/1.0/g/feed\""));

		//Assert only the connect reaches the external proxy
		assertThat(jvmProxyInterceptedURLs, hasSize(1));
		assertThat(jvmProxyInterceptedURLs, contains("mockbin.com:443"));

	}


	private void sendGetAndAssert(String url, int expectedCode, String expectedLocation, boolean follow) throws MalformedURLException, IOException {
		HttpURLConnection con = (HttpURLConnection)new URL(url).openConnection();
		con.setInstanceFollowRedirects(follow);

		int responseCode = con.getResponseCode();

		assertThat(responseCode, is(expectedCode));
		assertThat(con.getHeaderField("Location"), is(expectedLocation));
	}

	private String assertCodeAndReturnContent(String url, int expectedCode) throws MalformedURLException, IOException {
		return assertCodeAndReturnContent(url, expectedCode, false);
	}

	private String assertCodeAndReturnContent(String url, int expectedCode, boolean follow) throws MalformedURLException, IOException {
		HttpURLConnection con = (HttpURLConnection)new URL(url).openConnection();
		con.setInstanceFollowRedirects(follow);

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
