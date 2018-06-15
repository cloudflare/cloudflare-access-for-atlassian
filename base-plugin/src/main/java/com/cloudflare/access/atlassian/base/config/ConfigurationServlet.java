package com.cloudflare.access.atlassian.base.config;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;

import org.apache.commons.lang3.StringUtils;

import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.templaterenderer.RenderingException;
import com.atlassian.templaterenderer.TemplateRenderer;

@Scanned
public class ConfigurationServlet extends HttpServlet{

	private static final long serialVersionUID = -4504881490626321134L;
	private static final String CTX_RESULT = "result";
	private static final String CTX_CONFIG = "config";

	private final UserManager userManager;
	private final TemplateRenderer renderer;
    private final ConfigurationService configurationService;

	@Inject
	public ConfigurationServlet(@ComponentImport UserManager userManager,
								@ComponentImport TemplateRenderer renderer,
								ConfigurationService configurationService){
		this.userManager = userManager;
		this.renderer = renderer;
		this.configurationService = configurationService;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    if(userIsNotAuthorized(request, response)) {
	    	return;
	    }

	    Map<String, Object> context = new HashMap<>();

	    configurationService
	    	.loadConfigurationVariables()
	    	.ifPresent(config -> context.put(CTX_CONFIG, config));

	    renderTemplate(context, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    if(userIsNotAuthorized(request, response)) {
	    	return;
	    }

	    ConfigurationVariables updatedConfig = loadFromRequest(request);
	    Set<ConstraintViolation<ConfigurationVariables>> violations = Validation.buildDefaultValidatorFactory().getValidator().validate(updatedConfig);

	    Map<String, Object> context = new HashMap<>();

	    if(violations.isEmpty()) {
	    	try {
	    		configurationService.save(updatedConfig);
	    		context.put(CTX_RESULT, ConfigSaveResult.ok());
	    	}catch (Exception e) {
	    		e.printStackTrace();
	    		context.put(CTX_RESULT, ConfigSaveResult.error(e.getMessage(), Collections.emptySet()));
			}
	    }else {
	    	List<String> violationMessages = violations.stream().map(v -> v.getMessage()).collect(Collectors.toList());
	    	context.put(CTX_RESULT, ConfigSaveResult.error("Invalid Configuration", violationMessages));
	    }

	    context.put(CTX_CONFIG, updatedConfig);
	    renderTemplate(context, response);
	}

	private boolean userIsNotAuthorized(HttpServletRequest request, HttpServletResponse response) throws IOException {
		UserKey userKey = userManager.getRemoteUserKey(request);
		if(userKey == null || userManager.isSystemAdmin(userKey) == false) {
			response.sendError(401, "User not authenticated or authorized");
			return true;
		}
		return false;
	}

	private void renderTemplate(Map<String, Object> context, HttpServletResponse response) throws RenderingException, IOException {
	    response.setContentType("text/html;charset=utf-8");
	    renderer.render("/templates/config.vm", context, response.getWriter());
	}

	private ConfigurationVariables loadFromRequest(HttpServletRequest request) {
		String tokenAudience = request.getParameter("tokenAudience");
	    String authDomain = request.getParameter("authDomain");
	    String localConnectorHost = request.getParameter("localConnectorHost");
	    String localConnectorPort = request.getParameter("localConnectorPort");

	    int localConnectorPortNumber = StringUtils.isNumeric(localConnectorPort) ? Integer.parseInt(localConnectorPort) : 0;

		return new ConfigurationVariables(tokenAudience, authDomain, localConnectorHost, localConnectorPortNumber);
	}

}
