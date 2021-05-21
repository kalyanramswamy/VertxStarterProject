package com.vertx.starter.util;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.vertx.starter.exception.ObjectMapperException;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
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

    private static ObjectMapper getMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JodaModule());
        return mapper;
    }

    public static <T> T getClassObject(String data, Class<T> tClass) {
        try {
            return getMapper().readValue(data, tClass);
        } catch (Exception e) {
            objectMapperExceptionHandler(e);
        }
        return null;
    }

    public static void objectMapperExceptionHandler(Exception exception) {
        if(exception instanceof InvalidFormatException) {
            InvalidFormatException e = (InvalidFormatException) exception;
            log.error("Mapper InvalidFormatException:" + e);
            throw new ObjectMapperException("Invalid value " + e.getValue() + " for " + e.getTargetType().getSimpleName());
        } else if(exception instanceof JsonMappingException) {
            JsonMappingException e = (JsonMappingException) exception;
            log.error("Mapper JsonMappingException: " + e);
            StringBuilder errorMsg = new StringBuilder();
            for (JsonMappingException.Reference reference: e.getPath()) {
                if(errorMsg.toString().length() == 0) {
                    errorMsg.append("Invalid value for ").append(reference.getFieldName());
                } else {
                    errorMsg.append(", Invalid value for ").append(reference.getFieldName());
                }
            }
            throw new ObjectMapperException("Error: " + errorMsg.toString());
        } else if(exception instanceof JsonParseException) {
            JsonParseException e = (JsonParseException) exception;
            log.error("Mapper JsonParseException :" + e);
            throw new ObjectMapperException("Failed to parse request: " + e.getRequestPayload().getRawPayload());
        } else if(exception instanceof IOException) {
            IOException e = (IOException) exception;
            log.error("Mapper IOException :" + e);
            throw new ObjectMapperException("Error while processing the data, error: " + e.getMessage());
        } else {
            log.error("Mapper Exception :" + exception);
            throw new ObjectMapperException("Error while processing the data, error: " + exception.getMessage());
        }
    }
}
