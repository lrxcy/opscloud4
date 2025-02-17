package com.baiyi.opscloud.datasource.zabbix;

import com.alibaba.fastjson.JSON;
import com.baiyi.opscloud.datasource.zabbix.base.BaseZabbixTest;
import com.baiyi.opscloud.zabbix.entry.ZabbixTemplate;
import com.baiyi.opscloud.zabbix.handler.ZabbixTemplateHandler;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import javax.annotation.Resource;

/**
 * @Author baiyi
 * @Date 2021/8/23 2:56 下午
 * @Version 1.0
 */
@Slf4j
public class ZabbixTemplateTest extends BaseZabbixTest {

    @Resource
    private ZabbixTemplateHandler zabbixTemplateHandler;

    @Test
    void getTemplateByName(){
        ZabbixTemplate zabbixTemplate = zabbixTemplateHandler.getByName(getConfig().getZabbix(),"Template Module Zabbix agent active");
        log.info(JSON.toJSONString(zabbixTemplate));
    }

    @Test
    void getTemplateById(){
        ZabbixTemplate zabbixTemplate = zabbixTemplateHandler.getById(getConfig().getZabbix(),"10292");
        log.info(JSON.toJSONString(zabbixTemplate));
    }
}
