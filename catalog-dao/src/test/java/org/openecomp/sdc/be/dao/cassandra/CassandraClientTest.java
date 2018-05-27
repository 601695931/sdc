package org.openecomp.sdc.be.dao.cassandra;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.tinkerpop.gremlin.structure.T;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.utils.DAOConfDependentTest;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Cluster.Builder;
import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.MappingManager;

import fj.data.Either;
import mockit.Deencapsulation;

public class CassandraClientTest extends DAOConfDependentTest {

	private CassandraClient createTestSubject() {
		return new CassandraClient();
	}

	@Test
	public void testSetLocalDc() throws Exception {
		CassandraClient testSubject;
		Cluster.Builder clusterBuilder = null;

		Builder mock = Mockito.mock(Cluster.Builder.class);
		Mockito.when(mock.withLoadBalancingPolicy(Mockito.any())).thenReturn(new Builder());
		// default test
		testSubject = createTestSubject();
		Deencapsulation.invoke(testSubject, "setLocalDc", new Object[] { Cluster.Builder.class });

		ConfigurationManager.getConfigurationManager().getConfiguration().getCassandraConfig()
				.setLocalDataCenter("mock");

		testSubject = createTestSubject();
		Deencapsulation.invoke(testSubject, "setLocalDc", mock);
	}

	@Test
	public void testEnableSsl() throws Exception {
		CassandraClient testSubject;
		Cluster.Builder clusterBuilder = null;

		Builder mock = Mockito.mock(Cluster.Builder.class);
		Mockito.when(mock.withSSL()).thenReturn(new Builder());

		ConfigurationManager.getConfigurationManager().getConfiguration().getCassandraConfig().setSsl(false);
		// default test
		testSubject = createTestSubject();
		Deencapsulation.invoke(testSubject, "enableSsl", mock);

		ConfigurationManager.getConfigurationManager().getConfiguration().getCassandraConfig().setSsl(true);
		// default test
		testSubject = createTestSubject();
		Deencapsulation.invoke(testSubject, "enableSsl", mock);
		ConfigurationManager.getConfigurationManager().getConfiguration().getCassandraConfig().setTruststorePath(null);
		ConfigurationManager.getConfigurationManager().getConfiguration().getCassandraConfig()
				.setTruststorePassword(null);
		Deencapsulation.invoke(testSubject, "enableSsl", mock);
	}

	@Test
	public void testEnableAuthentication() throws Exception {
		CassandraClient testSubject;
		Builder mock = Mockito.mock(Cluster.Builder.class);
		Mockito.when(mock.withCredentials(Mockito.any(), Mockito.any())).thenReturn(new Builder());

		// default test
		testSubject = createTestSubject();
		Deencapsulation.invoke(testSubject, "enableAuthentication", Cluster.Builder.class);

		ConfigurationManager.getConfigurationManager().getConfiguration().getCassandraConfig().setAuthenticate(true);
		Deencapsulation.invoke(testSubject, "enableAuthentication", mock);
		ConfigurationManager.getConfigurationManager().getConfiguration().getCassandraConfig().setUsername(null);
		ConfigurationManager.getConfigurationManager().getConfiguration().getCassandraConfig().setPassword(null);
		Deencapsulation.invoke(testSubject, "enableAuthentication", mock);
	}

	@Test
	public void testConnect() throws Exception {
		CassandraClient testSubject;
		String keyspace = "";
		Either<ImmutablePair<Session, MappingManager>, CassandraOperationStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.connect(keyspace);
	}

	@Test
	public void testSave() throws Exception {
		CassandraClient testSubject;
		T entity = null;
		Class<T> clazz = null;
		MappingManager manager = null;
		CassandraOperationStatus result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.save(entity, clazz, manager);
	}

	@Test
	public void testGetById() throws Exception {
		CassandraClient testSubject;
		String id = "";
		Class<T> clazz = null;
		MappingManager manager = null;
		Either<T, CassandraOperationStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getById(id, clazz, manager);
	}

	@Test
	public void testDelete() throws Exception {
		CassandraClient testSubject;
		String id = "";
		Class<T> clazz = null;
		MappingManager manager = null;
		CassandraOperationStatus result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.delete(id, clazz, manager);
	}

	@Test
	public void testIsConnected() throws Exception {
		CassandraClient testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isConnected();
	}

	@Test
	public void testCloseClient() throws Exception {
		CassandraClient testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.closeClient();
	}
}