package com.genn.N12_RPC.Server.Handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class RPCHandler extends ChannelInboundHandlerAdapter {


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        /*
        调用的时候，需要服务端传的信息格式是：接口名-方法名-参数1:参数1值-参数2:参数2值
            即 UserService-findById-id:5
         */
        String[] split = msg.toString().split("-");
        if(split.length>=2){
            String serviceName = split[0];
            String methodName = split[1];
            //这里就假设它传了UserService-findById-id:5
            String param1value = split[2];
            String[] paramAndValue = param1value.split(":");
            String param = paramAndValue[0];
            String value = paramAndValue[1];

        }else{

        }
    }


}
