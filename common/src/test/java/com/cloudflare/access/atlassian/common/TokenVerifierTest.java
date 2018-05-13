package com.cloudflare.access.atlassian.common;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import com.cloudflare.access.atlassian.common.context.VerificationContext;
import com.cloudflare.access.atlassian.common.exception.InvalidJWTException;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;

@RunWith(DataProviderRunner.class)
public class TokenVerifierTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private String token;

	@Before
	public void createTestToken() {
		String[] tokenParts = {
				"eyJhbGciOiJSUzI1NiIsImtpZCI6ImJjY2RmOTlhYzMzNmM5Mjc4ZTNjN2FjNzFiZWJjYmU0NjdiYmJmZDFmYjAxM2M4NGM5Mzg4OWRhMDc3YjlkNzkiLCJ0eXAiOiJKV1QifQ",
				"eyJhdWQiOlsiYjI2NTg0NmM4NWU3MGFhNDA2NTVhNzZlNTM4NThkZjZjNzljYjJkMDQ1M2ZlZmY0OTVmM2M1Yjc5NWZiNGQ1ZSJdLCJlbWFpbCI6ImZlbGlwZS5uYXNjaW1lbnRvMUBnbWFpbC5jb20iLCJleHAiOjE1MjYwNzA2NDAsImlhdCI6MTUyNTk4NDI0MSwiaXNzIjoiaHR0cHM6Ly9jZmFwbHVnaW4uY2xvdWRmbGFyZWFjY2Vzcy5jb20iLCJub25jZSI6ImRhMWUxOTRkNDI4YTgwZDNhY2IwNzcxOWI4ZDYzZjdlODViZjZlMmVlOWNmZmZiMGQ0Y2FiYWE4YmE0Mzg2Y2QiLCJzdWIiOiI0YjgyYjM4YS05MjM2LTQ5M2QtODc1Mi01ODBhZDAwMGVhM2UifQ",
				"wrRj3wDqmBIN8f4JX-ioM43mxggGp1BF7QnlF8pvs2bMiscJyZD61GHHBZj9rfnWBTVJ0jEzjk7ZlGmgG8ndKHFzzvqxbtaCalOEnG7rIgZ3ch_JOimABQCguAItM5WbQbDpePD431VKdCqtK4u88QaX-h-TJUqG2DeYA1ZXyi-OevIkYu7vaKjIei0eUa1qr5wtKBYFbjNDVCGfXasiYt8Z75hnzO1jhoNFOowLLECUKtE4-1Uazc3M6kSqKLqARe0aWwm1TWTZlE0Qtbdrup5wulFqHb_zdWvtlJKmPJF3W7d6NmgI17pmk8bW_KYqbkQNwyD_pAnVDXWZBKv3BQ"
		};
		this.token = Arrays.stream(tokenParts).collect(Collectors.joining("."));
	}

	@Test
    @DataProvider(value={ "null", "", "samplestringthatsisabadaudience" }, convertNulls=true)
	public void shouldNotAcceptBadAudience(String expectedAudience) {
		expectedException.expect(new ExceptionMatcher (InvalidJWTException.class, "JWT Audience does not match expected audience."));

		TokenVerifier tokenVerifier = new TokenVerifier(new TestVerificationContext().withAudience(expectedAudience));
		tokenVerifier.validate(token);
	}

	@Test
    @DataProvider(value={ "null", "", "badvalue", "https://evilwebsite.com" }, convertNulls=true)
	public void shouldNotAcceptBadIssuer(String expectedIssuer) {
		expectedException.expect(new ExceptionMatcher (InvalidJWTException.class, "JWT Issuer does not match expected issuer."));

		TokenVerifier tokenVerifier = new TokenVerifier(new TestVerificationContext().withIssuer(expectedIssuer));
		tokenVerifier.validate(token);

	}

	@Test
	public void shouldNotAcceptExpired() {
		expectedException.expect(new ExceptionMatcher (InvalidJWTException.class, "JWT expired since 2018-05-11T20:30:40Z (reference clock is 2018-05-11T20:31:00Z)."));

		LocalDateTime nineOClock = LocalDateTime.of(2018, Month.MAY, 11, 20, 31);
		Clock badClock = Clock.fixed(nineOClock.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);

		TokenVerifier tokenVerifier = new TokenVerifier(new TestVerificationContext().withClock(badClock));
		tokenVerifier.validate(token);
	}

	@Test
	@DataProvider(value={ "null", "", "badvalue", "eyJhbGciOiJSUzI1NiIsImtpZCI6ImJjY2RmOTlhYzMzNmM5Mjc4ZTNjN2FjNzFiZWJjYmU0NjdiYmJmZDFmYjAxM2M4NGM5Mzg4OWRhMDc3YjlkNzkiLCJ0eXAiOiJKV1QifQ" }, convertNulls=true)
	public void shouldNotAcceptMalformedTokens(String badToken) {
		expectedException.expect(new ExceptionMatcher (InvalidJWTException.class, String.format("Bad JWT: '%s'", badToken)));

		TokenVerifier tokenVerifier = new TokenVerifier(new TestVerificationContext());
		tokenVerifier.validate(badToken);
	}

	@Test
	public void shouldNotAcceptBadSigning() {
		expectedException.expect(new ExceptionMatcher (InvalidJWTException.class, String.format("Bad JWT: '%s' \nError: Invalid Signature", token)));
		String badJwkJson = "{\n" +
				"			\"kid\": \"bccdf99ac336c9278e3c7ac71bebcbe467bbbfd1fb013c84c93889da077b9d79\",\n" +
				"			\"kty\": \"RSA\",\n" +
				"			\"alg\": \"RS256\",\n" +
				"			\"use\": \"sig\",\n" +
				"			\"e\": \"AQAB\",\n" +
				"			\"n\": \"uEiwOJS9VDh_k2u21TI8zBUuDCY9rDjc9btqJ-assMhJv-0O17kw4nV4kBivswDfw8z6XMeU_lurbgc1_cWQdpQk03CPSLzk3NJLDNmfnBGQApHXxAyl8ba_-0SaAuEchwdcWD9bfuV-Dru2Qkg5hfVon7_aTOWsYF2L3wXOWRxUfL35TvsN6MQYBrdZ4IjaQcl2LDY3ugSV1LK8IpAR6JCFuNzro_CRuJR8BvtZArUC6k-rIzl9yQOHuvkoYHaXtMyFyrojCAZGG-NqREl0MfpuZQ2vgFUPRtxAHTb8CETqkXgKCMJHumetvcDnrIKwqyimJYfFbHbIYzmzxYKeYQ\"\n" +
				"		}";
		TokenVerifier tokenVerifier = new TokenVerifier(new TestVerificationContext().withJwkJson(badJwkJson));
		tokenVerifier.validate(token);
	}

	@Test
	public void shouldAcceptValidToken() {
		TokenVerifier tokenVerifier = new TokenVerifier(new TestVerificationContext());
		tokenVerifier.validate(token);
	}


	private static class TestVerificationContext implements VerificationContext{

		private String audience;
		private String issuer;
		private String jwkJson;
		private Clock clock;

		public TestVerificationContext() {
			super();
			this.audience = "b265846c85e70aa40655a76e53858df6c79cb2d0453feff495f3c5b795fb4d5e";
			this.issuer = "https://cfaplugin.cloudflareaccess.com";
			LocalDateTime nineOClock = LocalDateTime.of(2018, Month.MAY, 10, 20, 00);
			this.clock = Clock.fixed(nineOClock.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
			this.jwkJson = "{\n" +
					"			\"kid\": \"bccdf99ac336c9278e3c7ac71bebcbe467bbbfd1fb013c84c93889da077b9d79\",\n" +
					"			\"kty\": \"RSA\",\n" +
					"			\"alg\": \"RS256\",\n" +
					"			\"use\": \"sig\",\n" +
					"			\"e\": \"AQAB\",\n" +
					"			\"n\": \"x4SANlqANMzNkJfruQU8RbwZ6B_N-ed4b5-CscBHV1sIR3Fo6q-1BF7votDBIJ05q3ahKNYIqHDF7xiKhVCHxXVwYuh3HULcetylHSh-I6C_P66mLxHLLagLSvsmr6ZdCqdEsDJS33RUJtlIODt-3OBaPqyfUjy4Ql5d7LC5bfAztX3RyKwkrlT2X9o62-GmbgvJGwFrPISyWUHF9trH82oaxtN3TxZ4L3LKBkPdexIPuTJGs4wse0pMd-v7439E_Quzm-eM61eXM4IP5YEf5sjBxbGsZgqcNuEM_2S_K9AKyj0mOleSoBAfFCCkz2QZTn7jHXz2yreLbpPSXY9e3Q\"\n" +
					"		}";
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
			this.jwkJson = jwkJson;
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
		public String getSigningKeyAsJson() {
			return this.jwkJson;
		}

		@Override
		public Clock getClock() {
			return this.clock;
		}

	}

	private static class ExceptionMatcher extends BaseMatcher<Exception> {
		private Class<?> type;
		private String message;

		public ExceptionMatcher(Class<?> type, String message) {
			super();
			this.type = type;
			this.message = message;
		}

		@Override
		public boolean matches(final Object item) {
			final Exception e = (Exception) item;
			return type.isInstance(e) && Objects.equals(e.getMessage(), message);
		}

		@Override
		public void describeTo(Description description) {
			description
			.appendText("of type ")
			.appendValue(type.getName())
			.appendText(" and message to be ")
			.appendValue(message);

		}
	}
}
