package com.baiyi.opscloud.datasource.aliyun;

import com.aliyun.openservices.log.common.Project;
import com.baiyi.opscloud.BaseUnit;
import com.baiyi.opscloud.common.datasource.AliyunDsInstanceConfig;
import com.baiyi.opscloud.common.datasource.base.BaseDsInstanceConfig;
import com.baiyi.opscloud.datasource.aliyun.log.handler.AliyunLogHandler;
import com.baiyi.opscloud.datasource.factory.DsConfigFactory;
import com.baiyi.opscloud.domain.generator.opscloud.DatasourceConfig;
import com.baiyi.opscloud.service.datasource.DsConfigService;
import org.junit.jupiter.api.Test;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author baiyi
 * @Date 2021/9/16 4:39 下午
 * @Version 1.0
 */
public class AliyunLogTest extends BaseUnit {

    @Resource
    private DsConfigService dsConfigService;

    @Resource
    private DsConfigFactory dsFactory;

    @Resource
    private AliyunLogHandler aliyunLogHandler;

    @Test
    void listProjectTest() {
        AliyunDsInstanceConfig aliyunDsInstanceConfig = (AliyunDsInstanceConfig) getConfig();
        List<Project> projects = aliyunLogHandler.listProject(aliyunDsInstanceConfig.getAliyun(), "");
        System.out.println(projects);
    }

    @Test
    BaseDsInstanceConfig getConfig() {
        DatasourceConfig datasourceConfig = dsConfigService.getById(3);
        return dsFactory.build(datasourceConfig, AliyunDsInstanceConfig.class);
    }
}
