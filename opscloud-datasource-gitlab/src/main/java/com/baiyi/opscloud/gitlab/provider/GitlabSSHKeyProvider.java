package com.baiyi.opscloud.gitlab.provider;

import com.baiyi.opscloud.common.annotation.SingleTask;
import com.baiyi.opscloud.common.datasource.GitlabDsInstanceConfig;
import com.baiyi.opscloud.common.datasource.config.DsGitlabConfig;
import com.baiyi.opscloud.domain.types.DsAssetTypeEnum;
import com.baiyi.opscloud.common.type.DsTypeEnum;
import com.baiyi.opscloud.domain.builder.asset.AssetContainer;
import com.baiyi.opscloud.datasource.factory.AssetProviderFactory;
import com.baiyi.opscloud.datasource.model.DsInstanceContext;
import com.baiyi.opscloud.datasource.provider.asset.AbstractAssetRelationProvider;
import com.baiyi.opscloud.datasource.util.AssetUtil;
import com.baiyi.opscloud.domain.generator.opscloud.DatasourceConfig;
import com.baiyi.opscloud.domain.generator.opscloud.DatasourceInstance;
import com.baiyi.opscloud.domain.generator.opscloud.DatasourceInstanceAsset;
import com.baiyi.opscloud.gitlab.convert.GitlabAssetConvert;
import com.baiyi.opscloud.gitlab.handler.GitlabUserHandler;
import com.google.common.collect.Lists;
import org.gitlab.api.models.GitlabSSHKey;
import org.gitlab.api.models.GitlabUser;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author baiyi
 * @Date 2021/7/2 3:01 下午
 * @Version 1.0
 */
@Component
public class GitlabSSHKeyProvider extends AbstractAssetRelationProvider<GitlabSSHKey, GitlabUser> {

    @Resource
    private GitlabSSHKeyProvider gitlabSSHKeyProvider;

    @Override
    public String getInstanceType() {
        return DsTypeEnum.GITLAB.name();
    }

    private DsGitlabConfig.Gitlab buildConfig(DatasourceConfig dsConfig) {
        return dsConfigFactory.build(dsConfig, GitlabDsInstanceConfig.class).getGitlab();
    }

    @Override
    protected List<GitlabSSHKey> listEntries(DsInstanceContext dsInstanceContext, GitlabUser target) {
        DsGitlabConfig.Gitlab gitlab = buildConfig(dsInstanceContext.getDsConfig());
        try {
            return GitlabUserHandler.getUserSSHKeys(gitlab, target.getId()).stream().peek(e ->
                    e.setUser(target)
            ).collect(Collectors.toList());
        } catch (IOException ignored) {
        }
        return Lists.newArrayList();
    }

    @Override
    protected List<GitlabSSHKey> listEntries(DsInstanceContext dsInstanceContext) {
        DsGitlabConfig.Gitlab gitlab = buildConfig(dsInstanceContext.getDsConfig());
        List<GitlabUser> users = GitlabUserHandler.queryUsers(gitlab);
        List<GitlabSSHKey> keys = Lists.newArrayList();
        if (CollectionUtils.isEmpty(users))
            return keys;
        users.forEach(u -> {
            try {
                keys.addAll(GitlabUserHandler.getUserSSHKeys(gitlab, u.getId()).stream().peek(e ->
                        e.setUser(u)
                ).collect(Collectors.toList()));
            } catch (IOException ignored) {
            }
        });
        return keys;
    }

    @Override
    @SingleTask(name = "PullGitlabSSHKey", lockTime = "5m")
    public void pullAsset(int dsInstanceId) {
        doPull(dsInstanceId);
    }

    @Override
    public String getAssetType() {
        return DsAssetTypeEnum.GITLAB_SSHKEY.getType();
    }

    @Override
    public String getTargetAssetKey() {
        return DsAssetTypeEnum.GITLAB_USER.getType();
    }

    @Override
    protected boolean equals(DatasourceInstanceAsset asset, DatasourceInstanceAsset preAsset) {
        if (!AssetUtil.equals(preAsset.getAssetKey(), asset.getAssetKey()))
            return false;
        if (!AssetUtil.equals(preAsset.getAssetKey2(), asset.getAssetKey2()))
            return false;
        if (!AssetUtil.equals(preAsset.getName(), asset.getName()))
            return false;
        if (!AssetUtil.equals(preAsset.getDescription(), asset.getDescription()))
            return false;
        return true;
    }

    @Override
    protected AssetContainer toAssetContainer(DatasourceInstance dsInstance, GitlabSSHKey entry) {
        return GitlabAssetConvert.toAssetContainer(dsInstance, entry);
    }

    @Override
    public void afterPropertiesSet() {
        AssetProviderFactory.register(gitlabSSHKeyProvider);
    }
}

