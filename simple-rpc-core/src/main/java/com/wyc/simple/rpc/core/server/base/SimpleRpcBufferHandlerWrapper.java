package com.wyc.simple.rpc.core.server.base;

import com.wyc.simple.rpc.core.server.protocol.constant.SimpleRpcProtocolConstant;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.parsetools.RecordParser;

/**
 * 装饰者模式对原有 RecordParser 增强
 */
public class SimpleRpcBufferHandlerWrapper implements Handler<Buffer> {

    private final RecordParser recordParser;

    public SimpleRpcBufferHandlerWrapper(Handler<Buffer> handler) {
        recordParser = initRecordParser(handler);
    }

    @Override
    public void handle(Buffer buffer) {
        recordParser.handle(buffer);
    }

    /**
     * 使用装饰者模式对原有的实例进行增强
     * @param handler
     * @return
     */
    private RecordParser initRecordParser(Handler<Buffer> handler) {
        // 初始化解析器
        RecordParser parser = RecordParser.newFixed(SimpleRpcProtocolConstant.MESSAGE_HEADER_LENGTH);

        // 设置处理器
        parser.setOutput(new Handler<Buffer>() {
            int size = -1;
            Buffer resultBuffer = Buffer.buffer();

            @Override
            public void handle(Buffer buffer) {
                if(size == -1){
                    // 读取消息体的长度
                    size = buffer.getInt(13);
                    parser.fixedSizeMode(size);
                    // 读取消息头
                    resultBuffer.appendBuffer(buffer);
                } else {
                    // 读取消息体
                    resultBuffer.appendBuffer(buffer);
                    // 已拼接为完整的Buffer，执行处理
                    handler.handle(resultBuffer);
                    // 重置一轮
                    parser.fixedSizeMode(SimpleRpcProtocolConstant.MESSAGE_HEADER_LENGTH);
                    size = -1;
                    resultBuffer = Buffer.buffer();
                }
            }
        });

        return parser;
    }
}
