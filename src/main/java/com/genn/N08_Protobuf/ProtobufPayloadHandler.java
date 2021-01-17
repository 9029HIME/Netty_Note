package com.genn.N08_Protobuf;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ProtobufPayloadHandler extends SimpleChannelInboundHandler<ProtoData.Payload> {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("收到客户端的连接了");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ProtoData.Payload msg) throws Exception {
        ProtoData.Payload.DataType type = msg.getType();
        String typeName = type.name();
        System.out.println("收到的类型是："+typeName);
        if(type.equals(ProtoData.Payload.DataType.TYPE_TEACHER)){
            ProtoData.Teacher teacher = msg.getTeacher();
            System.out.println(String.format("收到客户端发来的消息，数据类型是%s," +
                    "通过Protobuf反序列化得到的结果" +
                    "是：姓名%s，年龄%s",typeName,teacher.getName(),teacher.getAge()));
        }else if(type.equals(ProtoData.Payload.DataType.TYPE_STUDENT)){
            ProtoData.Student student = msg.getStudent();
            System.out.println(String.format("收到客户端发来的消息，数据类型是%s," +
                    "通过Protobuf反序列化得到的结果" +
                    "是：姓名%s，id%s",typeName,student.getName(),student.getId()));
        }
    }
}
