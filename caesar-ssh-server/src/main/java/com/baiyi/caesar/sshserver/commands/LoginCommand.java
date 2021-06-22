package com.baiyi.caesar.sshserver.commands;

import com.baiyi.caesar.common.exception.ssh.SshRuntimeException;
import com.baiyi.caesar.common.util.IdUtil;
import com.baiyi.caesar.domain.generator.caesar.Server;
import com.baiyi.caesar.service.server.ServerService;
import com.baiyi.caesar.sshcore.handler.HostSystemHandler;
import com.baiyi.caesar.sshcore.handler.RemoteInvokeHandler;
import com.baiyi.caesar.sshcore.model.HostSystem;
import com.baiyi.caesar.sshcore.model.JSchSession;
import com.baiyi.caesar.sshcore.model.JSchSessionContainer;
import com.baiyi.caesar.sshcore.task.server.ServerSentOutputTask;
import com.baiyi.caesar.sshserver.PromptColor;
import com.baiyi.caesar.sshserver.SshContext;
import com.baiyi.caesar.sshserver.SshShellCommandFactory;
import com.baiyi.caesar.sshserver.SshShellHelper;
import com.baiyi.caesar.sshserver.util.SessionUtil;
import com.baiyi.caesar.sshserver.util.TerminalUtil;
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

/**
 * @Author baiyi
 * @Date 2021/5/31 4:58 下午
 * @Version 1.0
 */
@Slf4j
@SshShellComponent
@ShellCommandGroup("Login")
public final class LoginCommand {

    @Resource
    private SshShellHelper helper;

    @Resource
    private ServerService serverService;

    @Resource
    private HostSystemHandler hostSystemHandler;

    @ShellMethod(value = "Login server", key = {"open", "login"})
    public void login(@ShellOption(help = "Server id") int id, @ShellOption(help = "Account name", defaultValue = "") String account) {
        ServerSession serverSession = helper.getSshSession();
        String sessionId = SessionUtil.buildSessionId(serverSession.getIoSession());
        String instanceId = IdUtil.buildUUID();
        Terminal terminal = getTerminal();
        Runnable run = new ServerSentOutputTask(sessionId, serverSession, terminal);
        Thread thread = new Thread(run);
        thread.start();
        try {
            HostSystem hostSystem;
            Server server = serverService.getById(id);
            hostSystem = hostSystemHandler.buildHostSystem(server, serverSession.getUsername(), account);
            hostSystem.setInstanceId(instanceId);
            hostSystem.setTerminalSize(helper.terminalSize());
            RemoteInvokeHandler.openSSHTermOnSystemForSSHServer(sessionId, hostSystem);
            TerminalUtil.enterRawMode(terminal);
            Instant inst1 = Instant.now();
            Size size = terminal.getSize();
            while (true) {
                try {
                    if (!terminal.getSize().equals(size)) {
                        size = terminal.getSize();
                        TerminalUtil.resize(sessionId, instanceId, size);
                    }
                    int ch = terminal.reader().read(25L);
                    if (ch >= 0) {
                        printCommand(sessionId, instanceId, (char) ch);
                    }
                    if (isClosed(sessionId, instanceId)) {
                        Thread.sleep(150L);
                        sessionClosed("用户正常退出登录! 耗时:%s/s", inst1);
                        break;
                    }
                    Thread.sleep(25L);
                } catch (Exception e) {
                    e.printStackTrace();
                    sessionClosed("服务端连接已断开! 耗时:%s/s", inst1);
                    break;
                }
            }
        } catch (SshRuntimeException e) {
            ((ServerSentOutputTask) run).stop();
            throw e;
        }
        ((ServerSentOutputTask) run).stop();
        JSchSessionContainer.closeSession(sessionId, instanceId);
    }

    private void sessionClosed(String logout, Instant inst1) {
        helper.print(String.format(logout, Duration.between(inst1, Instant.now()).getSeconds()), PromptColor.RED);
    }

    private Terminal getTerminal() {
        SshContext sshContext = SshShellCommandFactory.SSH_THREAD_CONTEXT.get();
        if (sshContext == null) {
            throw new IllegalStateException("Unable to find ssh context");
        } else {
            return sshContext.getTerminal();
        }
    }

    private boolean isClosed(String sessionId, String instanceId) {
        JSchSession jSchSession = JSchSessionContainer.getBySessionId(sessionId, instanceId);
        assert jSchSession != null;
        return jSchSession.getChannel().isClosed();
    }

    private void printCommand(String sessionId, String instanceId, char cmd) throws Exception {
        JSchSession jSchSession = JSchSessionContainer.getBySessionId(sessionId, instanceId);
        if (jSchSession == null) throw new Exception();
        jSchSession.getCommander().print(cmd);
    }

}