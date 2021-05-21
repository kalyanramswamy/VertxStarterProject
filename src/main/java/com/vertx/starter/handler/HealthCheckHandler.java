package com.vertx.starter.handler;

import com.newrelic.api.agent.Trace;
import io.vertx.ext.web.RoutingContext;
import org.springframework.stereotype.Component;

@Component
public class HealthCheckHandler {
	@Trace(metricName="GET /health_check", dispatcher=true)
    public static void healthCheck(RoutingContext routingContext) {
        routingContext.response().end("I AM OK 200");            
    }
}
