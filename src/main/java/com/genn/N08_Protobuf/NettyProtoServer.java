package com.genn.N08_Protobuf;

import com.genn.N05_Netty.Handler.NettyServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;

import java.util.Random;
import java.util.Scanner;

public class NettyProtoServer {
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
                    option(ChannelOption.SO_BACKLOG, 128). //TCP/IP协议中的backlog参数，即可连接队列大小
                    childOption(ChannelOption.SO_KEEPALIVE, true). //设置保持活动连接状态
                    childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline()
                            //TODO 添加反序列化处理器，需要指定反序列化的类型
                            .addLast("protoDecoder",new ProtobufDecoder(ProtoData.Payload.getDefaultInstance()))
                            //TODO 添加序列化处理器
                            .addLast("protoEncoder",new ProtobufEncoder())
                            //TODO 注意！！！！处理器的顺序，接受数据的处理器要放在编解码的后面
                            .addLast(new ProtobufPayloadHandler());
                }
            });  //设置Handler

        /*
            绑定端口并且同步，相当于启动服务器了
         */
            ChannelFuture connect = config.bind(8080).sync();
            //这里只是举个例子 如果bind不同步的话，主线程跑完就关掉了
            connect.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if(future.isSuccess()){
                        System.out.println("端口8080监听成功");
                    }else{
                        System.out.println("监听失败");
                    }
                }
            });
            //对关闭通道进行监听
            connect.channel().closeFuture().sync();
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
