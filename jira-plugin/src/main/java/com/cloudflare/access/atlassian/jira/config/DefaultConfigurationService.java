package com.cloudflare.access.atlassian.jira.config;

import static com.cloudflare.access.atlassian.jira.config.ConfigurationVariables.*;

import java.util.Objects;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.cloudflare.access.atlassian.common.config.PluginConfiguration;

@Component
public class DefaultConfigurationService implements ConfigurationService{

	private final PluginSettingsFactory pluginSettingsFactory;
	private final TransactionTemplate transactionTemplate;

	@Inject
	public DefaultConfigurationService(@ComponentImport PluginSettingsFactory pluginSettingsFactory,
								@ComponentImport TransactionTemplate transactionTemplate) {
		super();
		this.pluginSettingsFactory = pluginSettingsFactory;
		this.transactionTemplate = transactionTemplate;
	}

	@Override
	public void save(ConfigurationVariables configVariables) {
		transactionTemplate.execute(new TransactionCallback<Void>() {
			@Override
			public Void doInTransaction() {
				PluginSettings settings = pluginSettingsFactory.createGlobalSettings();

				settings.put(TOKEN_AUDIENCE_SETTINGS_KEY, configVariables.getTokenAudience());
			    settings.put(AUTH_DOMAIN_SETTINGS_KEY, configVariables.getAuthDomain());
			    settings.put(LOCAL_CONNECTOR_HOST_SETTINGS_KEY, configVariables.getLocalConnectorHost());
			    settings.put(LOCAL_CONNECTOR_PORT_SETTINGS_KEY, String.valueOf(configVariables.getLocalConnectorPort()));
			    settings.put(VALID_FLAG_SETTINGS_KEY, "true");

			    return null;
			}
		});
	}

	@Override
	public Optional<ConfigurationVariables> loadConfigurationVariables() {
		return transactionTemplate.execute(new TransactionCallback<Optional<ConfigurationVariables>>() {
			@Override
			public Optional<ConfigurationVariables> doInTransaction() {
				PluginSettings settings = pluginSettingsFactory.createGlobalSettings();

				String validFlag = (String) settings.get(VALID_FLAG_SETTINGS_KEY);

				if(Objects.equals(validFlag, "true") == false) {
					return Optional.empty();
				}

				String tokenAudience = (String) settings.get(TOKEN_AUDIENCE_SETTINGS_KEY);
			    String authDomain = (String) settings.get(AUTH_DOMAIN_SETTINGS_KEY);
			    String localConnectorHost = (String) settings.get(LOCAL_CONNECTOR_HOST_SETTINGS_KEY);
			    String localConnectorPortSetting = (String) settings.get(LOCAL_CONNECTOR_PORT_SETTINGS_KEY);

			    int localConnectorPort = 0;
			    if(StringUtils.isNotBlank(localConnectorPortSetting)) {
			    	localConnectorPort = Integer.parseInt(localConnectorPortSetting);
			    }

			    return Optional.of(new ConfigurationVariables(tokenAudience, authDomain, localConnectorHost, localConnectorPort));
			}
		});
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
