package com.genn.N09_Pipeline;

import com.genn.N05_Netty.Handler.NettyClientHandler;
import com.genn.N09_Pipeline.Handler.ClientEncodeHandler;
import com.genn.N09_Pipeline.Handler.ClientSendHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class Client {
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
                            //TODO 对于客户端来说，出站的时候，顺序是从尾到头，所以Encode要放前面
                            ch.pipeline().addLast(new ClientEncodeHandler())
                            .addLast(new ClientSendHandler());
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
