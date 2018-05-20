package com.cloudflare.access.atlassian.jira.util;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class RequestInspector {
	private static final String divider = "---------------------------------------------------------------------------------------\n";

	public static String getRequestedResourceInfo(HttpServletRequest request) {
		StringBuilder sb =  new StringBuilder();
		sb.append(divider);
		sb.append("ServerName:" + request.getServerName());
		sb.append("ServletPath:" + request.getServletPath());
		return sb.toString();
	}

	public static String getHeadersAndCookies(HttpServletRequest request) {
		if(request.getHeaderNames() == null) return "";
		StringBuilder sb =  new StringBuilder();
		sb.append(divider);
		Enumeration<String> headers = request.getHeaderNames();
		while (headers.hasMoreElements()) {
			String headerName = headers.nextElement();
			sb.append(String.format("Header[%s]: '%s'\n", headerName, enumerationAsList(request.getHeaders(headerName))));
		}
		sb.append(divider);
		sb.append("\n\n");
		sb.append(divider);
		Cookie[] cookies = request.getCookies();
		if(cookies == null) {
			sb.append("No cookies in the request!\n");
		}else {
			for (Cookie cookie : cookies) {
				sb.append(String.format("Cookie[%s]@'%s': '%s'\n", cookie.getName(), cookie.getDomain(), cookie.getValue()));
			}
		}

		sb.append(divider);
		return sb.toString();
	}

	public static String getSessionContents(HttpServletRequest request) {
		final HttpSession httpSession = request.getSession(false);
		if(httpSession == null) return "NO SESSION";

		StringBuilder sb =  new StringBuilder();
		sb.append(divider);
		Enumeration<String> sessionKeys = httpSession.getAttributeNames();
		while (sessionKeys.hasMoreElements()) {
			String key = sessionKeys.nextElement();
			sb.append(String.format("Session[%s]: '%s'\n", key, Objects.toString(httpSession.getAttribute(key))));
		}
		return sb.toString();
	}

	private static List<String> enumerationAsList(Enumeration<String> e){
		List<String> list = new ArrayList<>();
		while(e.hasMoreElements()) list.add(e.nextElement());
		return list;
	}

}
