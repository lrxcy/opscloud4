package com.baiyi.opscloud.service.auth;

import com.alibaba.fastjson.JSON;
import com.baiyi.opscloud.BaseUnit;
import com.baiyi.opscloud.domain.DataTable;
import com.baiyi.opscloud.domain.generator.opscloud.AuthResource;
import com.baiyi.opscloud.domain.param.auth.AuthResourceParam;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import javax.annotation.Resource;

/**
 * @Author baiyi
 * @Date 2021/5/11 11:31 上午
 * @Version 1.0
 */
@Slf4j
public class AuthResourceServiceTest extends BaseUnit {

    @Resource
    private AuthResourceService authResourceService;

    @Test
    void queryPageTest() {
        AuthResourceParam.AuthResourcePageQuery pageQuery = AuthResourceParam.AuthResourcePageQuery.builder()
                .needAuth(true)
                .resourceName("server")
                .build();
        pageQuery.setPage(1);
        pageQuery.setLength(10);
        DataTable<AuthResource> table = authResourceService.queryPageByParam(pageQuery);
        log.info(JSON.toJSON(table).toString());
    }
}
