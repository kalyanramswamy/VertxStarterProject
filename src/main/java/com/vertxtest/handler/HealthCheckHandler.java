package com.vertxtest.handler;

import com.newrelic.api.agent.Trace;
import io.vertx.ext.web.RoutingContext;

/**
 * Created by akshay.kumar1 on 10/10/16.
 */
public final class HealthCheckHandler {

    private HealthCheckHandler() {}

    
	@Trace(metricName="GET /health_check", dispatcher=true)
    public static void healthCheck(RoutingContext routingContext) {
        routingContext.response().end("I AM OK 200");            
    }
}
