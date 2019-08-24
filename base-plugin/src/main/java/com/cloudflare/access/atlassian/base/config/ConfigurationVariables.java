package com.cloudflare.access.atlassian.base.config;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.apache.bval.extras.constraints.net.Domain;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class ConfigurationVariables {

	private static final String SETTINGS_PREFIX = ConfigurationVariables.class.getName() + ".";

	public static final String VALID_FLAG_SETTINGS_KEY = SETTINGS_PREFIX + "is.valid";
	public static final String TOKEN_AUDIENCE_SETTINGS_KEY = SETTINGS_PREFIX + "tokenAudience";
	public static final String AUTH_DOMAIN_SETTINGS_KEY = SETTINGS_PREFIX + "authDomain";
	public static final String ALLOWED_EMAIL_DOMAIN_SETTINGS_KEY = SETTINGS_PREFIX + "authDomain";

	@NotNull(message="cfaccess.config.tokenAudience.should.not.be.empty")
	@Size(min=1, message="cfaccess.config.tokenAudience.should.not.be.empty")
	private String tokenAudience;

	@NotNull(message="cfaccess.config.authDomain.should.not.be.empty")
	@Size(min=1, message="cfaccess.config.authDomain.should.not.be.empty")
	@Domain(message="cfaccess.config.authDomain.should.be.valid")
	private String authDomain;

	@Domain(message="cfaccess.config.allowedEmailDomain.should.be.valid")
	private String allowedEmailDomain;

	public ConfigurationVariables(ConfigurationVariablesActiveObject activeObject) {
		super();
		this.tokenAudience = activeObject.getTokenAudience();
		this.authDomain = activeObject.getAuthDomain();
		this.allowedEmailDomain = activeObject.getAllowedEmailDomain();
	}

	public ConfigurationVariables(String tokenAudience, String authDomain, String allowedEmailDomain) {
		super();
		this.tokenAudience = tokenAudience;
		this.authDomain = authDomain;
		this.allowedEmailDomain = allowedEmailDomain;
	}

	public String getTokenAudience() {
		return tokenAudience;
	}

	public String getAuthDomain() {
		return authDomain;
	}

	public String getAllowedEmailDomain() {
		return allowedEmailDomain;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
