# HBASE

## <span id="HBase概述">HBase概述</span>
HBase 是一种分布式、可扩展、支持海量数据存储的 NoSQL 数据库。

逻辑上，HBase 的数据模型同关系型数据库很类似，数据存储在一张表中，有行有列。但从 HBase 的底层物理存储结构(K-V)来看，
HBase 更像是一个 multi-dimensional map。

## <span id="HBase数据模型">HBase数据模型</span>
**HBase 逻辑结构**  
![逻辑结构图](https://raw.githubusercontent.com/hongyidashi/big-data-study/master/note/images/hbase/%E9%80%BB%E8%BE%91%E7%BB%93%E6%9E%84%E5%9B%BE.jpg)  

**HBase 物理存储结构**  
![物理存储结构图](https://raw.githubusercontent.com/hongyidashi/big-data-study/master/note/images/hbase/%E7%89%A9%E7%90%86%E5%AD%98%E5%82%A8%E7%BB%93%E6%9E%84.jpg)

**数据模型**
1. NameSpace  
命名空间，类似于关系型数据库的 DataBase 概念，每个命名空间下有多个表。HBase 有两个自带的命名空间，分别是 hbase 和 default，
hbase 中存放的是 HBase 内置的表， default 表是用户默认使用的命名空间。

2. Region  
类似于关系型数据库的表概念。不同的是，HBase 定义表时只需要声明列族即可，不需要声明具体的列。这意味着，往 HBase 写入数据时，
字段可以动态、按需指定。因此，和关系型数据库相比，HBase 能够轻松应对字段变更的场景。

3. Row  
HBase 表中的每行数据都由一个 RowKey 和多个 Column(列)组成，数据是按照 RowKey 的字典顺序存储的，
并且查询数据时只能根据 RowKey 进行检索，所以 RowKey 的设计十分重要。RowKey 可以是任意字符串(最大长度是 64KB，实际应用中长度一般为 10-100bytes)，
在 hbase 内部，RowKey 保存为字节数组。
>访问hbase table中的行，只有三种方式：
>1. 通过单个row key访问；
>2. 通过row key的range；
>3. 全表扫描。

4. Column  
HBase 中的每个列都由 Column Family(列族)和 Column Qualifier(列限定符)进行限 定，例如 info:name，info:age。建表时，
只需指明列族，而列限定符无需预先定义。
 
5. Time Stamp  
用于标识数据的不同版本(version)，每条数据写入时，如果不指定时间戳，系统会自动为其加上该字段，其值为写入 HBase 的时间。  
HBase 中通过 row 和 columns 确定的为一个存贮单元称为 cell。每个 cell 都保存着同一份数据的多个版本，版本通过时间戳来索引；
时间戳的类型是 64 位整型。时间戳可以由 hbase(在数据写入时自动)赋值，此时时间戳是精确到毫秒的当前系统时间。时间戳也可以由客户显式赋值。
如果应用程序要避免数据版本冲突，就必须自己生成具有唯一性的时间戳。每个 cell 中，不同版本的数据按照时间倒序排序，即最新的数据排在最前面。
>cell  
>由{row key, column(=<family> + <label>), version} 唯一确定的单元。cell 中的数据是没有类型的，全部是字节码形式存贮。
>插入数据的时候不指定 时间戳 用的是 HBase 服务端的时间戳！服务端的时间戳！服务端的时间戳！在写代码时 sleep 是没用的！

## <span id="HBase架构">HBase架构</span>
HBase架构图：  
![HBase架构图](https://img2018.cnblogs.com/blog/1222878/201906/1222878-20190602190004759-235734166.png)

