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
		this.jwkJsons.add("{\n" + 
				"			\"kid\": \"6593d9acf92b87faca750a78f7b308a844a57ae51c25ed7322fe06d6d8a6b9b0\",\n" + 
				"			\"kty\": \"RSA\",\n" + 
				"			\"alg\": \"RS256\",\n" + 
				"			\"use\": \"sig\",\n" + 
				"			\"e\": \"AQAB\",\n" + 
				"			\"n\": \"z5kk3ksqN2_3HRc1I0Wqfmpyii-2GeZiczd1BdUaZCm59HuRpTzFy44sP0Vo1ND8oJA33CnotkSX5TWZpZNfeD3B0HX3GRqYTQxu08le3gL91GlHgJ_yMyNl2tLCTUqDDhFs1YGM0PJCNgsasQHNkaY3bSSkcUlYyoPWWEyPPmU5eOhUbcjRb9sGJV03HyA-93GXEZIVrX4aIdPu7dteMuAb7YiMJ6nnldouydIqmlK4N6iIvPOXQoBuMOIARSnjy6AVYh8pCc9yJPwMn_SxK27HEmfFDxx8Ed_oYWm3-kONhVoTTGd5p9fDX4i2_D2mWMvJ_EhTRlWIA-Qc962O3w\"\n" + 
				"		}");
		this.jwkJsons.add("{\n" + 
				"			\"kid\": \"7b956c1e7ab1ea3a3b4649adbe6ac2cd546e9d373d3b942cfc0c71b4c58f9457\",\n" + 
				"			\"kty\": \"RSA\",\n" + 
				"			\"alg\": \"RS256\",\n" + 
				"			\"use\": \"sig\",\n" + 
				"			\"e\": \"AQAB\",\n" + 
				"			\"n\": \"1FsOGfFsdlWJwQlWO5gM_RfzO3EsZOCDPCUR0ltc3f4z89mQEljuMkEgsIQ-0GdZluuo7ucp_CilqOeFOck6QjjNWPAzwkpD1nbvfboBZ2RwHlWlLrY4cubCv63cK1447Zwf_KobylRXpV0rDWw0NUPKHK0YO4rD5eikr2DXwbNIMFmXNxcXxhtAYVgjgNjkkc4hZosvM4KofrTviUQtCwIy2agwpe5PUF1gq7P-jnrhyPFhiV2PW8a820re7Bfg5YoGyUEwwrO4NAjfKb6zRexol4TJAWwSD4kYTc93AVh-Sw1ESbWeNAlLemM3iHhOLhyB0F-J9lfIdE9cMIB7jw\"\n" + 
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