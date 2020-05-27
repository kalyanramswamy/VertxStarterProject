package com.vertxtest.exception;

import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class VertxException extends Exception {

	private static final long serialVersionUID = -5781799913689135289L;
	private vertxError cmsError;
	private String reason;

	public VertxException(vertxError cmsError, String reason) {
		super(reason);
		this.cmsError = cmsError;
		this.reason = reason;
	}
	
	  public int getHttpErrorCode() {
		    return 400;
		  }
}
