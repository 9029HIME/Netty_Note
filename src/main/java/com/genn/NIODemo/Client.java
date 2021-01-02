package com.genn.NIODemo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Client {
    public static void main(String[] args) throws Exception {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        if(!socketChannel.connect(new InetSocketAddress(8080))){
            //由于客户端连接服务端是非阻塞的，因此需要手动重试
            while(!socketChannel.finishConnect() ) {
                System.out.println("服务端貌似还没开启，先睡一秒再重连");
                Thread.sleep(1000);
            }
        }
        for(int i=0;i<2;i++) {
            Thread.sleep(10000);
            String content = "HelloWorld";
            ByteBuffer buffer = ByteBuffer.wrap(content.getBytes());
            socketChannel.write(buffer);
        }
        System.in.read();
        socketChannel.close();
    }
}
