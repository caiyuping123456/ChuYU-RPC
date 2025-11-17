package org.example.http.tcp;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetServer;
import lombok.extern.slf4j.Slf4j;
import org.example.handler.TcpServerHandler;
import org.example.http.HttpService;

@Slf4j
public class VertxTcpServer implements HttpService {

    /**
     * 请求处理器，用于处理请求体中的请求
     * @param requestData
     * @return
     */
    private byte[] handlerRequest(byte[] requestData){
        return "Hello Client".getBytes();
    }

    /**
     * 处理请求，同时对请求做出响应
     * @param port
     */
    @Override
    public void doStart(int port) {
        //创建Vert.x实例
        Vertx vertx = Vertx.vertx();
        //创建TCP服务器
        NetServer server = vertx.createNetServer();

        //请求处理
        server.connectHandler(new TcpServerHandler());
//        server.connectHandler(socket->{
//            //处理连接
//            socket.handler(buffer -> {
//                //处理接受到的请求体
//                byte[] bytes = buffer.getBytes();
//                //处理请求体请求
//                //byte[] responseData = handlerRequest(bytes);
//                //发送响应
//                //的字节数组缓冲区
//                socket.write(Buffer.buffer(bytes));
//            });
//        });

        // 启动 TCP 服务器并监听指定端口
        server.listen(port, result -> {
            if (result.succeeded()) {
                //成功，打印（这里使用log）
                log.info("TCP server started on port {}" ,port);
            } else {
                log.info("Failed to start TCP server: {}" , result.cause());
            }
        });
    }

//    public static void main(String[] args) {
//        //启动端口8888
//        new VertxTcpServer().doStart(8888);
//    }
}
