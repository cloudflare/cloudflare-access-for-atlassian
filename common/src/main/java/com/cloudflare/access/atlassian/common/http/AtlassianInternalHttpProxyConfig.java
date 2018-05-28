package com.cloudflare.access.atlassian.common.http;

public class AtlassianInternalHttpProxyConfig {

	private String address;
	private int port;
	private boolean useHttps;

	public AtlassianInternalHttpProxyConfig(String address, int port, boolean useHttps) {
		super();
		this.address = address;
		this.port = port;
		this.useHttps = useHttps;
	}

	public String getAddress() {
		return address;
	}

	public int getPort() {
		return port;
	}

	public boolean shouldUseHttps() {
		return useHttps;
	}

}
