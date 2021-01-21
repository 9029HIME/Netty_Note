package com.genn.N10_TCP.CommomHandler;

import com.genn.N10_TCP.Entity.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

public class MessageDecoder extends ReplayingDecoder<Message> {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        //先读取前4位字节，获取消息长度
        int length = in.readInt();
        byte[] content = new byte[length];
        //读取该消息长度的字节数
        in.readBytes(content);
        //封装成对象，给下一个handler
        Message message = new Message();
        message.setLength(length);
        message.setContent(content);
        out.add(message);
    }
}
