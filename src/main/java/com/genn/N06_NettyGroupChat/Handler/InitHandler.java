package com.genn.N06_NettyGroupChat.Handler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

public class InitHandler extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline().addLast("encode",new StringEncoder())
        .addLast("decode",new StringDecoder())
        .addLast(new ChatServerHandler())
        /*
        TODO 这里添加的是netty内置的handler，可以实现心跳效果
            readerIdleTime:这个连接多长时间没发生读时间，Netty就发送一个心跳包检测是否还有连接
            writerIdleTime:这个连接多长时间没发生写时间，Netty就发送一个心跳包检测是否还有连接
            allIdleTime: ..........没发生读或写................是否还有连接
            值得注意的是：如果客户端手动关闭连接，服务端是会接受到这个事件，并调用handlerRemove和channelInactive
            这一点在三次握手四次挥手是可以知道的
            但是也有一种情况，客户端强制关闭连接（如强制结束进程，或电脑强制关机），这时候服务端是接受不了断连请求，还是会一直维护这个连接
            因此才需要心跳机制，每隔一段时间发送一个心跳包，查看连接是否还在
         */
        .addLast(new IdleStateHandler(3,5,7, TimeUnit.SECONDS))
        /*
        TODO 当发现空闲后，会触发特定的空闲事件IdleStateEvent，然后将该IdleStateEvent交给下一个Handler的userEventTriggered()方法
            内处理，该方法用来专门处理空闲事件
         */
        .addLast(new HeartBeatHandler());
    }
}
