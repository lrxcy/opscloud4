package com.baiyi.opscloud.datasource.serverGroup.impl;

import com.baiyi.opscloud.datasource.factory.DsConfigFactory;
import com.baiyi.opscloud.datasource.model.DsInstanceContext;
import com.baiyi.opscloud.datasource.provider.base.common.SimpleDsInstanceProvider;
import com.baiyi.opscloud.datasource.serverGroup.IServerGroup;
import com.baiyi.opscloud.datasource.serverGroup.factory.ServerGroupProviderFactory;
import com.baiyi.opscloud.domain.generator.opscloud.DatasourceConfig;
import com.baiyi.opscloud.domain.generator.opscloud.DatasourceInstance;
import com.baiyi.opscloud.domain.generator.opscloud.ServerGroup;
import com.baiyi.opscloud.domain.generator.opscloud.User;
import com.baiyi.opscloud.domain.vo.business.BaseBusiness;
import org.springframework.beans.factory.InitializingBean;

import javax.annotation.Resource;

/**
 * @Author baiyi
 * @Date 2021/8/24 1:44 下午
 * @Version 1.0
 */
public abstract class AbstractServerGroupProvider extends SimpleDsInstanceProvider implements IServerGroup, InitializingBean {

    @Resource
    protected DsConfigFactory dsConfigFactory;

    protected static ThreadLocal<DsInstanceContext> dsInstanceContext = new ThreadLocal<>();

    protected abstract void initialConfig(DatasourceConfig dsConfig);

    private void pre(DatasourceInstance dsInstance) {
        dsInstanceContext.set(buildDsInstanceContext(dsInstance.getId()));
        initialConfig(dsInstanceContext.get().getDsConfig());
    }

    @Override
    public void create(DatasourceInstance dsInstance, ServerGroup serverGroup) {
        pre(dsInstance);
        doCreate(serverGroup);
    }

    @Override
    public void update(DatasourceInstance dsInstance, ServerGroup serverGroup) {
        pre(dsInstance);
        doUpdate(serverGroup);
    }

    @Override
    public void delete(DatasourceInstance dsInstance, ServerGroup serverGroup) {
        pre(dsInstance);
        doDelete(serverGroup);
    }

    @Override
    public void grant(DatasourceInstance dsInstance, User user, BaseBusiness.IBusiness businessResource) {
        pre(dsInstance);
        doGrant(user, businessResource);
    }

    @Override
    public void revoke(DatasourceInstance dsInstance, User user, BaseBusiness.IBusiness businessResource) {
        pre(dsInstance);
        doRevoke(user, businessResource);
    }

    protected abstract void doCreate(ServerGroup serverGroup);

    protected abstract void doUpdate(ServerGroup serverGroup);

    protected abstract void doDelete(ServerGroup serverGroup);

    protected abstract void doGrant(User user, BaseBusiness.IBusiness businessResource);

    protected abstract void doRevoke(User user, BaseBusiness.IBusiness businessResource);

    protected abstract int getBusinessResourceType();

    @Override
    public void afterPropertiesSet() {
        ServerGroupProviderFactory.register(this);
    }

}