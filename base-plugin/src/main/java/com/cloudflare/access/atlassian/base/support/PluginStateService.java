package com.cloudflare.access.atlassian.base.support;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.event.PluginEventListener;
import com.atlassian.plugin.event.events.PluginEnabledEvent;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;

@Component
public class PluginStateService implements InitializingBean, DisposableBean {

	private static final Logger log = LoggerFactory.getLogger(PluginStateService.class);

	private final EventPublisher eventPublisher;

	private boolean ready;

	@Autowired
	public PluginStateService(@ComponentImport EventPublisher eventPublisher) {
		Objects.requireNonNull(eventPublisher);
		this.eventPublisher = eventPublisher;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.eventPublisher.register(this);
	}

	@Override
	public void destroy() throws Exception {
		this.eventPublisher.unregister(this);
	}


	@PluginEventListener
	public void onEvent(PluginEnabledEvent pluginEnabledEvent) {
		log.info("Pugin enabled event received, setting READY flag!");
		this.ready = true;
	}

	public boolean isReady() {
		return ready;
	}

}
