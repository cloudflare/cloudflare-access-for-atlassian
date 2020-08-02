package com.cloudflare.access.atlassian.common;

import org.apache.cxf.rs.security.jose.jwk.JsonWebKey;
import org.apache.cxf.rs.security.jose.jwk.JsonWebKeys;
import org.apache.cxf.rs.security.jose.jwk.JwkUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudflare.access.atlassian.common.exception.CertificateProcessingException;
import com.cloudflare.access.atlassian.common.http.SimpleHttp;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class CertificateProvider {

	private static final Logger log = LoggerFactory.getLogger(CertificateProvider.class);

	private final LoadingCache<String, JsonWebKeys> jwkSetCache;

	private SimpleHttp http;

	public CertificateProvider(SimpleHttp http) {
		this.http = http;
		this.jwkSetCache = CacheBuilder
				.newBuilder()
				.build(CacheLoader.from(this::loadJwk));
	}

	public JsonWebKey getJwk(String url, String kid) {
		JsonWebKey jwk = jwkSetCache.getUnchecked(url).getKey(kid);
		if(jwk == null) {
			jwkSetCache.invalidate(url);
			jwk = jwkSetCache.getUnchecked(url).getKey(kid);
		}
		return jwk;
	}

	private JsonWebKeys loadJwk(String url) {
		try {
			log.info("Loading JWKs from {} ...", url);
			return JwkUtils.readJwkSet(http.get(url));
		} catch (Exception e) {
			e.printStackTrace();
			throw new CertificateProcessingException(String.format("Unable to request/parse certs from URL '%s': %s", url, e.getMessage()), e);
		}
	}
}
