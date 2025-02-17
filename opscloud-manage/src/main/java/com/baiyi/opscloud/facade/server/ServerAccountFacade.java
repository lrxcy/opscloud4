package com.baiyi.opscloud.facade.server;

import com.baiyi.opscloud.domain.DataTable;
import com.baiyi.opscloud.domain.param.server.ServerAccountParam;
import com.baiyi.opscloud.domain.vo.server.ServerAccountVO;

/**
 * @Author baiyi
 * @Date 2021/5/25 11:26 上午
 * @Version 1.0
 */
public interface ServerAccountFacade {

    DataTable<ServerAccountVO.Account> queryServerAccountPage(ServerAccountParam.ServerAccountPageQuery pageQuery);

    void addServerAccount(ServerAccountVO.Account account);

    void updateServerAccount(ServerAccountVO.Account account);

    void updateServerAccountPermission(ServerAccountParam.UpdateServerAccountPermission updatePermission);
}
