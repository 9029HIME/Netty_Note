package com.genn.Netty.ByteBuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

public class NettyByteBuf {
    public static void main(String[] args) {

        //创建一个底层是byte[10]的ByteBuf对象,如果不指定，默认值为256
        ByteBuf byteBuf = Unpooled.buffer(10);
        System.out.println(byteBuf.capacity());
        System.out.println(byteBuf.writerIndex());
        for (int i = 0; i < 10; i++) {
            byteBuf.writeByte(i);
        }

        /*
        ByteBuf的读不需要flip()，因为底层维护了readIndex、writerIndex、capacity
        每新增一个byte，writerIndex+1，直到等于capacity
        每读取一个byte（是普通读取，并非按下标读取），会使readIndex+1，readIndex本质表示下一次要读取的下标，直到等于writerIndex便不再读取（否则报越界）
        readIndex:[0,writerIndex]
        writerIndex:[0,capacity]
         */
        for (int i = 0; i < byteBuf.capacity()+1; i++) {
            System.out.println(byteBuf.readByte());
        }


        ByteBuf byString = Unpooled.copiedBuffer("hello", CharsetUtil.UTF_8);
        if(byString.hasArray()){
            //直接拿字节流
            byte[] content = byString.array();
            System.out.println(byString.arrayOffset());
            System.out.println(byString.readerIndex());
            System.out.println(byString.writerIndex());
            System.out.println(byString.capacity());

            //这里指的是可读长度，即writerIndex - readerIndex
            System.out.println(byString.readableBytes());
            //这里指从bystring的字节数组下标为0的数据开始，往后取3个，并转成字符串
            System.out.println(byString.getCharSequence(0,3,CharsetUtil.UTF_8));
        }
    }
}
