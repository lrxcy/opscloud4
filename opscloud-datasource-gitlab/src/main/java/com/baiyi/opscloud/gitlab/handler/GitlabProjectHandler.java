package com.baiyi.opscloud.gitlab.handler;

import com.baiyi.opscloud.common.datasource.config.DsGitlabConfig;
import com.baiyi.opscloud.gitlab.factory.GitlabFactory;
import org.gitlab.api.GitlabAPI;
import org.gitlab.api.models.GitlabProject;

import java.util.List;

/**
 * @Author baiyi
 * @Date 2021/6/21 6:29 下午
 * @Version 1.0
 */
public class GitlabProjectHandler {

    public static List<GitlabProject> queryProjects(DsGitlabConfig.Gitlab gitlab) {
        return buildAPI(gitlab).getProjects();
    }

    public static List<GitlabProject> queryGroupProjects(DsGitlabConfig.Gitlab gitlab, Integer groupId) {
        return buildAPI(gitlab).getGroupProjects(groupId);
    }

    private static GitlabAPI buildAPI(DsGitlabConfig.Gitlab gitlab) {
        return GitlabFactory.buildGitlabAPI(gitlab);
    }
}
