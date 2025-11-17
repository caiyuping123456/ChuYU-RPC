package org.example.http;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import org.example.handler.HttpServerHandler;

public class VertxHttpServer implements HttpService{
    @Override
    public void doStart(int port) {
        //创建Vertx实例
        Vertx vertx = Vertx.vertx();
        //创建Http服务器
        HttpServer httpServer = vertx.createHttpServer();
        //监听端口同时处理请求
        httpServer.requestHandler(new HttpServerHandler());
//        httpServer.requestHandler(request -> {
//            //处理request请求
//            System.out.println("打印：方法："+request.method()+"端口："+port);
//            //发送请求
//            request.response().putHeader("content-type", "text/plain").end("你好");
//        });
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
