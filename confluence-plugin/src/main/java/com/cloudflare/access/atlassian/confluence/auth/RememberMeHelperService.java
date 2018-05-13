package com.cloudflare.access.atlassian.confluence.auth;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;

import com.atlassian.sal.api.component.ComponentLocator;
import com.atlassian.seraph.service.rememberme.RememberMeService;


@Component
public class RememberMeHelperService {

	public void removeRememberMeCookie(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		ComponentLocator.getComponent(RememberMeService.class).removeRememberMeCookie(httpRequest, httpResponse);
	}

}
