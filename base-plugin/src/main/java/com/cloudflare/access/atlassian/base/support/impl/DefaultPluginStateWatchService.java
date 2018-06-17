package com.cloudflare.access.atlassian.base.support.impl;

import java.util.Optional;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.event.events.PluginEnabledEvent;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.cloudflare.access.atlassian.base.auth.CloudflarePluginDetails;
import com.cloudflare.access.atlassian.base.config.ConfigurationChangedEvent;
import com.cloudflare.access.atlassian.base.config.ConfigurationService;
import com.cloudflare.access.atlassian.base.support.PluginStateWatchService;
import com.cloudflare.access.atlassian.base.utils.EnvironmentFlags;
import com.cloudflare.access.atlassian.common.config.PluginConfiguration;
import com.cloudflare.access.atlassian.common.http.AtlassianInternalHttpProxy;

@Component
public class DefaultPluginStateWatchService implements PluginStateWatchService{

	private static final Logger log = LoggerFactory.getLogger(DefaultPluginStateWatchService.class);

	private ConfigurationService configurationService;
	private EventPublisher eventPublisher;
	private CloudflarePluginDetails pluginDetails;
	private final boolean proxyDisabled;

	@Inject
	public DefaultPluginStateWatchService(ConfigurationService configurationService,
											@ComponentImport EventPublisher eventPublisher,
											CloudflarePluginDetails pluginDetails,
											Environment env) {
		this.configurationService = configurationService;
		this.eventPublisher = eventPublisher;
		this.pluginDetails = pluginDetails;
		this.proxyDisabled = EnvironmentFlags.isFiltersDisabled(env);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		log.info("Plugin initializing...");

		log.info("Registering as event listener...");
		eventPublisher.register(this);
	}


	@Override
	public void destroy() throws Exception {
		log.info("Destroying plugin...");

		log.info("Unregistering event listener...");
		eventPublisher.unregister(this);

		shutdownProxy();
	}

	@EventListener
	public void onPluginEnabled(PluginEnabledEvent event) {
		log.info("Receiveing plugin enabled event...");
        Plugin plugin = event.getPlugin();
        if (this.pluginDetails.getPluginKey().equals(plugin.getKey())) {
        	startProxy();
        }
	}

	@Override
	@EventListener
	public void onConfigChange(ConfigurationChangedEvent event) {
		log.info("Receiveing configuration changed event...");
		shutdownProxy();
		startProxy();
	}


	private void startProxy() {
		if(proxyDisabled) {
			log.debug("Internal proxy is disabled, not starting!");
			return;
		}
		Optional<PluginConfiguration> pluginConfiguration = this.configurationService.getPluginConfiguration();
		pluginConfiguration.ifPresent(cfg -> {
			log.debug("Initializing internal proxy...");
			AtlassianInternalHttpProxy.INSTANCE.init(cfg.getInternalProxyConfig());
			log.debug("Filter initialized");
		});
	}


	private void shutdownProxy() {
		if(proxyDisabled) {
			log.debug("Internal proxy is disabled, no shutdown needed!");
			return;
		}

		log.debug("Shutting down internal proxy...");
		AtlassianInternalHttpProxy.INSTANCE.shutdown();
		log.debug("Filter destroyed");
	}
}
