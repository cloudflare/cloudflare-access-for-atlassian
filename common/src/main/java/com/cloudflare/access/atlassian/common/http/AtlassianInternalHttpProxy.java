package com.cloudflare.access.atlassian.common.http;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.client.utils.URIBuilder;
import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersAdapter;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
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


//TODO handle chained proxy
public class AtlassianInternalHttpProxy {

	public static final Logger log = LoggerFactory.getLogger(AtlassianInternalHttpProxy.class);
	public static final AtlassianInternalHttpProxy INSTANCE = new AtlassianInternalHttpProxy();
	private HttpProxyServer server;

	private AtlassianInternalHttpProxy() {}

	public void init(AtlassianInternalHttpProxyConfig config) {
		this.shutdown();

		this.server =
			    DefaultHttpProxyServer.bootstrap()
			        .withPort(0)
			        .withFiltersSource(new LocalServerForwardAdapter(config.getAddress(), config.getPort()))
			        .start();

		System.setProperty("http.proxyHost", "127.0.0.1");
		System.setProperty("http.proxyPort", String.valueOf(server.getListenAddress().getPort()));
	}

	public void shutdown() {
		if(this.server != null) {
			this.server.stop();
			System.setProperty("http.proxyHost", "");
			System.setProperty("http.proxyPort", "");
		}
	}

	private final class LocalServerForwardAdapter extends HttpFiltersSourceAdapter {
		private final String hostToForward;
		private final int portToForward;

		private LocalServerForwardAdapter(String hostToForward, int portToForward) {
			this.hostToForward = hostToForward;
			this.portToForward = portToForward;
		}

		@Override
		public HttpFilters filterRequest(HttpRequest originalRequest, ChannelHandlerContext ctx) {
		    return new HttpFiltersAdapter(originalRequest) {
		        @Override
		        public HttpResponse clientToProxyRequest(HttpObject httpObject) {
		        	log.debug("Intercepting outgoing request...");
		        	if (shouldRewrite(httpObject)) {
		        		HttpRequest httpRequest = (HttpRequest) httpObject;
						URI localUrl = rewriteUri(httpRequest);
						log.debug("Sending redirect: \n\tFrom: {}\n\tTo: {}", httpRequest.getUri(), localUrl);
	        			DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FOUND);
	        			HttpHeaders.setHeader(response, Names.CONNECTION, Values.CLOSE);
	        			HttpHeaders.setHeader(response, Names.LOCATION, localUrl.toString());
	        			return response;
		        	}
		        	return null;
		        }

		        private boolean shouldRewrite(HttpObject httpObject) {
		        	if (httpObject instanceof HttpRequest) {
		        		HttpRequest httpRequest = (HttpRequest) httpObject;
		        		if (httpRequest.getUri().matches("^.*/rest/gadgets/.*$")) {
		        			return true;
		        		}
		        	}
		        	return false;
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
		    };
		}
	}

}
