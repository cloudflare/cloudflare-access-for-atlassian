package com.cloudflare.access.atlassian.common;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import com.cloudflare.access.atlassian.common.http.SimpleHttp;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CertificateProviderTest {

	@Test
	public void testProcessingCertificateFromUrl() throws IOException {
		final String url = "https://cfaplugin.oraculo.io/cdn-cgi/access/certs";
		SimpleHttp httpMock = mock(SimpleHttp.class);
		when(httpMock.get(url)).thenReturn(
				"{\n" +
				"  \"keys\": [\n" +
				"    {\n" +
				"      \"kid\": \"bccdf99ac336c9278e3c7ac71bebcbe467bbbfd1fb013c84c93889da077b9d79\",\n" +
				"      \"kty\": \"RSA\",\n" +
				"      \"alg\": \"RS256\",\n" +
				"      \"use\": \"sig\",\n" +
				"      \"e\": \"AQAB\",\n" +
				"      \"n\": \"x4SANlqANMzNkJfruQU8RbwZ6B_N-ed4b5-CscBHV1sIR3Fo6q-1BF7votDBIJ05q3ahKNYIqHDF7xiKhVCHxXVwYuh3HULcetylHSh-I6C_P66mLxHLLagLSvsmr6ZdCqdEsDJS33RUJtlIODt-3OBaPqyfUjy4Ql5d7LC5bfAztX3RyKwkrlT2X9o62-GmbgvJGwFrPISyWUHF9trH82oaxtN3TxZ4L3LKBkPdexIPuTJGs4wse0pMd-v7439E_Quzm-eM61eXM4IP5YEf5sjBxbGsZgqcNuEM_2S_K9AKyj0mOleSoBAfFCCkz2QZTn7jHXz2yreLbpPSXY9e3Q\"\n" +
				"    },\n" +
				"    {\n" +
				"      \"kid\": \"d018809a98fba8261a7e732c53603dac25d25d3ea73b219e2193a04977f3ff55\",\n" +
				"      \"kty\": \"RSA\",\n" +
				"      \"alg\": \"RS256\",\n" +
				"      \"use\": \"sig\",\n" +
				"      \"e\": \"AQAB\",\n" +
				"      \"n\": \"uEiwOJS9VDh_k2u21TI8zBUuDCY9rDjc9btqJ-assMhJv-0O17kw4nV4kBivswDfw8z6XMeU_lurbgc1_cWQdpQk03CPSLzk3NJLDNmfnBGQApHXxAyl8ba_-0SaAuEchwdcWD9bfuV-Dru2Qkg5hfVon7_aTOWsYF2L3wXOWRxUfL35TvsN6MQYBrdZ4IjaQcl2LDY3ugSV1LK8IpAR6JCFuNzro_CRuJR8BvtZArUC6k-rIzl9yQOHuvkoYHaXtMyFyrojCAZGG-NqREl0MfpuZQ2vgFUPRtxAHTb8CETqkXgKCMJHumetvcDnrIKwqyimJYfFbHbIYzmzxYKeYQ\"\n" +
				"    }\n" +
				"  ]\n" +
				"}"
		);
		CertificateProvider certificateProvider = new CertificateProvider(httpMock);
		List<String> certificates = certificateProvider.getCerticatesAsJson(url);

		assertThat(certificates, hasSize(2));
		JsonNode cert0 = new ObjectMapper().readTree(certificates.get(0));
		assertThat(cert0.get("kid").asText(null), equalTo("bccdf99ac336c9278e3c7ac71bebcbe467bbbfd1fb013c84c93889da077b9d79"));
		assertThat(cert0.get("alg").asText(null), equalTo("RS256"));
		assertThat(cert0.get("n").asText(null), equalTo("x4SANlqANMzNkJfruQU8RbwZ6B_N-ed4b5-CscBHV1sIR3Fo6q-1BF7votDBIJ05q3ahKNYIqHDF7xiKhVCHxXVwYuh3HULcetylHSh-I6C_P66mLxHLLagLSvsmr6ZdCqdEsDJS33RUJtlIODt-3OBaPqyfUjy4Ql5d7LC5bfAztX3RyKwkrlT2X9o62-GmbgvJGwFrPISyWUHF9trH82oaxtN3TxZ4L3LKBkPdexIPuTJGs4wse0pMd-v7439E_Quzm-eM61eXM4IP5YEf5sjBxbGsZgqcNuEM_2S_K9AKyj0mOleSoBAfFCCkz2QZTn7jHXz2yreLbpPSXY9e3Q"));

		JsonNode cert1 = new ObjectMapper().readTree(certificates.get(1));
		assertThat(cert1.get("kid").asText(null), equalTo("d018809a98fba8261a7e732c53603dac25d25d3ea73b219e2193a04977f3ff55"));
		assertThat(cert1.get("alg").asText(null), equalTo("RS256"));
		assertThat(cert1.get("n").asText(null), equalTo("uEiwOJS9VDh_k2u21TI8zBUuDCY9rDjc9btqJ-assMhJv-0O17kw4nV4kBivswDfw8z6XMeU_lurbgc1_cWQdpQk03CPSLzk3NJLDNmfnBGQApHXxAyl8ba_-0SaAuEchwdcWD9bfuV-Dru2Qkg5hfVon7_aTOWsYF2L3wXOWRxUfL35TvsN6MQYBrdZ4IjaQcl2LDY3ugSV1LK8IpAR6JCFuNzro_CRuJR8BvtZArUC6k-rIzl9yQOHuvkoYHaXtMyFyrojCAZGG-NqREl0MfpuZQ2vgFUPRtxAHTb8CETqkXgKCMJHumetvcDnrIKwqyimJYfFbHbIYzmzxYKeYQ"));
	}


}
