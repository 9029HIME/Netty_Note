package com.genn.GroupChatDemo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.security.Key;
import java.util.Iterator;

public class GroupServer {
    private Selector selector;
    private ServerSocketChannel serverSocketChannel;
    private static final Integer PORT = 8081;

    public GroupServer() {
        try {
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().bind(new InetSocketAddress(PORT));
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void open() {
        try {
            while (true) {
                selector.select();
                System.out.println("select finished");
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey next = iterator.next();
                    iterator.remove();

                    if (next.isAcceptable()) {
                        System.out.println("本次请求触发了连接事件");
                        ;
                        SocketChannel accept = serverSocketChannel.accept();
                        System.out.println(String.format("ip为%s的用户已上线", (InetSocketAddress) accept.getRemoteAddress()));
                        accept.configureBlocking(false);
                        accept.register(selector, SelectionKey.OP_READ);
                    }
                    //TODO 接受消息，并广播
                    if (next.isReadable()) {
                        SocketChannel channel = (SocketChannel) next.channel();
                        String content = getMsg(next);
                        if(content!=null) {
                            broadcastMsg(content, channel);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取信息
     */
    public String getMsg(SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();
        try {
            StringBuffer content = new StringBuffer("");
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            int read = 10;
            while (true) {
                byteBuffer.clear();
                read = channel.read(byteBuffer);
                if (read == 0) {
                    break;
                }
                byteBuffer.flip();
                content.append(new String(byteBuffer.array()));
            }
            System.out.println(String.format("收到来自客户端%s的消息：%s", channel.getRemoteAddress(), content));
            return String.format("%s的用户说:%s",channel.getRemoteAddress(),content);
        }catch (IOException e){
            try {
                System.out.println(String.format("%s已离线",channel.getRemoteAddress()));
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            try {
                channel.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            key.cancel();
            return null;
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 服务器广播信息(除了发送者)
     */
    public void broadcastMsg(String content, SocketChannel myself) {

        for (SelectionKey key : selector.keys()) {
            try {
                Channel channel = key.channel();
                if (channel instanceof SocketChannel && channel != myself) {
                    SocketChannel dst = (SocketChannel) channel;
                    ByteBuffer send = ByteBuffer.wrap(content.getBytes());
                    System.out.println("已广播信息："+content);
                    dst.write(send);
                }
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        GroupServer server = new GroupServer();
        server.open();
    }
}
