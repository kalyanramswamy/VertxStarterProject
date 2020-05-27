package com.vertxtest.verticles;

import com.newrelic.api.agent.NewRelic;
import com.vertxtest.exception.VertxException;
import com.vertxtest.handler.HealthCheckHandler;
import com.vertxtest.util.ConfigManager;
import com.vertxtest.util.Constants;
import com.vertxtest.util.LogFactory;
import com.vertxtest.util.UtilityFunctions;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.DecodeException;
import io.vertx.core.logging.Logger;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.Arrays;
import java.util.UUID;

public class MainVerticle extends AbstractVerticle {
    private static HttpServer httpServer;
    public static final Logger logger = LogFactory.getLogger(MainVerticle.class);

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        super.start(startFuture);
        ConfigManager.setConfig(config());

        startUtil();
    }

    private void startUtil() {
        Router router = Router.router(vertx);
        prepareRoutes(router);

        // This verticle will run the http server to listen to the requests.
        HttpServerOptions options = new HttpServerOptions();
        options.setCompressionSupported(true);
        options.setAcceptBacklog(10000).setSendBufferSize(4 * 1024).setReceiveBufferSize(4 * 1024);
        httpServer = vertx.createHttpServer(options);
        httpServer.requestHandler(router::accept).listen(config().getInteger("server_port"));
    }

    private void prepareRoutes(Router router) {
        router.post().handler(BodyHandler.create());
        router.put().handler(BodyHandler.create());
        router.patch().handler(BodyHandler.create());
        router.route().handler( routingContext -> {
            String requestId = UUID.randomUUID().toString();
            HttpServerRequest request = routingContext.request();
            if(!UtilityFunctions.isNullOrEmpty(request.getHeader("requestId"))){
                requestId = request.getHeader("requestId");
            }
            routingContext.data().put(Constants.REQUEST_ID, requestId);
            logger.info(UtilityFunctions.stringRequest(routingContext));
            routingContext.next();
        });

        router.route().failureHandler(handler -> {
            Throwable exception = handler.failure();
            String path = handler.request().path();
            if (exception instanceof VertxException) {
                logger.error("Got CMS Exception " + exception.getMessage() + " For "  + "  "  + path + " Stack Trace  : " + Arrays.toString(exception.getStackTrace()) );
                exception.printStackTrace();
                UtilityFunctions.sendFailure(handler.request().response(), exception.getMessage(), HttpResponseStatus.BAD_REQUEST.code());
            } else if (exception instanceof DecodeException) {
                logger.error("Got decode Exception " + exception.getMessage()  + " For " + "  " + path +  " Stack Trace  : " + Arrays.toString(exception.getStackTrace()) );
                exception.printStackTrace();
                UtilityFunctions.sendFailure(handler.request().response(), "Invalid input", HttpResponseStatus.BAD_REQUEST.code());
            } else if (exception instanceof NumberFormatException) {
                logger.error("Got NumberFormatException " + exception.getMessage()  + " For " + "  " +  path + " Stack Trace  : " + Arrays.toString(exception.getStackTrace()) );
                exception.printStackTrace();
                UtilityFunctions.sendFailure(handler.request().response(), "Parameter should be numeric", HttpResponseStatus.BAD_REQUEST.code());
            } else if (exception instanceof Exception) {
                logger.error("Got Exception " + exception.getMessage()  + " For " + "  " + path +  " Stack Trace  : " + Arrays.toString(exception.getStackTrace()) );
                exception.printStackTrace();
                UtilityFunctions.sendFailure(handler.request().response(), "Something went wrong, Please try again later", HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
            }
            NewRelic.noticeError(exception);
        });

        router.get("/health_check").handler(HealthCheckHandler::healthCheck);
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        super.stop(stopFuture);
    }
}
