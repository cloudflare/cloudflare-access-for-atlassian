package com.cloudflare.access.atlassian.base.config.impl;

import java.util.Optional;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.cloudflare.access.atlassian.base.config.ConfigurationChangedEvent;
import com.cloudflare.access.atlassian.base.config.ConfigurationService;
import com.cloudflare.access.atlassian.base.config.ConfigurationVariables;
import com.cloudflare.access.atlassian.base.config.ConfigurationVariablesActiveObject;
import com.cloudflare.access.atlassian.base.config.PersistentPluginConfiguration;
import com.cloudflare.access.atlassian.common.config.PluginConfiguration;

@Component
public class DefaultConfigurationService implements ConfigurationService{

	private static final Logger log = LoggerFactory.getLogger(DefaultConfigurationService.class);
	private final ActiveObjects activeObjects;
	private final EventPublisher eventPublisher;

	@Inject
	public DefaultConfigurationService(@ComponentImport ActiveObjects activeObjects,
										@ComponentImport EventPublisher eventPublisher) {
		super();
		this.activeObjects = activeObjects;
		this.eventPublisher = eventPublisher;
	}

	@Override
	public void save(ConfigurationVariables configVariables) {
		ConfigurationVariablesActiveObject ao = findFirst()
				.orElseGet(() -> activeObjects.create(ConfigurationVariablesActiveObject.class));

		ao.setTokenAudience(configVariables.getTokenAudience());
		ao.setAuthDomain(configVariables.getAuthDomain());
		ao.setLocalConnectorHost(configVariables.getLocalConnectorHost());
		ao.setLocalConnectorPort(configVariables.getLocalConnectorPort());
		ao.save();

		log.info("Publishing configuration changed event...");
		eventPublisher.publish(new ConfigurationChangedEvent(this));
	}

	@Override
	public Optional<ConfigurationVariables> loadConfigurationVariables() {
		Optional<ConfigurationVariablesActiveObject> persistedVariables = findFirst();

		if(persistedVariables.isPresent())
			return Optional.of(new ConfigurationVariables(persistedVariables.get()));

		return Optional.empty();
	}

	private Optional<ConfigurationVariablesActiveObject> findFirst() {
		ConfigurationVariablesActiveObject persistedConfig = activeObjects.executeInTransaction(new TransactionCallback<ConfigurationVariablesActiveObject>() {

			@Override
			public ConfigurationVariablesActiveObject doInTransaction() {
				ConfigurationVariablesActiveObject[] result = activeObjects.find(ConfigurationVariablesActiveObject.class);
				if(result.length == 0) return null;
				return result[0];
			}
		});

		return Optional.ofNullable(persistedConfig);
	}

	@Override
	public Optional<PluginConfiguration> getPluginConfiguration() {
		Optional<ConfigurationVariables> variables = loadConfigurationVariables();
		if(variables.isPresent()) {
			return Optional.of(new PersistentPluginConfiguration(variables.get()));
		}else {
			return Optional.empty();
		}
	}
}
