package com.cloudflare.access.atlassian.base.support;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.cloudflare.access.atlassian.base.config.ConfigurationChangedEvent;

public interface PluginStateWatchService extends InitializingBean, DisposableBean{

	void onConfigChange(ConfigurationChangedEvent event);

}
