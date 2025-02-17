package com.baiyi.opscloud.sshcore.enums;

import lombok.Getter;

/**
 * @Author baiyi
 * @Date 2021/7/14 5:22 下午
 * @Version 1.0
 */
@Getter
public enum SessionTypeEnum {

    WEB_TERMINAL("WEB_TERMINAL"),
    SSH_SERVER("SSH_SERVER"),
    KUBERNETES_TERMINAL("KUBERNETES_TERMINAL");

    private String type;

    SessionTypeEnum(String type) {
        this.type = type;
    }

}
