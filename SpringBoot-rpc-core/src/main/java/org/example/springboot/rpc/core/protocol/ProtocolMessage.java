package org.example.springboot.rpc.core.protocol;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProtocolMessage<T> {
    /**
     * 请求头
     */
    private Header header;
    /**
     * 请求体(请求或响应体)
     */
    private T body;

    /**
     * header内部类定义
     */
    @Data
    public static class Header{
        /**
         * 魔数，用于保证安全
         */
        private byte magic;
        /**
         * 版本号
         */
        private byte version;
        /**
         * 序列化器
         */
        private byte serializer;
        /**
         * 消息类型(请求/响应)
         */
        private byte type;
        /**
         * 状态
         */
        private byte status;
        /**
         * 请求id
         */
        private long requestId;
        /**
         * 消息体长度
         */
        private int bodyLength;
    }

}
