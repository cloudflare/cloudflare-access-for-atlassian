package com.cloudflare.access.atlassian.base.support;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class GithubVersionProvider implements RemoteVersionProvider{

	@Override
	public String getLatestReleaseVersion() {
		final String latestReleaseUrl = "https://api.github.com/repos/cloudflare/cloudflare-access-for-atlassian/releases/latest";

		try(CloseableHttpClient http = HttpClients.createMinimal()){
			HttpGet request = new HttpGet(latestReleaseUrl);
			//TODO remove to release
			request.setHeader("Authorization", "Basic " + Base64.encodeBase64String("felipebn:8cb7b1ef19465da8a5b32b79b75d864e572e7622".getBytes(StandardCharsets.UTF_8)));

			CloseableHttpResponse response = http.execute(request);

			//TODO parse response
			String json = EntityUtils.toString(response.getEntity());

			JsonNode root = new ObjectMapper().readTree(json);
			String tagName = root.get("tag_name").asText();

			return tagName.replaceFirst("^v([0-9.])", "\\1");
		} catch (ParseException | IOException e) {
			throw new RuntimeException("Unable to obtain the remote version", e);
		}
	}


}
