package com.baiyi.opscloud.ansible.play;

import com.baiyi.opscloud.ansible.play.message.base.BasePlayMessage;
import org.springframework.beans.factory.InitializingBean;

/**
 * @Author baiyi
 * @Date 2021/9/26 5:26 下午
 * @Version 1.0
 */
public abstract class AbstractTaskPlayProcess<T extends BasePlayMessage> implements ITaskPlayProcess, InitializingBean {

    abstract protected T getMessage(String message);

    /**
     * 注册
     */
    @Override
    public void afterPropertiesSet() {
        ServerTaskPlayFactory.register(this);
    }
}

