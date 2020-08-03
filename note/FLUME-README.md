# Flume
1. [Flume概述](#Flume概述)
2. [Flume基础架构](#Flume基础架构)
3. [Flume拦截器](#Flume拦截器)

## <span id="Flume概述">Flume概述</span>
Flume 是一个高可用的，高可靠的，分布式的海量日志采集、聚合和传 输的系统。Flume 基于流式架构，灵活简单。  
Flume 支持在日志系统中定制各类数据发送方，用于收集数据；同时，Flume 提供对数据进行简单处理，并写到各种数据接受方（可定制）的能力。

## <span id="Flume基础架构">Flume基础架构</span>
Flume 基础架构图：  
![flume架构图](https://www.ibm.com/developerworks/cn/data/library/bd-flumews/fig01.png)

**Agent**

Agent 是一个 JVM 进程，它以事件的形式将数据从源头送至目的。  
Agent 主要有 3 个部分组成：  

- Source（源）-> 生成数据的地方，是负责接收数据到Flume Agent的组件；
> 从事件生成器接收数据，以event事件的形式传给一个或多个channel。
- Channel（通道）-> Channel 是位于 Source 和 Sink 之间的缓冲区，Channel 是线程安全的，可以同时处理几个 Source 的写入操作和几个 Sink 的读取操作；
> 从 source 中接收 flume event，作为临时存放地，缓存到 buffer 中，直到 sink 将其消费掉，是 source 和 sink 之间的桥梁；  
> Channel是事务的，可以和多个source或sink协同。
- Sink（沉槽）-> Sink 不断地轮询 Channel 中的事件且批量地移除它们，并将这些事件批量写入到存储 或索引系统、或者被发送到另一个 Flume Agent。
> 存放数据到 HDFS，从 channel 中消费 event，并分发给 destination(目的地)，sink 的 destination 也可以是另一个 agent 或者 HDFS、HBASE；  
> 注意：一个 flume 的 agent，可以有多个 source，channel，sink。

**Event**

传输单元，Flume 数据传输的基本单元，以 Event 的形式将数据从源头送至目的地。 Event 由 Header 和 Body 两部分组成，
Header 用来存放该 event 的一些属性，为 K-V 结构，Body 用来存放该条数据，形式为字节数组。

## <span id="Flume拦截器">Flume拦截器</span>
当我们需要对数据进行过滤时，除了我们在 Source、Channel 和 Sink 进行代码修改之外，Flume 为我们提供了拦截器，拦截器是 chain(链) 形式的。  
拦截器的位置在 Source 和 Channel 之间，当我们为 Source 指定拦截器后，我们在拦截器中会得到 event，
根据需求我们可以对 event 进行保留还是弃，抛弃的数据不会进入Channel中。
