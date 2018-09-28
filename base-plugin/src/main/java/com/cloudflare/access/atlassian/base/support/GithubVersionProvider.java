package com.cloudflare.access.atlassian.base.support;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

import java.util.concurrent.TimeUnit;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

@Component
public class GithubVersionProvider implements RemoteVersionProvider{

	private static final Logger log = LoggerFactory.getLogger(GithubVersionProvider.class);
	private Supplier<String> fetchResult;

	public GithubVersionProvider() {
		this.fetchResult = Suppliers.memoizeWithExpiration(this::fetchLatestReleasedVersion, 5, TimeUnit.MINUTES);
	}

	@Override
	public String getLatestReleaseVersion() {
		return fetchResult.get();
	}

	private String fetchLatestReleasedVersion() {
		final String latestReleaseUrl = "https://api.github.com/repos/cloudflare/cloudflare-access-for-atlassian/releases/latest";

		try(CloseableHttpClient http = HttpClients.createMinimal()){
			log.debug("Trying to fetch latest release version from GH API: {}", latestReleaseUrl);
			HttpGet request = new HttpGet(latestReleaseUrl);

			CloseableHttpResponse response = http.execute(request);

			String json = EntityUtils.toString(response.getEntity());
			log.debug("Received JSON: {}", json);

			JsonNode root = new ObjectMapper().readTree(json);
			String tagName = root.get("tag_name").asText();
			String semanticVersion = defaultIfBlank(tagName, "").replaceFirst("^v", "");

			log.debug("Latest release tag name and semantic version: [tagName:{}, version:{}]", tagName, semanticVersion);
			return semanticVersion;
		} catch (Exception e) {
			log.error("Plugin update check failed, reason: " + e.getMessage(), e);
			return "";
		}
	}

}
