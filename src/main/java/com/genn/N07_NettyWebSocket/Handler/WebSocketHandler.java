package com.genn.N07_NettyWebSocket.Handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

/**
 * TextWebSocketFrame表示一个文本帧，服务端和客户端的信息以这个类型进行交互
 */
public class WebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        System.out.println(String.format("websocket协议接受到ip为%s，channel为%s的消息：%s",ctx.channel().remoteAddress(),ctx.channel().hashCode(),msg.text()));
        ctx.channel().writeAndFlush(new TextWebSocketFrame("服务端已经收到你的消息了"));
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        System.out.println(String.format("channel为%s的请求handlerAdded了",ctx.channel().hashCode()));
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        System.out.println(String.format("channel为%s的请求handlerRemoved了",ctx.channel().hashCode()));
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(String.format("channel为%s的请求channelActive了",ctx.channel().hashCode()));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(String.format("channel为%s的请求channelInactive了",ctx.channel().hashCode()));
    }
}
