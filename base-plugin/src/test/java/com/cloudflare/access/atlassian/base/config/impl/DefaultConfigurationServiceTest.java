package com.cloudflare.access.atlassian.base.config.impl;

import static org.junit.Assert.assertNotNull;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.activeobjects.test.TestActiveObjects;
import com.atlassian.event.api.EventPublisher;
import com.cloudflare.access.atlassian.base.config.ConfigurationVariables;
import com.cloudflare.access.atlassian.base.config.ConfigurationVariablesActiveObject;
import com.cloudflare.access.atlassian.base.config.TokenAudienceActiveObject;

import net.java.ao.EntityManager;
import net.java.ao.test.junit.ActiveObjectsJUnitRunner;

@RunWith(ActiveObjectsJUnitRunner.class)
public class DefaultConfigurationServiceTest {

	private EntityManager entityManager;
	
	private DefaultConfigurationService configService;
	
	private EventPublisher eventPublisherMock;
	
	@Before
	public void setup() {
		assertNotNull(entityManager);
		eventPublisherMock = Mockito.mock(EventPublisher.class);

		ActiveObjects ao = new TestActiveObjects(entityManager);
		ao.migrate(
			TokenAudienceActiveObject.class,
			ConfigurationVariablesActiveObject.class
		);
		
		configService = new DefaultConfigurationService(ao, eventPublisherMock);
	}
	
	@Test
	public void saveShouldPersistSingleAudience() throws SQLException {
		configService.save(new ConfigurationVariables("the_audience", "domain", "testdomain.com", "someattribute"));
		
		ConfigurationVariablesActiveObject[] configs = entityManager.find(ConfigurationVariablesActiveObject.class);
		
		assertThat(configs.length, is(1));
		assertThat(configs[0].getTokenAudiences().length, is(1));
		assertThat(configs[0].getTokenAudiences()[0].getValue(), is("the_audience"));
	}
	
	@Test
	public void saveShouldRemovePreviousAudiences() throws SQLException {
		configService.save(new ConfigurationVariables("the_audience_01", "domain", "testdomain.com", "someattribute"));
		configService.save(new ConfigurationVariables("the_audience_02", "domain", "testdomain.com", "someattribute"));
		ConfigurationVariablesActiveObject[] configs = entityManager.find(ConfigurationVariablesActiveObject.class);
		
		assertThat(configs.length, is(1));
		assertThat(configs[0].getTokenAudiences().length, is(1));
		assertThat(configs[0].getTokenAudiences()[0].getValue(), is("the_audience_02"));
	}
}
