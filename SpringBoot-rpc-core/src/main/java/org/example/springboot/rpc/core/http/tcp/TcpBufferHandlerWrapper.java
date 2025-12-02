package org.example.springboot.rpc.core.http.tcp;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.parsetools.RecordParser;
import org.example.springboot.rpc.core.constant.ProtocolConstant;

/**
 * 装饰者模式（使用 recordParser 对原有的 buffer 处理能力进行增强）
 */
public class TcpBufferHandlerWrapper implements Handler<Buffer> {
    private final RecordParser recordParser;
    public TcpBufferHandlerWrapper(Handler<Buffer> bufferHandler) {
        // 初始化RecordParser
        recordParser = initRecordParser(bufferHandler);
    }
    @Override
    public void handle(Buffer buffer) {
        recordParser.handle(buffer);
    }
    private RecordParser initRecordParser(Handler<Buffer> bufferHandler) {
        // 构造 parser
        // 指定特定长度的消息，这里时说读取17byte的消息
        RecordParser parser = RecordParser.newFixed(ProtocolConstant.MESSAGE_HEADER_LENGTH);
        parser.setOutput(new Handler<Buffer>() {
            // 初始化
            // 状态变量：记录当前消息体的长度，-1 表示尚未读取到头部
            int size = -1;
            // 一次完整的读取（头 + 体）
            Buffer resultBuffer = Buffer.buffer();
            @Override
            public void handle(Buffer buffer) {
                if (-1 == size) {
                    // 读取消息体长度（4字节，从13到16）
                    size = buffer.getInt(13);
                    // 动态切换 RecordParser 的模式：接下来要读取 size 字节的数据（即消息体）
                    parser.fixedSizeMode(size);
                    // 写入头信息到结果
                    resultBuffer.appendBuffer(buffer);
                } else {
                    // 写入体信息到结果
                    resultBuffer.appendBuffer(buffer);
                    // 已拼接为完整 Buffer，执行处理
                    bufferHandler.handle(resultBuffer);
                    // 重置一轮
                    parser.fixedSizeMode(ProtocolConstant.MESSAGE_HEADER_LENGTH);
                    size = -1;
                    resultBuffer = Buffer.buffer();
                }
            }
        });
        return parser;
    }
}
