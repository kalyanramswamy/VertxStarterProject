package com.vertx.starter.server;

import com.newrelic.api.agent.NewRelic;
import com.vertx.starter.exception.VertxException;
import com.vertx.starter.handler.HealthCheckHandler;
import com.vertx.starter.util.Constants;
import com.vertx.starter.util.UtilityFunctions;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.DecodeException;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Arrays;
import java.util.UUID;

/**
 * @author kalyan.s
 */
@Slf4j
public class ApplicationRouter {
    private HealthCheckHandler healthCheckHandler;

    public void init(Router router) {
        routerHandlers(router);
        dependencyInjector();
        defineRoutes(router);
    }

    private void defineRoutes(Router router) {
        router.get("/health").handler(HealthCheckHandler::healthCheck);
    }

    private void dependencyInjector() {
        ApplicationContext context = new AnnotationConfigApplicationContext(VertxApplicationContext.class);
        healthCheckHandler = context.getBean(HealthCheckHandler.class);
    }

    private void routerHandlers(Router router) {
        router.post().handler(BodyHandler.create());
        router.put().handler(BodyHandler.create());
        router.patch().handler(BodyHandler.create());
        router.get().handler(BodyHandler.create());
        router.route().handler( routingContext -> {
            String requestId = UUID.randomUUID().toString();
            HttpServerRequest request = routingContext.request();
            if(!UtilityFunctions.isNullOrEmpty(request.getHeader("requestId"))){
                requestId = request.getHeader("requestId");
            }
            routingContext.data().put(Constants.REQUEST_ID, requestId);
            log.info(UtilityFunctions.stringRequest(routingContext));
            routingContext.next();
        });

        router.route().failureHandler(handler -> {
            Throwable exception = handler.failure();
            String path = handler.request().path();
            if (exception instanceof VertxException) {
                log.error("Got Vertx Exception " + exception.getMessage() + " For "  + "  "  + path + " Stack Trace  : " + Arrays.toString(exception.getStackTrace()) );
                exception.printStackTrace();
                UtilityFunctions.sendFailure(handler.request().response(), exception.getMessage(), HttpResponseStatus.BAD_REQUEST.code());
            } else if (exception instanceof DecodeException) {
                log.error("Got decode Exception " + exception.getMessage()  + " For " + "  " + path +  " Stack Trace  : " + Arrays.toString(exception.getStackTrace()) );
                exception.printStackTrace();
                UtilityFunctions.sendFailure(handler.request().response(), "Invalid input", HttpResponseStatus.BAD_REQUEST.code());
            } else if (exception instanceof NumberFormatException) {
                log.error("Got NumberFormatException " + exception.getMessage()  + " For " + "  " +  path + " Stack Trace  : " + Arrays.toString(exception.getStackTrace()) );
                exception.printStackTrace();
                UtilityFunctions.sendFailure(handler.request().response(), "Parameter should be numeric", HttpResponseStatus.BAD_REQUEST.code());
            } else if (exception instanceof Exception) {
                log.error("Got Exception " + exception.getMessage()  + " For " + "  " + path +  " Stack Trace  : " + Arrays.toString(exception.getStackTrace()) );
                exception.printStackTrace();
                UtilityFunctions.sendFailure(handler.request().response(), "Something went wrong, Please try again later", HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
            }
            NewRelic.noticeError(exception);
        });
    }
}
