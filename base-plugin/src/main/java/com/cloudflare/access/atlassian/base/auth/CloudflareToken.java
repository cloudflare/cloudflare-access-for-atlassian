package com.cloudflare.access.atlassian.base.auth;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.Arrays;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.cxf.rs.security.jose.jwt.JwtToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudflare.access.atlassian.common.TokenVerifier;
import com.cloudflare.access.atlassian.common.context.AuthenticationContext;
import com.cloudflare.access.atlassian.common.exception.InvalidJWTException;

public class CloudflareToken {

	public static final String CF_ACCESS_JWT_HEADER = "cf-access-jwt-assertion";
	public static final String CF_ACCESS_JWT_COOKIE = "CF_Authorization";

	private static final Logger log = LoggerFactory.getLogger(CloudflareToken.class);

	private final boolean tokenNotPresent;
	private JwtToken jwt;

	public CloudflareToken(HttpServletRequest request) {
		String token = getJWT(request);
		this.tokenNotPresent = isBlank(token);
	}

	public CloudflareToken(HttpServletRequest request, AuthenticationContext authContext) {
		String token = getJWT(request);
		this.tokenNotPresent = isBlank(token);
		if(this.tokenNotPresent == false) {
			this.jwt = new TokenVerifier(authContext).validate(token);
		}
	}

	public boolean isNotPresent() {
		return this.tokenNotPresent;
	}

	public String getUserEmail() {
		tokenMustBePresent();
		return (String) jwt.getClaim("email");
	}

	private String getJWT(HttpServletRequest request) {
		String jwt = getFromHeader(request);
		if(isBlank(jwt)) {
			log.debug("JWT not available in header");
			jwt = getFromCookie(request);
		}

		if(isBlank(jwt)) {
			log.debug("JWT not available in cookie");
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

	private void tokenMustBePresent() {
		if(this.tokenNotPresent) {
			throw new InvalidJWTException("Token is not present");
		}
	}
}
