package com.baiyi.opscloud.datasource.manager;

import com.baiyi.opscloud.common.type.DsTypeEnum;
import com.baiyi.opscloud.datasource.manager.base.BaseManager;
import com.baiyi.opscloud.datasource.manager.base.IManager;
import com.baiyi.opscloud.datasource.serverGroup.factory.ServerGroupProviderFactory;
import com.baiyi.opscloud.domain.generator.opscloud.DatasourceInstance;
import com.baiyi.opscloud.domain.generator.opscloud.ServerGroup;
import com.baiyi.opscloud.domain.generator.opscloud.User;
import com.baiyi.opscloud.domain.vo.business.BaseBusiness;
import com.github.xiaoymin.knife4j.core.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author baiyi
 * @Date 2021/8/24 11:24 上午
 * @Version 1.0
 */
@Slf4j
@Component
public class DsServerGroupManager extends BaseManager implements IManager<ServerGroup> {

    private static final String SERVER_TAG = "Server";

    /**
     * 过滤实例类型
     */
    private static final DsTypeEnum[] filterInstanceTypes = {DsTypeEnum.ZABBIX};

    @Override
    protected DsTypeEnum[] getFilterInstanceTypes() {
        return filterInstanceTypes;
    }

    @Override
    protected String getTag() {
        return SERVER_TAG;
    }

    @Override
    public void create(ServerGroup serverGroup) {
        List<DatasourceInstance> instances = listInstance();
        if (CollectionUtils.isEmpty(instances)) {
            log.info("{} 数据源服务器组管理: 无可用实例", this.getClass().getSimpleName());
            return;
        }
        instances.forEach(e -> ServerGroupProviderFactory.getIServerGroupByInstanceType(e.getInstanceType()).create(e, serverGroup));
    }

    @Override
    public void update(ServerGroup serverGroup) {
        List<DatasourceInstance> instances = listInstance();
        if (CollectionUtils.isEmpty(instances)) {
            log.info("{} 数据源服务器组管理: 无可用实例", this.getClass().getSimpleName());
            return;
        }
        instances.forEach(e -> ServerGroupProviderFactory.getIServerGroupByInstanceType(e.getInstanceType()).update(e, serverGroup));
    }

    @Override
    public void delete(ServerGroup serverGroup) {
        // TODO
    }

    @Override
    public void grant(User user, BaseBusiness.IBusiness businessResource) {
        List<DatasourceInstance> instances = listInstance();
        if (CollectionUtils.isEmpty(instances)) {
            log.info("{} 数据源服务器组管理: 无可用实例", this.getClass().getSimpleName());
            return;
        }
        instances.forEach(e -> ServerGroupProviderFactory.getIServerGroupByInstanceType(e.getInstanceType()).grant(e, user, businessResource));
    }

    @Override
    public void revoke(User user, BaseBusiness.IBusiness businessResource) {
        List<DatasourceInstance> instances = listInstance();
        if (CollectionUtils.isEmpty(instances)) {
            log.info("{} 数据源服务器组管理: 无可用实例", this.getClass().getSimpleName());
            return;
        }
        instances.forEach(e -> ServerGroupProviderFactory.getIServerGroupByInstanceType(e.getInstanceType()).revoke(e, user, businessResource));
    }

}
