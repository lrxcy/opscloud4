package com.baiyi.opscloud.datasource.account.impl.base;

import com.baiyi.opscloud.datasource.account.AccountProviderFactory;
import com.baiyi.opscloud.datasource.account.IAccount;
import com.baiyi.opscloud.datasource.factory.DsConfigFactory;
import com.baiyi.opscloud.datasource.model.DsInstanceContext;
import com.baiyi.opscloud.datasource.provider.base.common.SimpleDsInstanceProvider;
import com.baiyi.opscloud.domain.generator.opscloud.DatasourceConfig;
import com.baiyi.opscloud.domain.generator.opscloud.DatasourceInstance;
import com.baiyi.opscloud.domain.generator.opscloud.User;
import com.baiyi.opscloud.domain.generator.opscloud.UserPermission;
import com.baiyi.opscloud.domain.vo.business.BaseBusiness;
import com.baiyi.opscloud.service.user.UserPermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author baiyi
 * @Date 2021/8/11 2:05 下午
 * @Version 1.0
 */
@Slf4j
public abstract class AbstractAccountProvider extends SimpleDsInstanceProvider implements IAccount, InitializingBean {

    @Resource
    protected DsConfigFactory dsConfigFactory;

    @Resource
    private UserPermissionService userPermissionService;

    protected static ThreadLocal<DsInstanceContext> dsInstanceContext = new ThreadLocal<>();

    protected List<UserPermission> queryUserPermission(User user, Integer businessType) {
        return userPermissionService.queryByUserPermission(user.getId(), businessType);
    }

    protected abstract void initialConfig(DatasourceConfig dsConfig);

    private void pre(DatasourceInstance dsInstance) {
        dsInstanceContext.set(buildDsInstanceContext(dsInstance.getId()));
        initialConfig(dsInstanceContext.get().getDsConfig());
    }

    @Override
    public void create(DatasourceInstance dsInstance, User user) {
        pre(dsInstance);
        doCreate(user);
    }

    @Override
    public void update(DatasourceInstance dsInstance, User user) {
        pre(dsInstance);
        doUpdate(user);
    }

    @Override
    public void delete(DatasourceInstance dsInstance, User user) {
        pre(dsInstance);
        doDelete(user);
    }

    @Override
    public void grant(DatasourceInstance dsInstance, User user, BaseBusiness.IBusiness businessResource) {
        if (getBusinessResourceType() == businessResource.getBusinessType()) {
            pre(dsInstance);
            doGrant(user, businessResource);
        }
    }

    @Override
    public void revoke(DatasourceInstance dsInstance, User user, BaseBusiness.IBusiness businessResource) {
        if (getBusinessResourceType() == businessResource.getBusinessType()) {
            pre(dsInstance);
            doRevoke(user, businessResource);
        }
    }

    protected abstract void doCreate(User user);

    protected abstract void doUpdate(User user);

    protected abstract void doDelete(User user);

    public abstract void doGrant(User user, BaseBusiness.IBusiness businessResource);

    public abstract void doRevoke(User user, BaseBusiness.IBusiness businessResource);

    protected abstract int getBusinessResourceType();

    @Override
    public void afterPropertiesSet() {
        AccountProviderFactory.register(this);
    }
}
