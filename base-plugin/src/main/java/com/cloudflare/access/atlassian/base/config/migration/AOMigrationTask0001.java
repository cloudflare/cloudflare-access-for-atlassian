package com.cloudflare.access.atlassian.base.config.migration;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.activeobjects.external.ActiveObjectsUpgradeTask;
import com.atlassian.activeobjects.external.ModelVersion;
import com.cloudflare.access.atlassian.base.config.ConfigurationVariablesActiveObject;
import com.cloudflare.access.atlassian.base.config.TokenAudienceActiveObject;
import com.google.common.collect.Sets;

public class AOMigrationTask0001 implements ActiveObjectsUpgradeTask {

	private static final Logger log = LoggerFactory.getLogger(AOMigrationTask0001.class);
	
	@Override
	public ModelVersion getModelVersion() {
		return ModelVersion.valueOf("1");
	}

	@Override
	public void upgrade(ModelVersion modelVersion, ActiveObjects ao) {
		log.info("Migrating configurations entities...");
		
		ao.migrate(
			ConfigurationVariablesActiveObject.class, 
			TokenAudienceActiveObject.class
		);
	
		for(ConfigurationVariablesActiveObject cfg : ao.find(ConfigurationVariablesActiveObject.class)) {
			TokenAudienceActiveObject audience = ao.create(TokenAudienceActiveObject.class); 
			audience.setConfig(cfg);
			audience.setValue(cfg.getTokenAudience());
			audience.save();
			log.info("Updated configuration audiences for config {}", cfg.getID());
		}
		
	}

}
