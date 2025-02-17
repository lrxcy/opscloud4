package com.baiyi.opscloud.event.customer.impl;

import com.baiyi.opscloud.common.base.Global;
import com.baiyi.opscloud.common.topic.TopicHelper;
import com.baiyi.opscloud.domain.types.EventActionTypeEnum;
import com.baiyi.opscloud.event.IEvent;
import com.baiyi.opscloud.event.NoticeEvent;
import com.baiyi.opscloud.event.customer.EventConsumerFactory;
import com.baiyi.opscloud.event.customer.IEventConsumer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.scheduling.annotation.Async;

import javax.annotation.Resource;

/**
 * @Author baiyi
 * @Date 2021/8/17 7:02 下午
 * @Version 1.0
 */
public abstract class AbstractEventConsumer<T> implements IEventConsumer, InitializingBean {

    @Resource
    protected TopicHelper topicHelper;

    @Override
    @Async(value = Global.TaskPools.DEFAULT)
    public void onMessage(NoticeEvent noticeEvent) {
        preEventHandle(noticeEvent); // 预处理
        String action = noticeEvent.getMessage().getAction();
        messageRoute(noticeEvent, action);
        postEventHandle(noticeEvent); // 后处理
    }

    private void messageRoute(NoticeEvent noticeEvent, String action) {
        if (EventActionTypeEnum.CREATE.name().equals(action)) {
            onCreateMessage(noticeEvent);
            return;
        }
        if (EventActionTypeEnum.UPDATE.name().equals(action)) {
            onUpdateMessage(noticeEvent);
            return;
        }
        if (EventActionTypeEnum.DELETE.name().equals(action)) {
            onDeleteMessage(noticeEvent);
        }
    }

    protected void onCreateMessage(NoticeEvent noticeEvent) {
    }

    protected void onUpdateMessage(NoticeEvent noticeEvent) {
    }

    protected void onDeleteMessage(NoticeEvent noticeEvent) {
    }

    /**
     * 预处理
     *
     * @param noticeEvent
     */
    protected void preEventHandle(NoticeEvent noticeEvent) {
    }

    /**
     * 后处理
     *
     * @param noticeEvent
     */
    protected void postEventHandle(NoticeEvent noticeEvent) {
    }

    protected T toEventData(IEvent<T> event) {
        return event.getBody();
    }

    @Override
    public void afterPropertiesSet() {
        EventConsumerFactory.register(this);
    }
}
