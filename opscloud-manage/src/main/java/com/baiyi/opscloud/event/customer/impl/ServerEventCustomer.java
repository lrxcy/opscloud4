package com.baiyi.opscloud.event.customer.impl;

import com.baiyi.opscloud.ansible.ServerGroupingAlgorithm;
import com.baiyi.opscloud.common.topic.TopicHelper;
import com.baiyi.opscloud.datasource.manager.DsServerManager;
import com.baiyi.opscloud.domain.generator.opscloud.Server;
import com.baiyi.opscloud.domain.types.BusinessTypeEnum;
import com.baiyi.opscloud.event.NoticeEvent;
import com.baiyi.opscloud.facade.datasource.aliyun.AliyunLogFacade;
import com.baiyi.opscloud.util.ServerTreeUtil;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Author baiyi
 * @Date 2021/8/17 7:03 下午
 * @Version 1.0
 */
@Component
public class ServerEventCustomer extends AbstractEventConsumer<Server> {

    @Resource
    private ServerTreeUtil serverTreeUtil;

    @Resource
    private ServerGroupingAlgorithm serverGroupingAlgorithm;

    @Resource
    private DsServerManager dsServerManager;

    @Resource
    private AliyunLogFacade aliyunLogFacade;

    @Override
    public String getEventType() {
        return BusinessTypeEnum.SERVER.name();
    }

    @Override
    protected void preEventHandle(NoticeEvent noticeEvent) {
        Server eventData = toEventData(noticeEvent.getMessage());
        serverGroupingAlgorithm.evictGrouping(eventData.getServerGroupId());
        serverGroupingAlgorithm.evictIntactGrouping(eventData.getServerGroupId(),true);
        serverGroupingAlgorithm.evictIntactGrouping(eventData.getServerGroupId(),false);
        serverTreeUtil.evictWrap(eventData.getServerGroupId());
        // 发送Topic 定时任务延迟执行
        topicHelper.send(TopicHelper.Topics.ASSET_SUBSCRIPTION_TASK, 1);
        // 异步任务，推送阿里云日志服务主机组
        aliyunLogFacade.pushLogMemberByServerGroupId(eventData.getServerGroupId());
    }

    @Override
    protected void onCreateMessage(NoticeEvent noticeEvent) {
        Server eventData = toEventData(noticeEvent.getMessage());
        dsServerManager.create(eventData);
    }

    @Override
    protected void onUpdateMessage(NoticeEvent noticeEvent) {
        Server eventData = toEventData(noticeEvent.getMessage());
        dsServerManager.update(eventData);
    }

    @Override
    protected void onDeleteMessage(NoticeEvent noticeEvent) {
        Server eventData = toEventData(noticeEvent.getMessage());
        dsServerManager.delete(eventData);
    }

}
