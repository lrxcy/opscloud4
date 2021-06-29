package com.baiyi.caesar.sshserver.util;

import com.google.common.base.Joiner;

/**
 * @Author baiyi
 * @Date 2021/6/8 4:31 下午
 * @Version 1.0
 */
public class ServerTableUtil {

    public static final String DIVIDING_LINE = "------+---------------------------------+---------------------------------+------------+--------------------------------+-------------------------------------";

    public static final String TABLE_HEADERS = buildTableHeaders();

    public static String buildTableHeaders() {
        return Joiner.on("").join(String.format(" %-5s|", "ID"),
                String.format(" %-32s|", "Server Name"),
                String.format(" %-32s|", "ServerGroup Name"),
                String.format(" %-11s|", "Env"),
                String.format(" %-31s|", "IP"),
                String.format(" %-4s", "Account"));
    }

    public static String buildPagination(long totalNum,int page, int length) {
        int tp = 0;
        try {
            tp = (int) (totalNum - 1) / length + 1;
        } catch (Exception e) {
        }
        return Joiner.on(" ,").join("页码: " + page, "分页长度: " + length, "总页数: " + tp, "总数量: " + totalNum, "翻页< 上一页: b 下一页: n >");
    }

}

