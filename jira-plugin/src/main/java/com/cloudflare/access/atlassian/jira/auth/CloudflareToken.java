package com.cloudflare.access.atlassian.jira.auth;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.Arrays;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.cxf.rs.security.jose.jwt.JwtToken;

import com.cloudflare.access.atlassian.common.TokenVerifier;
import com.cloudflare.access.atlassian.common.context.AuthenticationContext;

public class CloudflareToken {

	private static final String CF_ACCESS_JWT_HEADER = "cf-access-jwt-assertion";
	private static final String CF_ACCESS_JWT_COOKIE = "CF_Authorization";
	private JwtToken jwt;

	public CloudflareToken(HttpServletRequest request, AuthenticationContext authContext) {
		String token = getJWT(request);
		TokenVerifier tokenVerifier = new TokenVerifier(authContext);
		this.jwt = tokenVerifier.validate(token);
	}

	public String getUserEmail() {
		return (String) jwt.getClaim("email");
	}

	private String getJWT(HttpServletRequest request) {
		String jwt = getFromHeader(request);
		if(isBlank(jwt)) {
			jwt = getFromCookie(request);
		}

		if(isBlank(jwt)) {
			//TODO use custom exception
			throw new IllegalStateException("No Cloudflare Access token available in the request");
		}
		return jwt;
	}

	private String getFromHeader(HttpServletRequest request){
		return request.getHeader(CF_ACCESS_JWT_HEADER);
	}

	private String getFromCookie(HttpServletRequest request){
		if(request.getCookies() == null) {
			return null;
		}

		return Arrays.stream(request.getCookies())
				.filter(cookie -> CF_ACCESS_JWT_COOKIE.equals(cookie.getName()))
				.findFirst()
				.map(Cookie::getValue)
				.orElse(null);
	}
}
