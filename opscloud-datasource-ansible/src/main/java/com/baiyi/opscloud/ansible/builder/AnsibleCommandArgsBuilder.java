package com.baiyi.opscloud.ansible.builder;

import com.baiyi.opscloud.common.datasource.config.DsAnsibleConfig;
import com.baiyi.opscloud.ansible.args.CommandArgs;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.lang3.StringUtils;

/**
 * @Author baiyi
 * @Date 2020/4/6 5:27 下午
 * @Version 1.0
 */
public class AnsibleCommandArgsBuilder {

    /**
     * @param ansible
     * @param args
     * @return
     */
    public static CommandLine build(DsAnsibleConfig.Ansible ansible, CommandArgs args) {
        CommandLine commandLine = AnsibleArgsBuilder.build(ansible, args);

        if (args.isVersion())
            return commandLine;

        commandLine.addArgument("-m");
        if (!StringUtils.isEmpty(args.getModuleName())) {
            commandLine.addArgument(args.getModuleName());
        } else {
            commandLine.addArgument("shell");
        }

        if (!StringUtils.isEmpty(args.getModuleArguments())) {
            commandLine.addArgument("-a");
            commandLine.addArgument(args.getModuleArguments());
        }

        return commandLine;
    }
}
