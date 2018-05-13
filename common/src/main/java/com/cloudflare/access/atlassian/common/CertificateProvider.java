package com.cloudflare.access.atlassian.common;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.cloudflare.access.atlassian.common.exception.CertificateProcessingException;
import com.cloudflare.access.atlassian.common.http.SimpleHttp;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class CertificateProvider {

	private final LoadingCache<String, List<String>> certificateCache;
	private final SimpleHttp http;

	public CertificateProvider(SimpleHttp http) {
		this.http = http;
		this.certificateCache = CacheBuilder
				.newBuilder()
				.expireAfterWrite(1, TimeUnit.MINUTES)
				.build(new CacheLoader<String, List<String>>(){
					@Override
					public List<String> load(String url) throws Exception {
						return loadCertificatesAsJson(url);
					}
				});
	}

	public List<String> getCerticatesAsJson(String url){
		try {
			return certificateCache.get(url);
		} catch (ExecutionException e) {
			e.printStackTrace();
			throw new CertificateProcessingException(String.format("Unable to load certs from Cache '%s': %s", url, e.getMessage()), e);
		}
	}

	public List<String> loadCertificatesAsJson(String url){
		try {
			String certsJson = http.get(url);
			return parseJson(certsJson);
		} catch (Exception e) {
			e.printStackTrace();
			throw new CertificateProcessingException(String.format("Unable to request/parse certs from URL '%s': %s", url, e.getMessage()), e);
		}
	}

	private List<String> parseJson(String certsJsonObject) throws IOException, JsonProcessingException {
		List<String> certificatesAsJson = new ArrayList<>();
		ObjectMapper jackson = new ObjectMapper();
		JsonNode root = jackson.readTree(certsJsonObject);
		for (JsonNode keysItem : root.withArray("keys")) {
			certificatesAsJson.add(jackson.writeValueAsString(keysItem));
		}
		return certificatesAsJson;
	}

}
