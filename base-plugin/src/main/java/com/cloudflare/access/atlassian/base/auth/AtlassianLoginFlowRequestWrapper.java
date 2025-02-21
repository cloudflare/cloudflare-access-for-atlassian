package com.cloudflare.access.atlassian.base.auth;

import com.cloudflare.access.atlassian.base.utils.SessionUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;

/**
 * Wraps the request to intercept the session creation and enforce the Atlassian Login flow.
 * This is required as in case the current session fallback to the Atlassian application login
 * the login process recycles the initial session and the flag is lost. By intercepting the
 * session creation we can ensure the flag is always set.
 */
public class AtlassianLoginFlowRequestWrapper extends HttpServletRequestWrapper {

    private final HttpServletRequest originalRequest;

    public AtlassianLoginFlowRequestWrapper(HttpServletRequest request) {
        super(request);
        this.originalRequest = request;
    }

    @Override
    public HttpSession getSession() {
        // It always creates a new session if one does not exist yet, enforce the flow.
        final HttpSession session = super.getSession();
        SessionUtils.enableAtlassianFlowSession(originalRequest);
        return session;
    }

    @Override
    public HttpSession getSession(boolean create) {
        final HttpSession session = super.getSession(create);
        if(session != null) {
            // Here a session may have not been created, if that is the case the plugin
            // also doesn't create one.
            SessionUtils.enableAtlassianFlowSession(originalRequest);
        }
        return session;
    }
}
