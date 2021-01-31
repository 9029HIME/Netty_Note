package com.genn.N12_RPC.Server;

import com.genn.N12_RPC.Server.Handler.RPCHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class Server {
    public static void start(String host,int port){
        EventLoopGroup parent = new NioEventLoopGroup();
        EventLoopGroup children = new NioEventLoopGroup();

        try{

            ServerBootstrap config = new ServerBootstrap();
            config.group(parent,children)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new StringEncoder())
                            .addLast(new StringDecoder())
                            .addLast(new RPCHandler());
                        }
                    });
            ChannelFuture sync = config.bind(host, port).sync();
            ChannelFuture sync1 = sync.channel().closeFuture().sync();

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            parent.shutdownGracefully();
            children.shutdownGracefully();
        }
    }
}
