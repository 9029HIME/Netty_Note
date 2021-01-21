# 前提：

​	NIO的select()的底层调用，如果是Windows操作系统，则调用OS的select()函数。如果是linux2.6以上版本，则调用OS的epoll()函数。如果是linux2.6以下版本，则调用OS的select()或poll()函数

# 先来了解一下Linux的IO模式有哪些：

## blocking IO

​	即阻塞IO，步骤是进程调用recvfrom()函数，此时进程让系统调用BLOCK，将自己变为阻塞状态（阻塞状态时不占用CPU资源）。recvfrom()函数被调用后kernel等待数据的到来，直到数据准备好了（如收到了一个完整TCP/UDP包的信息），Kernel会将数据拷贝到用户态进程的内存中，然后返回结果，进程才解除阻塞。
阻塞IO有一个很大的问题：阻塞时间大部分花在等待数据接收完成，很耗时且耗资源。

## nonblocking IO：

​	即非阻塞IO，与阻塞IO不同的是，进程调用recvfrom()函数时，当底层socket对象的数据还没准备好时，kernel会直接给进程返回一个error，而不会阻塞到数据准备好再返回。等数据准备好后，客户端再调用recvfrom()函数，kernel会直接将数据拷贝到用户态内存里。
​	非阻塞IO也有一个问题：由于是非阻塞的，数据未准备好时会直接返回一个error，进程收到error后可以继续执行其他命令，等过一段时间再调用recvfrom()看看数据准备好没。但这样不断的调用recvfrom()，开发者很难规划调用的时间间隔。

## IO multiplexing

​	IO multiplexing：即IO多路复用，**一个进程监控多个Socket**，有三种实现：select()、poll()、epoll()。当进程调用这三种函数其中一种时，进程调用Block变为阻塞，Kernel监视所有socket（应该说是本次调用需要监视的所有socket），当其中任意一个socket准备好数据，函数返回，进程阻塞结束，然后进程再调用recvfrom()函数，kernel就会将准备好的socket数据拷贝到用户态内存里

## asynchronous IO

​	即异步IO，进程发起aio_read()后，不会阻塞。kernel直接返回。kernel会自己等待数据的准备，等完成后直接将数据拷贝到用户内存中，一切完成后，kernel会给进程发送一个singal，通知他read已经完成了（关于异步IO，其实不太了解具体有哪些应用实现）



# IO多路复用在Linux下的不同实现

## select()

```c
int select (int n, fd_set *readfds, fd_set *writefds, fd_set *exceptfds, struct timeval *timeout);
```

​	先说一下各个参数，首先是fd_set。fd_set本质是一个结构体，一个fd_set可以代表多个fd，一个fd对应一个socket连接。有四个宏可以对fd_set进行操作

​		FD_ZERO(fd_set *)  清空一个fd集合；

​		FD_SET(int ,fd_set *)	将一个fd添加到一个指定的文件描述符集合中；

​		FD_CLR(int ,fd_set*)    将一个给定的fd从集合中删除；

​		FD_ISSET(int ,fd_set* )  检查集合中指定的fd是否可以读写。

​	假设一个fd_set长尾8bit，每一个bit都能代表一个fd的状态，有以下操作：

​	1.fd_set set; FD_ZERO(&set);set的结果是 0000,0000

​	2.假设fd = 5，FD_SET(fd,&set)，set变成0001,0000。即从低位往上数第5位变为1。

​	3.FD_SET(2,&set); FD_SET(3,&set)，set变为0001,0110。

​	4.如果select这个fd_set → 这个fd_set的fd2与fd3触发了事件 → select返回 → fd_set变为 0000,0110（fd5没触发事件，所以被清掉了）	

​	maxfdp：是一个整数值，是指集合中所有文件描述符的范围，即所有文件描述符的最大值加1，fd_set是以位图的方式存储fd的，**maxfpd本质是定义了fd_set中有效的个数**。

​	readfds：表示监视里面fd是否发生读事件，如果其中一个fd触发读时间，select会返回一个大于0的值用来表示有文件可读。如果一直没有，则会一直阻塞，如果设置了timeout且超出timeout时间则会返回0。如果发生错误则返回负值。readfds也可以设为null，表示本次select()不关心读时间。

​	writefds：与readfds功能相似，不过监听的是写事件。

​	expectfds：与readfds，writefds相似，不过监听的是错误异常文件。

​	假如传入了readfds，writefds，select()调用后只要有一个fds的事件触发了，就会直接返回，否则一直阻塞（没有配timeout的话）

​	进程调用select()后：(我们假定这个fd就是socket文件描述符，注意一个socket包含一个读缓冲区、一个写缓冲区)

​	1.将fd_set从用户态空间拷贝内核态空间。

​	2.遍历所有fd_set里所有fd，调用fd的poll()方法，poll()方法将当前进程挂到该fd的等待队列中，**此时进程还未进入阻塞状态**。

​	3.poll()方法还会返回该fd是否已事件就绪的mask掩码，并将这个mask掩码FD_SET到fd_set里

​	4.select每一次遍历fd_set的fd时，都会看fd的poll()方法是否返回了**已事件就绪的mask掩码**，如果遍历完所有fd发现没一个就绪（当然，如果有就绪了select就会直接返回了），**就会让进程进入阻塞状态**。如果没有配置timeout进程会一直阻塞，反之则阻塞到timeout的时间后，进程解除阻塞状态重新去抢CPU时间片。

​	5.当监视的fd有一个就绪时（事件触发)，fd的设备驱动会唤醒进程（**因为设备驱动会维护队列**），进程解除阻塞，从所有fd等待队列中移除。此时select()也返回了。

​	6.等进程唤醒后被CPU调度到时，进程遍历fd_set里所有fd，就能找到哪一个fd的事件就绪了，之后就是对该fd调用recvfrom()读取数据。 select()的缺点是每次调用都要将进程往[fd队列]里[每一个fd]的[等待队列]里插入/移除，需要来回将fd从用户态与内核态之间拷贝。底层规定fd队列是一个数组，且大小不能超过1024 

## poll()

```c
int poll (struct pollfd *fds, unsigned int nfds, int timeout);
```

```c
struct pollfd {
	int fd; /* 文件描述符 */c
	short events; /* 监听的事件 */ 
	short revents; /* 返回的事件 */ 
};
```

​	poll()主要关注的是需要监听的事件与返回的事件，流程与select()相似。和select()一样，**都需要来回将fd从用户态与内核态之间拷贝。且与select()相比底层是个连表，没有1024的限制。性能会随着fd数量的增加而减少。**

# **epoll()**

​	