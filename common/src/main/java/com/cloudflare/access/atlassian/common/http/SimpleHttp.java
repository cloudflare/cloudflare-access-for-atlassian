package com.cloudflare.access.atlassian.common.http;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.cloudflare.access.atlassian.common.exception.HttpRequestException;

public class SimpleHttp {

	public String get(String url) {
		try(CloseableHttpClient http = HttpClients.createMinimal()){
			CloseableHttpResponse response = http.execute(new HttpGet(url));
			return EntityUtils.toString(response.getEntity());
		} catch (Exception e) {
			e.printStackTrace();
			throw new HttpRequestException(String.format("Error executing request to URL '%s': %s", url, e.getMessage()), e);
		}
	}


}
