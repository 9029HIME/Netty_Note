package com.genn.N05_Netty.Handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    /**
     * 重写这个方法，用来读取数据,TODO 如果数据过长，会调用多次
     * @param ctx 上下文对象
     * @param msg 客户端实际发送的信息，需要转成ByteBuf（Netty内置对象）来处理
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel channel = ctx.channel();
        System.out.println(String.format("当前线程：%s,当前EventLoop：%s", Thread.currentThread().getName(),channel.eventLoop()));
        ctx.channel().eventLoop().execute(()->{
            try {
                Thread.sleep(3000);
                System.out.println(String.format("任务1睡醒了，他的线程是%s", Thread.currentThread().getName()));
            }catch (Exception e){
                e.printStackTrace();
            }
        });
        ctx.channel().eventLoop().execute(()->{
            try {
                Thread.sleep(3000);
                System.out.println(String.format("任务2睡醒了，他的线程是%s", Thread.currentThread().getName()));
            }catch (Exception e){
                e.printStackTrace();
            }
        });
//        System.out.println(String.format("channel是%s,channel内的pipeline是%s,ctx中的pipeline是%s",channel,channel.pipeline(),ctx.pipeline()));
        ByteBuf content = (ByteBuf) msg;
        System.out.println("来自客户端的信息："+content.toString(CharsetUtil.UTF_8));
        System.out.println("客户端地址："+ctx.channel().remoteAddress());

    }

    /**
     * 重写这个方法，在最后一次channelRead后调用
     * @param ctx 上下文对象
     * @throws Exception
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        /*
        我这里的设计是，读取完消息后，给客户端回一段信息
        只write是不会发送的，还得flush
         */
        ctx.writeAndFlush(Unpooled.copiedBuffer("我收到你的消息辣", CharsetUtil.UTF_8));
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
