package com.genn.N07_NettyWebSocket;

import com.genn.N07_NettyWebSocket.Handler.WebSocketInitHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 *      todo WebSocket本质是基于HTTP的协议
 *  WebSocket 看成是 HTTP 协议为了支持长连接所打的一个大补丁，它和 HTTP 有一些共性，
 *  是为了解决 HTTP 本身无法解决的某些问题而做出的一个改良设计。在以前 HTTP 协议中所谓的
 *  keep-alive connection 是指在一次 TCP 连接中完成多个 HTTP 请求，但是对每个请求仍然要单独发 header；
 *  所谓的 polling 是指从客户端（一般就是浏览器）不断主动的向服务器发 HTTP 请求查询是否有新数据。
 *  这两种模式有一个共同的缺点，就是除了真正的数据部分外，服务器和客户端还要大量交换 HTTP header，信息交换效率很低。
 *  它们建立的“长连接”都是伪.长连接，只不过好处是不需要对现有的 HTTP server 和浏览器架构做修改就能实现。
 *
 *  WebSocket 解决的第一个问题是，通过第一个 HTTP request 建立了 TCP 连接之后，
 *  之后的交换数据都不需要再发 HTTP request了，使得这个长连接变成了一个真.长连接。
 *  但是不需要发送 HTTP header就能交换数据显然和原有的 HTTP 协议是有区别的，
 *  所以它需要对服务器和客户端都进行升级才能实现。在此基础上 WebSocket 还是一个双通道的连接，
 *  在同一个 TCP 连接上既可以发也可以收信息。此外还有 multiplexing 功能，几个不同的 URI 可以复用同一个 WebSocket 连接。这些都是原来的 HTTP 不能做到的。
 *
 *
 *
 *  keep-alive 只是一种为了达到复用tcp连接的“协商”行为，双方并没有建立正真的连接会话，服务端也可以不认可，也可以随时（在任何一次请求完成后）关闭掉。
 *  WebSocket 不同，它本身就规定了是正真的、双工的长连接，两边都必须要维持住连接的状态。
 *
 *  另外，http协议决定了浏览器端总是主动发起方，http的服务端总是被动的接受、响应请求，从不主动。而WebSocket协议，
 *  在连接之后，客户端、服务端是完全平等的，不存在主动、被动之说。
 */
public class Server {
    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();

        try {
            ServerBootstrap config = new ServerBootstrap();
            config.group(boss,worker).channel(NioServerSocketChannel.class)
                    .childHandler(new WebSocketInitHandler());
            ChannelFuture close = config.bind(8080).sync();
            close.channel().closeFuture().sync();
        }finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}
