package com.genn.NIODemo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Arrays;
import java.util.Iterator;

/**
 * 通道：SocketChannel
 * 可以理解为：
 *  1.A通道以事件Event-A注册到Selector-A时，会创建SelectionKey-A。SelectionKey-A关联A通道。同时关联 事件为Event-A。
 *  2.SelectionKey-A会添加到Selector-A的keys集合里。
 *  3.select后，如果发现A通道的请求触发了Event-A了，就会将SelectionKey-A添加到selectedKeys集合里，TODO 注意！一般遍历拿到key后删除是删除selectedKeys的数据，而不是keys。
 *  4.通过遍历selectedKeys拿到SelectionKey-A，再通过SelectionKey-A拿到A通道，处理通道里的数据。
 *  5.即:一次连接一个通道，一个通道能处理多次请求。
 *  6.TODO NIO里一个Selector本质是单线程轮询，其中一个请求处理阻塞了，还是不能处理其他请求。
 *  TODO 1:NIO是如何区分来自不同进程请求的通道？（感觉是基于底层的Socket）
 *  TODO 2:如果关闭了通道，那为什么keys里不删除该通道的key？
 */
public class Server {
    public static void main(String[] args) throws Exception {
        //监听8080端口
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        InetSocketAddress inetSocketAddress = new InetSocketAddress(8080);
        serverSocketChannel.socket().bind(inetSocketAddress);
        //开启Selector
        Selector selector = Selector.open();

        serverSocketChannel.configureBlocking(false);
        //server的channel是等待连接事件
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        while(true){
            //只要还没事件，就一直阻塞
            selector.select();
            System.out.println("select finished");
            //走到这里，说明已经有一个请求进来了
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while(iterator.hasNext()){
                SelectionKey key = iterator.next();
                iterator.remove();
                //不同的连接事件，要做不同的处理
                if(key.isAcceptable()) {
                    System.out.println("连接事件");
                    //如果是连接事件，可以通过serverSocketChannel获得一个socketChannel
                    //TODO 这里和BIO最大的不同是：BIO是不管有没有连接来，都会阻塞，一边阻塞一边等连接来。而NIO是等连接来了，才建立连接。
                    SocketChannel accept = serverSocketChannel.accept();
                    System.out.println("连接时accept的hashCode是："+accept.hashCode());
                    //拿到客户端的连接后，记得注册进selector里，注意绑定事件，这里就以OP_READ来监听（即服务端要接受客户端的数据）
                    accept.configureBlocking(false);
                    accept.register(selector,SelectionKey.OP_READ);
                    System.out.println(selector.keys().size());
                    //TODO 在这里可以看到,NIO里一个Selector本质是单线程轮询，其中一个请求处理阻塞了，还是不能处理其他请求。
//                    System.out.println("我连接的时候就阻塞你了，反正我是单线程");
//                    Thread.sleep(5000);
                }
                //如果是读事件
                StringBuffer content = new StringBuffer("");
                if(key.isReadable()){
                    System.out.println("读事件");
                    //此时是已经有连接的操作，需要通过key直接拿到客户端连接
                    SocketChannel channel =(SocketChannel) key.channel();
                    //TODO 可以看到，这个socketChannel和上面连接时获取的socketChannel是同一个
                    System.out.println("读时accept的hashCode是："+channel.hashCode());
                    ByteBuffer byteBuffer = ByteBuffer.allocate(2);
                    int read = 10;
                    //TODO read为0：已经没有东西可以读了。但通道仍未关闭。read为-1，表示客户端的连接已单方面关闭。但服务端仍能收到事件，所以此时要服务端关闭通道，否则会一直循环触发读事件，且读到的内容为00000000000000000000000000000000
                    while(true) {
                        byteBuffer.clear();
                        read = channel.read(byteBuffer);
                        if(read==0){
                            break;
                        }else if(read == -1){
                            channel.close();
                            key.cancel();
                            break;
                        }
                        byteBuffer.flip();
                        content.append(new String(byteBuffer.array()));
                    }
                    System.out.println("收到来自客户端的消息："+content);
                }
            }
        }
    }
}
