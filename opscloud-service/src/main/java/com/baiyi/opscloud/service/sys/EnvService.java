package com.baiyi.opscloud.service.sys;

import com.baiyi.opscloud.domain.DataTable;
import com.baiyi.opscloud.domain.generator.opscloud.Env;
import com.baiyi.opscloud.domain.param.sys.EnvParam;

/**
 * @Author baiyi
 * @Date 2021/5/25 4:34 下午
 * @Version 1.0
 */
public interface EnvService {

    void add(Env env);

    void update(Env env);

    DataTable<Env> queryPageByParam(EnvParam.EnvPageQuery pageQuery);

    Env getByEnvType(Integer envType);
}
