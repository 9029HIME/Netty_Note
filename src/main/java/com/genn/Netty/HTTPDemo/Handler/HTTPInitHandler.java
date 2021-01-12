package com.genn.Netty.HTTPDemo.Handler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;

//TODO 话说这个SocketChannel是Netty封装的
public class HTTPInitHandler extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        //添加handler
        ch.pipeline().addLast("encodec",new HttpServerCodec()).addLast("responseHandler1",new HTTPServeriHandler());
        System.out.println();
    }
}
