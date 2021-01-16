package com.genn.NettyGroupChat;

import com.genn.Netty.Handler.NettyClientHandler;
import com.genn.NettyGroupChat.Handler.ChatClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.util.Scanner;

public class NettyChatClient {
    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup clientEvent = new NioEventLoopGroup();
        try {
            Bootstrap config = new Bootstrap();
            config.group(clientEvent).
                    channel(NioSocketChannel.class).
                    handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast("encode",new StringEncoder())
                            .addLast("decode",new StringDecoder())
                            .addLast(new ChatClientHandler());
                        }
                    });
            System.out.println("客户端配置完成");

            //连接服务端
            ChannelFuture connect = config.connect("localhost", 8080).sync();
            Channel channel = connect.channel();
            Scanner sc= new Scanner(System.in);
            while(sc.hasNextLine()){
                String content = sc.nextLine();
                channel.writeAndFlush(content);
            }
        }finally {
            clientEvent.shutdownGracefully();
        }
    }
}
