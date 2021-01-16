package com.genn.NettyWebSocket.Handler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

public class WebSocketInitHandler extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline().addLast(new HttpServerCodec())
        /*
        HTTP协议以块方式写，需要用到这个处理器
         */
        .addLast(new ChunkedWriteHandler())
        /*
        HTTP协议的数据是分段传输的，如果数据量特别大，会出现多次HTTP请求。
        这个处理器能将多个段聚合起来，这里设置的最大大小是8M
         */
        .addLast(new HttpObjectAggregator(8192))
        /*
        websocket是以帧（frame)的形式传递
        这个handler的核心功能是将HTTP协议转为WebSocket协议
        在浏览器可以通过 ws://localhost:8080/hello请求会路由到这个handler的处理
         */
        .addLast(new WebSocketServerProtocolHandler("/hello"))
        /*
        用下面的来处理业务逻辑
         */
        .addLast(new WebSocketHandler());
    }
}
