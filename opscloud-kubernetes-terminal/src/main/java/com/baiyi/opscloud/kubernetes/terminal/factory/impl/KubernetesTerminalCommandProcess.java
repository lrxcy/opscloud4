package com.baiyi.opscloud.kubernetes.terminal.factory.impl;

import com.baiyi.opscloud.domain.generator.opscloud.TerminalSession;
import com.baiyi.opscloud.kubernetes.terminal.factory.AbstractKubernetesTerminalProcess;
import com.baiyi.opscloud.sshcore.base.ITerminalProcess;
import com.baiyi.opscloud.sshcore.enums.MessageState;
import com.baiyi.opscloud.sshcore.message.kubernetes.KubernetesCommandMessage;
import com.baiyi.opscloud.sshcore.model.KubernetesSession;
import com.baiyi.opscloud.sshcore.model.KubernetesSessionContainer;
import com.google.gson.GsonBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.websocket.Session;
import java.io.IOException;
import java.util.Map;

/**
 * @Author baiyi
 * @Date 2021/7/19 2:25 下午
 * @Version 1.0
 */
@Component
public class KubernetesTerminalCommandProcess extends AbstractKubernetesTerminalProcess<KubernetesCommandMessage> implements ITerminalProcess {

    /**
     * 登录
     *
     * @return
     */
    @Override
    public String getState() {
        return MessageState.COMMAND.getState();
    }

    @Override
    public void process(String message, Session session, TerminalSession terminalSession) {
        KubernetesCommandMessage commandMessage = getMessage(message);
        if (StringUtils.isEmpty(commandMessage.getCommand()))
            return;
        if (!isBatch(terminalSession)) {
            printCommand(terminalSession.getSessionId(), commandMessage.getInstanceId(), commandMessage.getCommand());
        } else {
            Map<String, KubernetesSession> sessionMap = KubernetesSessionContainer.getBySessionId(terminalSession.getSessionId());
            sessionMap.keySet().parallelStream().forEach(e -> printCommand(terminalSession.getSessionId(), e, commandMessage.getCommand()));
        }
    }

    private void printCommand(String sessionId, String instanceId, String cmd) {
        KubernetesSession kubernetesSession = KubernetesSessionContainer.getBySessionId(sessionId, instanceId);
        if (kubernetesSession == null) return;
        try{
            kubernetesSession.getExecWatch().getInput().write(cmd.getBytes());
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    protected KubernetesCommandMessage getMessage(String message) {
        return new GsonBuilder().create().fromJson(message, KubernetesCommandMessage.class);
    }

}
