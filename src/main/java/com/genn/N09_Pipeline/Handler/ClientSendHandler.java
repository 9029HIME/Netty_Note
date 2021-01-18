package com.genn.N09_Pipeline.Handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ClientSendHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("和服务端建立了连接后，会调用一次客户端的入站，我们在这里给服务端send一个Long数据");
        /*
        TODO 对于客户端来说，写消息给服务端是出站，因此会调用出站的方法链
        TODO 如果不用编解码器的话，ctx是无法writeAndFlush一个Long类型数据
         */

        ctx.channel().writeAndFlush(12345L);
    }


}
