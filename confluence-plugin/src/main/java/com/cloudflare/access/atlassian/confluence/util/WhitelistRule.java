package com.cloudflare.access.atlassian.confluence.util;

import javax.servlet.http.HttpServletRequest;

//TODO move to commons
@FunctionalInterface
public interface WhitelistRule {

	boolean match(HttpServletRequest request);

}
