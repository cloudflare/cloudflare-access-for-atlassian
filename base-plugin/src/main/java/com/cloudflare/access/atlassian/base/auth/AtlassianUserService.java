package com.cloudflare.access.atlassian.base.auth;

import com.atlassian.crowd.embedded.api.User;

public interface AtlassianUserService {

	User getUser(String email);

}
