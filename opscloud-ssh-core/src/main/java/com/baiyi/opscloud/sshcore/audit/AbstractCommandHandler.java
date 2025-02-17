package com.baiyi.opscloud.sshcore.audit;

import com.baiyi.opscloud.domain.generator.opscloud.TerminalSessionInstance;
import com.baiyi.opscloud.domain.generator.opscloud.TerminalSessionInstanceCommand;
import com.baiyi.opscloud.service.terminal.TerminalSessionInstanceCommandService;
import com.baiyi.opscloud.service.terminal.TerminalSessionInstanceService;
import com.baiyi.opscloud.sshcore.config.TerminalConfig;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.regex.Pattern;

/**
 * @Author baiyi
 * @Date 2021/7/28 4:31 下午
 * @Version 1.0
 */
public abstract class AbstractCommandHandler {

    @Resource
    private TerminalConfig terminalConfig;

    @Resource
    private TerminalSessionInstanceService terminalSessionInstanceService;

    @Resource
    private TerminalSessionInstanceCommandService terminalSessionInstanceCommandService;

    protected abstract String getInputRegex();

    protected abstract String getBsRegex();

    /**
     * 记录命令 inpunt & output
     *
     * @param sessionId
     * @param instanceId
     */
    public void recordCommand(String sessionId, String instanceId) {
        TerminalSessionInstance terminalSessionInstance = terminalSessionInstanceService.getByUniqueKey(sessionId, instanceId);
        String commanderLogPath = terminalConfig.buildAuditLogPath(sessionId, instanceId);
        String str;
        InstanceCommandBuilder builder = null;
        String regex = getInputRegex();
        try {
            LineNumberReader reader = new LineNumberReader(new FileReader(commanderLogPath));
            while ((str = reader.readLine()) != null) {
                if (!str.isEmpty()) {
                    boolean isInput = Pattern.matches(regex, str);
                    if (isInput) {
                        if (builder != null) {
                            // save
                            TerminalSessionInstanceCommand auditCommand = builder.build();
                            if(auditCommand != null){
                                if (!StringUtils.isEmpty(auditCommand.getInputFormatted()))
                                    terminalSessionInstanceCommandService.add(auditCommand);
                                builder = null;
                            }
                        }
                        builder = builder(terminalSessionInstance.getId(), str);
                    } else {
                        if (builder != null)
                            builder.addOutput(str);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private ImmutablePair<Integer, Integer> getIndex(String inputStr) {
        int index1 = inputStr.indexOf("$");
        int index2 = inputStr.indexOf("#");
        if (index1 >= index2)
            return ImmutablePair.of(index2, index1);
        return ImmutablePair.of(index1, index2);
    }

    public InstanceCommandBuilder builder(Integer terminalSessionInstanceId, String inputStr) {
        ImmutablePair<Integer, Integer> ip = getIndex(inputStr);
        int index = ip.getLeft() != -1 ? ip.getLeft() : ip.getRight();
        if (index == -1) return null;
        TerminalSessionInstanceCommand command = TerminalSessionInstanceCommand.builder()
                .terminalSessionInstanceId(terminalSessionInstanceId)
                .prompt(inputStr.substring(0, index + 1))
                .input(inputStr.substring(index + 2))
                .build();
        formatInput(command);
        return InstanceCommandBuilder.newBuilder(command);
    }

    public void formatInput(TerminalSessionInstanceCommand command) {
        String input = command.getInput();
        while (input.contains("\b")) {
            String ni = input.replaceFirst(getBsRegex(), ""); // 退格处理
            if (ni.equals(input)) { // 避免死循环
                ni = input.replaceFirst(".?\b", "");
            }
            input = ni;
        }
        // 删除所有不可见字符（但不包含退格）
        String inputFormatted = eraseInvisibleCharacters(input);
        command.setInputFormatted(inputFormatted);
        command.setIsFormatted(true);
    }


    protected String eraseInvisibleCharacters(String input){
       return input.replaceAll("\\p{C}", "");
    }
}
