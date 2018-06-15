package com.cloudflare.access.atlassian.jira.support;

import java.util.Optional;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.cloudflare.access.atlassian.common.config.PluginConfiguration;
import com.cloudflare.access.atlassian.common.http.AtlassianInternalHttpProxy;
import com.cloudflare.access.atlassian.jira.config.ConfigurationService;

@Component
public class DefaultPluginStateWatchService implements PluginStateWatchService{

	private static final Logger log = LoggerFactory.getLogger(DefaultPluginStateWatchService.class);

	private ConfigurationService configurationService;

	@Inject
	public DefaultPluginStateWatchService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		log.info("Plugin initializing...");
		startProxy();
	}


	@Override
	public void destroy() throws Exception {
		log.info("Destroying plugin...");
		shutdownProxy();
	}

	@Override
	public void onConfigChange() {
		//TODO listen to config changed event
		shutdownProxy();
		startProxy();
	}


	private void startProxy() {
		Optional<PluginConfiguration> pluginConfiguration = this.configurationService.getPluginConfiguration();
		pluginConfiguration.ifPresent(cfg -> {
			log.debug("Initializing internal proxy...");
			AtlassianInternalHttpProxy.INSTANCE.init(cfg.getInternalProxyConfig());
			log.debug("Filter initialized");
		});
	}


	private void shutdownProxy() {
		log.debug("Shutting down internal proxy...");
		AtlassianInternalHttpProxy.INSTANCE.shutdown();
		log.debug("Filter destroyed");
	}
}
