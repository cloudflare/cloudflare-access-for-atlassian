package com.cloudflare.access.atlassian.base.auth;

import java.io.IOException;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.cloudflare.access.atlassian.base.utils.SessionUtils;
import com.google.common.collect.Maps;

@Scanned
public class AuthenticationErrorServlet extends HttpServlet {
	private static final long serialVersionUID = -5289553688902542990L;

	public static final String ERROR_MSG_PARAM = "reason";
	public static final String PATH = "/plugins/servlet/cloudflareaccess/auth/error.vm";
	private static final String ERROR_TEMPLATE = "/templates/error.vm";

	private TemplateRenderer templateRenderer;

	private RememberMeHelperService rememberMeHelperService;

	@Inject
	public AuthenticationErrorServlet(@ComponentImport TemplateRenderer templateRenderer, RememberMeHelperService rememberMeHelperService) {
		this.templateRenderer = templateRenderer;
		this.rememberMeHelperService = rememberMeHelperService;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/html;charset=utf-8");

        Map<String, Object> context = Maps.newHashMap();
        context.put("errorMessage", req.getParameter(ERROR_MSG_PARAM));

        SessionUtils.clearSession(req);
        this.rememberMeHelperService.clearRemeberMe(req, resp);

        templateRenderer.render(ERROR_TEMPLATE, context, resp.getWriter());
	}

}