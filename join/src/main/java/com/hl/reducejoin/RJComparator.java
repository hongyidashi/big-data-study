package com.hl.reducejoin;

import com.hl.bean.OrderBean;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;

/**
 * 描述: 自定义分组
 * 作者: panhongtong
 * 创建时间: 2020-06-25 23:05
 **/
public class RJComparator extends WritableComparator {

    public RJComparator() {
        super(OrderBean.class,true);
    }

    /**
     * 同一个pid的数据会被分为同一组
     */
    @Override
    public int compare(WritableComparable a, WritableComparable b) {
        OrderBean oa = (OrderBean) a;
        OrderBean ob = (OrderBean) b;
        return oa.getPid().compareTo(ob.getPid());
    }
}
