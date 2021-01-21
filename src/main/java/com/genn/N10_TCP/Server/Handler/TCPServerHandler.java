package com.genn.N10_TCP.Server.Handler;

import com.genn.N10_TCP.Entity.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

public class TCPServerHandler extends SimpleChannelInboundHandler<Message> {
//    @Override
//    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        Message message = (Message) msg;
//        byte[] content = message.getContent();
//        int length = message.getLength();
//        System.out.println(String.format("本次收到的信息长度是%s，内容是%s",length,
//                new String(content,CharsetUtil.UTF_8)));
//    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        Message message = (Message) msg;
        byte[] content = message.getContent();
        int length = message.getLength();
        System.out.println(String.format("本次收到的信息长度是%s，内容是%s",length,
                new String(content,CharsetUtil.UTF_8)));
    }
}
