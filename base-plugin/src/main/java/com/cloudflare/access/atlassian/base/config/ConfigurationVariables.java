package com.cloudflare.access.atlassian.base.config;


import java.util.Arrays;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.apache.bval.extras.constraints.net.Domain;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.cloudflare.access.atlassian.base.config.validation.NullableDomain;

public class ConfigurationVariables {

	private static final String SETTINGS_PREFIX = ConfigurationVariables.class.getName() + ".";

	public static final String VALID_FLAG_SETTINGS_KEY = SETTINGS_PREFIX + "is.valid";
	public static final String TOKEN_AUDIENCE_SETTINGS_KEY = SETTINGS_PREFIX + "tokenAudience";
	public static final String AUTH_DOMAIN_SETTINGS_KEY = SETTINGS_PREFIX + "authDomain";
	public static final String ALLOWED_EMAIL_DOMAIN_SETTINGS_KEY = SETTINGS_PREFIX + "authDomain";
	public static final String USER_MATCHING_ATTRIBUTE_SETTINGS_KEY = SETTINGS_PREFIX + "userMatchingAttribute";

	@NotNull(message="cfaccess.config.tokenAudience.should.not.be.empty")
	@Size(min=1, message="cfaccess.config.tokenAudience.should.not.be.empty")
	private String tokenAudience;

	@NotNull(message="cfaccess.config.authDomain.should.not.be.empty")
	@Size(min=1, message="cfaccess.config.authDomain.should.not.be.empty")
	@Domain(message="cfaccess.config.authDomain.should.be.valid")
	private String authDomain;

	@NullableDomain(message="cfaccess.config.allowedEmailDomain.should.be.valid")
	private String allowedEmailDomain;

	@NotNull(message="cfaccess.config.userMatchingAttribute.should.be.valid")
	private UserMatchingAttribute userMatchingAttribute;

	public ConfigurationVariables(ConfigurationVariablesActiveObject activeObject) {
		super();
		this.tokenAudience = Arrays.stream(activeObject.getTokenAudiences())
				.findFirst()
				.map(TokenAudienceActiveObject::getValue)
				.orElse(null);
		this.authDomain = activeObject.getAuthDomain();
		this.allowedEmailDomain = activeObject.getAllowedEmailDomain();
		this.userMatchingAttribute = ObjectUtils.defaultIfNull(activeObject.getUserMatchingAttribute(), UserMatchingAttribute.defaultAttribute());
	}

	public ConfigurationVariables(String tokenAudience, String authDomain, String allowedEmailDomain, String userMatchingAttribute) {
		super();
		this.tokenAudience = tokenAudience;
		this.authDomain = authDomain;
		this.allowedEmailDomain = allowedEmailDomain;
		this.userMatchingAttribute = EnumUtils.isValidEnum(UserMatchingAttribute.class, userMatchingAttribute) ? UserMatchingAttribute.valueOf(userMatchingAttribute) : UserMatchingAttribute.EMAIL;
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

	@NotNull
	public UserMatchingAttribute getUserMatchingAttribute() {
		return userMatchingAttribute;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
