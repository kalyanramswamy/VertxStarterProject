package com.vertxtest.util;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.logging.LogManager;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public final class LogFactory {

	private LogFactory() {
	}

	public static Logger getLogger(Class clazz) {
		Logger logger = null;
		try {
			InputStream inputStream = new FileInputStream("./config/logging.properties");
			LogManager.getLogManager().readConfiguration(inputStream);
			logger = (Logger) LoggerFactory.getLogger(clazz.getName());
			logger.info("Logger initialized");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return logger;
	}
}