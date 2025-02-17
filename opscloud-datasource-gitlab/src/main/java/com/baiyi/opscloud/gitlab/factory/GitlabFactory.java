package com.baiyi.opscloud.gitlab.factory;

import com.baiyi.opscloud.common.datasource.config.DsGitlabConfig;
import org.gitlab.api.GitlabAPI;

/**
 * @Author baiyi
 * @Date 2021/6/21 4:44 下午
 * @Version 1.0
 */
public class GitlabFactory {

    public static GitlabAPI buildGitlabAPI(DsGitlabConfig.Gitlab gitlab) {
        return GitlabAPI.connect(gitlab.getUrl(), gitlab.getToken());
    }

}
