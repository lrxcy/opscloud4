package com.baiyi.opscloud.zabbix.provider;

import com.baiyi.opscloud.common.annotation.SingleTask;
import com.baiyi.opscloud.common.datasource.ZabbixDsInstanceConfig;
import com.baiyi.opscloud.common.datasource.config.DsZabbixConfig;
import com.baiyi.opscloud.domain.types.DsAssetTypeEnum;
import com.baiyi.opscloud.common.type.DsTypeEnum;
import com.baiyi.opscloud.domain.builder.asset.AssetContainer;
import com.baiyi.opscloud.datasource.factory.AssetProviderFactory;
import com.baiyi.opscloud.datasource.model.DsInstanceContext;
import com.baiyi.opscloud.datasource.provider.asset.AbstractAssetRelationProvider;
import com.baiyi.opscloud.datasource.provider.base.param.UniqueAssetParam;
import com.baiyi.opscloud.datasource.util.AssetUtil;
import com.baiyi.opscloud.domain.generator.opscloud.DatasourceConfig;
import com.baiyi.opscloud.domain.generator.opscloud.DatasourceInstance;
import com.baiyi.opscloud.domain.generator.opscloud.DatasourceInstanceAsset;
import com.baiyi.opscloud.zabbix.convert.ZabbixTriggerAssetConvert;
import com.baiyi.opscloud.zabbix.entry.ZabbixHost;
import com.baiyi.opscloud.zabbix.entry.ZabbixTrigger;
import com.baiyi.opscloud.zabbix.handler.ZabbixTriggerHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author <a href="mailto:xiuyuan@xinc818.group">修远</a>
 * @Date 2021/7/2 3:43 下午
 * @Since 1.0
 */

@Component
public class ZabbixTriggerProvider extends AbstractAssetRelationProvider<ZabbixTrigger, ZabbixHost> {

    @Resource
    private ZabbixTriggerHandler zabbixTriggerHandler;

    @Resource
    private ZabbixTriggerProvider zabbixTriggerProvider;

    @Override
    public String getInstanceType() {
        return DsTypeEnum.ZABBIX.name();
    }

    private DsZabbixConfig.Zabbix buildConfig(DatasourceConfig dsConfig) {
        return dsConfigFactory.build(dsConfig, ZabbixDsInstanceConfig.class).getZabbix();
    }

    @Override
    protected List<ZabbixTrigger> listEntries(DsInstanceContext dsInstanceContext, ZabbixHost target) {
        DsZabbixConfig.Zabbix zabbix = buildConfig(dsInstanceContext.getDsConfig());
        return zabbixTriggerHandler.listTriggerByHost(zabbix, target);
    }

    @Override
    protected List<ZabbixTrigger> listEntries(DsInstanceContext dsInstanceContext) {
        return zabbixTriggerHandler.listTriggers(buildConfig(dsInstanceContext.getDsConfig()));
    }

    @Override
    protected ZabbixTrigger getEntry(DsInstanceContext dsInstanceContext, UniqueAssetParam param) {
        return zabbixTriggerHandler.getTriggerById(buildConfig(dsInstanceContext.getDsConfig()), param.getAssetId());
    }

    @Override
    @SingleTask(name = "PullZabbixTrigger", lockTime = "5m")
    public void pullAsset(int dsInstanceId) {
        doPull(dsInstanceId);
    }

    @Override
    public String getAssetType() {
        return DsAssetTypeEnum.ZABBIX_TRIGGER.getType();
    }

    @Override
    public String getTargetAssetKey() {
        return DsAssetTypeEnum.ZABBIX_HOST.getType();
    }

    @Override
    protected boolean equals(DatasourceInstanceAsset asset, DatasourceInstanceAsset preAsset) {
        if (!AssetUtil.equals(preAsset.getKind(), asset.getKind()))
            return false;
        if (!AssetUtil.equals(preAsset.getName(), asset.getName()))
            return false;
        if (preAsset.getIsActive() != asset.getIsActive())
            return false;
        if (preAsset.getCreatedTime() != asset.getCreatedTime())
            return false;
        return true;
    }

    @Override
    protected AssetContainer toAssetContainer(DatasourceInstance dsInstance, ZabbixTrigger entry) {
        return ZabbixTriggerAssetConvert.toAssetContainer(dsInstance, entry);
    }

    @Override
    public void afterPropertiesSet() {
        AssetProviderFactory.register(zabbixTriggerProvider);
    }
}

