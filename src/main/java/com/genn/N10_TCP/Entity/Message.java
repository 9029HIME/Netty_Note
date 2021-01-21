package com.genn.N10_TCP.Entity;

public class Message {
    //消息长度
    private int length;
    //消息字节流
    private byte[] content;

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}
