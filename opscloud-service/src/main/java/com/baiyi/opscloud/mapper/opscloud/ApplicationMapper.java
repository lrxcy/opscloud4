package com.baiyi.opscloud.mapper.opscloud;

import com.baiyi.opscloud.domain.generator.opscloud.Application;
import com.baiyi.opscloud.domain.param.application.ApplicationParam;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface ApplicationMapper extends Mapper<Application> {

    List<Application> queryUserPermissionApplicationByParam(ApplicationParam.UserPermissionApplicationPageQuery pageQuery);
}