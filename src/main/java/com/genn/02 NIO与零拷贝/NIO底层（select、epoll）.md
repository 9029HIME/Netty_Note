# 前提：

​	NIO的select()的底层调用，如果是Windows操作系统，则调用OS的select()函数。如果是linux2.6以上版本，则调用OS的epoll()函数。如果是linux2.6以下版本，则调用OS的select()或poll()函数

# 先来了解一下Linux的IO模式有哪些：

## blocking IO

​	即阻塞IO，步骤是进程调用recvfrom()函数，此时进程让系统调用BLOCK，将自己变为阻塞状态（阻塞状态时不占用CPU资源）。recvfrom()函数被调用后kernel等待数据的到来，直到数据准备好了（如收到了一个完整TCP/UDP包的信息），Kernel会将数据拷贝到用户态进程的内存中，然后返回结果，进程才解除阻塞。
​	阻塞IO有一个很大的问题：阻塞时间大部分花在等待数据接收完成，很耗时且耗资源。

## nonblocking IO：

​	即非阻塞IO，与阻塞IO不同的是，进程调用recvfrom()函数时，当底层socket对象的数据还没准备好时，kernel会直接给进程返回一个error，而不会阻塞到数据准备好再返回。等数据准备好后，客户端再调用recvfrom()函数，kernel会直接将数据拷贝到用户态内存里。
​	非阻塞IO也有一个问题：由于是非阻塞的，数据未准备好时会直接返回一个error，进程收到error后可以继续执行其他命令，等过一段时间再调用recvfrom()看看数据准备好没。但这样不断的调用recvfrom()，开发者很难规划调用的时间间隔。

## IO multiplexing

​	IO multiplexing：即IO多路复用，**一个进程监控多个Socket**，有三种实现：select()、poll()、epoll()。当进程调用这三种函数其中一种时，进程调用Block变为阻塞，Kernel监视所有socket（应该说是本次调用需要监视的所有socket），当其中任意一个socket准备好数据，函数返回，进程阻塞结束，然后进程再调用recvfrom()函数，kernel就会将准备好的socket数据拷贝到用户态内存里

## asynchronous IO

​	即异步IO，进程发起aio_read()后，不会阻塞。kernel直接返回。kernel会自己等待数据的准备，等完成后直接将数据拷贝到用户内存中，一切完成后，kernel会给进程发送一个singal，通知他read已经完成了（关于异步IO，其实不太了解具体有哪些应用实现）



# 典型的blocking IO 是如何实现的

​	首先要明确一个概念：**每个Socket都有一个等待队列**，用来存放**等待操作Socket**的进程，当进程A调用recvfrom()从socket1的接收缓冲区读取数据时，如缓冲区有数据，则直接读取。如没有数据，**进程A会让OS使自己进入挂载到socket1的等待队列中，此时进程A变为阻塞状态。**

​	当网卡接收到发给socket1的数据后，网卡会先将数据传送到内存，然后网卡会通知CPU有数据到达。CPU此时会做两件事：1.将内存的数据写到socket1的接收缓冲区内 2.唤醒进程，当进程A被CPU调度到后（看图解，貌似是直接插队）此时recvfrom()就能读到缓冲区内的数据了。

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

​	epoll()三个主要系统函数

```c
/**
    创建一个ep_fd对象（也可以当作是eventpoll对象）并返回，size用来告诉内核这个监听的数目一共有多大，注意这里的size和select的maxfdp，epoll的size只是一个大小建议。
    ep_fd对象会占用一个fd值（epoll完后需要close()来关闭ep_fd）。
**/
int epoll_create(int size)；
/**
    epfd:上面创建的ep_fd。
    op:对需要监听的fd进行op操作，op操作有三种：EPOLL_CTL_ADD（新增fd监听）、EPOLL_CTL_DEL（删除fd监听）、EPOLL_CTL_MOD（修改fd监听）。
    fd：需要监听的fd。
    event：告诉内核需要对这个fd的监听什么事件，本质是一个epoll_event结构体。
    epoll_ctl()主要作用是将某个fd(fd)和待监听事件(event)转换为epoll_item，并注册到ep_fd(epfd)的一颗红黑树里，
**/
int epoll_ctl(int epfd, int op, int fd, struct epoll_event *event)；
/**
    epfd：上面创建的ep_gfd。
    events：用来从内核获取发生的事件，是一个集合。
    maxevents：events集合的大小，不能超过epoll_create()的size。
    timeout：超时时间，如果超时则返回0。如果没有超时时间，则会一直阻塞。
**/
int epoll_wait(int epfd, struct epoll_event * events, int maxevents, int timeout);
```

​	调用epoll_create()时，会创建一个ep_fd(也就是eventpoll对象)，这个对象包含了两个关键的属性：

```c
struct eventpoll {
　　...
　　//红黑树
　　struct rb_root rbr;
　　//双向链表
　　struct list_head rdllist;
　　...
};
```

​	调用epoll_ctl()时，会将某个fd(fd)和待监听事件(event)转换为epoll_item，并注册到ep_fd(epfd)的rbr红黑树里，同时给这个epoll_item**注册一个回调函数（即ep_poll_callback）到内核**（其实这一点不是特别理解），回调函数是与**设备驱动程序**建立关系，当fd的事件触发或**已经触发**后，设备驱动程序会识别出来并触发回调函数，回调函数触发后会将该epoll_item加入到rdllist双向链表里。	

​	调用epoll_wait()时，本质只是检查ep_fd的**rdllist是否有epoll_item**，如果不为空则将epoll_item与其数量返回，为空则**进程会挂载到eventpoll的等待队列里进行阻塞**，直到timeout超时，如果没有timeout则一直阻塞，直到rdllist内有epoll_item。回调函数将epoll_item添加到rdllist的同时，会唤醒epoll_item里阻塞的进程，进程被CPU调度到后可以从fdllist中得知哪些fd(socket)就绪了，从而进行下一步操作（如读取）。

​	总得来说：epoll_create()新建eventpoll、epoll_ctl()把fd与事件注册到eventpoll、epoll_wait()等待eventpoll里的fd因事件触发后被添加到fdllist里

​	epoll()有两种触发方式：EPOLLLT、EPOLLET

​	EPOLLLT（水平触发）：假设收缓冲区有100Byte数据到来，第一次epoll_wait()触发，进程只读了其中50Byte，剩余50Byte在收缓冲区。第二次epoll_wait()会触发，让进程读剩下的50Byte。如果进程还是不读，第三次epoll_wait()也会触发， 让进程继续读那50Byte。**即只要socket缓冲区内有数据可读，每一次epoll_wait()都会返回就绪事件。**

​	EPOLLET（边缘触发）：假设收缓冲区有100Byte数据到来，第一次epoll_wait()触发，进程只读了其中50Byte，剩余50Byte在收缓冲区。**第二次epoll_wait()不会触发**，剩余的50Byte会一直在收缓冲区里，之后的epoll_wait()都不会触发，**直到有新的数据来到收缓冲区，epoll_wait()才会触发。**