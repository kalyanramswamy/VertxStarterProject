package com.vertx.starter;

import com.newrelic.api.agent.NewRelic;
import com.vertx.starter.exception.VertxException;
import com.vertx.starter.handler.HealthCheckHandler;
import com.vertx.starter.server.ApplicationRouter;
import com.vertx.starter.util.Constants;
import com.vertx.starter.util.ConfigManager;
import com.vertx.starter.util.UtilityFunctions;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.DecodeException;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.UUID;

@Slf4j
public class Application extends AbstractVerticle {
    private static HttpServer httpServer;

    @Override
    public void start(Promise<Void> promise) throws Exception {
        log.info("Starting Application...");

        super.start(promise);
        ConfigManager.setConfig(config());

        Router router = Router.router(vertx);

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

        router.get("/health").handler(HealthCheckHandler::healthCheck);

        // Start Http Server
        HttpServerOptions options = new HttpServerOptions();
        options.setCompressionSupported(true);
        options.setAcceptBacklog(10000).setSendBufferSize(4 * 1024).setReceiveBufferSize(4 * 1024);
        httpServer = vertx.createHttpServer(options);
        httpServer.requestHandler(router).listen(ConfigManager.getConfig().getInteger(Constants.APPLICATION_SERVER_PORT));
    }

    @Override
    public void stop(Promise<Void> stopPromise) throws Exception {
        super.stop(stopPromise);
    }
}
