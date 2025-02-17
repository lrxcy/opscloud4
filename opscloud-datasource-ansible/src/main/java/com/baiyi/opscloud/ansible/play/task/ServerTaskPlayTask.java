package com.baiyi.opscloud.ansible.play.task;

import com.baiyi.opscloud.ansible.play.PlayOutputMessage;
import com.baiyi.opscloud.domain.generator.opscloud.ServerTaskMember;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.websocket.Session;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

/**
 * @Author baiyi
 * @Date 2021/9/26 5:36 下午
 * @Version 1.0
 */
@Slf4j
public class ServerTaskPlayTask implements Runnable {

    private Session session;

    private ServerTaskMember serverTaskMember;


    public ServerTaskPlayTask(Session session, ServerTaskMember serverTaskMember) {
        this.session = session;
        this.serverTaskMember = serverTaskMember;
    }

    @Override
    public void run() {
        String output;
        String error = "";
        try {
            LineNumberReader outputReader = new LineNumberReader(new FileReader(serverTaskMember.getOutputMsg()));
            LineNumberReader errorReader = new LineNumberReader(new FileReader(serverTaskMember.getErrorMsg()));
            while (session.isOpen() &&
                    ((output = outputReader.readLine()) != null || (error = errorReader.readLine()) != null)) {
                send(output, error);
                Thread.sleep(100);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        log.info("serverTaskPlayTask线程结束! serverTaskId = {} , serverTaskMemberId = {}", serverTaskMember.getServerTaskId(), serverTaskMember.getId());
    }

    private void send(String output, String error) throws IOException {
        PlayOutputMessage pom = PlayOutputMessage.builder()
                .instanceId(serverTaskMember.getServerName())
                .serverTaskId(serverTaskMember.getServerTaskId())
                .serverTaskMemberId(serverTaskMember.getId())
                .output(StringUtils.isEmpty(output) ? "" : output + "\n")
                .error(StringUtils.isEmpty(error) ? "" : error + "\n")
                .build();
        session.getBasicRemote().sendText(pom.toString());
    }

}
