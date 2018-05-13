package com.cloudflare.access.atlassian.common;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.cloudflare.access.atlassian.common.exception.CertificateProcessingException;
import com.cloudflare.access.atlassian.common.http.SimpleHttp;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CertificateProvider {

	private final SimpleHttp http;

	public CertificateProvider(SimpleHttp http) {
		this.http = http;
	}

	public List<String> getCerticatesAsJson(String url){
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
