# HIVE
1. [Hive基本概念](#Hive基本概念)
2. [Hive工作原理](#Hive工作原理)
3. [Hive和数据库比较](#Hive和数据库比较)
4. [Hive数据类型](#Hive数据类型)
    + [基本数据类型](#基本数据类型)
    + [隐式转换](#隐式转换)
    


## <span id="Hive基本概念">Hive基本概念</span>
Hive 是**基于 Hadoop** 的一个数据仓库工具，可以将结构化的数据文件映射为一张表，并提供类 SQL 查询功能。  
本质是**将 HQL 转化成 MapReduce 程序**。  

**为啥说是基于 Hadoop ？**  
- 数据存储在 hdfs 上；
- 数据计算用 MapReduce。

**优点**  
1. 操作接口采用类 SQL 语法，简单易上手~~个屁~~，避免了去写 MapReduce；
2. 提供统一的元数据管理；
3. 延展性：Hive支持用户自定义函数，用户可以根据自己的需求来实现自己的函数；
4. 容错：良好的容错性，节点出现问题SQL仍可完成执行；
5. 可扩展：为超大数据集设计了计算/扩展能力（MR作为计算引擎，HDFS作为存储系统），一般情况下不需要重启服务Hive可以自由的扩展集群的规模。

**缺点**  
1. Hive 的 HQL 表达能力有限；
    + 迭代式算法无法表达；
    + 数据挖掘方面不擅长。
2. Hive 的效率比较低；
    + Hive 自动生成的 MapReduce 作业，通常情况下不够智能化；
    + Hive 调优比较困难，粒度较粗。
3. Hive 可控性差。

**总的来说就是：易上手，适合处理大数据，对于处理小数据没有优势。**

## <span id="Hive工作原理">Hive工作原理</span>
架构图英文装逼版：
![架构图英文装逼版](https://pic1.zhimg.com/80/2d3d0078e986bd8011a0dd13ac8b3601_1440w.jpg?source=1940ef5c)

架构图中文体验版：
![架构图中文体验版](https://img2018.cnblogs.com/blog/1800958/201909/1800958-20190929111919314-580649360.jpg)

**组成结构**  
1. 用户接口：Client  
CLI(hive shell)、JDBC/ODBC(java 访问 hive)、WEBUI(浏览器访问 hive)；
2. 元数据：MetaStore  
元数据包括：表名、表所属的数据库(默认是 default)、表的拥有者、列/分区字段、表 的类型(是否是外部表)、表的数据所在目录等；  
>默认存储在自带的 derby 数据库中，推荐使用 MySQL 存储 MetaStore
3. Hadoop
使用 HDFS 进行存储，使用 MapReduce 进行计算;
4. 驱动器：Driver
    1. 解析器(SQL Parser)：将 SQL 字符串转换成抽象语法树 AST，这一步一般都用第三方工具库完成，比如 antlr；对 AST 进行语法分析，
    比如表是否存在、字段是否存在、SQL 语义是否有误；
    2. 编译器(Physical Plan)：将 AST 编译生成逻辑执行计划；
    3. 优化器(Query Optimizer)：对逻辑执行计划进行优化；
    4. 执行器(Execution):把逻辑执行计划转换成可以运行的物理计划；对于 Hive 来说，就是 MR/Spark。

**工作原理**  
Hive 通过给用户提供的一系列交互接口，接收到用户的指令(SQL)，使用自己的 Driver，结合元数据(MetaStore)，
将这些指令翻译成 MapReduce，提交到 Hadoop 中执行，最后，将 执行返回的结果输出到用户交互接口。

Hive的工作原理简单来说就是**查询引擎**，接收到一个 SQL，而后面要做的事情包括：
1. 词法/语法分析；
2. 语义分析：从 MegaStore 获取模式信息，验证 SQL 中的表名、列名，以及数据类型的检核和隐式转换，Hive提供的函数和用户自定义的函数(UDF/UAF)；
3. 逻辑计划生成：生成逻辑计划-算子树(不会)；
4. 逻辑计划优化：对算子树进行优化，包括列剪枝，分区剪枝，谓词下推等(原谅我还是都不懂)；
5. 物理计划生成：将逻辑计划生产包含由 MapReduce 任务组成的 DAG(有向无环图) 的物理计划；
6. 物理计划执行；
7. 最后把查询结果返回。  
~~8. 当场去世。~~

## <span id="Hive和数据库比较">Hive和数据库比较</span>
从结构上来看，Hive 和数据库除了拥有类似的查询语言，再无类似之处。

1. 查询语言  
Hive 采用了类似 SQL 的查询语言 HQL；
2. 数据存储位置  
Hive 是建立在 Hadoop 之上的，所有 Hive 的数据都是存储在 **HDFS** 中的；  
而数据库则可以将数据保存在**块设备或者本地文件系统**中；
3. 数据更新  
Hive 是针对数据仓库应用设计的，而数据仓库的内容是读多写少的，Hive 中**不建议对数据的改写**，所有的数据都是在加载的时候确定好的；  
而数据库中的数据通常是需要**经常进行修改**。
4. 索引  
Hive 在加载数据的过程中不会对数据进行任何处理，甚至不会对数据进行扫描，因此也**没有对数据中的某些 Key 建立索引**；  
数据库中，通常会针对一个或者几个列**建立索引**，因此对于少量的特定条件的数据的访问，数据库可以有很高的效率，较低的延迟；
>Hive 要访问数据中满足条件的特定值时，需要暴力扫描整个数据，因此访问延迟较高。由于 MapReduce 的引入， Hive 可以并行访问数据，
>因此即使没有索引，对于大数据量的访问，Hive 仍然可以体现出优势。
5. 执行  
Hive 中大多数查询的执行是通过 Hadoop 提供的 MapReduce 来实现的；  
而数据库通常有自己的执行引擎；
6. 执行延迟  
Hive 在查询数据的时候，没有索引，需要扫描整个表，还由于MapReduce 的引入，在利用 MapReduce 执行 Hive 查询时，也会有较高的延迟，因此延迟较高；  
数据库的执行延迟较低；当然，这个低是有条件的，即数据规模较小，当数据规模大到超过数据库的处理能的时候，Hive 的并行计算就体现出优势；
7. 可扩展性  
由于 Hive 是建立在 Hadoop 之上的，因此 Hive 的可扩展性是和 Hadoop 的可扩展性是一致的；  
而数据库由于 ACID 语义的严格限制，扩展行非常有限；
8. 数据规模  
由于 Hive 建立在集群上并可以利用 MapReduce 进行并行计算，因此可以支持很大规模的数据；  
而数据库可以支持的数据规模较小（相比之下）。

## <span id="Hive数据类型">Hive数据类型</span>

### <span id="基本数据类型">基本数据类型</span>
Hive 表中的列支持以下基本数据类型：

| 大类                                    | 类型                                                         |
| --------------------------------------- | ------------------------------------------------------------ |
| **Integers（整型）**                    | TINYINT—1 字节的有符号整数 <br/>SMALLINT—2 字节的有符号整数<br/> INT—4 字节的有符号整数<br/> BIGINT—8 字节的有符号整数 |
| **Boolean（布尔型）**                   | BOOLEAN—TRUE/FALSE                                           |
| **Floating point numbers（浮点型）**    | FLOAT— 单精度浮点型 <br/>DOUBLE—双精度浮点型                 |
| **Fixed point numbers（定点数）**       | DECIMAL—用户自定义精度定点数，比如 DECIMAL(7,2)               |
| **String types（字符串）**              | STRING—指定字符集的字符序列<br/> VARCHAR—具有最大长度限制的字符序列 <br/>CHAR—固定长度的字符序列 |
| **Date and time types（日期时间类型）** | TIMESTAMP —  时间戳 <br/>TIMESTAMP WITH LOCAL TIME ZONE — 时间戳，纳秒精度<br/> DATE—日期类型 |
| **Binary types（二进制类型）**          | BINARY—字节序列                                              |

>TIMESTAMP 和 TIMESTAMP WITH LOCAL TIME ZONE 的区别
>- TIMESTAMP WITH LOCAL TIME ZONE：用户提交时间给数据库时，会被转换成数据库所在的时区来保存。查询时则按照查询客户端的不同，转换为查询客户端所在时区的时间；
>- TIMESTAMP ：提交什么时间就保存什么时间，查询时也不做任何转换。

### <span id="隐式转换">隐式转换</span>
Hive 中基本数据类型遵循以下的层次结构，按照这个层次结构，子类型到祖先类型允许隐式转换；
例如 INT 类型的数据允许隐式转换为 BIGINT 类型。  
额外注意的是：按照类型层次结构允许将 STRING 类型隐式转换为 DOUBLE 类型。

**图片裂开了，希望看得懂**
- Type
    - Primitive Type
        - Number
            - DOUBLE
                - FLOAT
                    - BIGINT
                        - INT
                            - SMALLINT
                                - TINYINT
                - STRING
        - BOOLEAN




附上 **隐式转换表-头痛欲裂版**
![隐式转换表-头痛欲裂版](https://img-blog.csdn.net/20180518102522489?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzM3NjA5NzAx/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

