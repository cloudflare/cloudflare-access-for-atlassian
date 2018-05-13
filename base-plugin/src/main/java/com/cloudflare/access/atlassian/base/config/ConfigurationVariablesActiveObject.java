package com.cloudflare.access.atlassian.base.config;

import net.java.ao.Entity;
import net.java.ao.schema.Table;

@Table("cf_config_vars")
public interface ConfigurationVariablesActiveObject extends Entity{

	public String getTokenAudience();
	public void setTokenAudience(String tokenAudience);

	public String getAuthDomain();
	public void setAuthDomain(String authDomain);

}
