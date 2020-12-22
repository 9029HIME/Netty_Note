package com.genn;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    /**
     * 使用FileChannel写文件
     * 数据流方向：
     *  content → ByteBuffer → OutputStream（包裹了FileChannel） → 文件
     */
    @Test
    public void writeFileChannel() throws Exception {
        FileOutputStream outputStream = null;
        try {
            String content = "File-Nio";
            outputStream = new FileOutputStream("src\\main\\resources\\fileChannel.txt");
            FileChannel fileChannel = outputStream.getChannel();
            //容量不够的话会抛BufferOverflowException
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            byteBuffer.put(content.getBytes());
            //转为读模式，
            byteBuffer.flip();
            int write = fileChannel.write(byteBuffer);
        }finally {
            outputStream.close();
        }

    }

    /**
     * 使用FileChannel读文件
     * 数据流方向：
     *  文件 → InputStream（包裹了FileChannel）→ ByteBuffer → Content
     */
    @Test
    public void readFileChannel() throws Exception {
        FileInputStream inputStream = null;
        try {
            File file = new File("src\\main\\resources\\fileChannel.txt");
            inputStream = new FileInputStream(file);
            FileChannel fileChannel = inputStream.getChannel();
            //容量不够的话会抛BufferOverflowException
            ByteBuffer byteBuffer = ByteBuffer.allocate((int)file.length());
            int read = fileChannel.read(byteBuffer);
            byte[] array = byteBuffer.array();
            System.out.println(new String(array));
        }finally {
            inputStream.close();
        }
    }

    /**
     * 使用一个Buffer完成文件读写
     * @throws Exception
     */
    @Test
    public void oneBufferCopy() throws Exception {
        File from = new File("src\\main\\resources\\fileChannel.txt");
        File to = new File("src\\main\\resources\\fileChannel.txt(copy)");
        if(!to.exists()){
            to.createNewFile();
        }
        FileInputStream f = new FileInputStream(from);
        FileOutputStream o = new FileOutputStream(to);
        FileChannel fChannel = f.getChannel();
        FileChannel oChannel = o.getChannel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        while (true) {
            byteBuffer.clear();
            int read = fChannel.read(byteBuffer);
            if(read==-1){
                break;
            }
            byteBuffer.flip();
            oChannel.write(byteBuffer);
        }
    }

    /**
     * 直接使用channel的TransferFrom进行拷贝
     * @throws Exception
     */
    @Test
    public void noBufferCopy() throws Exception {
        File from = new File("src\\main\\resources\\fileChannel.txt");
        File to = new File("src\\main\\resources\\fileChannel.txt(copy)");
        if(!to.exists()){
            to.createNewFile();
        }
        FileInputStream f = new FileInputStream(from);
        FileOutputStream o = new FileOutputStream(to);
        FileChannel fChannel = f.getChannel();
        FileChannel oChannel = o.getChannel();
        oChannel.transferFrom(fChannel,0,fChannel.size());
        oChannel.close();
        fChannel.close();
        o.close();
        f.close();
    }

    @Test
    public void testMappedByteBuffer() throws Exception {
        RandomAccessFile file = new RandomAccessFile("src\\main\\resources\\MappedFile.txt","rw");
        FileChannel channel = file.getChannel();
        //参数1：读写模式
        //参数2：修改范围的起始位置
        //参数3：修改范围大小，以字节为单位（包左不包右）
        //这里我只以读写模式，获取了文件第1个到第3个字节的缓冲区
        MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, 2);
        //超出可操作范围会抛异常
        buffer.put("ab".getBytes());
        channel.close();
        file.close();
    }
}
