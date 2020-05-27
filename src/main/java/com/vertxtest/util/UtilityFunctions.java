package com.vertxtest.util;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class UtilityFunctions {
    public static boolean isNullOrEmpty(String input) {
        return null == input || input.isEmpty();
    }

    public static String stringRequest(RoutingContext routingContext) {
        return "Started  " + routingContext.request().method()
                + " requestId : "+ routingContext.data().get("requestId")
                + "  " + routingContext.request().path() + " request body is: " + routingContext.getBodyAsString()
                + " request query are: " + routingContext.request().query() + " request query headers are: " +
                (routingContext.request().headers() == null ? null : routingContext.request().headers().entries());
    }

    public static void sendFailure(HttpServerResponse httpServerResponse, String reason, int statusCode) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("status", "FAILURE");
        jsonObject.put("reason", reason);
        httpServerResponse.putHeader("content-type", "application/json; charset=utf-8");
        httpServerResponse.putHeader("charset", "UTF-8");
        httpServerResponse.setStatusCode(statusCode);
        httpServerResponse.end(jsonObject.toString());

    }
}
