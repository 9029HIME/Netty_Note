package com.genn.N10_TCP.CommomHandler;

import com.genn.N10_TCP.Entity.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class MessageEncoder extends MessageToByteEncoder<Message> {
    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
        /*
         * 先传输长度，再传输内容
         * 根据TCP协议的规定，消息总是按需到达的
         * 假设传message1{length:3,content:abc},message1{length:5,content:zxcvb}
         * 到接收方的socket缓冲区内，其信息转成字节也必定是:3abc5zxcvb
         */
        out.writeInt(msg.getLength());
        out.writeBytes(msg.getContent());
        System.out.println(String.format("本次消息已传输，消息长度为%s",msg.getLength()));
    }
}
