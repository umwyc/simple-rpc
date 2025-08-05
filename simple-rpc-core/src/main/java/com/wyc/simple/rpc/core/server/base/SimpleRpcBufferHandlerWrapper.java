package com.wyc.simple.rpc.core.server.base;

import com.wyc.simple.rpc.core.server.protocol.constant.SimpleRpcProtocolConstant;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.parsetools.RecordParser;

/**
 * 装饰者模式对原有 RecordParser 增强
 */
public class SimpleRpcBufferHandlerWrapper implements Handler<Buffer> {

    private RecordParser parser;

    public SimpleRpcBufferHandlerWrapper(Handler<Buffer> handler) {
        parser = getEnhancedRecordParser(handler);
    }

    private RecordParser getEnhancedRecordParser(Handler<Buffer> handler) {

        parser = RecordParser.newFixed(SimpleRpcProtocolConstant.MESSAGE_HEADER_LENGTH);

        parser.setOutput(new Handler<Buffer>() {
            int size = -1;
            Buffer result = Buffer.buffer();

            @Override
            public void handle(Buffer buffer) {
                if (size == -1) {
                    // 获取请求体长度
                    size = buffer.getInt(SimpleRpcProtocolConstant.BODY_LENGTH_OFFSET);
                    parser.fixedSizeMode(size);
                    // 读取请求头
                    result.appendBuffer(buffer);
                } else {
                    // 读取请求体
                    result.appendBuffer(buffer);
                    // 截断消息
                    handler.handle(result);
                    // 重制一轮
                    size = -1;
                    result = Buffer.buffer();
                }
            }
        });

        return parser;
    }

    @Override
    public void handle(Buffer buffer) {
        parser.handle(buffer);
    }
}
