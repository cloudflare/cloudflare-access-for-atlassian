package com.cloudflare.access.atlassian.common.http;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersAdapter;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import org.littleshoot.proxy.impl.ProxyUtils;
import org.littleshoot.proxy.mitm.Authority;
import org.littleshoot.proxy.mitm.CertificateSniffingMitmManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpHeaders.Names;
import io.netty.handler.codec.http.HttpHeaders.Values;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.util.AttributeKey;


//TODO handle chained proxy
public class AtlassianInternalHttpProxy {

	public static final Logger log = LoggerFactory.getLogger(AtlassianInternalHttpProxy.class);
	public static final AtlassianInternalHttpProxy INSTANCE = new AtlassianInternalHttpProxy();
	private HttpProxyServer server;

	private AtlassianInternalHttpProxy() {}

	public void init(AtlassianInternalHttpProxyConfig config) {
		try {
			this.shutdown();

			Authority authority = createCertificateAuthority();
			this.server =
				    DefaultHttpProxyServer.bootstrap()
				        .withPort(0)
				        .withManInTheMiddle(new CertificateSniffingMitmManager(authority))
				        .withFiltersSource(new LocalServerForwardAdapter(config))
				        .start();

			setupJvmProxyProperties("127.0.0.1", String.valueOf(server.getListenAddress().getPort()));
			setupJvmSSLContext();
		}catch (Throwable e) {
			throw new RuntimeException("Unable to create an internal proxy!", e);
		}
	}

	private Authority createCertificateAuthority() {
		File keyStoreDir = new File("cloudflare-access-atlassian-plugin");
		keyStoreDir.mkdir();

		Authority authority = new Authority(keyStoreDir,
											"cfaccess-plugin",
											"changeit".toCharArray(),
											"Internal proxy to keep internal atlassian product requests in the local network",
											"LittleProxy-mitm on Cloudflare Access Plugin",
											"Certificate Authority", "LittleProxy-mitm on Cloudflare Access Plugin",
											"Internal proxy to keep internal atlassian product requests in the local network");
		return authority;
	}

	public void shutdown() {
		if(this.server != null) {
			this.server.stop();
			setupJvmProxyProperties("", "");
		}
	}

	private void setupJvmProxyProperties(String host, String port) {
		System.setProperty("http.proxyHost", host);
		System.setProperty("http.proxyPort", port);

		//TODO read whatever is in the property already
		System.setProperty("http.nonProxyHosts", "localhost");

		System.setProperty("https.proxyHost", host);
		System.setProperty("https.proxyPort", port);
	}

	private void setupJvmSSLContext() {
		try {
			TrustManager[] trustManagers = InsecureTrustManagerFactory.INSTANCE
					.getTrustManagers();
			SSLContext ctx = SSLContext.getInstance("TLS");
			ctx.init(null, trustManagers, null);
			SSLContext.setDefault(ctx);
		}catch (Throwable e) {
			throw new RuntimeException("Unable to setup the SSL context!", e);
		}
	}

	private static final class LocalServerForwardAdapter extends HttpFiltersSourceAdapter {
		private final AttributeKey<String> CONNECTED_URL = AttributeKey.valueOf("connected_url");
		private AtlassianInternalHttpProxyConfig config;

		public LocalServerForwardAdapter(AtlassianInternalHttpProxyConfig config) {
			this.config = config;
		}

		@Override
		public HttpFilters filterRequest(HttpRequest originalRequest, ChannelHandlerContext clientCtx) {
			if (ProxyUtils.isCONNECT(originalRequest)) {
				log.debug("Proxy intercepting connect request...");
            	setContextConnectedAttribute(clientCtx, originalRequest.getUri());
                return new HttpFiltersAdapter(originalRequest, clientCtx);
            }

			String connectedUrl = getConnectedUrlFromChannel(clientCtx);
			if(connectedUrl != null) {
				log.debug("Proxying HTTPS request...");
				return new LocalHttpsFilter(originalRequest, connectedUrl, config);
			}

			log.debug("Proxying HTTP request...");
		    return new LocalHttpFilter(originalRequest, config);
		}

		private String getConnectedUrlFromChannel(ChannelHandlerContext clientCtx) {
			return clientCtx.channel().attr(CONNECTED_URL).get();
		}

		private void setContextConnectedAttribute(ChannelHandlerContext clientCtx, String uri) {
			if (clientCtx != null) {
			    String prefix = "https://" + uri.replaceFirst(":443$", "");
			    log.debug("Proxy channel CONNECTED_URL set to {}", prefix);
			    clientCtx.channel().attr(CONNECTED_URL).set(prefix);
			}
		}

	}



	/**
	 *
	 * Filters HTTP requests forwarding them
	 * with a 302 status code to a local
	 * address.
	 *
	 */
	private static final class LocalHttpFilter extends HttpFiltersAdapter {
		private String hostToForward;
		private int portToForward;

		public LocalHttpFilter(HttpRequest originalRequest, AtlassianInternalHttpProxyConfig config) {
			super(originalRequest);
			this.hostToForward = config.getAddress();
			this.portToForward = config.getPort();
		}

		@Override
		public HttpResponse clientToProxyRequest(HttpObject httpObject) {
			log.debug("Proxy intercepting HTTP request...");
			if (RewriteRule.shouldRewrite(httpObject)) {
				HttpRequest httpRequest = (HttpRequest) httpObject;
				URI localUrl = rewriteUri(httpRequest);
				log.debug("Proxy redirecting: \n\tFrom: {}\n\tTo: {}", httpRequest.getUri(), localUrl);
				DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FOUND);
				HttpHeaders.setHeader(response, Names.CONNECTION, Values.CLOSE);
				HttpHeaders.setHeader(response, Names.LOCATION, localUrl.toString());
				return response;
			}
			return null;
		}

		private URI rewriteUri(HttpRequest httpRequest){
			try {
				URIBuilder uriBuilder;
				uriBuilder = new URIBuilder(httpRequest.getUri());
				uriBuilder.setHost(hostToForward);
				uriBuilder.setPort(portToForward);
				URI localUrl = uriBuilder.build();
				return localUrl;
			} catch (URISyntaxException e) {
				throw new RuntimeException("Unable to rewrite " + httpRequest.getUri(), e);
			}
		}
	}

	/**
	 *
	 * Filters HTTPS requests by
	 * doing a HTTP request to the local
	 * address.
	 *
	 */
	private static final class LocalHttpsFilter extends HttpFiltersAdapter {
		private final String finalUri;

		public LocalHttpsFilter(HttpRequest originalRequest, String connectedUrl, AtlassianInternalHttpProxyConfig config) {
			super(originalRequest);
			this.finalUri = rewriteUri(connectedUrl + originalRequest.getUri(), config);
		}

		private String rewriteUri(String requestedUri, AtlassianInternalHttpProxyConfig config){
			try {
				URIBuilder uriBuilder;
				uriBuilder = new URIBuilder(requestedUri);
				if(config.shouldUseHttps()) {
					uriBuilder.setScheme("https");
				}else {
					uriBuilder.setScheme("http");
				}
				uriBuilder.setHost(config.getAddress());
				uriBuilder.setPort(config.getPort());
				URI localUrl = uriBuilder.build();
				return localUrl.toString();
			} catch (URISyntaxException e) {
				throw new RuntimeException("Unable to rewrite " + requestedUri, e);
			}
		}

		@Override
		public HttpObject proxyToClientResponse(HttpObject httpObject) {
			log.debug("Proxy filtering HTTPS response to {}", finalUri);
		    if(RewriteRule.shouldRewrite(finalUri)) {
		    	log.debug("Proxy replacing HTTPS response by sending HTTP request to {}", finalUri);
		    	try(CloseableHttpResponse proxyGet = HttpClientBuilder
		    		.create()
		    		.build()
		    		.execute(new HttpGet(finalUri))){

		    		HttpEntity entity = proxyGet.getEntity();
		    		byte[] bytes = EntityUtils.toByteArray(entity);

		    		log.debug("Creating new response from {}", finalUri);

		        	DefaultFullHttpResponse newResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
		        	HttpHeaders.setHeader(newResponse, Names.CONNECTION, Values.CLOSE);
		        	HttpHeaders.setHeader(newResponse, Names.CONTENT_LENGTH, bytes.length);
		        	newResponse.content().writeBytes(bytes);
		    		return newResponse;
		    	} catch (Throwable e) {
		    		log.error("Unable to replace HTTPS proxied request response", e);
					throw new RuntimeException("Unable to replace HTTPS proxied request response", e);
				}
		    }
			return httpObject;
		}

	}

	private static class RewriteRule{

		static boolean shouldRewrite(HttpObject httpObject) {
			if (httpObject instanceof HttpRequest) {
				return shouldRewrite(((HttpRequest) httpObject).getUri());
			}
			return false;
		}

		static boolean shouldRewrite(String uri) {
			if (uri.matches("^.*/rest/gadgets/.*$")) {
				return true;
			}
			return false;
		}
	}
}
