package com.genn.N05_Netty.Handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

public class NettyClientHandler extends ChannelInboundHandlerAdapter {

    /**
     * 当连接成功，通道就绪时会触发该方法
     * @param ctx 上下文
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("客户端连接成功！");
        ctx.writeAndFlush(Unpooled.copiedBuffer("你好呀服务器~", CharsetUtil.UTF_8));
    }

    /**
     * 当服务器给我发消息时，读取消息，TODO 同样，如果太长，会调用该方法多次
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf content = (ByteBuf) msg;
        System.out.println("服务端发出的消息："+content.toString(CharsetUtil.UTF_8));
        System.out.println("服务端地址："+ctx.channel().remoteAddress());
    }

    /**
     * 重写这个方法，当异常触发时关闭通道
     * @param ctx 上下文对象
     * @param cause 触发的异常
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
