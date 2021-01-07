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