package com.cloudflare.access.atlassian.common;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.cloudflare.access.atlassian.common.context.AuthenticationContext;

class TestVerificationContext implements AuthenticationContext{

	private String audience;
	private String issuer;
	private List<String> jwkJsons;
	private Clock clock;

	public TestVerificationContext() {
		super();
		this.audience = "b265846c85e70aa40655a76e53858df6c79cb2d0453feff495f3c5b795fb4d5e";
		this.issuer = "https://cfaplugin.cloudflareaccess.com";
		LocalDateTime nineOClock = LocalDateTime.of(2018, Month.MAY, 10, 20, 00);
		this.clock = Clock.fixed(nineOClock.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
		this.jwkJsons = new ArrayList<>();
		this.jwkJsons.add("{\n" +
				"			\"kid\": \"bccdf99ac336c9278e3c7ac71bebcbe467bbbfd1fb013c84c93889da077b9d79\",\n" +
				"			\"kty\": \"RSA\",\n" +
				"			\"alg\": \"RS256\",\n" +
				"			\"use\": \"sig\",\n" +
				"			\"e\": \"AQAB\",\n" +
				"			\"n\": \"x4SANlqANMzNkJfruQU8RbwZ6B_N-ed4b5-CscBHV1sIR3Fo6q-1BF7votDBIJ05q3ahKNYIqHDF7xiKhVCHxXVwYuh3HULcetylHSh-I6C_P66mLxHLLagLSvsmr6ZdCqdEsDJS33RUJtlIODt-3OBaPqyfUjy4Ql5d7LC5bfAztX3RyKwkrlT2X9o62-GmbgvJGwFrPISyWUHF9trH82oaxtN3TxZ4L3LKBkPdexIPuTJGs4wse0pMd-v7439E_Quzm-eM61eXM4IP5YEf5sjBxbGsZgqcNuEM_2S_K9AKyj0mOleSoBAfFCCkz2QZTn7jHXz2yreLbpPSXY9e3Q\"\n" +
				"		}");
		this.jwkJsons.add("{\n" +
				"			\"kid\": \"facb70eaf23a06573eb449601e345b8d2de562fb943de556d2cfda34870f7629\",\n" +
				"			\"kty\": \"RSA\",\n" +
				"			\"alg\": \"RS256\",\n" +
				"			\"use\": \"sig\",\n" +
				"			\"e\": \"AQAB\",\n" +
				"			\"n\": \"tFw6PCssph26IC8gkdGCYmRDJWdZnp3tiWqkcJ_ILgRr-FdwITaIroCNWXoB7DoqttjXUSwDgE9emtMk6wo_Lt3jAwmjrkH6Fo663GfcZy0BHf9s8Y2F2cXtgsWu7oKeeXJuh_7fGsmJ44gTKAEa_w7cx0nmRjPlecDWThQMFsejttMuavXfLbtnNfSiJ5VMHuZHfqHW4jCCUYFcrOzaqIWG0covhyrdbIF8oip0wpWtyET7jbuxhASNsJQfBQET-Gkdzd8wvHE4qOdUwlrtgJuLgW2S7MBgBtBmPG3n9cQ4asJRUUE-CXw6PjYbVCyVJ7ifjoo_knWEZpQXhl1rAQ\"\n" +
				"		}");
	}

	TestVerificationContext withAudience(String audience) {
		this.audience = audience;
		return this;
	}

	TestVerificationContext withIssuer(String issuer) {
		this.issuer = issuer;
		return this;
	}

	TestVerificationContext withClock(Clock clock) {
		this.clock = clock;
		return this;
	}

	TestVerificationContext withJwkJson(String jwkJson) {
		this.jwkJsons = Collections.singletonList(jwkJson);
		return this;
	}

	@Override
	public String getAudience() {
		return this.audience;
	}

	@Override
	public String getIssuer() {
		return this.issuer;
	}

	@Override
	public List<String> getSigningKeyAsJson() {
		return this.jwkJsons;
	}

	@Override
	public String getLogoutUrl() {
		return "unusedhere";
	}

	@Override
	public Clock getClock() {
		return this.clock;
	}

}