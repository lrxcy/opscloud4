package com.baiyi.opscloud.service.terminal;

import com.baiyi.opscloud.domain.DataTable;
import com.baiyi.opscloud.domain.generator.opscloud.TerminalSessionInstanceCommand;
import com.baiyi.opscloud.domain.param.terminal.TerminalSessionInstanceCommandParam;

import java.util.List;

/**
 * @Author baiyi
 * @Date 2021/7/28 10:50 上午
 * @Version 1.0
 */
public interface TerminalSessionInstanceCommandService {

    void add(TerminalSessionInstanceCommand command);

    List<TerminalSessionInstanceCommand> queryByInstanceId(Integer terminalSessionInstanceId);

    DataTable<TerminalSessionInstanceCommand> queryTerminalSessionInstanceCommandPage(TerminalSessionInstanceCommandParam.InstanceCommandPageQuery pageQuery);
}
