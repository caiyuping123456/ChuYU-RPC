package org.example;

import cn.hutool.core.util.IdUtil;
import io.vertx.core.buffer.Buffer;
import org.example.Enum.ProtocolMessageSerializerEnum;
import org.example.Enum.ProtocolMessageStatusEnum;
import org.example.Enum.ProtocolMessageTypeEnum;
import org.example.constant.ProtocolConstant;
import org.example.constant.RpcConstant;
import org.example.model.RpcRequest;
import org.example.protocol.ProtocolMessage;
import org.example.protocol.ProtocolMessageDecoder;
import org.example.protocol.ProtocolMessageEncoder;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 * 消息器测试
 */
public class ProtocolMessageTest {
    @Test
    public void testEncodeAndDecode() throws IOException {
        // 构造消息
        ProtocolMessage<RpcRequest> protocolMessage = new ProtocolMessage<>();
        //定义消息头
        ProtocolMessage.Header header = new ProtocolMessage.Header();
        //设置魔数//保证安全
        header.setMagic(ProtocolConstant.PROTOCOL_MAGIC);
        //版本号
        header.setVersion(ProtocolConstant.PROTOCOL_VERSION);
        //序列化器
        header.setSerializer((byte) ProtocolMessageSerializerEnum.JDK.getKey());
        //类型
        header.setType((byte) ProtocolMessageTypeEnum.REQUEST.getKey());
        //状态
        header.setStatus((byte) ProtocolMessageStatusEnum.OK.getValue());
        //请求Id
        header.setRequestId(IdUtil.getSnowflakeNextId());
        //请求体长度
        header.setBodyLength(0);
        //封装rpc请求体
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setServiceName("myService");
        rpcRequest.setMethodName("myMethod");
        rpcRequest.setServiceVersion(RpcConstant.DEFAULT_SERVICE_VERSION);
        rpcRequest.setParameterTypes(new Class[]{String.class});
        rpcRequest.setArgs(new Object[]{"aaa", "bbb"});

        protocolMessage.setHeader(header);
        protocolMessage.setBody(rpcRequest);
        System.out.println("protocolMessage"+protocolMessage.toString());
        //对消息进行编码（压缩空间）
        Buffer encodeBuffer = ProtocolMessageEncoder.encode(protocolMessage);
        System.out.println("encodeBuffer"+encodeBuffer.toString());
        ProtocolMessage<?> message = ProtocolMessageDecoder.decode(encodeBuffer);
        System.out.println("message"+message.toString());
        //断言验证
        Assert.assertNotNull(message);
    }
}
