package com.cloudflare.access.atlassian.jira.config;

import java.util.Collection;
import java.util.Collections;

public class ConfigSaveResult {

	public static final ConfigSaveResult error(String msg, Collection<String> violations) {
		return new ConfigSaveResult(msg, violations);
	}

	public static final ConfigSaveResult ok() {
		return new ConfigSaveResult("Configuration updated!");
	}

	private boolean error;
	private String msgTitle;
	private String msgCssClass;
	private String msg;
	private Collection<String> violations;

	public ConfigSaveResult(String msg, Collection<String> violations) {
		super();
		this.error = true;
		this.msgTitle = "Error";
		this.msgCssClass = "aui-message-error";
		this.msg = msg;
		this.violations = violations;
	}

	private ConfigSaveResult(String msg) {
		super();
		this.error = false;
		this.msgTitle = "Success";
		this.msgCssClass = "aui-message-success";
		this.msg = msg;
		this.violations = Collections.emptySet();
	}

	public boolean isError() {
		return error;
	}

	public String getMsgTitle() {
		return msgTitle;
	}

	public String getMsgCssClass() {
		return msgCssClass;
	}

	public String getMsg() {
		return msg;
	}

	public Collection<String> getViolations() {
		return violations;
	}
}
