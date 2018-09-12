package com.cloudflare.access.atlassian.common;

import java.time.Instant;
import java.util.Objects;

import org.apache.cxf.rs.security.jose.jwk.JwkUtils;
import org.apache.cxf.rs.security.jose.jws.JwsSignatureVerifier;
import org.apache.cxf.rs.security.jose.jws.JwsUtils;
import org.apache.cxf.rs.security.jose.jwt.JoseJwtConsumer;
import org.apache.cxf.rs.security.jose.jwt.JwtClaims;
import org.apache.cxf.rs.security.jose.jwt.JwtToken;

import com.cloudflare.access.atlassian.common.context.AuthenticationContext;
import com.cloudflare.access.atlassian.common.exception.InvalidJWTException;

public class TokenVerifier {

	private AuthenticationContext context;

	public TokenVerifier(AuthenticationContext context) {
		this.context = context;
	}

	public JwtToken validate(String token) throws InvalidJWTException{
		JwtToken jwt = getJWT(token);
		validateClaims(jwt);
		return jwt;
	}

	private void validateClaims(JwtToken jwt) {
		try {
			new ClaimsVerifier(jwt.getClaims())
				.validateAudience()
				.validateExpire()
				.validateIssuer();
		}catch (Exception e) {
			throw new InvalidJWTException("Invalid or expired token. Please logout and try again or proceed with your Atlassian credentials.", e);
		}
	}

	private JwtToken getJWT(String token)  {
		Exception lastTryFailureCause = null;
		for(String jsonKey : context.getSigningKeyAsJson()) {
			try {
				JwtToken jwtToken = tryParseJwt(jsonKey, token);
				if(jwtToken != null)
					return jwtToken;
			}catch (Exception e) {
				lastTryFailureCause = e;
			}
		}
		throw new InvalidJWTException("Invalid or expired token. Please logout and try again or proceed with your Atlassian credentials.", lastTryFailureCause);
	}

	private JwtToken tryParseJwt(String jsonKey, String token) {
		JwsSignatureVerifier signatureVerifier = getSignatureVerifier(jsonKey);
		JwtToken jwt = new JoseJwtConsumer().getJwtToken(token, null, signatureVerifier);
		return jwt;
	}

	private JwsSignatureVerifier getSignatureVerifier(String jsonKey) {
		return JwsUtils.getSignatureVerifier(JwkUtils.readJwkKey(jsonKey));
	}

	private class ClaimsVerifier{
		private JwtClaims claims;

		public ClaimsVerifier(JwtClaims jwtClaims) {
			this.claims = jwtClaims;
		}

		ClaimsVerifier validateAudience() {
			if(Objects.equals(claims.getAudience(), context.getAudience()) == false) {
				//TODO debug log
				throw new InvalidJWTException("JWT Audience does not match expected audience.");
			}
			return this;
		}

		ClaimsVerifier validateExpire() {
			Instant nowInstant = Instant.now(context.getClock());
			if(nowInstant.getEpochSecond() > claims.getExpiryTime()) {
				//TODO debug log
				throw new InvalidJWTException(String.format("JWT expired since %s (reference clock is %s).", Instant.ofEpochSecond(claims.getExpiryTime()), nowInstant));
			}
			return this;
		}

		ClaimsVerifier validateIssuer() {
			if(Objects.equals(claims.getIssuer(), context.getIssuer()) == false) {
				//TODO debug log
				throw new InvalidJWTException("JWT Issuer does not match expected issuer.");
			}
			return this;
		}
	}

}
