package com.vertx.starter.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author kalyan.s
 */

@Configuration
@ComponentScan({"com.vertx.starter"})
@Slf4j
public class VertxApplicationContext {
}
