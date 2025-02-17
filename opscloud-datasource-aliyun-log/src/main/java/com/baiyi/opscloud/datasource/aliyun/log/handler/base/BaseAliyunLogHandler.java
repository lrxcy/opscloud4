package com.baiyi.opscloud.datasource.aliyun.log.handler.base;

import com.aliyun.openservices.log.Client;
import com.baiyi.opscloud.common.datasource.config.DsAliyunConfig;
import org.apache.commons.lang3.StringUtils;

/**
 * @Author baiyi
 * @Date 2021/9/16 4:51 下午
 * @Version 1.0
 */
public abstract class BaseAliyunLogHandler {

    protected static final int QUERY_SIZE = 100;

    protected static final String ALIYUN_LOG_ENDPOINT = "${regionId}.log.aliyuncs.com";

    /**
     * 未配置 regionId 则使用杭州区
     * @param aliyun
     * @return
     */
    protected Client buildClient(DsAliyunConfig.Aliyun aliyun) {
        String regionId = aliyun.getRegionId();
        if (StringUtils.isEmpty(regionId))
            regionId = "cn-hangzhou";
        String endpoint = ALIYUN_LOG_ENDPOINT.replace("${regionId}", regionId);
        return new Client(endpoint, aliyun.getAccount().getAccessKeyId(), aliyun.getAccount().getSecret());
    }
}
