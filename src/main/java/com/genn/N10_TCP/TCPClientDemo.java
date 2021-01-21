package com.genn.N10_TCP;

import com.genn.N05_Netty.Handler.NettyClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class TCPClientDemo {
    public static void main(String[] args) throws InterruptedException {
        //客户端只需要一个EventLoopGroup
        EventLoopGroup clientEvent = new NioEventLoopGroup();

        try {
            Bootstrap config = new Bootstrap();
            config.group(clientEvent).
                    channel(NioSocketChannel.class).
                    handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new TCPClientHandler());
                        }
                    });
            System.out.println("客户端配置完成");

            //连接服务端
            ChannelFuture localhost = config.connect("localhost", 8080).sync();
            localhost.channel().closeFuture().sync();
        }finally {
            clientEvent.shutdownGracefully();
        }
    }
}
