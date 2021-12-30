package com.cloudflare.access.atlassian.common.http;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import com.cloudflare.access.atlassian.common.exception.HttpRequestException;

public class SimpleHttp {

	public String get(String url) {
		try(CloseableHttpClient http = httpClientWithTimeout()){
			CloseableHttpResponse response = http.execute(new HttpGet(url));
			return EntityUtils.toString(response.getEntity());
		} catch (Exception e) {
			e.printStackTrace();
			throw new HttpRequestException(String.format("Error executing request to URL '%s': %s", url, e.getMessage()), e);
		}
	}

	public static CloseableHttpClient httpClientWithTimeout() {
		return HttpClientBuilder.create()
				.setDefaultRequestConfig(defaultRequestConfigBuilder().build())
				.build();
	}

	public static RequestConfig.Builder defaultRequestConfigBuilder() {
		return RequestConfig.custom()
				  .setConnectTimeout(10 * 1000)
				  .setConnectionRequestTimeout(10 * 1000)
				  .setSocketTimeout(10 * 1000);
	}
}
