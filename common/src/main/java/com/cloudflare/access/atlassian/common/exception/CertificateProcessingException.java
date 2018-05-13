package com.cloudflare.access.atlassian.common.exception;

public class CertificateProcessingException extends RuntimeException {

	private static final long serialVersionUID = -2327039864502214974L;

	public CertificateProcessingException(String msg) {
		super(msg);
	}

	public CertificateProcessingException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
