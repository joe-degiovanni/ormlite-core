package com.j256.ormlite.logger;

import com.j256.ormlite.logger.Log4j2LogBackend.Log4j2LogBackendFactory;

public class Log4j2LogBackendTest extends BaseLogBackendTest {

	public Log4j2LogBackendTest() {
		super(new Log4j2LogBackendFactory());
	}
}
