package com.baiyi.opscloud.ldap.factory;

import com.baiyi.opscloud.common.datasource.config.DsLdapConfig;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.transaction.compensating.manager.TransactionAwareContextSourceProxy;

/**
 * @Author baiyi
 * @Date 2021/5/15 6:37 下午
 * @Version 1.0
 */
public class LdapFactory {

    public static LdapTemplate buildLdapTemplate(DsLdapConfig.Ldap ldapConfig) {
        LdapContextSource contextSource = buildLdapContextSource(ldapConfig);
        TransactionAwareContextSourceProxy sourceProxy = buildTransactionAwareContextSourceProxy(contextSource);
        return new LdapTemplate(sourceProxy);
    }

    private static LdapContextSource buildLdapContextSource(DsLdapConfig.Ldap config) {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(config.getUrl());
        contextSource.setBase(config.getBase());
        contextSource.setUserDn(config.getManager().getDn());
        contextSource.setPassword(config.getManager().getPassword());
        contextSource.setPooled(true);
        contextSource.afterPropertiesSet();
        return contextSource;
    }

    private static TransactionAwareContextSourceProxy buildTransactionAwareContextSourceProxy(LdapContextSource contextSource) {
        TransactionAwareContextSourceProxy sourceProxy = new TransactionAwareContextSourceProxy(contextSource);
        return sourceProxy;
    }
}
