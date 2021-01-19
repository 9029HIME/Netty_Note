# c前提：

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

​	select()函数监视的文件描述符（事件）有三种，writefds，readfds，expectfds（TODO 这个expectfds是啥？）。进程调用select()后，会进入所有socket的等待队列中，此时进程进入阻塞状态。当监视的描述符有一种就绪时（事件触发)，进程解除阻塞；从所有socket等待队列中移除。等被CPU调度到时，进程遍历所有socket，就能找到哪一个socket的事件就绪了，之后就是对该socket调用recvfrom()读取数据。
select()的缺点是每次调用都要将进程往[socket队列]里[每一个socket]的[等待队列]里插入/移除，底层规定socket队列是一个数组，且大小不能超过1024

```c
int poll (struct pollfd *fds, unsigned int nfds, int timeout);
```

```c
struct pollfd {
	int fd; /* file descriptor */
	short events; /* requested events to watch */ 
	short revents; /* returned events witnessed */ 
};
```

