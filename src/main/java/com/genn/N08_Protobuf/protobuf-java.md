这只是Java的protobuf，感觉protobuf比较复杂，而且Go的protobuf编写必和Java的不一样。

这份笔记是在Netty学习中对Protobuf的简单记录。详细的Protobuf笔记可能会在以后的gRPC学习中出现。

on 2021-01-17

# 为什么不用JSON或Java的序列化机制？

​	JSON太重了，同一份数据，转成protobuf的大小是转成JSON的1/5，更利于在网络中传输（毕竟大数据量的情况下，更要注重数据压缩了）。Java自带的序列化机制效率本身就不高，序列化后文件的体积很大，**且不能跨语言**。因此用在网络应用开发中用TCP+Protobuf是一个不错的选择，不过Web开发的话还是常用HTTP+JSON。

​	其实Protobuf所谓的跨语言不是特别准确，只是谷歌为Protobuf提供了**许多语言的支持**而已。

# Protobuf简介

​	使用protobuf，首先需要三样东西：protobuf文件、protobuf编译器、语言支持的API。

## protobuf文件

​	用来定义protobuf信息，本身有一套protobuf语法，分为2.x与3.x版本，目前主流都用3.x版本的语法。

## protobuf编译器

​	可以依照protobuf文件的内容，将protobuf文件转成对应语言的源码文件，如在Java中，可以通过**protobuf的Java编译器**将按Java规则语法编写的protobuf文件转成.java源文件。

## 语言支持的API

​	protobuf编译器编译出源文件后，可以通过该语言的Protobuf-API将源文件对应的结构体（或类）对象进行序列化和反序列化。如编译出A.java源文件，那我可以使用Java的Protobuf-API将A的**某个内部类对象a**进行序列化或反序列化。

# Protobuf使用

## Protobuf文件编写

​	TODO 这里只针对Java的写法，且内容可能还不全面 on 2021-01-17

​	新建一个文件ProtoData.proto

```protobuf
syntax = "proto3";  //protobuf版本号
option optimize_for = SPEED;  //加快解析
option java_outer_classname = "ProtoData";//java外部类名，也是java文件名
option java_package = "com.genn.N08_Protobuf"; //生成的Java类所属包名

//Protobuf是基于message来管理数据的
message Student{  //会在ProtoData里生成一个内部类Student，Student是真正发送，进行序列化的对象
  int64 id = 1;   //Student类中有一个属性，名为id，类型为int64（在Java里会转成long类型），TODO 1表示属性的序号，并非值
  string name = 2;
}

message Teacher{
  string name = 1;
  int32  age = 2;
}

//在Protobuf里，可以通过message来管理其他message(我觉得除了以下的方式，还会用更灵活的方式)
message Payload{
  /*
  先定义一个枚举类，用来判断Payload的具体类型是什么
    TODO 这里是逻辑关联，需要开发者通过枚举具体值来判断，并没有很强硬的物理关键
   */
  enum DataType{
    TYPE_STUDENT = 0;	//枚举里的数值不代表属性序号，而是具体值
    TYPE_TEACHER = 1;
  }

  //在Payload里定义一个类型为DataType，变量名为type的属性
  DataType type = 1;

  /*
  在Payload里面定义一个Student对象和Teacher对象，即上面定义好的Student与Teacher类型
  oneof代表着，一个payload对象里，只能包含[Student对象或Teacher对象]中的其中一个，不能两个都有
   */
  oneof body{
    Student student = 2;
    Teacher teacher = 3;
  }
}
```

## Protobuf编译器

​	将编译器放到protobuf文件同级目录下，输入命令protoc.exe --java_out=. ProtoData.proto进行编译，编译完成后会在同级目录下生成ProtoData.java源文件，ProtoData内定义了两个内部类:**Student与Teacher**。

![image-20210117170324823](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20210117170324823.png)

## Protobuf在Netty的使用

引入Java的Protobuf-API

```xml
<dependency>
  <groupId>com.google.protobuf</groupId>
  <artifactId>protobuf-java</artifactId>
  <version>3.13.0</version>
</dependency>
```

### 配置服务端

```java
ServerBootstrap config = new ServerBootstrap();
config.group(bossGroup, workerGroup).    //设置两个线程组
        channel(NioServerSocketChannel.class). //设置NioSocketChannel来封装ServerSocketChannel
        option(ChannelOption.SO_BACKLOG, 128). //TCP/IP协议中的backlog参数，即可连接队列大小
        childOption(ChannelOption.SO_KEEPALIVE, true). //设置保持活动连接状态
        childHandler(new ChannelInitializer<SocketChannel>() {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline()
                //TODO 添加反序列化处理器，需要指定反序列化的类型
                .addLast("protoDecoder",new ProtobufDecoder(ProtoData.Payload.getDefaultInstance()))
                //TODO 添加序列化处理器
                .addLast("protoEncoder",new ProtobufEncoder())
                //TODO 注意！！！！处理器的顺序，接受数据的处理器要放在编解码的后面
                .addLast(new ProtobufPayloadHandler());
    }
});
```

### 服务端Handler

```java
public class ProtobufPayloadHandler extends SimpleChannelInboundHandler<ProtoData.Payload> {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("收到客户端的连接了");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ProtoData.Payload msg) throws Exception {
        ProtoData.Payload.DataType type = msg.getType();
        String typeName = type.name();
        System.out.println("收到的类型是："+typeName);
        if(type.equals(ProtoData.Payload.DataType.TYPE_TEACHER)){
            ProtoData.Teacher teacher = msg.getTeacher();
            System.out.println(String.format("收到客户端发来的消息，数据类型是%s," +
                    "通过Protobuf反序列化得到的结果" +
                    "是：姓名%s，年龄%s",typeName,teacher.getName(),teacher.getAge()));
        }else if(type.equals(ProtoData.Payload.DataType.TYPE_STUDENT)){
            ProtoData.Student student = msg.getStudent();
            System.out.println(String.format("收到客户端发来的消息，数据类型是%s," +
                    "通过Protobuf反序列化得到的结果" +
                    "是：姓名%s，id%s",typeName,student.getName(),student.getId()));
        }
    }
}
```

### 客户端配置

```java
Bootstrap config = new Bootstrap();
config.group(clientEvent).
        channel(NioSocketChannel.class).
        handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline()
                        //TODO 添加反序列化处理器，需要指定反序列化的类型
                        .addLast("protoDecoder",new ProtobufDecoder(ProtoData.Payload.getDefaultInstance()))
                        //TODO 添加序列化处理器
                        .addLast("protoEncoder",new ProtobufEncoder());
            }
        });
System.out.println("客户端配置完成");
```

### 客户端关键发送数据部分

```java
ChannelFuture connect = config.connect("localhost", 8080).sync();
System.out.println("连接完成");
Channel channel = connect.channel();
int random = new Random().nextInt(3);
ProtoData.Payload payload= null;
if(0==random){
    payload = ProtoData.Payload.newBuilder()
            .setType(ProtoData.Payload.DataType.TYPE_STUDENT)
            .setStudent(
                    ProtoData.Student.newBuilder()
                            .setId(10001)
                            .setName("黄俊严")
                            .build())
            .build();
    System.out.println("准备发送的数据："+payload.getType()+"-"+payload.getStudent().getName());
}else{
    payload = ProtoData.Payload.newBuilder()
            .setType(ProtoData.Payload.DataType.TYPE_TEACHER)
            .setTeacher(
                    ProtoData.Teacher.newBuilder()
                            .setAge(27)
                            .setName("老师")
                            .build())
            .build();
    System.out.println("准备发送的数据："+payload.getType()+"-"+payload.getTeacher().getName());
}
channel.writeAndFlush(payload);
```



# 缺陷

​	上面的Netty整合protobuf的代码实在有点难看，已知的有以下五个问题

​	1.Protobuf如何规定级联属性？如Teacher里包含Student。

​	2.如果有N种对象要传输，那DataType就要定义N个枚举？然后handler里要添加N个if嵌套或switch-case，判断收到的是哪一种对象？

​	3.反序列化类型需要明确指定：new ProtobufDecoder(ProtoData.Payload.getDefaultInstance())

​	4.源码得知AbstractMessage的toString()是被final了，不知道为什么会这样设计，要打印出对象的话只好通过JSON序列化。

​	5.总的来看这种写法扩展性极差...没有JSON那么兼容...

​	**也有可能是我目前了解的不太够，可能存在更好的整合办法，但我还未发现。希望之后学gRPC能够发现Protobuf更优雅的调用方法。**