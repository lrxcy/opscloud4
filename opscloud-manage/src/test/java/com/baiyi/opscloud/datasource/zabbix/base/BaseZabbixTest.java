package com.baiyi.opscloud.datasource.zabbix.base;

import com.baiyi.opscloud.BaseUnit;
import com.baiyi.opscloud.common.datasource.ZabbixDsInstanceConfig;
import com.baiyi.opscloud.datasource.factory.DsConfigFactory;
import com.baiyi.opscloud.domain.generator.opscloud.DatasourceConfig;
import com.baiyi.opscloud.service.datasource.DsConfigService;

import javax.annotation.Resource;

/**
 * @Author baiyi
 * @Date 2021/8/23 2:57 下午
 * @Version 1.0
 */
public class BaseZabbixTest extends BaseUnit {


    @Resource
    private DsConfigService dsConfigService;

    @Resource
    private   DsConfigFactory dsFactory;

    protected ZabbixDsInstanceConfig getConfig() {
        DatasourceConfig datasourceConfig = dsConfigService.getById(4);
        return dsFactory.build(datasourceConfig, ZabbixDsInstanceConfig.class);
    }

}
