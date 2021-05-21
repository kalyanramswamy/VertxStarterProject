package com.vertx.starter.exception;

import lombok.Getter;

@Getter
public enum vertxError {

	TEST("VX-1000", "test");

	private final String code;
	private final String message;

	vertxError(String code, String message) {
		this.code = code;
		this.message = message;
	}
}
