package com.cloudflare.access.atlassian.base.auth;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.stream.Collectors;

import com.cloudflare.access.atlassian.common.context.AuthenticationContext;

class TestAuthenticationContext implements AuthenticationContext{

	private String audience;
	private String issuer;
	private String jwkJson;
	private Clock clock;
	private String validToken;

	public TestAuthenticationContext() {
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
		String[] tokenParts = {
				"eyJhbGciOiJSUzI1NiIsImtpZCI6ImJjY2RmOTlhYzMzNmM5Mjc4ZTNjN2FjNzFiZWJjYmU0NjdiYmJmZDFmYjAxM2M4NGM5Mzg4OWRhMDc3YjlkNzkiLCJ0eXAiOiJKV1QifQ",
				"eyJhdWQiOlsiYjI2NTg0NmM4NWU3MGFhNDA2NTVhNzZlNTM4NThkZjZjNzljYjJkMDQ1M2ZlZmY0OTVmM2M1Yjc5NWZiNGQ1ZSJdLCJlbWFpbCI6ImZlbGlwZS5uYXNjaW1lbnRvMUBnbWFpbC5jb20iLCJleHAiOjE1MjYwNzA2NDAsImlhdCI6MTUyNTk4NDI0MSwiaXNzIjoiaHR0cHM6Ly9jZmFwbHVnaW4uY2xvdWRmbGFyZWFjY2Vzcy5jb20iLCJub25jZSI6ImRhMWUxOTRkNDI4YTgwZDNhY2IwNzcxOWI4ZDYzZjdlODViZjZlMmVlOWNmZmZiMGQ0Y2FiYWE4YmE0Mzg2Y2QiLCJzdWIiOiI0YjgyYjM4YS05MjM2LTQ5M2QtODc1Mi01ODBhZDAwMGVhM2UifQ",
				"wrRj3wDqmBIN8f4JX-ioM43mxggGp1BF7QnlF8pvs2bMiscJyZD61GHHBZj9rfnWBTVJ0jEzjk7ZlGmgG8ndKHFzzvqxbtaCalOEnG7rIgZ3ch_JOimABQCguAItM5WbQbDpePD431VKdCqtK4u88QaX-h-TJUqG2DeYA1ZXyi-OevIkYu7vaKjIei0eUa1qr5wtKBYFbjNDVCGfXasiYt8Z75hnzO1jhoNFOowLLECUKtE4-1Uazc3M6kSqKLqARe0aWwm1TWTZlE0Qtbdrup5wulFqHb_zdWvtlJKmPJF3W7d6NmgI17pmk8bW_KYqbkQNwyD_pAnVDXWZBKv3BQ"
		};
		this.validToken = Arrays.stream(tokenParts).collect(Collectors.joining("."));
	}

	TestAuthenticationContext withAudience(String audience) {
		this.audience = audience;
		return this;
	}

	TestAuthenticationContext withIssuer(String issuer) {
		this.issuer = issuer;
		return this;
	}

	TestAuthenticationContext withClock(Clock clock) {
		this.clock = clock;
		return this;
	}

	TestAuthenticationContext withJwkJson(String jwkJson) {
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
	public String getLogoutUrl() {
		return "unusedhere";
	}

	@Override
	public Clock getClock() {
		return this.clock;
	}

	public String getValidToken() {
		return validToken;
	}

	public String getTokenOwnerEmail() {
		return "felipe.nascimento1@gmail.com";
	}

}