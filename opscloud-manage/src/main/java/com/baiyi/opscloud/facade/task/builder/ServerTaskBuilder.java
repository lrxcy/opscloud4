package com.baiyi.opscloud.facade.task.builder;

import com.baiyi.opscloud.common.util.IdUtil;
import com.baiyi.opscloud.common.util.SessionUtil;
import com.baiyi.opscloud.domain.generator.opscloud.ServerTask;
import com.baiyi.opscloud.domain.param.task.ServerTaskParam;

import java.util.Date;

/**
 * @Author baiyi
 * @Date 2021/9/22 9:45 上午
 * @Version 1.0
 */
public class ServerTaskBuilder {

    public static ServerTask newBuilder(ServerTaskParam.SubmitServerTask submitServerTask) {
        return ServerTask.builder()
                .instanceUuid(submitServerTask.getInstanceUuid())
                .taskUuid(IdUtil.buildUUID())
                .ansiblePlaybookId(submitServerTask.getAnsiblePlaybookId())
                .username(SessionUtil.getUsername())
                .memberSize(submitServerTask.getServers().size())
                .taskType(submitServerTask.getTaskType())
                .vars(submitServerTask.getVars())
                .tags(submitServerTask.getTags())
                .stopType(0)
                .finalized(false)
                .startTime(new Date())
                .build();
    }
}
