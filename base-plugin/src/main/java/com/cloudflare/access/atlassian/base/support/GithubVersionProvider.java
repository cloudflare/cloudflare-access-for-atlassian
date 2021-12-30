package com.cloudflare.access.atlassian.base.support;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

import java.util.concurrent.TimeUnit;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.cloudflare.access.atlassian.common.http.SimpleHttp;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

@Component
public class GithubVersionProvider implements RemoteVersionProvider{

	private static final Logger log = LoggerFactory.getLogger(GithubVersionProvider.class);
	private final Supplier<String> fetchResult;
	private final String latestReleaseCheckUrl;

	public GithubVersionProvider() {
		this("https://api.github.com/repos/cloudflare/cloudflare-access-for-atlassian/releases/latest");
	}

	GithubVersionProvider(String latestReleaseCheckUrl){
		this.latestReleaseCheckUrl = latestReleaseCheckUrl;
		this.fetchResult = Suppliers.memoizeWithExpiration(this::fetchLatestReleasedVersion, 5, TimeUnit.MINUTES);
	}

	@Override
	public String getLatestReleaseVersion() {
		return fetchResult.get();
	}

	private String fetchLatestReleasedVersion() {
		String json = "REQUEST_NOT_EXECUTED";

		try(CloseableHttpClient http = SimpleHttp.httpClientWithTimeout()){
			log.debug("Trying to fetch latest release version from GH API: {}", this.latestReleaseCheckUrl);
			HttpGet request = new HttpGet(this.latestReleaseCheckUrl);

			CloseableHttpResponse response = http.execute(request);

			json = EntityUtils.toString(response.getEntity());
			log.debug("Received JSON: {}", json);

			JsonNode root = new ObjectMapper().readTree(json);
			String tagName = root.get("tag_name").asText();
			String semanticVersion = defaultIfBlank(tagName, "").replaceFirst("^v", "");

			log.debug("Latest release tag name and semantic version: [tagName:{}, version:{}]", tagName, semanticVersion);
			return semanticVersion;
		} catch (Exception e) {
			log.error("Plugin update check failed, GH api response: " + json, e);
			return "";
		}
	}

}
