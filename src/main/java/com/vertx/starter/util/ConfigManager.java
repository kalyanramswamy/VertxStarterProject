package com.vertx.starter.util;

import io.vertx.core.json.JsonObject;

public class ConfigManager {

	public static JsonObject getConfig() {
		return config;
	}

	public static void setConfig(JsonObject config) {
		ConfigManager.config = config;
	}

	private static JsonObject config;
}