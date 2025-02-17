package com.baiyi.opscloud.terminal.factory.impl;

import com.baiyi.opscloud.domain.generator.opscloud.TerminalSession;
import com.baiyi.opscloud.sshcore.enums.MessageState;
import com.baiyi.opscloud.sshcore.message.server.BaseServerMessage;
import com.baiyi.opscloud.sshcore.model.JSchSession;
import com.baiyi.opscloud.sshcore.model.JSchSessionContainer;
import com.baiyi.opscloud.terminal.factory.AbstractServerTerminalProcess;
import org.springframework.stereotype.Component;

import javax.websocket.Session;
import java.util.Map;

/**
 * @Author baiyi
 * @Date 2020/5/11 9:38 上午
 * @Version 1.0
 */
@Component
public class CloseProcess extends AbstractServerTerminalProcess<BaseServerMessage> {

    /**
     * 关闭会话
     *
     * @return
     */
    @Override
    public String getState() {
        return MessageState.CLOSE.getState();
    }

    @Override
    public void process(String message, Session session, TerminalSession terminalSession) {
        Map<String, JSchSession> sessionMap = JSchSessionContainer.getBySessionId(terminalSession.getSessionId());
        if (sessionMap == null) return;
        for (String instanceId : sessionMap.keySet())
            try {
                JSchSession jSchSession = sessionMap.get(instanceId);
                jSchSession.getChannel().disconnect();
                simpleTerminalSessionFacade.closeTerminalSessionInstance(terminalSession, instanceId); // 设置关闭会话
                auditCommandHandler.recordCommand(terminalSession.getSessionId(),instanceId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        try {
            sessionMap.clear();
            session.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        simpleTerminalSessionFacade.closeTerminalSession(terminalSession);
        terminalSession = null;
    }

    @Override
    protected BaseServerMessage getMessage(String message) {
        return null;
    }
}
