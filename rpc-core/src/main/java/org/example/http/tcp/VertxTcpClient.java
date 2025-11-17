package org.example.http.tcp;

import cn.hutool.core.util.IdUtil;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;
import org.example.Enum.ProtocolMessageSerializerEnum;
import org.example.Enum.ProtocolMessageTypeEnum;
import org.example.RpcApplication;
import org.example.constant.ProtocolConstant;
import org.example.model.RpcRequest;
import org.example.model.RpcResponse;
import org.example.model.ServiceMetaInfo;
import org.example.protocol.ProtocolMessage;
import org.example.protocol.ProtocolMessageDecoder;
import org.example.protocol.ProtocolMessageEncoder;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
* Vertx TCP 请求客户端
*/
public class VertxTcpClient {

    /**
     * 发送请求
     *
     * @param rpcRequest
     * @param serviceMetaInfo
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public static RpcResponse doRequest(RpcRequest rpcRequest, ServiceMetaInfo serviceMetaInfo) throws InterruptedException, ExecutionException, TimeoutException {
        // 发送 TCP 请求
        Vertx vertx = Vertx.vertx();
        NetClient netClient = vertx.createNetClient();
        CompletableFuture<RpcResponse> responseFuture = new CompletableFuture<>();
        netClient.connect(serviceMetaInfo.getServicePort(), serviceMetaInfo.getServiceHost(),
                result -> {
                    if (!result.succeeded()) {
                        System.err.println("Failed to connect to TCP server");
                        return;
                    }
                    NetSocket socket = result.result();
                    // 发送数据
                    // 构造消息
                    ProtocolMessage<RpcRequest> protocolMessage = new ProtocolMessage<>();
                    ProtocolMessage.Header header = new ProtocolMessage.Header();
                    header.setMagic(ProtocolConstant.PROTOCOL_MAGIC);
                    header.setVersion(ProtocolConstant.PROTOCOL_VERSION);
                    header.setSerializer((byte) ProtocolMessageSerializerEnum.getEnumByValue(RpcApplication.getRpcConfig().getSerializer()).getKey());
                    header.setType((byte) ProtocolMessageTypeEnum.REQUEST.getKey());
                    // 生成全局请求 ID
                    header.setRequestId(IdUtil.getSnowflakeNextId());
                    protocolMessage.setHeader(header);
                    protocolMessage.setBody(rpcRequest);

                    // 编码请求
                    try {
                        Buffer encodeBuffer = ProtocolMessageEncoder.encode(protocolMessage);
                        socket.write(encodeBuffer);
                    } catch (IOException e) {
                        throw new RuntimeException("协议消息编码错误");
                    }

                    // 接收响应
                    TcpBufferHandlerWrapper bufferHandlerWrapper = new TcpBufferHandlerWrapper(
                            buffer -> {
                                try {
                                    //(ProtocolMessage<RpcResponse>)
                                    ProtocolMessage<RpcResponse> rpcResponseProtocolMessage = (ProtocolMessage<RpcResponse>)ProtocolMessageDecoder.decode(buffer);
                                    responseFuture.complete(rpcResponseProtocolMessage.getBody());
                                } catch (IOException e) {
                                    throw new RuntimeException("协议消息解码错误");
                                }
                            }
                    );
                    socket.handler(bufferHandlerWrapper);

                });

        RpcResponse rpcResponse = responseFuture.get();
        // 记得关闭连接（修改后）
        netClient.close().toCompletionStage().toCompletableFuture().get(1, java.util.concurrent.TimeUnit.SECONDS);
        vertx.close().toCompletionStage().toCompletableFuture().get(2, java.util.concurrent.TimeUnit.SECONDS);
        return rpcResponse;
    }
}

//
//public class VertxTcpClient {
//    //这个是用于做测试
//    public void start(){
//        //创建vertx实例
//        Vertx vertx = Vertx.vertx();
//
//        //连接服务器
//        vertx.createNetClient().connect(8888, "localhost",result->{
//            if (result.succeeded()) {
//                System.out.println("Connected to TCP server");
//                //获取结果
//                NetSocket socket = result.result();
//                //发送请求
//                socket.write("Hello, server!");
//                //接受请求
//                socket.handler(handler->{
//                    System.out.println("Received response from server:"+handler.toString());
//                });
//            }else {
//                System.err.println("Failed to connect to TCP server");
//            }
//        });
//    }
//
//    public static void main(String[] args) {
//        //开启
//        new VertxTcpClient().start();
//    }
//}
