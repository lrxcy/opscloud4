package com.baiyi.opscloud.facade.task.impl;

import com.baiyi.opscloud.ansible.args.PlaybookArgs;
import com.baiyi.opscloud.ansible.builder.AnsiblePlaybookArgsBuilder;
import com.baiyi.opscloud.ansible.recorder.TaskLogStorehouse;
import com.baiyi.opscloud.ansible.task.AnsibleServerTask;
import com.baiyi.opscloud.ansible.util.AnsibleUtil;
import com.baiyi.opscloud.common.base.ServerTaskStatusEnum;
import com.baiyi.opscloud.common.datasource.AnsibleDsInstanceConfig;
import com.baiyi.opscloud.common.datasource.config.DsAnsibleConfig;
import com.baiyi.opscloud.common.exception.common.CommonRuntimeException;
import com.baiyi.opscloud.common.util.TimeUtil;
import com.baiyi.opscloud.datasource.factory.DsConfigFactory;
import com.baiyi.opscloud.datasource.model.DsInstanceContext;
import com.baiyi.opscloud.datasource.provider.base.common.SimpleDsInstanceProvider;
import com.baiyi.opscloud.datasource.util.SystemEnvUtil;
import com.baiyi.opscloud.domain.DataTable;
import com.baiyi.opscloud.domain.generator.opscloud.AnsiblePlaybook;
import com.baiyi.opscloud.domain.generator.opscloud.ServerTask;
import com.baiyi.opscloud.domain.generator.opscloud.ServerTaskMember;
import com.baiyi.opscloud.domain.param.task.ServerTaskParam;
import com.baiyi.opscloud.domain.vo.server.ServerVO;
import com.baiyi.opscloud.domain.vo.task.ServerTaskVO;
import com.baiyi.opscloud.facade.task.ServerTaskFacade;
import com.baiyi.opscloud.facade.task.builder.ServerTaskBuilder;
import com.baiyi.opscloud.facade.task.builder.ServerTaskMemberBuilder;
import com.baiyi.opscloud.packer.task.ServerTaskPacker;
import com.baiyi.opscloud.service.ansible.AnsiblePlaybookService;
import com.baiyi.opscloud.service.task.ServerTaskMemberService;
import com.baiyi.opscloud.service.task.ServerTaskService;
import com.baiyi.opscloud.util.PlaybookUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * @Author baiyi
 * @Date 2021/9/18 3:21 下午
 * @Version 1.0
 */
@Slf4j
@Component
public class ServerTaskFacadeImpl extends SimpleDsInstanceProvider implements ServerTaskFacade {

    @Resource
    private ServerTaskService serverTaskService;

    @Resource
    private ServerTaskMemberService serverTaskMemberService;

    @Resource
    private TaskLogStorehouse taskLogStorehouse;

    @Resource
    private AnsiblePlaybookService ansiblePlaybookService;

    @Resource
    private DsConfigFactory dsConfigFactory;

    @Resource
    private ServerTaskPacker serverTaskPacker;

    private static final int MAX_EXECUTING = 20;

    @Override
    public DataTable<ServerTaskVO.ServerTask> queryServerTaskPage(ServerTaskParam.ServerTaskPageQuery pageQuery) {
        DataTable<ServerTask> table = serverTaskService.queryServerTaskPage(pageQuery);
        return new DataTable<>(table.getData().stream().map(e -> serverTaskPacker.wrapToVO(e, pageQuery)).collect(Collectors.toList())
                , table.getTotalNum());
    }

    @Override
    public void submitServerTask(ServerTaskParam.SubmitServerTask submitServerTask) {
        ServerTask serverTask = ServerTaskBuilder.newBuilder(submitServerTask);
        serverTaskService.add(serverTask);
        List<ServerTaskMember> members = record(serverTask, submitServerTask.getServers());
        executeServerTask(serverTask, members);
    }

    /**
     * 录入服务器列表信息
     *
     * @param serverTask
     * @param servers
     */
    private List<ServerTaskMember> record(ServerTask serverTask, List<ServerVO.Server> servers) {
        if (CollectionUtils.isEmpty(servers)) throw new CommonRuntimeException("服务器列表为空！");
        List<ServerTaskMember> members = Lists.newArrayList();
        servers.forEach(server -> {
            ServerTaskMember member = ServerTaskMemberBuilder.newBuilder(serverTask, server);
            member.setOutputMsg(taskLogStorehouse.buildOutputLogPath(serverTask.getTaskUuid(), member));
            member.setErrorMsg(taskLogStorehouse.buildErrorLogPath(serverTask.getTaskUuid(),  member));
            serverTaskMemberService.add(member);
            members.add(member);
        });
        return members;
    }

    private void executeServerTask(ServerTask serverTask, List<ServerTaskMember> members) {
        // 构建上下文
        DsInstanceContext instanceContext = buildDsInstanceContext(serverTask.getInstanceUuid());
        DsAnsibleConfig.Ansible ansible = dsConfigFactory.build(instanceContext.getDsConfig(), AnsibleDsInstanceConfig.class).getAnsible();
        AnsiblePlaybook ansiblePlaybook = ansiblePlaybookService.getById(serverTask.getAnsiblePlaybookId());

        PlaybookArgs args = PlaybookArgs.builder()
                .extraVars(AnsibleUtil.toVars(serverTask.getVars()).getVars())
                .keyFile(SystemEnvUtil.renderEnvHome(ansible.getPrivateKey()))
                .playbook(PlaybookUtil.toPath(ansiblePlaybook))
                .inventory(SystemEnvUtil.renderEnvHome(ansible.getInventoryHost()))
                .build();

        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(MAX_EXECUTING);
        // 使用迭代器遍历并执行所有服务器任务
        Iterator<ServerTaskMember> iter = members.iterator();
        while (iter.hasNext()) {
            // 查询当前执行中的任务是否达到最大并发
            if (serverTaskMemberService.countByTaskStatus(serverTask.getId(), ServerTaskStatusEnum.EXECUTING.name()) < MAX_EXECUTING) {
                ServerTaskMember serverTaskMember = iter.next();
                iter.remove();
                args.setHosts(serverTaskMember.getManageIp());
                CommandLine commandLine = AnsiblePlaybookArgsBuilder.build(ansible, args);
                AnsibleServerTask ansibleServerTask = new AnsibleServerTask(serverTask.getTaskUuid(),
                        serverTaskMember,
                        commandLine,
                        serverTaskMemberService,
                        taskLogStorehouse);
                fixedThreadPool.execute(ansibleServerTask); // 执行任务
            } else {
                try {
                    Thread.sleep(TimeUtil.secondTime * 3); // 延迟
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        }
        traceEndOfTask(serverTask);
    }

    /**
     * 追踪任务直到完成
     *
     * @param serverTask
     */
    private void traceEndOfTask(ServerTask serverTask) {
        while (true) {
            if (serverTask.getMemberSize() == serverTaskMemberService.countByFinalized(serverTask.getId(), true)) {
                serverTask.setFinalized(true);
                serverTask.setEndTime(new Date());
                serverTaskService.update(serverTask);
                // 任务完成
                return;
            } else {
                try {
                    Thread.sleep(TimeUtil.secondTime); // 延迟
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        }
    }


}
