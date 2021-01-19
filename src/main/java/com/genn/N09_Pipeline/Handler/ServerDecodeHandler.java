package com.genn.N09_Pipeline.Handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

//ByteToMessageDecoder本身是一个InboundHandler
public class ServerDecodeHandler extends ByteToMessageDecoder {
    /**
     * 自定义编码handler，要将byte转成Long，并交给下一个handler处理
     * @param ctx
     * @param in
     * @param out
     * @throws Exception
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        /*
        如果可读的byte数≥8，就拿8个字节转成Long
        TODO 为什么用if而不是while？
            只要ByteBuf还有数据，decode方法就会被调用多次，直到读完
         */
        if(in.readableBytes()>=8){
            out.add(in.readLong());
        }
    }
}
