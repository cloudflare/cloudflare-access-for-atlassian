package com.cloudflare.access.atlassian.base.config;

import net.java.ao.Entity;
import net.java.ao.schema.Table;

@Table("cf_config_audiences")
public interface TokenAudienceActiveObject extends Entity{

	ConfigurationVariablesActiveObject getConfig();
	void setConfig(ConfigurationVariablesActiveObject config);

	String getValue();
	void setValue(String value);
	
}
