package com.cloudflare.access.atlassian.base.auth;

import java.io.IOException;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.annotations.security.UnrestrictedAccess;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.cloudflare.access.atlassian.base.utils.SessionUtils;
import com.google.common.collect.Maps;

@Scanned
public class AuthenticationErrorServlet extends HttpServlet {
	private static final long serialVersionUID = -5289553688902542990L;

	private static final Logger log = LoggerFactory.getLogger(AuthenticationErrorServlet.class);

	public static final String ERROR_MSG_PARAM = "reason";
	private static final String REDIRECT_TO_LOGIN_PARAM = "goToLogin";

	public static final String PATH = "/plugins/servlet/cloudflareaccess/auth/error";
	private static final String ERROR_TEMPLATE = "/templates/error.vm";

	private TemplateRenderer templateRenderer;
	private String loginPath;

	@Inject
	public AuthenticationErrorServlet(@ComponentImport TemplateRenderer templateRenderer) {
		this.templateRenderer = templateRenderer;
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		this.loginPath = config.getInitParameter("loginPath");
	}

	@Override
	@UnrestrictedAccess
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		if(shouldRedirectToLogin(req)) {
			redirectToLoginWithAtlassianFlowEnabled(req, resp);
		}else {
			renderErrorPage(req, resp);
		}
	}

	private boolean shouldRedirectToLogin(HttpServletRequest req) {
		return req.getParameterMap().containsKey(REDIRECT_TO_LOGIN_PARAM);
	}

	private void redirectToLoginWithAtlassianFlowEnabled(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		log.debug("Enabling the atlassian flow for login, this will bypass the plugin during the current session...");
		SessionUtils.enableAtlassianFlowSession(req);

		String redirectPath = req.getContextPath() + this.loginPath;
		redirectPath = redirectPath.startsWith("//") ? redirectPath.substring(1) : redirectPath;

		log.debug("Redirecting user to: " + redirectPath);
		resp.sendRedirect(redirectPath);
	}

	private void renderErrorPage(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType("text/html;charset=utf-8");

		Map<String, Object> context = Maps.newHashMap();
		context.put("errorMessage", req.getParameter(ERROR_MSG_PARAM));

		templateRenderer.render(ERROR_TEMPLATE, context, resp.getWriter());
	}



}