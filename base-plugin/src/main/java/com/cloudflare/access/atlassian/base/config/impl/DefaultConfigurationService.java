package com.cloudflare.access.atlassian.base.config.impl;

import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.substringAfterLast;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.activeobjects.tx.Transactional;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.cloudflare.access.atlassian.base.config.ConfigurationChangedEvent;
import com.cloudflare.access.atlassian.base.config.ConfigurationService;
import com.cloudflare.access.atlassian.base.config.ConfigurationVariables;
import com.cloudflare.access.atlassian.base.config.ConfigurationVariablesActiveObject;
import com.cloudflare.access.atlassian.base.config.PersistentPluginConfiguration;
import com.cloudflare.access.atlassian.base.config.PluginConfiguration;
import com.cloudflare.access.atlassian.base.config.TokenAudienceActiveObject;
import com.cloudflare.access.atlassian.common.CertificateProvider;
import com.cloudflare.access.atlassian.common.http.SimpleHttp;
import com.google.common.collect.Sets;

import net.java.ao.Entity;

@Component
@Transactional
public class DefaultConfigurationService implements ConfigurationService{

	private static final Logger log = LoggerFactory.getLogger(DefaultConfigurationService.class);
	private final ActiveObjects activeObjects;
	private final EventPublisher eventPublisher;
	private final CertificateProvider certificateProvider;
	private final Map<CacheKey,ConfigurationVariables> configCache;

	@Inject
	public DefaultConfigurationService(@ComponentImport ActiveObjects activeObjects,
										@ComponentImport EventPublisher eventPublisher) {
		super();
		this.activeObjects = activeObjects;
		this.eventPublisher = eventPublisher;
		this.certificateProvider = new CertificateProvider(new SimpleHttp());
		this.configCache = new ConcurrentHashMap<>();
	}

	@Override
	public void save(ConfigurationVariables configVariables) {
		ConfigurationVariablesActiveObject entity = findFirst()
				.orElseGet(() -> activeObjects.create(ConfigurationVariablesActiveObject.class));

		entity.setAuthDomain(configVariables.getAuthDomain());
		entity.setAllowedEmailDomain(configVariables.getAllowedEmailDomain());
		entity.setUserMatchingAttribute(configVariables.getUserMatchingAttribute());
		
		saveAudiences(configVariables, entity);
		entity.save();

		log.info("Publishing configuration changed event...");
		eventPublisher.publish(new ConfigurationChangedEvent(this));

		this.configCache.put(CacheKey.MAIN_CONFIG, configVariables);
	}

	private void saveAudiences(ConfigurationVariables configVariables, ConfigurationVariablesActiveObject currentConfig) {
		Collection<TokenAudienceActiveObject> currentAudiences = Arrays.stream(currentConfig.getTokenAudiences()).collect(Collectors.toList());
		Set<String> updatedAudienceTags = Sets.newHashSet(configVariables.getTokenAudience());

		TokenAudienceActiveObject[] removedAudiences = currentAudiences.stream()
			.filter(aud -> updatedAudienceTags.contains(aud.getValue()) == false)
			.toArray(TokenAudienceActiveObject[]::new);
		activeObjects.delete(removedAudiences);
		
		Set<String> currentAudienceTags = currentAudiences.stream()
				.map(aud -> aud.getValue())
				.collect(Collectors.toSet());
		
		updatedAudienceTags
			.stream()
			.filter(tag -> currentAudienceTags.contains(tag) == false)
			.map(tag -> {
				TokenAudienceActiveObject aud = activeObjects.create(TokenAudienceActiveObject.class);
				aud.setConfig(currentConfig);
				aud.setValue(tag);
				return aud;
			})
			.forEach(Entity::save);
	}
	
	@Override
	public Optional<ConfigurationVariables> loadConfigurationVariables() {
		ConfigurationVariables configurationVariables = this.configCache.computeIfAbsent(CacheKey.MAIN_CONFIG, key -> {
			return findFirst()
					.map(ConfigurationVariables::new)
					.orElse(null);
		});

		return Optional.ofNullable(configurationVariables);
	}

	private Optional<ConfigurationVariablesActiveObject> findFirst() {
		ConfigurationVariablesActiveObject[] result = activeObjects.find(ConfigurationVariablesActiveObject.class);
		if(result.length == 0) return Optional.empty();
		return Optional.of(result[0]);
	}

	@Override
	public Optional<PluginConfiguration> getPluginConfiguration() {
		Optional<ConfigurationVariables> variables = loadConfigurationVariables();
		if(variables.isPresent()) {
			return Optional.of(new PersistentPluginConfiguration(variables.get(), certificateProvider));
		}else {
			return Optional.empty();
		}
	}

	@Override
	public boolean emailDomainRequiresAtlassianAuthentication(String email) {
		String emailDomain = defaultString(substringAfterLast(email, "@"), "");
		Optional<String> allowedEmailDomain = getPluginConfiguration().flatMap(cfg -> cfg.getAllowedEmailDomain());
		if(allowedEmailDomain.isPresent()) {
			return emailDomain.equalsIgnoreCase(allowedEmailDomain.get()) == false;
		}
		//by default, don't require atlassian authentication.
		return false;
	}

	private enum CacheKey{
		MAIN_CONFIG;
	}
}
