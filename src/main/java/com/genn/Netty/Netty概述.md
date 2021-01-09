# NIO的缺点：

​	底层复杂，开发困难，还有epoll Bug

# Netty概述：

​	封装了NIO API，能够相对快速开发网络应用的NIO框架。Netty4.x只需基于jdk6。Netty5已被废弃（具体原因未知）。Netty主要基于"主从Reactor多线程"的线程模型。

# Reactor模型：

​	注意，线程模型只是一种规范，不同的线程模型有不同的实现。同一个线程模型的实现之间不一定相同。

## 1.Reactor模式：

​	多个连接的请求统一阻塞在一个Reactor中，当某个请求有数据可以处理时（触发事件）才处理请求。Reactor模型可以分为单Reactor单线程、单Reactor多线程、主从Reactor模型三种不同的**实现**。

## 2.单Reactor单线程：

​	在com.genn.GroupChatDemo包下的NIO群聊demo就是一个典型的单Reactor单线程模型，当事件触发后，**（中间再被select()后）**Reactor线程会处理请求。**注意！由于是单线程，如果在处理请求时遭遇阻塞，即使其他请求已就绪，Reactor也无法处理其他请求。**本质上并不能迎合高并发，但和BIO网络通讯的不同是，BIO是每一个连接与请求默认对应一个线程（包括阻塞部分），即N个请求打进来，但不建立连接的话，会导致N个线程阻塞，**即N:N**，**而且每一个连接的请求（如读，写）都对应一个线程**，大大降低服务器性能。单Reactor模型起码还有个Selector在挡着，**即N:1**，等连接的事件（如读，写）触发后才调度处理请求，**整体上只有一个线程来处理多个请求**

​	一般情况下，是不会用这种模型的

![img](https://img2018.cnblogs.com/blog/371217/201812/371217-20181216205228564-867800649.png)

## 3.单Reactor多线程：

![img](https://img2018.cnblogs.com/blog/371217/201812/371217-20181216205238924-890218087.png)

​	和单Reactor单线程不同的是，单Reactor多线程从线程池里取线程，用来处理请求。当处理完成后，归还线程，**Reactor再调用send将结果返回，即结果的响应是由Reactor线程做的，只有处理请求是交给多线程**

## 4.主从Reactor多线程：

![img](https://img2018.cnblogs.com/blog/371217/201812/371217-20181216205249173-151738900.png)

​	和单Reactor多线程不同的是，由两种Reactor：MainReactor与SubReactor，MainReactor只处理连接事件，并将其他事件注册到SubReactor上。接下来其他事件的请求就交给SubReactor处理，从这里开始就和单Reactor多线程一样的。值得注意的是，**一个MainReactor可以由多个SubReactor**。



# Netty的线程模型

​	了解Netty的线程模型前，先了解一下几个概念：

​	1.EventLoop

​		封装着一个Selector，每一个EventLoop对应着一个**EventLoop线程**。Netty默认根据处理器核数提供了一个推荐值，即线程数&EventLoop的个数

​	private static final int DEFAULT_EVENT_LOOP_THREADS; static {   DEFAULT_EVENT_LOOP_THREADS = Math.max(1, SystemPropertyUtil.getInt(       "io.netty.eventLoopThreads", Runtime.getRuntime().availableProcessors() * 2));   if (logger.isDebugEnabled()) {     logger.debug("-Dio.netty.eventLoopThreads: {}", DEFAULT_EVENT_LOOP_THREADS);   } }

​	2.EventLoopGroup

​		一组EventLoop的封装，可以理解hannelPipeLine为EventLoop池。EventLoopGroup提供了next接口，可以按照**一定规则**在EventLoopGroup里取出一个EventLoop使用

​	3.ChannelPipeline

​		在Netty中每一个SocketChannel维护着一个ChannelPipeline实例，每一个ChannelPipeline维护着一个ChannelHandler链表。ChannelPipeline的默认实现是DefaultChannelPipeline。ChannelHandler有两个实现，ChannelInBoundHandlerAdapter与ChannelOutBoundHandlerAdapter，前者用来**处理来自客户端的请求**，后者**处理发送给客户端请求**。我们可以自定义ChannelHandler的实现类，将其放入ChannelPipeline。当事件触发后，ChannelPipeline会根据不同的类型调用不同的Handler处理。**ChannelPipeline本很是责任链模式的一个变种**。

![在这里插入图片描述](https://img-blog.csdnimg.cn/20181105212249587.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L01lbWVyeV9sYXN0,size_16,color_FFFFFF,t_70)

​	在Netty的线程模型中，有两个EventLoopGroup：Boss组和Worker组。Boss组调用next选出一个Boss来处理Accept事件，处理完成后得到一个socketChannel交给Worker组，此时Worker组也会调用next选出一个Worker，将从Boss里得到的socketChannel注册到自己的Selector上，最后等待该连接的事件触发(select())。**通常情况下，Boss组建议只有一个Boss，当然也可以设置多个Boss。**

​	当Worker里一个连接的事件触发后，会通过SelectionKey找到socketChannel与socketChannel绑定的ChannelPipeline，并将socketChannel交给ChannelPipeline处理，ChannelPipeline会根据事件类型，调用对应的Handler来处理请求，最后响应给客户端。