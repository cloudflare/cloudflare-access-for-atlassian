package com.cloudflare.access.atlassian.base.support;


import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

public class GithubVersionProviderTest {

	private static final long DEFAULT_READ_TIMEOUT = 10L;
	private SlowHttpServer testServer;

	@Before
	public void setup() throws IOException {
		this.testServer = new SlowHttpServer();
	}

	@After
	public void teardown() {
		this.testServer.shutdown();
	}

	@Test
	public void testThatCanRetrieveLatestReleaseInfo() {
		String latestReleaseVersion = new GithubVersionProvider().getLatestReleaseVersion();
		assertTrue(StringUtils.isNotBlank(latestReleaseVersion));
		assertTrue(latestReleaseVersion.matches("[0-9]+\\.[0-9]+\\.[0-9]+$"));
	}

	@Test(timeout = 30 * 1000)
	public void testThatItDoesNotHangLongerThanDefaultTimeoutReturningEmpty() {
		testServer.makeServerHang();
		Instant start = Instant.now();
		String latestReleaseVersion = new GithubVersionProvider(testServer.getUrl("/slow")).getLatestReleaseVersion();
		long hangDuration = Duration.between(start, Instant.now()).toSeconds();
		testServer.releaseServerWork();

		assertThat(hangDuration, is(greaterThanOrEqualTo(DEFAULT_READ_TIMEOUT)));
		// check that it didn't took too much longer than the read timeout to complete
		assertThat(hangDuration, is(lessThanOrEqualTo(DEFAULT_READ_TIMEOUT + 2)));
		assertTrue(StringUtils.isBlank(latestReleaseVersion));
	}


	/**
	 * This is a test HTTP server that will provide
	 * a way to hang a request for some time to validate
	 * the timeout configuration.
	 */
	static class SlowHttpServer{
		private static final Logger log = LoggerFactory.getLogger(SlowHttpServer.class);
		private final HttpServer server;

		/**
		 * Used to control the server hanging
		 */
		private ReentrantLock lock = new ReentrantLock();

		SlowHttpServer() throws IOException{
			server = HttpServer.create(new InetSocketAddress(0), 0);
			log.info("Started test http server at port {}", server.getAddress().getPort());
			server.createContext("/slow", this::handleGetWithDelay);
			server.setExecutor(null);
			server.start();
		}

		void handleGetWithDelay(HttpExchange he) throws IOException {
			lock.lock();
			lock.unlock();
			he.sendResponseHeaders(200, 0);
			OutputStream os = he.getResponseBody();
			os.close();
		}

		void shutdown(){
			releaseServerWork();
			log.info("Shutdown test http server...");
			server.stop(0);
		}

		void makeServerHang() {
			lock.lock();
		}

		void releaseServerWork() {
			if(lock.isLocked()) {
				lock.unlock();
			}
		}

		String getUrl(String path) {
			return String.format("http://localhost:%d%s", server.getAddress().getPort(), path);
		}
	}
}
