package com.cloudflare.access.atlassian.base.config;

import net.java.ao.Entity;
import net.java.ao.schema.Table;

@Table("cf_config_vars")
public interface ConfigurationVariablesActiveObject extends Entity{

	String getTokenAudience();
	void setTokenAudience(String tokenAudience);

	String getAuthDomain();
	void setAuthDomain(String authDomain);

	String getAllowedEmailDomain();
	void setAllowedEmailDomain(String allowedEmailDomain);
}
