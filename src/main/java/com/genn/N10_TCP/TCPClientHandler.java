package com.genn.N10_TCP;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

public class TCPClientHandler extends ChannelInboundHandlerAdapter {

    /**
     * 连接成功收到服务器的响应后，给他发10个数据
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        for (int i = 0; i < 100; i++) {
            ByteBuf byteBuf = Unpooled.copiedBuffer("nihaoabcdefghijklmn:" + 1, CharsetUtil.UTF_8);
            ctx.writeAndFlush(byteBuf);
        }
    }
}
