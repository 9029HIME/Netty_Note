package com.genn.N08_Protobuf;

import com.genn.N05_Netty.Handler.NettyClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;

import java.util.Random;

public class NettyProtoClient {
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
                            ch.pipeline()
                                    //TODO 添加反序列化处理器，需要指定反序列化的类型
                                    .addLast("protoDecoder",new ProtobufDecoder(ProtoData.Payload.getDefaultInstance()))
                                    //TODO 添加序列化处理器
                                    .addLast("protoEncoder",new ProtobufEncoder());
                        }
                    });
            System.out.println("客户端配置完成");

            //连接服务端
            ChannelFuture connect = config.connect("localhost", 8080).sync();
            System.out.println("连接完成");
            Channel channel = connect.channel();
            int random = new Random().nextInt(3);
            ProtoData.Payload payload= null;
            if(0==random){
                payload = ProtoData.Payload.newBuilder()
                        .setType(ProtoData.Payload.DataType.TYPE_STUDENT)
                        .setStudent(
                                ProtoData.Student.newBuilder()
                                        .setId(10001)
                                        .setName("黄俊严")
                                        .build())
                        .build();
                System.out.println("准备发送的数据："+payload.getType()+"-"+payload.getStudent().getName());
            }else{
                payload = ProtoData.Payload.newBuilder()
                        .setType(ProtoData.Payload.DataType.TYPE_TEACHER)
                        .setTeacher(
                                ProtoData.Teacher.newBuilder()
                                        .setAge(27)
                                        .setName("老师")
                                        .build())
                        .build();
                System.out.println("准备发送的数据："+payload.getType()+"-"+payload.getTeacher().getName());
            }
            channel.writeAndFlush(payload);
        }finally {
            clientEvent.shutdownGracefully();
        }
    }
}
