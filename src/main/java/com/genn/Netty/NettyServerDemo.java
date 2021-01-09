package com.genn.Netty;

import com.genn.Netty.Handler.NettyServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class NettyServerDemo {
    public static void main(String[] args) throws InterruptedException {
        /*
            BossGroup与WorkerGroup
            1.BossGroup只处理accept请求，workerGroup处理read和write请求
            2.两个group在正式运行时，都是死循环
         */
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        /*
            设置启动参数
         */
        try {
            ServerBootstrap config = new ServerBootstrap();
            config.group(bossGroup, workerGroup).    //设置两个线程组
                    channel(NioServerSocketChannel.class). //设置NioSocketChannel来封装ServerSocketChannel
                    option(ChannelOption.SO_BACKLOG, 128). //线程队列等待连接个数
                    childOption(ChannelOption.SO_KEEPALIVE, true). //设置保持活动连接状态
                    childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new NettyServerHandler());  //获取channel的pipeLine
                }
            });  //设置Handler

        /*
            绑定端口并且同步，相当于启动服务器了
         */
            ChannelFuture sync = config.bind(8080).sync();


            //对关闭通道进行监听
            sync.channel().closeFuture().sync();
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
