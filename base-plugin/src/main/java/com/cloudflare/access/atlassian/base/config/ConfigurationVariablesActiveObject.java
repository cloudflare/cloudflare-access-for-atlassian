package com.cloudflare.access.atlassian.base.config;

import net.java.ao.Entity;
import net.java.ao.OneToMany;
import net.java.ao.schema.Table;

@Table("cf_config_vars")
public interface ConfigurationVariablesActiveObject extends Entity{

	/**
	 * This is deprecated, token audience is a collection now.
	 * @return
	 */
	@Deprecated(forRemoval = true)
	String getTokenAudience();
	@Deprecated(forRemoval = true)
	void setTokenAudience(String tokenAudience);
	
	@OneToMany
	TokenAudienceActiveObject[] getTokenAudiences();

	String getAuthDomain();
	void setAuthDomain(String authDomain);

	String getAllowedEmailDomain();
	void setAllowedEmailDomain(String allowedEmailDomain);

	UserMatchingAttribute getUserMatchingAttribute();
	void setUserMatchingAttribute(UserMatchingAttribute userMatchingAttribute);
}
