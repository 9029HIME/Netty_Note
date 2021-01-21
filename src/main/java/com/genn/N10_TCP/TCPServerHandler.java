package com.genn.N10_TCP;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

public class TCPServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;
        byte[] array = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(array);
        String content = new String(array, CharsetUtil.UTF_8);
        System.out.println("本次收到的内容是"+content);
    }
}
