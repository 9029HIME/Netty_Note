package com.genn.N06_NettyGroupChat.Handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;

public class HeartBeatHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent){
            IdleStateEvent event = (IdleStateEvent) evt;

            switch (event.state()){
                case READER_IDLE:
                    System.out.println(String.format("ip地址为%s的通道发生了读空闲",ctx.channel().remoteAddress()));
                    break;
                case WRITER_IDLE:
                    System.out.println(String.format("ip地址为%s的通道发生了写空闲",ctx.channel().remoteAddress()));
                    break;
                case ALL_IDLE:
                    System.out.println(String.format("ip地址为%s的通道发生了空闲",ctx.channel().remoteAddress()));
                    break;
            }
        }
    }
}
