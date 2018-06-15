package com.cloudflare.access.atlassian.jira.config;

import java.util.Optional;

import com.cloudflare.access.atlassian.common.config.PluginConfiguration;

public interface ConfigurationService {

	void save(ConfigurationVariables configurationVariables);

	Optional<ConfigurationVariables> loadConfigurationVariables();

	Optional<PluginConfiguration> getPluginConfiguration();
}
