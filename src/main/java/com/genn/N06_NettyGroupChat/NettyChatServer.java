package com.genn.N06_NettyGroupChat;

import com.genn.N06_NettyGroupChat.Handler.InitHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class NettyChatServer {
    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();

        try {
            ServerBootstrap config = new ServerBootstrap();
            config.group(boss,worker).channel(NioServerSocketChannel.class)
            .childHandler(new InitHandler());
            ChannelFuture close = config.bind(8080).sync();
            close.channel().closeFuture().sync();
        }finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}
