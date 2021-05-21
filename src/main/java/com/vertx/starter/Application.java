package com.vertx.starter;

import com.vertx.starter.server.ApplicationRouter;
import com.vertx.starter.util.Constants;
import com.vertx.starter.util.ConfigManager;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Application extends AbstractVerticle {
    private static HttpServer httpServer;

    @Override
    public void start(Promise<Void> promise) throws Exception {
        log.info("Starting Application...");

        super.start(promise);
        ConfigManager.setConfig(config());

        Router router = Router.router(vertx);

        ApplicationRouter applicationRouter = new ApplicationRouter();
        applicationRouter.init(router);

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
