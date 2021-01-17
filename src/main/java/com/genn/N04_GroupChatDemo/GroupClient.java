package com.genn.N04_GroupChatDemo;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;

public class GroupClient {
    private String host = "127.0.0.1";
    private Integer port = 8081;
    //有可能不止接受信息，如接收文件，此时会有多个通道，就要选择器
    private Selector selector;
    private SocketChannel socketChannel;

    public GroupClient(){
        try {
            selector = Selector.open();
            socketChannel = SocketChannel.open();
            socketChannel.connect(new InetSocketAddress(host,port));
            socketChannel.configureBlocking(false);
            //这里只管读事件算了，即只接受服务器的广播信息，其他都不管
            socketChannel.register(selector, SelectionKey.OP_READ);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void sendMsg(String content){
        try {
            socketChannel.write(ByteBuffer.wrap(content.getBytes()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getMsg(){
        try {
            selector.select();
            //其实这里永远只有一个key
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while(iterator.hasNext()) {
                SelectionKey next = iterator.next();
                if (next.isReadable()) {
                    SocketChannel channel = (SocketChannel) next.channel();
                    //TODO 为什么这里设置大小为1024后，55行的read为1024？flip后的position也是1024？无解
                    ByteBuffer byteBuffer = ByteBuffer.allocate(10);
                    StringBuffer content = new StringBuffer("");
                    while (true) {
                        byteBuffer.clear();
                        int read = channel.read(byteBuffer);
                        if (read == 0){
                            break;
                        } else if (read == -1) {
                            //这里就是服务器和客户端突然断连了
                            channel.close();
                            next.cancel();
                            break;
                        }
                        byteBuffer.flip();
                        content.append(new String(byteBuffer.array()));
                    }
                    System.out.println(content);
                    iterator.remove();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        GroupClient client = new GroupClient();
        //粗暴地开一个线程，用来接受服务端的消息
        new Thread(()->{
            while(true){
                client.getMsg();
            }
        }).start();
        Scanner sc = new Scanner(System.in);
        while(sc.hasNextLine()){
            String msg = sc.nextLine();
            client.sendMsg(msg);
        }
    }
}
