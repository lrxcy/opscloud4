package com.baiyi.opscloud.datasource.provider.base.common;

import com.baiyi.opscloud.datasource.model.DsInstanceContext;
import com.baiyi.opscloud.domain.generator.opscloud.Credential;
import com.baiyi.opscloud.domain.generator.opscloud.DatasourceInstance;
import com.baiyi.opscloud.service.datasource.DsConfigService;
import com.baiyi.opscloud.service.datasource.DsInstanceService;
import com.baiyi.opscloud.service.sys.CredentialService;

import javax.annotation.Resource;

/**
 * @Author baiyi
 * @Date 2021/6/23 11:11 上午
 * @Version 1.0
 */
public abstract class SimpleDsInstanceProvider {

    @Resource
    private DsInstanceService dsInstanceService;

    @Resource
    private DsConfigService dsConfigService;

    @Resource
    private CredentialService credentialService;

    protected DsInstanceContext buildDsInstanceContext(String dsInstanceUuid) {
        DatasourceInstance dsInstance = dsInstanceService.getByUuid(dsInstanceUuid);
        return buildDsInstanceContext(dsInstance);
    }

    protected DsInstanceContext buildDsInstanceContext(int dsInstanceId) {
        DatasourceInstance dsInstance = dsInstanceService.getById(dsInstanceId);
        return buildDsInstanceContext(dsInstance);
    }

    protected DsInstanceContext buildDsInstanceContext(DatasourceInstance dsInstance) {
        return DsInstanceContext.builder()
                .dsInstance(dsInstance)
                .dsConfig(dsConfigService.getById(dsInstance.getConfigId()))
                .build();
    }

    protected Credential getCredential(int credentialId) {
        return credentialService.getById(credentialId);
    }

}
