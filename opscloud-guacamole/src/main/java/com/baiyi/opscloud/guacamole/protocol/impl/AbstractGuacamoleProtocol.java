package com.baiyi.opscloud.guacamole.protocol.impl;

import com.baiyi.opscloud.domain.generator.opscloud.Credential;
import com.baiyi.opscloud.domain.generator.opscloud.Server;
import com.baiyi.opscloud.domain.generator.opscloud.ServerAccount;
import com.baiyi.opscloud.domain.param.guacamole.GuacamoleParam;
import com.baiyi.opscloud.guacamole.protocol.GuacamoleProtocolFactory;
import com.baiyi.opscloud.guacamole.protocol.IGuacamoleProtocol;
import com.baiyi.opscloud.service.server.ServerAccountService;
import com.baiyi.opscloud.service.server.ServerService;
import com.baiyi.opscloud.service.sys.CredentialService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.net.GuacamoleSocket;
import org.apache.guacamole.net.InetGuacamoleSocket;
import org.apache.guacamole.protocol.ConfiguredGuacamoleSocket;
import org.apache.guacamole.protocol.GuacamoleConfiguration;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.beans.factory.InitializingBean;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @Author baiyi
 * @Date 2021/7/9 1:46 下午
 * @Version 1.0
 */
@Slf4j
public abstract class AbstractGuacamoleProtocol implements IGuacamoleProtocol, InitializingBean {

    @Resource
    private CredentialService credentialService;

    @Resource
    private StringEncryptor stringEncryptor;

    @Resource
    private ServerService serverService;

    @Resource
    private ServerAccountService serverAccountService;

    protected abstract Map<String, String> buildParameters(Server server, ServerAccount serverAccount,GuacamoleParam.Login guacamoleLogin);

    protected Credential getCredential(ServerAccount serverAccount) {
        Credential credential = credentialService.getById(serverAccount.getCredentialId());
        if (StringUtils.isEmpty(serverAccount.getUsername()))
            serverAccount.setUsername(credential.getUsername());
        credential.setCredential(stringEncryptor.decrypt(credential.getCredential()));
        return credential;
    }

    @Override
    public GuacamoleSocket buildGuacamoleSocket(GuacamoleParam.Login guacamoleLogin) throws GuacamoleException {
        GuacamoleConfiguration configuration = new GuacamoleConfiguration();
        configuration.setProtocol(getProtocol()); // 协议
        Server server = serverService.getById(guacamoleLogin.getServerId());
        ServerAccount serverAccount = serverAccountService.getById(guacamoleLogin.getServerAccountId());
        Map<String, String> parameters = buildParameters(server, serverAccount,guacamoleLogin);
        configuration.setParameters(parameters);
        return new ConfiguredGuacamoleSocket(
                new InetGuacamoleSocket("172.16.210.1", 4822),
                configuration
        );
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        GuacamoleProtocolFactory.register(this);
    }
}
