package com.baiyi.opscloud.packer.desensitized;

import com.baiyi.opscloud.domain.annotation.DesensitizedMethod;
import org.springframework.stereotype.Component;

/**
 * @Author <a href="mailto:xiuyuan@xinc818.group">修远</a>
 * @Date 2021/6/11 2:18 下午
 * @Since 1.0
 */

@Component
public class DesensitizedPacker<T> {

    @DesensitizedMethod
    public T desensitized(T source) {
        return source;
    }
}
