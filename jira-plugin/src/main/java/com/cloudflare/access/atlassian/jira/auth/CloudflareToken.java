package com.cloudflare.access.atlassian.jira.auth;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.Arrays;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.cxf.rs.security.jose.jwt.JwtToken;

import com.cloudflare.access.atlassian.common.TokenVerifier;
import com.cloudflare.access.atlassian.common.context.EnvironmentVerificationContext;

//TODO implement test
public class CloudflareToken {

	private static final String CF_ACCESS_JWT_HEADER = "cf-access-jwt-assertion";
	private static final String CF_ACCESS_JWT_COOKIE = "CF_Authorization";

	private final HttpServletRequest request;

	public CloudflareToken(HttpServletRequest request) {
		this.request = request;
	}

	public String getUserEmail() {
		String token = getJWT();
		TokenVerifier tokenVerifier = new TokenVerifier(new EnvironmentVerificationContext());
		JwtToken jwt = tokenVerifier.validate(token);
		return (String) jwt.getClaim("email");
	}

	private String getJWT() {
		String jwt = getFromHeader();
		if(isBlank(jwt)) {
			jwt = getFromCookie();
		}

		if(isBlank(jwt)) {
			//TODO throw exception
		}
		return jwt;
	}

	private String getFromHeader(){
		return this.request.getHeader(CF_ACCESS_JWT_HEADER);
	}

	private String getFromCookie(){
		return Arrays.stream(this.request.getCookies())
				.filter(cookie -> CF_ACCESS_JWT_COOKIE.equals(cookie.getName()))
				.findFirst()
				.map(Cookie::getValue)
				.orElse(null);
	}
}
