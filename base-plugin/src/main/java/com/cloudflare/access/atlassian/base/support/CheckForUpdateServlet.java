package com.cloudflare.access.atlassian.base.support;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;

import javax.inject.Inject;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import com.cloudflare.access.atlassian.base.auth.CloudflarePluginDetails;

@Scanned
public class CheckForUpdateServlet extends HttpServlet {

	private static final long serialVersionUID = 7746463406383116145L;
	private static final Logger log = LoggerFactory.getLogger(CheckForUpdateServlet.class);
	private RemoteVersionProvider remoteVersionProvider;
	private UserManager userManager;
	private PluginAccessor pluginAcessor;
	private CloudflarePluginDetails pluginDetails;

	@Inject
	public CheckForUpdateServlet(@ComponentImport RemoteVersionProvider remoteVersionProvider,
								 @ComponentImport UserManager userManager,
								 @ComponentImport PluginAccessor pluginAcessor,
								 @ComponentImport CloudflarePluginDetails pluginDetails) {
		this.remoteVersionProvider = remoteVersionProvider;
		this.userManager = userManager;
		this.pluginAcessor = pluginAcessor;
		this.pluginDetails = pluginDetails;
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try(PrintWriter writer = resp.getWriter()){
			if(shouldNotifyUser(req)) {
				writer.println(getEmptyJsonResponse());
			}else {
				Optional<String> newVersion = getNewVersion();
				if(newVersion.isPresent()) {
					writer.println(getSimpleJsonResponse(newVersion.get()));
				}else {
					writer.println(getEmptyJsonResponse());
				}
			}
			resp.setContentType("application/json");
		}
	}

	private boolean shouldNotifyUser(HttpServletRequest req) {
		UserKey currentUserKey = userManager.getRemoteUserKey(req);
		return userManager.isAdmin(currentUserKey);
	}

	private String getSimpleJsonResponse(String newVersion) {
		return String.format("{\"hasNewVersion\":true, \"newVersion\": \"%s\"}", newVersion);
	}

	private String getEmptyJsonResponse() {
		return String.format("{\"hasNewVersion\":false}");
	}

	private Optional<String> getNewVersion() {
		String currentVersion = getCurrentVersion();
		String latestReleaseVersion = remoteVersionProvider.getLatestReleaseVersion();

		if(StringUtils.isAnyBlank(currentVersion, latestReleaseVersion) || VersionComparator.INSTANCE.compare(latestReleaseVersion, currentVersion) > 0) {
			return Optional.empty();
		}else {
			return Optional.of(latestReleaseVersion);
		}
	}

	private String getCurrentVersion() {
		return pluginAcessor.getPlugin(this.pluginDetails.getPluginKey()).getPluginInformation().getVersion();
	}

}