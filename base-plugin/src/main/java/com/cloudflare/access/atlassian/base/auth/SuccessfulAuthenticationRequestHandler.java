package com.cloudflare.access.atlassian.base.auth;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.crowd.embedded.api.User;

public interface SuccessfulAuthenticationRequestHandler {

	void handle(HttpServletRequest httpRequest, HttpServletResponse httpResponse, FilterChain chain, User user) throws IOException, ServletException;

}
