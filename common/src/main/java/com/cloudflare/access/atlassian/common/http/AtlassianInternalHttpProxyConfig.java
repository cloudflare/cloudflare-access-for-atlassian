package com.cloudflare.access.atlassian.common.http;

public class AtlassianInternalHttpProxyConfig {

	private String address;
	private int port;
	public AtlassianInternalHttpProxyConfig(String address, int port) {
		super();
		this.address = address;
		this.port = port;
	}
	public String getAddress() {
		return address;
	}
	public int getPort() {
		return port;
	}


}
