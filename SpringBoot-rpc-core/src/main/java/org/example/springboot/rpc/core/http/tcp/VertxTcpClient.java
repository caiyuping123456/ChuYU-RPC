package org.example.springboot.rpc.core.http.tcp;

import cn.hutool.core.util.IdUtil;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;
import org.example.springboot.rpc.core.Enum.ProtocolMessageTypeEnum;
import org.example.springboot.rpc.core.config.RpcApplication;
import org.example.springboot.rpc.core.constant.ProtocolConstant;
import org.example.springboot.rpc.core.model.RpcRequest;
import org.example.springboot.rpc.core.model.RpcResponse;
import org.example.springboot.rpc.core.model.ServiceMetaInfo;
import org.example.springboot.rpc.core.protocol.ProtocolMessage;
import org.example.springboot.rpc.core.protocol.ProtocolMessageDecoder;
import org.example.springboot.rpc.core.protocol.ProtocolMessageEncoder;
import org.example.springboot.rpc.core.serializer.SerializerManager;

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
                    //判断是否请求成功
                    if (!result.succeeded()) {
                        System.err.println("Failed to connect to TCP server");
                        return;
                    }
                    //请求成功
                    NetSocket socket = result.result();
                    // 发送数据
                    // 构造消息
                    ProtocolMessage<RpcRequest> protocolMessage = new ProtocolMessage<>();
                    ProtocolMessage.Header header = new ProtocolMessage.Header();
                    header.setMagic(ProtocolConstant.PROTOCOL_MAGIC);
                    header.setVersion(ProtocolConstant.PROTOCOL_VERSION);
                    header.setSerializer((byte) SerializerManager.getProtocolMessageSerializerEnumByKey(RpcApplication.getRpcConfig().getSerializer()).intValue());
                    header.setType((byte) ProtocolMessageTypeEnum.REQUEST.getKey());
                    // 生成全局请求 ID
                    header.setRequestId(IdUtil.getSnowflakeNextId());
                    protocolMessage.setHeader(header);
                    protocolMessage.setBody(rpcRequest);

                    // 编码请求
                    try {
                        //对请求进行编码（17字节）
                        Buffer encodeBuffer = ProtocolMessageEncoder.encode(protocolMessage);
                        socket.write(encodeBuffer);
                    } catch (IOException e) {
                        throw new RuntimeException("协议消息编码错误");
                    }

                    // 接受到服务端的响应
                    // 接收响应
                    TcpBufferHandlerWrapper bufferHandlerWrapper = new TcpBufferHandlerWrapper(
                            buffer -> {
                                try {
                                    //(ProtocolMessage<RpcResponse>)
                                    ProtocolMessage<RpcResponse> rpcResponseProtocolMessage = (ProtocolMessage<RpcResponse>) ProtocolMessageDecoder.decode(buffer);
                                    responseFuture.complete(rpcResponseProtocolMessage.getBody());
                                } catch (IOException e) {
                                    throw new RuntimeException("协议消息解码错误");
                                }
                            }
                    );
                    // 将包装后的处理器注册到 NetSocket
                    socket.handler(bufferHandlerWrapper);
                });

        //这里是异步
        RpcResponse rpcResponse = responseFuture.get();
        // 记得关闭连接（修改后）
        netClient.close().toCompletionStage().toCompletableFuture().get(1, java.util.concurrent.TimeUnit.SECONDS);
        vertx.close().toCompletionStage().toCompletableFuture().get(2, java.util.concurrent.TimeUnit.SECONDS);
        return rpcResponse;
    }
}

