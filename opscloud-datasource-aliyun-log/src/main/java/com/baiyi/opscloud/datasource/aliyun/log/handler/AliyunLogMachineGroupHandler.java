package com.baiyi.opscloud.datasource.aliyun.log.handler;

import com.aliyun.openservices.log.Client;
import com.aliyun.openservices.log.common.MachineGroup;
import com.aliyun.openservices.log.exception.LogException;
import com.aliyun.openservices.log.request.*;
import com.baiyi.opscloud.common.datasource.config.DsAliyunConfig;
import com.baiyi.opscloud.datasource.aliyun.log.handler.base.BaseAliyunLogHandler;
import com.baiyi.opscloud.domain.vo.datasource.aliyun.AliyunLogMemberVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author baiyi
 * @Date 2020/6/13 11:47 上午
 * @Version 1.0
 */
@Slf4j
@Component
public class AliyunLogMachineGroupHandler extends BaseAliyunLogHandler {

    public static final String MACHINE_IDENTIFY_TYPE = "ip";

    public MachineGroup getMachineGroup(DsAliyunConfig.Aliyun aliyun, String project, String groupName) {
        GetMachineGroupRequest req = new GetMachineGroupRequest(project, groupName);
        try {
            Client client = buildClient(aliyun);
            return client.GetMachineGroup(req).GetMachineGroup();
        } catch (LogException lg) {
            log.error("阿里云日志服务查询MachineGroup错误! , {}", lg.GetErrorMessage());
        }
        return null;
    }

    public List<String> getMachineGroups(DsAliyunConfig.Aliyun aliyun, String project, String groupName) {
        int offset = 0;
        ListMachineGroupRequest req = new ListMachineGroupRequest(project, groupName, offset, QUERY_SIZE);
        List<String> machineGroups = new ArrayList<>();
        try {
            Client client = buildClient(aliyun);
            machineGroups = client.ListMachineGroup(req).GetMachineGroups();
        } catch (LogException lg) {
            log.error("阿里云日志服务查询MachineGroup错误! , {}", lg.GetErrorMessage());
        }
        return machineGroups;
    }

    public void updateMachineGroup(DsAliyunConfig.Aliyun aliyun,AliyunLogMemberVO.LogMember logMember) {
        MachineGroup machineGroup = new MachineGroup(logMember.getServerGroupName(), MACHINE_IDENTIFY_TYPE, logMember.getMachineList());
        machineGroup.SetGroupTopic(StringUtils.isEmpty(logMember.getTopic()) ? logMember.getServerGroupName() : logMember.getTopic());
        try {
            Client client = buildClient(aliyun);
            UpdateMachineGroupRequest req = new UpdateMachineGroupRequest(logMember.getLog().getProject(), machineGroup);
            client.UpdateMachineGroup(req);
        } catch (LogException lg) {
            log.error("阿里云日志服务更新MachineGroup错误! , {}", lg.GetErrorMessage());
        }
    }

    public void createMachineGroup(DsAliyunConfig.Aliyun aliyun, AliyunLogMemberVO.LogMember logMember) {
        MachineGroup machineGroup = new MachineGroup(logMember.getServerGroupName(), MACHINE_IDENTIFY_TYPE, logMember.getMachineList());
        machineGroup.SetGroupTopic(StringUtils.isEmpty(logMember.getTopic()) ? logMember.getServerGroupName() : logMember.getTopic());
        try {
            Client client = buildClient(aliyun);
            CreateMachineGroupRequest req = new CreateMachineGroupRequest(logMember.getLog().getProject(), machineGroup);
            client.CreateMachineGroup(req);
        } catch (LogException lg) {
            log.error("阿里云日志服务创建MachineGroup错误! , {}", lg.GetErrorMessage());
        }
    }

    /**
     * 将配置应用到机器组
     *
     * @param logMember
     * @return
     */
    private void applyConfigToMachineGroup(DsAliyunConfig.Aliyun aliyun, AliyunLogMemberVO.LogMember logMember) {
        ApplyConfigToMachineGroupRequest req = new ApplyConfigToMachineGroupRequest(logMember.getLog().getProject(), logMember.getServerGroup().getName(), logMember.getLog().getConfig());
        try {
            Client client = buildClient(aliyun);
            client.ApplyConfigToMachineGroup(req);
        } catch (LogException lg) {
            lg.printStackTrace();
        }
    }

}
