package org.example.springboot.rpc.core.http;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import org.example.springboot.rpc.core.handler.HttpServerHandler;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
public class VertxHttpServer implements HttpService {
    @Override
    public void doStart(int port) {
        //创建Vertx实例
        Vertx vertx = Vertx.vertx();
        //创建Http服务器
        HttpServer httpServer = vertx.createHttpServer();
        //监听端口同时处理请求
        httpServer.requestHandler(new HttpServerHandler());
        //启动http同时监听指定端口
        httpServer.listen(port, result->{
            if (result.succeeded()) {
                System.out.println("Server is now listening on port " + port);
            } else {
                System.err.println("Failed to start server: ");
            }
        });

    }
}
