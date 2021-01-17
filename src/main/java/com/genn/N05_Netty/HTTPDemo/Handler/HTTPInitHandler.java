package com.genn.N05_Netty.HTTPDemo.Handler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

//TODO 话说这个SocketChannel是Netty封装的
public class HTTPInitHandler extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        //添加handler
        ch.pipeline().addLast("encodec",new HttpServerCodec())
                .addLast("responseHandler1",new HTTPServeriHandler())
                .addLast(new IdleStateHandler(3,5,7, TimeUnit.SECONDS))
                .addLast(new HTTPHeartBeatHandler());;
    }
}
