package com.genn.N09_Pipeline.Handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class ClientEncodeHandler extends MessageToByteEncoder<Long> {
    @Override
    protected void encode(ChannelHandlerContext ctx, Long msg, ByteBuf out) throws Exception {
        System.out.println("即将编码的数据："+msg);
        out.writeLong(msg);
    }

    /**
     * MessageToByteEncoder关键源码
     * @Override
     *     public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
     *         ByteBuf buf = null;
     *         try {
     *             if (acceptOutboundMessage(msg)) {    //这里会判断msg的类型与泛型是否一致，如果不一致直接把他write出去，不会做编码操作
     *                 @SuppressWarnings("unchecked")
     *                 I cast = (I) msg;
     *                 buf = allocateBuffer(ctx, cast, preferDirect);
     *                 try {
     *                     encode(ctx, cast, buf);      //编码操作
     *                 } finally {
     *                     ReferenceCountUtil.release(cast);
     *                 }
     *
     *                 if (buf.isReadable()) {
     *                     ctx.write(buf, promise);
     *                 } else {
     *                     buf.release();
     *                     ctx.write(Unpooled.EMPTY_BUFFER, promise);
     *                 }
     *                 buf = null;
     *             } else {
     *                 ctx.write(msg, promise);
     *             }
     *         } catch (EncoderException e) {
     *             throw e;
     *         } catch (Throwable e) {
     *             throw new EncoderException(e);
     *         } finally {
     *             if (buf != null) {
     *                 buf.release();
     *             }
     *         }
     *     }
     */
}
