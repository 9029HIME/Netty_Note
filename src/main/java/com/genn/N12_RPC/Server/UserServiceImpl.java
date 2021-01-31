package com.genn.N12_RPC.Server;

import com.genn.N12_RPC.Common.Service.UserService;

public class UserServiceImpl implements UserService {

    @Override
    public String findById(String id) {
        System.out.println("来自客户端的id： "+id);
        return "现在暂时用String代替";
    }
}
