package com.baiyi.opscloud.jenkins.provider;

import com.baiyi.opscloud.common.annotation.SingleTask;
import com.baiyi.opscloud.common.datasource.JenkinsDsInstanceConfig;
import com.baiyi.opscloud.common.datasource.config.DsJenkinsConfig;
import com.baiyi.opscloud.domain.types.DsAssetTypeEnum;
import com.baiyi.opscloud.common.type.DsTypeEnum;
import com.baiyi.opscloud.domain.builder.asset.AssetContainer;
import com.baiyi.opscloud.datasource.factory.AssetProviderFactory;
import com.baiyi.opscloud.datasource.model.DsInstanceContext;
import com.baiyi.opscloud.datasource.provider.asset.BaseAssetProvider;
import com.baiyi.opscloud.datasource.util.AssetUtil;
import com.baiyi.opscloud.domain.generator.opscloud.DatasourceConfig;
import com.baiyi.opscloud.domain.generator.opscloud.DatasourceInstance;
import com.baiyi.opscloud.domain.generator.opscloud.DatasourceInstanceAsset;
import com.baiyi.opscloud.jenkins.convert.ComputerAssetConvert;
import com.baiyi.opscloud.jenkins.handler.JenkinsServerHandler;
import com.google.common.collect.Lists;
import com.offbytwo.jenkins.model.Computer;
import com.offbytwo.jenkins.model.ComputerWithDetails;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @Author baiyi
 * @Date 2021/7/2 9:59 上午
 * @Version 1.0
 */
@Component
public class JenkinsComputerProvider extends BaseAssetProvider<ComputerWithDetails> {

    @Resource
    private JenkinsComputerProvider jenkinsComputerProvider;

    @Override
    public String getInstanceType() {
        return DsTypeEnum.JENKINS.name();
    }

    @Override
    public String getAssetType() {
        return DsAssetTypeEnum.JENKINS_COMPUTER.getType();
    }

    private DsJenkinsConfig.Jenkins buildConfig(DatasourceConfig dsConfig) {
        return dsConfigFactory.build(dsConfig, JenkinsDsInstanceConfig.class).getJenkins();
    }

    @Override
    protected List<ComputerWithDetails> listEntries(DsInstanceContext dsInstanceContext) {
        try {
            Map<String, Computer> computerMap = JenkinsServerHandler.getComputers(buildConfig(dsInstanceContext.getDsConfig()));
            List<ComputerWithDetails> computerWithDetails = Lists.newArrayList();
            for (String k : computerMap.keySet())
                computerWithDetails.add(computerMap.get(k).details());
            return computerWithDetails;
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new RuntimeException("查询条目失败");
    }

    @Override
    @SingleTask(name = "PullJenkinsComputer", lockTime = "5m")
    public void pullAsset(int dsInstanceId) {
        doPull(dsInstanceId);
    }

    @Override
    protected boolean equals(DatasourceInstanceAsset asset, DatasourceInstanceAsset preAsset) {
        if (!AssetUtil.equals(preAsset.getName(), asset.getName()))
            return false;
        if (preAsset.getIsActive() != asset.getIsActive())
            return false;
        return true;
    }

    @Override
    protected AssetContainer toAssetContainer(DatasourceInstance dsInstance, ComputerWithDetails entry) {
        return ComputerAssetConvert.toAssetContainer(dsInstance, entry);
    }

    @Override
    public void afterPropertiesSet() {
        AssetProviderFactory.register(jenkinsComputerProvider);
    }
}
