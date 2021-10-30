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
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import com.cloudflare.access.atlassian.common.exception.InvalidJWTException;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;

@RunWith(DataProviderRunner.class)
public class TokenVerifierTest {

	private static String expectedFailureMessage = "Invalid or expired token. Please logout and try again or proceed with your Atlassian credentials.";
	private static String expectedVerificationFailureMessage = "Invalid token, unable to parse/verify. Please logout and try again or proceed with your Atlassian credentials.";
	private static String tokenForFirstSigningKey;
	private static String tokenForFirstSigningKeyWithEscapedUrl;
	private static String tokenForSecondSigningKey;
	private static String secondAudience = "dD8R7Ugv6f3w74S3t9dVGVZkgGJYK8KuBnKfzRV4mdgpsWc833kV6xtFRt47MkYu";
	private static String tokenWithAnotherAudience;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();


	@BeforeClass
	public static void createTestToken() {
		tokenForFirstSigningKey = Arrays.stream(new String[]{
				"eyJhbGciOiJSUzI1NiIsImtpZCI6ImJjY2RmOTlhYzMzNmM5Mjc4ZTNjN2FjNzFiZWJjYmU0NjdiYmJmZDFmYjAxM2M4NGM5Mzg4OWRhMDc3YjlkNzkiLCJ0eXAiOiJKV1QifQ",
				"eyJhdWQiOlsiYjI2NTg0NmM4NWU3MGFhNDA2NTVhNzZlNTM4NThkZjZjNzljYjJkMDQ1M2ZlZmY0OTVmM2M1Yjc5NWZiNGQ1ZSJdLCJlbWFpbCI6ImZlbGlwZS5uYXNjaW1lbnRvMUBnbWFpbC5jb20iLCJleHAiOjE1MjYwNzA2NDAsImlhdCI6MTUyNTk4NDI0MSwiaXNzIjoiaHR0cHM6Ly9jZmFwbHVnaW4uY2xvdWRmbGFyZWFjY2Vzcy5jb20iLCJub25jZSI6ImRhMWUxOTRkNDI4YTgwZDNhY2IwNzcxOWI4ZDYzZjdlODViZjZlMmVlOWNmZmZiMGQ0Y2FiYWE4YmE0Mzg2Y2QiLCJzdWIiOiI0YjgyYjM4YS05MjM2LTQ5M2QtODc1Mi01ODBhZDAwMGVhM2UifQ",
				"wrRj3wDqmBIN8f4JX-ioM43mxggGp1BF7QnlF8pvs2bMiscJyZD61GHHBZj9rfnWBTVJ0jEzjk7ZlGmgG8ndKHFzzvqxbtaCalOEnG7rIgZ3ch_JOimABQCguAItM5WbQbDpePD431VKdCqtK4u88QaX-h-TJUqG2DeYA1ZXyi-OevIkYu7vaKjIei0eUa1qr5wtKBYFbjNDVCGfXasiYt8Z75hnzO1jhoNFOowLLECUKtE4-1Uazc3M6kSqKLqARe0aWwm1TWTZlE0Qtbdrup5wulFqHb_zdWvtlJKmPJF3W7d6NmgI17pmk8bW_KYqbkQNwyD_pAnVDXWZBKv3BQ"
			}).collect(Collectors.joining("."));

		tokenForFirstSigningKeyWithEscapedUrl = Arrays.stream(new String[]{
				"eyJraWQiOiI2NTkzZDlhY2Y5MmI4N2ZhY2E3NTBhNzhmN2IzMDhhODQ0YTU3YWU1MWMyNWVkNzMyMmZlMDZkNmQ4YTZiOWIwIiwiYWxnIjoiUlMyNTYiLCJ0eXAiOiJKV1QifQ",
				"eyJhdWQiOiJiMjY1ODQ2Yzg1ZTcwYWE0MDY1NWE3NmU1Mzg1OGRmNmM3OWNiMmQwNDUzZmVmZjQ5NWYzYzViNzk1ZmI0ZDVlIiwiZXhwIjoxNTQ5NDk1Njk0LCJpc3MiOiJodHRwczpcL1wvY2ZhcGx1Z2luLmNsb3VkZmxhcmVhY2Nlc3MuY29tIiwiaWF0IjoxNTQ5NDA5Mjk0LCJzdWIiOiIifQ",
				"ougV1tAGM2eX_nQCYxA5bmheokL0cSM_4eKHg2xXx7HFam1RX8_5AucpHFt3BvHSmjgw31RadCRNO1kTvNF4ZaZ4Lo8_DIuwVKBssGQ5pCRGTO99a8NaBBMoOn5c-tOXqaKPwcvtZDql_-QtsI15SvuBaAUA3sfFnpm8BGsQLPYkmgt2ZHJclJzUT_rUO5WSEKGOoQLOc0WmYCBik6B2peILFru4hAgLkzNA5DW_Imj5cTvVgg0f7o4gQyuF-bGdtrXq0g84Ikowe2cttlvzGpWp0AfMQRwREiRo4Po2oIJeUG-sTepNdZ8MmmiROprnm0feTVgWmgqY83J6yr0CYA"
			}).collect(Collectors.joining("."));

		tokenForSecondSigningKey = Arrays.stream(new String[]{
				"eyJhbGciOiJSUzI1NiIsImtpZCI6ImZhY2I3MGVhZjIzYTA2NTczZWI0NDk2MDFlMzQ1YjhkMmRlNTYyZmI5NDNkZTU1NmQyY2ZkYTM0ODcwZjc2MjkiLCJ0eXAiOiJKV1QifQ",
				"eyJhdWQiOlsiYjI2NTg0NmM4NWU3MGFhNDA2NTVhNzZlNTM4NThkZjZjNzljYjJkMDQ1M2ZlZmY0OTVmM2M1Yjc5NWZiNGQ1ZSJdLCJlbWFpbCI6ImZlbGlwZS5uYXNjaW1lbnRvMUBnbWFpbC5jb20iLCJleHAiOjE1MzY4ODAyMjEsImlhdCI6MTUzNjc5MzgyMiwiaXNzIjoiaHR0cHM6Ly9jZmFwbHVnaW4uY2xvdWRmbGFyZWFjY2Vzcy5jb20iLCJub25jZSI6Ijk4MmNmNjAxNjVjMjU3MjFkNDVjOTMyMjBlOTUxMjMwMjM0NzA5ODZkYjVmMTgzZjAwMGQ5MDQ3ODNlZDFmNWYiLCJzdWIiOiI0YjgyYjM4YS05MjM2LTQ5M2QtODc1Mi01ODBhZDAwMGVhM2UifQ",
				"fBBDqgM48N12HHaBMsj7FVgBrhfJ5JsgwhIaJYECAM59UgO-uOyAG-Zz-_wLE3lJ9VLvTZKfaN7URzJsGvydb6ni6PJAzdccHHsS6WBRite1FqO-_wn0HYBrHBaz9v66FhmY4feVqZ_pLO0Lm4uSQ6uWXtPEjwpKchnDBSDKWdiz8NWjbQ8oLUTCCNxEfCmmPHcVjCgnjcqsKJiHSvPYxzQtQf_IHkFoywnSGUv00QniI1J2KxUweNcUzVllCKDqMnwFWvcZOePct6_YDB6aiQ2SoRNk3fDmdMhw_RVUKE-SKavju-MNF_QiqJCOj0s0chP7uw2jyDGHI5vCG43nyw"
			}).collect(Collectors.joining("."));

		tokenWithAnotherAudience = Arrays.stream(new String[]{
				"eyJhbGciOiJSUzI1NiIsImtpZCI6IkI1TVMwcDluN2lUankzVW16cmhqRlJkUmxBSlkwaGh3WFM5Ymc4NU1zbTAiLCJ0eXAiOiJKV1QifQ",
				"eyJhdWQiOlsiZEQ4UjdVZ3Y2ZjN3NzRTM3Q5ZFZHVlprZ0dKWUs4S3VCbktmelJWNG1kZ3BzV2M4MzNrVjZ4dEZSdDQ3TWtZdSJdLCJlbWFpbCI6ImZlbGlwZS5uYXNjaW1lbnRvMUBnbWFpbC5jb20iLCJleHAiOjE1MjYwNzA2NDAsImlhdCI6MTUyNTk4NDI0MSwiaXNzIjoiaHR0cHM6Ly9jZmFwbHVnaW4uY2xvdWRmbGFyZWFjY2Vzcy5jb20iLCJub25jZSI6ImRhMWUxOTRkNDI4YTgwZDNhY2IwNzcxOWI4ZDYzZjdlODViZjZlMmVlOWNmZmZiMGQ0Y2FiYWE4YmE0Mzg2Y2QiLCJzdWIiOiI0YjgyYjM4YS05MjM2LTQ5M2QtODc1Mi01ODBhZDAwMGVhM2UifQ",
				"VwCMxp3c19byqo2cCl_hMNABI67g87fywwbOd223_IikUVxH3K1yrd50vRIGmoTtkGvy89rE0dhmRFlSwv_0fTVi1SJWsi0cW8urhtH4xL115eqTQufKpQGlrSvESj3byLgKKyoVst2IlGcdRbFTdSgcKEGs7EPKclOKMWR8awEuiwdh4bN7s-s_P1WFcP8TpbqCk_Z6aHSWZXaOHVplklYAIDtKES3XADWHRA8kqH7Dfp-p0htiI8OoLba84ZVYnxWvPcakuwdBKSIWmhvz8v6qT-T8EWMCT1SXMS2HNDLiKg0rr9eIvLMe5uk7KyGHbspeXXiuKr_ZXlpIPMklng"
			}).collect(Collectors.joining("."));
	}

	@Test
    @DataProvider(value={ "null", "", "samplestringthatsisabadaudience" }, convertNulls=true)
	public void shouldNotAcceptBadAudience(String expectedAudience) {
		expectedException.expect(new ExceptionMatcher (InvalidJWTException.class, expectedFailureMessage));

		TokenVerifier tokenVerifier = new TokenVerifier(new TestVerificationContext().withAudiences(expectedAudience));
		tokenVerifier.validate(tokenForFirstSigningKey);
	}

	@Test
    @DataProvider(value={ "null", "", "badvalue", "https://evilwebsite.com" }, convertNulls=true)
	public void shouldNotAcceptBadIssuer(String expectedIssuer) {
		expectedException.expect(new ExceptionMatcher (InvalidJWTException.class, expectedFailureMessage));

		TokenVerifier tokenVerifier = new TokenVerifier(new TestVerificationContext().withIssuer(expectedIssuer));
		tokenVerifier.validate(tokenForFirstSigningKey);

	}

	@Test
	public void shouldNotAcceptExpired() {
		expectedException.expect(new ExceptionMatcher (InvalidJWTException.class, expectedFailureMessage));

		LocalDateTime nineOClock = LocalDateTime.of(2018, Month.MAY, 11, 20, 31);
		Clock badClock = Clock.fixed(nineOClock.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);

		TokenVerifier tokenVerifier = new TokenVerifier(new TestVerificationContext().withClock(badClock));
		tokenVerifier.validate(tokenForFirstSigningKey);
	}

	@Test
	@DataProvider(value={ "null", "", "badvalue", "eyJhbGciOiJSUzI1NiIsImtpZCI6ImJjY2RmOTlhYzMzNmM5Mjc4ZTNjN2FjNzFiZWJjYmU0NjdiYmJmZDFmYjAxM2M4NGM5Mzg4OWRhMDc3YjlkNzkiLCJ0eXAiOiJKV1QifQ" }, convertNulls=true)
	public void shouldNotAcceptMalformedTokens(String badToken) {
		expectedException.expect(new ExceptionMatcher (InvalidJWTException.class, expectedVerificationFailureMessage));

		TokenVerifier tokenVerifier = new TokenVerifier(new TestVerificationContext());
		tokenVerifier.validate(badToken);
	}

	@Test
	public void shouldNotAcceptBadSigning() {
		expectedException.expect(new ExceptionMatcher (InvalidJWTException.class, expectedVerificationFailureMessage));
		String badJwkJson = "{\n" +
				"			\"kid\": \"bccdf99ac336c9278e3c7ac71bebcbe467bbbfd1fb013c84c93889da077b9d79\",\n" +
				"			\"kty\": \"RSA\",\n" +
				"			\"alg\": \"RS256\",\n" +
				"			\"use\": \"sig\",\n" +
				"			\"e\": \"AQAB\",\n" +
				"			\"n\": \"uEiwOJS9VDh_k2u21TI8zBUuDCY9rDjc9btqJ-assMhJv-0O17kw4nV4kBivswDfw8z6XMeU_lurbgc1_cWQdpQk03CPSLzk3NJLDNmfnBGQApHXxAyl8ba_-0SaAuEchwdcWD9bfuV-Dru2Qkg5hfVon7_aTOWsYF2L3wXOWRxUfL35TvsN6MQYBrdZ4IjaQcl2LDY3ugSV1LK8IpAR6JCFuNzro_CRuJR8BvtZArUC6k-rIzl9yQOHuvkoYHaXtMyFyrojCAZGG-NqREl0MfpuZQ2vgFUPRtxAHTb8CETqkXgKCMJHumetvcDnrIKwqyimJYfFbHbIYzmzxYKeYQ\"\n" +
				"		}";
		TokenVerifier tokenVerifier = new TokenVerifier(new TestVerificationContext().withJwkJson(badJwkJson));
		tokenVerifier.validate(tokenForFirstSigningKey);
	}

	@Test
	public void shouldAcceptValidToken() {
		TokenVerifier tokenVerifier = new TokenVerifier(new TestVerificationContext());
		tokenVerifier.validate(tokenForFirstSigningKey);
	}

	@Test
	public void shouldAcceptValidTokenWithEscapedIssuer() {
		TokenVerifier tokenVerifier = new TokenVerifier(new TestVerificationContext());
		tokenVerifier.validate(tokenForFirstSigningKeyWithEscapedUrl);
	}

	@Test
	public void shouldAcceptValidTokenSignedWithSecondKey() {
		LocalDateTime halfPastOne = LocalDateTime.of(2018, Month.SEPTEMBER, 13, 01, 30);
		Clock secondTokenClock = Clock.fixed(halfPastOne.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
		TokenVerifier tokenVerifier = new TokenVerifier(new TestVerificationContext().withClock(secondTokenClock));
		tokenVerifier.validate(tokenForSecondSigningKey);
	}

	@Test
	public void shouldAcceptTokensWithDifferentAudiences() {
		TestVerificationContext context = new TestVerificationContext().withAdditionalAudience(secondAudience);
		TokenVerifier tokenVerifier = new TokenVerifier(context);

		tokenVerifier.validate(tokenForFirstSigningKey);
		tokenVerifier.validate(tokenWithAnotherAudience);
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
