package com.genn.Netty.HTTPDemo.Handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.util.Iterator;
import java.util.Map;

/**
 * 1.SimpleChannelInboundHandler是ChannelInboundHandlerAdapter的子类
 * 2.HttpObject表示客户端和服务端相互通信的数据(可以理解为http请求的一个包装，包含了请求头，请求体等信息)
 */
public class HTTPServeriHandler extends SimpleChannelInboundHandler<HttpObject> {
    /**
     * 类似channelRead()
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        //判断是不是HTTP请求，注意这玩意是Netty的类
        if(msg instanceof HttpRequest){
            System.out.println("class of msg:"+msg);
            System.out.println("客户端的地址："+ctx.channel().remoteAddress());
            //还得强转为HttpRequest
            HttpRequest request = (HttpRequest) msg;
            HttpHeaders headers = request.headers();
            Iterator<Map.Entry<String, String>> iterator = headers.iterator();
            while (iterator.hasNext()){
                Map.Entry<String, String> next = iterator.next();
                System.out.println(String.format("请求头：%s,请求体：%s",next.getKey(),next.getValue()));
            }

            //TODO 在这里也可以对指定路径进行拦截或分派，可以做到类似MVC的效果
            String uri = request.uri();
            if(uri.equals("/favicon.ico")){
                return;
            }


            //做出响应
            ByteBuf content = Unpooled.copiedBuffer("你好呀我是服务器", CharsetUtil.UTF_8);
            DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,content);
            response.headers().set(HttpHeaderNames.CONTENT_TYPE,"text/plain;charset=utf-8");
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH,content.readableBytes());
            ctx.writeAndFlush(response);
        }
    }
}
