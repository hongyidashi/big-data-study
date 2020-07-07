package com.hl.video.utils;

/**
 * 描述: 数据清洗工具类
 * 作者: panhongtong
 * 创建时间: 2020-07-06 18:00
 **/
public class ETLUtil {

    /**
     * 1、去掉字段数小于9的行
     * 2、去掉类别间的空格
     * 3、将相关视频ID的分隔符由 "\t" 修改为 "&"
     * @param oriStr 输入的原始数据
     * @return 过滤后的数据
     */
    public static String etlStr(String oriStr) {
        StringBuffer buffer = new StringBuffer();
        // 切割
        String[] fiels = oriStr.split("\t");

        // 去掉字段数小于9的行
        if (fiels.length < 9) {
            return null;
        }

        // 去掉类别间的空格
        fiels[3] = fiels[3].replaceAll(" ", "");

        for (int i = 0; i < fiels.length; i++) {
            // 处理非相关视频ID字段
            if (i < 9) {
                if (i == fiels.length - 1) {
                    buffer.append(fiels[i]);
                } else {
                    buffer.append(fiels[i]).append("\t");
                }
            } else {
                // 处理相关视频ID字段
                if (i == fiels.length - 1) {
                    buffer.append(fiels[i]);
                } else {
                    buffer.append(fiels[i]).append("&");
                }
            }
        }
        return buffer.toString();
    }

}
