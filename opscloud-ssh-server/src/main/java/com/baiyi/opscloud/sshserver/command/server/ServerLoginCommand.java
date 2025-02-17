package com.baiyi.opscloud.sshserver.command.server;

import com.baiyi.opscloud.common.exception.ssh.SshRuntimeException;
import com.baiyi.opscloud.common.util.IdUtil;
import com.baiyi.opscloud.domain.generator.opscloud.Server;
import com.baiyi.opscloud.domain.generator.opscloud.TerminalSessionInstance;
import com.baiyi.opscloud.domain.vo.server.ServerVO;
import com.baiyi.opscloud.service.server.ServerService;
import com.baiyi.opscloud.sshcore.audit.AuditServerCommandHandler;
import com.baiyi.opscloud.sshcore.builder.TerminalSessionInstanceBuilder;
import com.baiyi.opscloud.sshcore.enums.InstanceSessionTypeEnum;
import com.baiyi.opscloud.sshcore.facade.SimpleTerminalSessionFacade;
import com.baiyi.opscloud.sshcore.handler.HostSystemHandler;
import com.baiyi.opscloud.sshcore.handler.RemoteInvokeHandler;
import com.baiyi.opscloud.sshcore.model.HostSystem;
import com.baiyi.opscloud.sshcore.model.JSchSession;
import com.baiyi.opscloud.sshcore.model.JSchSessionContainer;
import com.baiyi.opscloud.sshcore.model.SessionIdMapper;
import com.baiyi.opscloud.sshserver.PromptColor;
import com.baiyi.opscloud.sshserver.SshContext;
import com.baiyi.opscloud.sshserver.SshShellCommandFactory;
import com.baiyi.opscloud.sshserver.SshShellHelper;
import com.baiyi.opscloud.sshserver.annotation.InvokeSessionUser;
import com.baiyi.opscloud.sshserver.command.component.SshShellComponent;
import com.baiyi.opscloud.sshserver.command.context.SessionCommandContext;
import com.baiyi.opscloud.sshserver.packer.SshServerPacker;
import com.baiyi.opscloud.sshserver.util.TerminalUtil;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.server.session.ServerSession;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * @Author baiyi
 * @Date 2021/5/31 4:58 下午
 * @Version 1.0
 */
@Slf4j
@SshShellComponent
@ShellCommandGroup("Server")
public class ServerLoginCommand {

    @Resource
    private AuditServerCommandHandler auditCommandHandler;

    @Resource
    private SshShellHelper helper;

    @Resource
    private ServerService serverService;

    @Resource
    private HostSystemHandler hostSystemHandler;

    @Resource
    private SshServerPacker sshServerPacker;

    @Resource
    private SimpleTerminalSessionFacade simpleTerminalSessionFacade;

    private String toInstanceId(Server server) {
        ServerVO.Server serverVO = sshServerPacker.wrapToVO(server);
        return Joiner.on("#").join(serverVO.getDisplayName(), server.getPrivateIp(), IdUtil.buildUUID());
    }

    @InvokeSessionUser(invokeAdmin = true)
    @ShellMethod(value = "登录服务器(开启会话)", key = {"open", "login"})
    public void login(@ShellOption(help = "Server Id") int id,
                      @ShellOption(help = "Account Name", defaultValue = "") String account,
                      @ShellOption(value = {"-A", "--admin"}, help = "Admin") boolean admin) {
        ServerSession serverSession = helper.getSshSession();
        String sessionId = SessionIdMapper.getSessionId(serverSession.getIoSession());
        Terminal terminal = getTerminal();

        SshContext sshContext = getSshContext();

        Map<Integer, Integer> idMapper = SessionCommandContext.getIdMapper();
        Server server = serverService.getById(idMapper.get(id));
        String instanceId = toInstanceId(server);
        try {
            HostSystem hostSystem = hostSystemHandler.buildHostSystem(server, account);
            hostSystem.setInstanceId(instanceId);
            hostSystem.setTerminalSize(helper.terminalSize());
            TerminalSessionInstance terminalSessionInstance = TerminalSessionInstanceBuilder.build(sessionId, hostSystem, InstanceSessionTypeEnum.SERVER);
            simpleTerminalSessionFacade.recordTerminalSessionInstance(
                    terminalSessionInstance
            );

            RemoteInvokeHandler.openSSHServer(sessionId, hostSystem, sshContext.getSshShellRunnable().getOs());
            //terminal.enterRawMode();
            TerminalUtil.rawModeSupportVintr(terminal);
            Instant inst1 = Instant.now(); // 计时
            Size size = terminal.getSize();
            try {
                while (true) {
                    if (isClosed(sessionId, instanceId)) {
                        Thread.sleep(150L);
                        sessionClosed("用户正常退出登录! 耗时:%s/s", inst1);
                        break;
                    }
                    tryResize(size, terminal, sessionId, instanceId);
                    printJSchSession(sessionId, instanceId, terminal.reader().read(5L));
                }
            } catch (Exception e) {
                // e.printStackTrace();
                sessionClosed("服务端连接已断开! 耗时:%s/s", inst1);
            } finally {
                simpleTerminalSessionFacade.closeTerminalSessionInstance(terminalSessionInstance);
            }
        } catch (SshRuntimeException e) {
            throw e;
        }
        auditCommandHandler.recordCommand(sessionId, instanceId);
        JSchSessionContainer.closeSession(sessionId, instanceId);
    }

    private void tryResize(Size size, Terminal terminal, String sessionId, String instanceId) {
        if (!terminal.getSize().equals(size)) {
            size = terminal.getSize();
            TerminalUtil.resize(sessionId, instanceId, size);
        }
    }

    private Terminal getTerminal() {
        SshContext sshContext = SshShellCommandFactory.SSH_THREAD_CONTEXT.get();
        if (sshContext == null) {
            throw new IllegalStateException("Unable to find ssh context");
        } else {
            return sshContext.getTerminal();
        }
    }

    private SshContext getSshContext() {
        SshContext sshContext = SshShellCommandFactory.SSH_THREAD_CONTEXT.get();
        if (sshContext == null) {
            throw new IllegalStateException("Unable to find ssh context");
        } else {
            return sshContext;
        }
    }

    private void sessionClosed(String logout, Instant inst1) {
        helper.print(String.format(logout, Duration.between(inst1, Instant.now()).getSeconds()), PromptColor.RED);
    }

    private boolean isClosed(String sessionId, String instanceId) {
        JSchSession jSchSession = JSchSessionContainer.getBySessionId(sessionId, instanceId);
        assert jSchSession != null;
        return jSchSession.getChannel().isClosed();
    }

    private void printJSchSession(String sessionId, String instanceId, int ch) throws Exception {
        if (ch < 0) return;
        JSchSession jSchSession = JSchSessionContainer.getBySessionId(sessionId, instanceId);
        if (jSchSession == null) throw new Exception();
        // jSchSession.getCommander().write(String.valueOf((char) ch).getBytes(StandardCharsets.UTF_8 ));
        jSchSession.getCommander().print((char) ch);

    }

}
