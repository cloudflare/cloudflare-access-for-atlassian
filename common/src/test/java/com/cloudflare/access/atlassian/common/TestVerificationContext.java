package com.cloudflare.access.atlassian.common;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.cxf.rs.security.jose.jwk.JsonWebKey;
import org.apache.cxf.rs.security.jose.jwk.JsonWebKeys;
import org.apache.cxf.rs.security.jose.jwk.JwkUtils;

import com.cloudflare.access.atlassian.common.context.AuthenticationContext;

class TestVerificationContext implements AuthenticationContext{

	private final JsonWebKeys jwkSet;
	private Set<String> audiences;
	private String issuer;
	private Clock clock;

	public TestVerificationContext() {
		super();
		withAudiences("b265846c85e70aa40655a76e53858df6c79cb2d0453feff495f3c5b795fb4d5e");
		withIssuer("https://cfaplugin.cloudflareaccess.com");
		LocalDateTime nineOClock = LocalDateTime.of(2018, Month.MAY, 10, 20, 00);
		withClock(Clock.fixed(nineOClock.toInstant(ZoneOffset.UTC), ZoneOffset.UTC));
		List<String> jwkJsons = new ArrayList<>();
		jwkJsons.add("{\n" +
				"			\"kid\": \"bccdf99ac336c9278e3c7ac71bebcbe467bbbfd1fb013c84c93889da077b9d79\",\n" +
				"			\"kty\": \"RSA\",\n" +
				"			\"alg\": \"RS256\",\n" +
				"			\"use\": \"sig\",\n" +
				"			\"e\": \"AQAB\",\n" +
				"			\"n\": \"x4SANlqANMzNkJfruQU8RbwZ6B_N-ed4b5-CscBHV1sIR3Fo6q-1BF7votDBIJ05q3ahKNYIqHDF7xiKhVCHxXVwYuh3HULcetylHSh-I6C_P66mLxHLLagLSvsmr6ZdCqdEsDJS33RUJtlIODt-3OBaPqyfUjy4Ql5d7LC5bfAztX3RyKwkrlT2X9o62-GmbgvJGwFrPISyWUHF9trH82oaxtN3TxZ4L3LKBkPdexIPuTJGs4wse0pMd-v7439E_Quzm-eM61eXM4IP5YEf5sjBxbGsZgqcNuEM_2S_K9AKyj0mOleSoBAfFCCkz2QZTn7jHXz2yreLbpPSXY9e3Q\"\n" +
				"		}");
		jwkJsons.add("{\n" +
				"			\"kid\": \"facb70eaf23a06573eb449601e345b8d2de562fb943de556d2cfda34870f7629\",\n" +
				"			\"kty\": \"RSA\",\n" +
				"			\"alg\": \"RS256\",\n" +
				"			\"use\": \"sig\",\n" +
				"			\"e\": \"AQAB\",\n" +
				"			\"n\": \"tFw6PCssph26IC8gkdGCYmRDJWdZnp3tiWqkcJ_ILgRr-FdwITaIroCNWXoB7DoqttjXUSwDgE9emtMk6wo_Lt3jAwmjrkH6Fo663GfcZy0BHf9s8Y2F2cXtgsWu7oKeeXJuh_7fGsmJ44gTKAEa_w7cx0nmRjPlecDWThQMFsejttMuavXfLbtnNfSiJ5VMHuZHfqHW4jCCUYFcrOzaqIWG0covhyrdbIF8oip0wpWtyET7jbuxhASNsJQfBQET-Gkdzd8wvHE4qOdUwlrtgJuLgW2S7MBgBtBmPG3n9cQ4asJRUUE-CXw6PjYbVCyVJ7ifjoo_knWEZpQXhl1rAQ\"\n" +
				"		}");
		jwkJsons.add("{\n" +
				"			\"kid\": \"6593d9acf92b87faca750a78f7b308a844a57ae51c25ed7322fe06d6d8a6b9b0\",\n" +
				"			\"kty\": \"RSA\",\n" +
				"			\"alg\": \"RS256\",\n" +
				"			\"use\": \"sig\",\n" +
				"			\"e\": \"AQAB\",\n" +
				"			\"n\": \"z5kk3ksqN2_3HRc1I0Wqfmpyii-2GeZiczd1BdUaZCm59HuRpTzFy44sP0Vo1ND8oJA33CnotkSX5TWZpZNfeD3B0HX3GRqYTQxu08le3gL91GlHgJ_yMyNl2tLCTUqDDhFs1YGM0PJCNgsasQHNkaY3bSSkcUlYyoPWWEyPPmU5eOhUbcjRb9sGJV03HyA-93GXEZIVrX4aIdPu7dteMuAb7YiMJ6nnldouydIqmlK4N6iIvPOXQoBuMOIARSnjy6AVYh8pCc9yJPwMn_SxK27HEmfFDxx8Ed_oYWm3-kONhVoTTGd5p9fDX4i2_D2mWMvJ_EhTRlWIA-Qc962O3w\"\n" +
				"		}");
		jwkJsons.add("{\n" +
				"			\"kid\": \"7b956c1e7ab1ea3a3b4649adbe6ac2cd546e9d373d3b942cfc0c71b4c58f9457\",\n" +
				"			\"kty\": \"RSA\",\n" +
				"			\"alg\": \"RS256\",\n" +
				"			\"use\": \"sig\",\n" +
				"			\"e\": \"AQAB\",\n" +
				"			\"n\": \"1FsOGfFsdlWJwQlWO5gM_RfzO3EsZOCDPCUR0ltc3f4z89mQEljuMkEgsIQ-0GdZluuo7ucp_CilqOeFOck6QjjNWPAzwkpD1nbvfboBZ2RwHlWlLrY4cubCv63cK1447Zwf_KobylRXpV0rDWw0NUPKHK0YO4rD5eikr2DXwbNIMFmXNxcXxhtAYVgjgNjkkc4hZosvM4KofrTviUQtCwIy2agwpe5PUF1gq7P-jnrhyPFhiV2PW8a820re7Bfg5YoGyUEwwrO4NAjfKb6zRexol4TJAWwSD4kYTc93AVh-Sw1ESbWeNAlLemM3iHhOLhyB0F-J9lfIdE9cMIB7jw\"\n" +
				"		}");
		jwkJsons.add("{\n" +
				"           \"kid\": \"B5MS0p9n7iTjy3UmzrhjFRdRlAJY0hhwXS9bg85Msm0\",\n" +
				"           \"kty\": \"RSA\",\n" +
				"           \"alg\": \"RS256\",\n" +
				"           \"use\": \"sig\",\n" +
				"           \"e\": \"AQAB\",\n" +
				"           \"n\": \"huLKORZdrfihcfFPe2zeR2ToexvJKXnxX_Ynz2AA5tiDzQzFjjcYfkmt0s4b9yLXuw-LGeo7mH5sJeWeEITW_bUhaMUdHPmbGI5ouNilgy73pocQvicIY8JNLKplm5tWFt_zu3k3D2YuULsN7AkaePyYvWswCFiA7RlT6GznaxY9BjIsfaMaCGMLb8M5IWf5pKCLJflQXCEdeoHxIH9YNXZ2ifucLqZnv_my1twzyi-QHF0u5B74IpTYBHNqlzE2A-_JvzVYs4S_xqd6_YGE4tZ0OCMhjKSdVZ1xxFyHW3rYZpTsKxB20elKWIzErxx3h31Eb7NlPXgPyc34VLeqtw\"\n" +
				"       }");
		jwkJsons.add("{\n" +
				"    \"kty\": \"RSA\",\n" +
				"    \"e\": \"AQAB\",\n" +
				"    \"use\": \"sig\",\n" +
				"    \"kid\": \"LclHlXOdxeA7idWcI6JRWaGuSKg=\",\n" +
				"    \"alg\": \"RS256\",\n" +
				"    \"n\": \"kNRbOFoHAmxbkFdD3EtLrB8RxE0M4ooABZgZQ4FuJ5izsVy9o1v3eWFimkog8P4pylQMlb1zN0A_TM6KY6l0zEzb1xUaWyu-K04ntJO4HwDdjTZT4Wwu9hIm8__2X7jlMATpzSSmEZZochutHQtvM8-3au8AsS0sbxAclin6EF90DneRen1YJExlsDO2XjcacoirV3yIkNqJWUO-FSvOWGVe2wCYhupdA91A1cXfBiqZyJKm8u86y4K29lu3EnAhV19XPfrIPJId-2bL8NV8gjMHsfLkoIVn8uhK4Npa5Wm17YLdVTu-BHjzwzWlP0QVX1dVybedQ7JeumisBC02VQ\"\n" +
				"}");
		jwkSet = new JsonWebKeys();
		jwkSet.setKeys(jwkJsons.stream().map(JwkUtils::readJwkKey).collect(Collectors.toList()));
	}

	TestVerificationContext withAudiences(String...audiences) {
		this.audiences = Arrays.stream(audiences).collect(Collectors.toSet());
		return this;
	}

	TestVerificationContext withAdditionalAudience(String audience) {
		this.audiences = Stream.concat(Stream.of(audience), audiences.stream()).collect(Collectors.toSet());
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
		jwkSet.setKeys(Stream.of(jwkJson).map(JwkUtils::readJwkKey).collect(Collectors.toList()));
		return this;
	}

	@Override
	public Set<String> getAudiences() {
		return this.audiences;
	}

	@Override
	public String getIssuer() {
		return this.issuer;
	}

	@Override
	public JsonWebKey getJwk(String kid) {
		return jwkSet.getKey(kid);
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