package com.genn.N10_TCP.Client.Handler;

import com.genn.N10_TCP.Entity.Message;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

import java.nio.charset.StandardCharsets;

public class TCPClientHandler extends SimpleChannelInboundHandler<Message> {

    /**
     * 连接成功收到服务器的响应后，给他发10个数据
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        for (int i = 0; i < 100; i++) {
            String msg = "nihaoabcdefghijklmn:1";
            byte[] content = msg.getBytes(StandardCharsets.UTF_8);
            int length = content.length;
            Message message = new Message();
            message.setLength(length);
            message.setContent(content);
            ctx.writeAndFlush(message);
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {

    }
}
