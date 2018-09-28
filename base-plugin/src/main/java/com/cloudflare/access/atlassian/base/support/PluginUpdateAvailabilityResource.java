package com.cloudflare.access.atlassian.base.support;

import java.io.IOException;
import java.util.Optional;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import com.cloudflare.access.atlassian.base.auth.CloudflarePluginDetails;
import com.cloudflare.access.atlassian.base.utils.SessionUtils;

@Path("/pluginUpdateAvailability")
public class PluginUpdateAvailabilityResource{


	private static final Logger log = LoggerFactory.getLogger(PluginUpdateAvailabilityResource.class);
	private RemoteVersionProvider remoteVersionProvider;
	private UserManager userManager;
	private PluginAccessor pluginAcessor;
	private CloudflarePluginDetails pluginDetails;

	@Autowired
	public PluginUpdateAvailabilityResource(RemoteVersionProvider remoteVersionProvider,
								 @ComponentImport UserManager userManager,
								 @ComponentImport PluginAccessor pluginAcessor,
								 CloudflarePluginDetails pluginDetails) {
		this.remoteVersionProvider = remoteVersionProvider;
		this.userManager = userManager;
		this.pluginAcessor = pluginAcessor;
		this.pluginDetails = pluginDetails;
	}


	@GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Path("/check")
	public Response check(@Context HttpServletRequest req) throws ServletException, IOException {
		if(shouldNotifyUser(req)) {
			Optional<String> newVersion = getNewVersion();
			if(newVersion.isPresent()) {
				SessionUtils.markUpdateCheckNotified(req);
				return Response.ok(new PluginUpdateAvailability(newVersion.get())).build();
			}
		}

		return Response.ok(new PluginUpdateAvailability()).build();
	}

	private boolean shouldNotifyUser(HttpServletRequest req) {
		boolean alreadyNotified = SessionUtils.isUpdateCheckAlreadyNotified(req);
		UserKey currentUserKey = userManager.getRemoteUserKey(req);
		return !alreadyNotified && userManager.isAdmin(currentUserKey);
	}

	private Optional<String> getNewVersion() {
		String currentVersion = getCurrentVersion();
		String latestReleaseVersion = remoteVersionProvider.getLatestReleaseVersion();

		log.debug("Comparing versions: [latest: {}, current: {}]", latestReleaseVersion, currentVersion);

		if(VersionComparator.INSTANCE.compare(latestReleaseVersion, currentVersion) > 0) {
			return Optional.of(latestReleaseVersion);
		}

		return Optional.empty();
	}

	private String getCurrentVersion() {
		return pluginAcessor.getPlugin(this.pluginDetails.getPluginKey()).getPluginInformation().getVersion();
	}

}