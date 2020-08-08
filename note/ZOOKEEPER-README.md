# ZOOKEEPER
1. [ZooKeeper概述](#ZooKeeper概述)
2. [ZooKeeper重要概念](#ZooKeeper重要概念)
3. [ZooKeeper架构](#ZooKeeper架构)

## <span id="ZooKeeper概述">ZooKeeper概述</span>
ZooKeeper 是一个典型的分布式数据一致性解决方案，分布式应用程序可以基于 ZooKeeper 实现诸如数据发布/订阅、负载均衡、命名服务、
分布式协调/通知、集群管理、Master 选举、分布式锁和分布式队列等功能。

Zookeeper 一个最常用的使用场景就是用于担任服务生产者和服务消费者的注册中心(提供发布订阅服务)。

## <span id="ZooKeeper重要概念">ZooKeeper重要概念</span>
**ZooKeeper基础概念**  
- ZooKeeper 本身就是一个分布式程序（只要**半数以上**节点存活，ZooKeeper 就能正常服务）；
- 为了保证高可用，最好是以集群形态来部署 ZooKeeper，这样只要集群中大部分机器是可用的（能够容忍一定的机器故障），
那么 ZooKeeper 本身仍然是可用的；
- ZooKeeper 将数据保存在内存中，这也就保证了高吞吐量和低延迟（但是内存限制了能够存储的容量不太大，
此限制也是保持 ZNode 中存储的数据量较小的进一步原因）；
- ZooKeeper 是高性能的。 在“读”多于“写”的应用程序中尤其地高性能，因为“写”会导致所有的服务器间同步状态；（“读”多于“写”是协调服务的典型场景。）
- ZooKeeper有临时节点的概念。 当创建临时节点的客户端会话一直保持活动，瞬时节点就一直存在。而当会话终结时，瞬时节点被删除；
持久节点是指一旦这个 ZNode 被创建了，除非主动进行 ZNode 的移除操作，否则这个 ZNode 将一直保存在 Zookeeper 上；
- ZooKeeper 底层其实只提供了两个功能：①管理（存储、读取）用户程序提交的数据；②为用户程序提供数据节点监听服务。


**会话（Session）**  
Session 指的是 ZooKeeper 服务器与客户端会话。在 ZooKeeper 中，一个客户端连接是指客户端和服务器之间的一个 TCP 长连接。
客户端启动的时候，首先会与服务器建立一个 TCP 连接，从第一次连接建立开始，客户端会话的生命周期也开始了。通过这个连接，
客户端能够通过心跳检测与服务器保持有效的会话，也能够向 Zookeeper 服务器发送请求并接受响应，
同时还能够通过该连接接收来自服务器的 Watch 事件通知。

Session 的 sessionTimeout 值用来设置一个客户端会话的超时时间。当由于服务器压力太大、网络故障或是客户端主动断开连接等各种原因导致客户端连接断开时，
只要在 sessionTimeout 规定的时间内能够重新连接上集群中任意一台服务器，那么之前创建的会话仍然有效。

在为客户端创建会话之前，服务端首先会为每个客户端都分配一个 sessionID。由于 sessionID 是 Zookeeper 会话的一个重要标识，
许多与会话相关的运行机制都是基于这个 sessionID 的，因此，无论是哪台服务器为客户端分配的 sessionID，都务必保证**全局唯一**。


**Znode**

在 Zookeeper 中，“节点"分为两类，第一类是指构成集群的机器，称之为机器节点；第二类则是指**数据模型中的数据单元**，称之为数据节点一一 ZNode。

>Zookeeper将所有数据存储在内存中，数据模型是一棵树（ZNode Tree)，由斜杠（/）的进行分割的路径，就是一个 ZNode，
>例如/foo/path1。每个上都会保存自己的数据内容，同时还会保存一系列属性信息。

在 Zookeeper 中，node 可以分为持久节点和临时节点两类。所谓持久节点是指一旦这个 ZNode 被创建了，除非主动进行ZNode的移除操作，
否则这个 ZNode 将一直保存在 Zookeeper 上。而临时节点就不一样了，它的生命周期和客户端会话绑定，一旦客户端会话失效，
那么这个客户端创建的所有临时节点都会被移除。  
另外，ZooKeeper 还允许用户为每个节点添加一个特殊的属性：SEQUENTIAL，一旦节点被标记上这个属性，那么在这个节点被创建的时候，
Zookeeper 会自动在其节点名后面追加上一个整型数字，这个整型数字是一个由父节点维护的自增数字。

**版本**  
Zookeeper 的每个 ZNode 上都会存储数据，对应于每个 ZNode，Zookeeper 都会为其维护一个叫作 Stat 的数据结构，
Stat 中记录了这个 ZNode 的三个数据版本，分别是 version（当前 ZNode 的版本）、
cversion（当前 ZNode 子节点的版本）和 aversion（当前 ZNode 的 ACL 版本）。

**ACL**  
ZooKeeper 采用 ACL（AccessControlLists）策略来进行权限控制，类似于 UNIX 文件系统的权限控制。Zookeeper 定义了如下5种权限：
- CREATE：创建子节点的权限；
- READ：获取节点数据和子节点列表的权限；
- WRITE：更新节点数据权限；
- DELETE：删除子节点的权限；
- ADMIN：设置节点 ACL 的权限。

**Watcher**  
Watcher（事件监听器），是 Zookeeper 中的一个很重要的特性。Zookeeper 允许用户在指定节点上注册一些 Watcher，
并且在一些特定事件触发的时候，ZooKeeper 服务端会将事件通知到感兴趣的客户端上去，该机制是 Zookeeper 实现分布式协调服务的重要特性。

**ZooKeeper 特点**  
- 顺序一致性：从同一客户端发起的事务请求，最终将会严格地按照顺序被应用到 ZooKeeper 中去；
- 原子性：所有事务请求的处理结果在整个集群中所有机器上的应用情况是一致的，也就是说，要么整个集群中所有的机器都成功应用了某一个事务，要么都没有应用；
- 单一系统映像：无论客户端连到哪一个 ZooKeeper 服务器上，其看到的服务端数据模型都是一致的；
- 可靠性：一旦一次更改请求被应用，更改的结果就会被持久化，直到被下一次更改覆盖。

## <span id="ZooKeeper架构">ZooKeeper架构</span>  
![ZooKeeper集群]()  

在 ZooKeeper 中没有选择传统的 Master/Slave 概念，而是引入了Leader、Follower 和 Observer 三种角色。

ZooKeeper 集群中的所有机器通过一个 Leader 选举过程来选定一台称为 “Leader” 的机器，Leader 既可以为客户端提供写服务又能提供读服务。
除了 Leader 外，Follower 和 Observer 都只能提供读服务。Follower 和 Observer 唯一的区别在于 Observer 机器不参与 Leader 的选举过程，
也不参与写操作的“过半写成功”策略，因此 Observer 机器可以在不影响写性能的情况下提升集群的读性能。

各个角色作用如下：
![zk集群角色职责]()

当 Leader 服务器出现网络中断、崩溃退出与重启等异常情况时，ZAB 协议就会进人恢复模式并选举产生新的Leader服务器。这个过程大致如下：
1. Leader election（选举阶段）：节点在一开始都处于选举阶段，只要有一个节点得到超半数节点的票数，它就可以当选准 leader；
2. Discovery（发现阶段）：在这个阶段，followers 跟准 leader 进行通信，同步 followers 最近接收的事务提议；
3. Synchronization（同步阶段）:同步阶段主要是利用 leader 前一阶段获得的最新提议历史，同步集群中所有的副本；
同步完成之后准 leader 才会成为真正的 leader；
4. Broadcast（广播阶段） 到了这个阶段，Zookeeper 集群才能正式对外提供事务服务，并且 leader 可以进行消息广播。
同时如果有新的节点加入，还需要对新节点进行同步。

