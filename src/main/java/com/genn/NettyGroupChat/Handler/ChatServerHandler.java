package com.genn.NettyGroupChat.Handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

public class ChatServerHandler extends SimpleChannelInboundHandler<String> {
    /*
    TODO 这是一个全局的channel集合，整个服务器的socketChannel都在这了
        当通道被关闭后，全局channel集合里也会remove掉该通道
     */
    private static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);


    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        //可以直接对channelGroup操作，变相为组内所有socketChannel操作
        channelGroup.writeAndFlush("ip为"+ctx.channel().remoteAddress()+"的用户加入了聊天");
        channelGroup.add(ctx.channel());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        channelGroup.writeAndFlush("ip为"+ctx.channel().remoteAddress()+"的用户离开了聊天");
        System.out.println("此时channelGroup的size是："+channelGroup.size());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("ip为"+ctx.channel().remoteAddress()+"的用户上线了");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("ip为"+ctx.channel().remoteAddress()+"的用户下线了");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        Channel myself = ctx.channel();
        System.out.println(String.format("[ip为：%s的用户]：%s",myself.remoteAddress(),msg));
        channelGroup.forEach(x->{
            if(x!=myself){
                x.writeAndFlush(String.format("[ip为：%s的用户]：%s",myself.remoteAddress(),msg));
            }else{
                x.writeAndFlush(String.format("[我]：%s",msg));
            }
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
