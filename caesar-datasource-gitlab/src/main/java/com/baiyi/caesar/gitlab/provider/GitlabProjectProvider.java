package com.baiyi.caesar.gitlab.provider;

import com.baiyi.caesar.common.annotation.SingleTask;
import com.baiyi.caesar.common.datasource.GitlabDsInstanceConfig;
import com.baiyi.caesar.common.datasource.config.DsGitlabConfig;
import com.baiyi.caesar.common.type.DsAssetTypeEnum;
import com.baiyi.caesar.common.type.DsTypeEnum;
import com.baiyi.caesar.datasource.asset.AbstractAssetRelationProvider;
import com.baiyi.caesar.datasource.builder.AssetContainer;
import com.baiyi.caesar.datasource.factory.AssetProviderFactory;
import com.baiyi.caesar.datasource.util.AssetUtil;
import com.baiyi.caesar.domain.generator.caesar.DatasourceConfig;
import com.baiyi.caesar.domain.generator.caesar.DatasourceInstance;
import com.baiyi.caesar.domain.generator.caesar.DatasourceInstanceAsset;
import com.baiyi.caesar.gitlab.convert.GitlabAssetConvert;
import com.baiyi.caesar.gitlab.handler.GitlabProjectHandler;
import org.gitlab.api.models.GitlabGroup;
import org.gitlab.api.models.GitlabProject;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author baiyi
 * @Date 2021/6/21 6:40 下午
 * @Version 1.0
 */
@Component
public class GitlabProjectProvider extends AbstractAssetRelationProvider<GitlabProject, GitlabGroup> {

    @Resource
    private GitlabProjectProvider gitlabProjectProvider;

    @Override
    public String getInstanceType() {
        return DsTypeEnum.GITLAB.name();
    }

    private DsGitlabConfig.Gitlab buildConfig(DatasourceConfig dsConfig) {
        return dsFactory.build(dsConfig, GitlabDsInstanceConfig.class).getGitlab();
    }

    @Override
    protected List<GitlabProject> listEntries(DatasourceConfig dsConfig, GitlabGroup target) {
        return GitlabProjectHandler.queryGroupProjects(buildConfig(dsConfig), target.getId());
    }

    @Override
    protected List<GitlabProject> listEntries(DatasourceConfig dsConfig) {
        return GitlabProjectHandler.queryProjects(buildConfig(dsConfig));
    }

    @Override
    @SingleTask(name = "PullGitlabProject", lockTime = 300)
    public void pullAsset(int dsInstanceId) {
        doPull(dsInstanceId);
    }

    @Override
    public String getAssetType() {
        return DsAssetTypeEnum.GITLAB_PROJECT.getType();
    }

    @Override
    public String getTargetAssetKey() {
        return DsAssetTypeEnum.GITLAB_GROUP.getType();
    }

    @Override
    protected boolean equals(DatasourceInstanceAsset asset, DatasourceInstanceAsset preAsset) {
        if (!AssetUtil.equals(preAsset.getAssetKey2(), asset.getAssetKey2()))
            return false;
        if (!AssetUtil.equals(preAsset.getName(), asset.getName()))
            return false;
        return true;
    }

    @Override
    protected AssetContainer toAssetContainer(DatasourceInstance dsInstance, GitlabProject entry) {
        return GitlabAssetConvert.toAssetContainer(dsInstance, entry);
    }

    @Override
    public void afterPropertiesSet() {
        AssetProviderFactory.register(gitlabProjectProvider);
    }
}