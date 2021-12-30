package com.cloudflare.access.atlassian.base.config.impl;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertNotNull;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

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
import com.google.common.collect.Sets;

import net.java.ao.EntityManager;
import net.java.ao.test.jdbc.NonTransactional;
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
	@NonTransactional
	public void saveShouldPersistSingleAudience() throws SQLException {
		configService.save(new ConfigurationVariables(Sets.newHashSet("the_audience"), "domain", "testdomain.com", "someattribute"));

		ConfigurationVariablesActiveObject[] configs = entityManager.find(ConfigurationVariablesActiveObject.class);

		assertThat(configs.length, is(1));
		assertThat(configs[0].getTokenAudiences().length, is(1));
		assertThat(configs[0].getTokenAudiences()[0].getValue(), is("the_audience"));
	}

	@Test
	@NonTransactional
	public void saveShouldPersistMultipleAudiences() throws SQLException {
		configService.save(new ConfigurationVariables(
				Sets.newHashSet("the_audience_01", "the_audience_02", "the_audience_03"),
				"domain",
				"testdomain.com",
				"someattribute"));

		ConfigurationVariablesActiveObject[] configs = entityManager.find(ConfigurationVariablesActiveObject.class);

		assertThat(configs.length, is(1));
		assertThat(configs[0].getTokenAudiences().length, is(3));
		Collection<String> audiences = Arrays.stream(configs[0].getTokenAudiences())
				.map(TokenAudienceActiveObject::getValue)
				.collect(Collectors.toList());
		assertThat(audiences , hasItems("the_audience_01", "the_audience_02", "the_audience_03"));
	}

	@Test
	@NonTransactional
	public void saveShouldRemovePreviousAudiences() throws SQLException {
		configService.save(new ConfigurationVariables(Sets.newHashSet("the_audience_01"), "domain", "testdomain.com", "someattribute"));
		configService.save(new ConfigurationVariables(Sets.newHashSet("the_audience_02"), "domain", "testdomain.com", "someattribute"));
		ConfigurationVariablesActiveObject[] configs = entityManager.find(ConfigurationVariablesActiveObject.class);

		assertThat(configs.length, is(1));
		assertThat(configs[0].getTokenAudiences().length, is(1));
		assertThat(configs[0].getTokenAudiences()[0].getValue(), is("the_audience_02"));
	}

	@Test
	@NonTransactional
	public void findFirstShouldPreloadRelations() throws SQLException {
		configService.save(new ConfigurationVariables(Sets.newHashSet("the_audience_01"), "domain", "testdomain.com", "someattribute"));

		ConfigurationVariablesActiveObject config = configService.findFirst().get();

		// Adds a listener to catch queries executed after findFirst returned.
		List<String> queriesAfterFindFirst = new ArrayList<>();
		entityManager.getProvider().addSqlListener(sql -> queriesAfterFindFirst.add(sql));

		// Assert the relation was loaded correctly, if preload was not done
		// this will trigger a query.
		assertThat(Arrays.stream(config.getTokenAudiences()).map(aud -> aud.getValue()).collect(Collectors.toSet()), is(Sets.newHashSet("the_audience_01")));
		assertThat(queriesAfterFindFirst, is(empty()));
	}
}
