package com.cloudflare.access.atlassian.base.config;

import java.io.IOException;
import java.lang.annotation.ElementType;
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
import javax.validation.Path;
import javax.validation.Path.Node;
import javax.validation.TraversableResolver;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;

import org.springframework.core.env.Environment;

import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.templaterenderer.RenderingException;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.cloudflare.access.atlassian.base.auth.CloudflarePluginDetails;
import com.cloudflare.access.atlassian.base.support.AtlassianApplicationType;
import com.cloudflare.access.atlassian.base.utils.EnvironmentFlags;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

@Scanned
public class ConfigurationServlet extends HttpServlet{

	private static final long serialVersionUID = -4504881490626321134L;
	private static final String CTX_RESULT = "result";
	private static final String CTX_SHOW_TITLE = "showTitle";
	private static final String CTX_FILTERING_DISABLED = "filteringDisabled";
	private static final String CTX_CONFIG = "config";


	private final UserManager userManager;
	private final TemplateRenderer renderer;
    private final ConfigurationService configurationService;
    private final CloudflarePluginDetails pluginDetails;

    private final Supplier<ValidatorFactory> validatorFactorySupplier;
	private boolean filteringDisabled;

	@Inject
	public ConfigurationServlet(@ComponentImport UserManager userManager,
								@ComponentImport TemplateRenderer renderer,
								ConfigurationService configurationService,
								CloudflarePluginDetails pluginDetails,
								Environment env){
		this.userManager = userManager;
		this.renderer = renderer;
		this.configurationService = configurationService;
		this.pluginDetails = pluginDetails;
		this.validatorFactorySupplier = Suppliers.memoize(() -> createValidatorFactory());
		this.filteringDisabled = EnvironmentFlags.isFiltersDisabled(env);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    if(userIsNotAuthorized(request, response)) {
	    	return;
	    }

	    Map<String, Object> context = createContext();

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
	    Set<ConstraintViolation<ConfigurationVariables>> violations = validateConfig(updatedConfig);

	    Map<String, Object> context = createContext();

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

	private Set<ConstraintViolation<ConfigurationVariables>> validateConfig(ConfigurationVariables updatedConfig) {
		return validatorFactorySupplier.get()
	    		.getValidator()
	    		.validate(updatedConfig);
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
	    String allowedEmailDomain = request.getParameter("allowedEmailDomain");
	    String userMatchingAttribute = request.getParameter("userMatchingAttribute");

		return new ConfigurationVariables(tokenAudience, authDomain, allowedEmailDomain, userMatchingAttribute);
	}

	private Map<String, Object> createContext(){
		Map<String, Object> context = new HashMap<>();
		context.put(CTX_SHOW_TITLE, this.pluginDetails.getApplicationType() != AtlassianApplicationType.CONFLUENCE);
		context.put(CTX_FILTERING_DISABLED, this.filteringDisabled);
		return context;
	}

	private static final ValidatorFactory createValidatorFactory() {
		return Validation.byDefaultProvider()
	    		.configure()
	    		.traversableResolver(new TraversableResolver() {
					@Override
					public boolean isReachable(Object traversableObject, Node traversableProperty, Class<?> rootBeanType,
							Path pathToTraversableObject, ElementType elementType) {
						return true;
					}

					@Override
					public boolean isCascadable(Object traversableObject, Node traversableProperty, Class<?> rootBeanType,
							Path pathToTraversableObject, ElementType elementType) {
						return true;
					}
				})
	    		.buildValidatorFactory();
	}
}
