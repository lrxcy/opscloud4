package com.baiyi.opscloud.service.terminal.impl;

import com.baiyi.opscloud.domain.DataTable;
import com.baiyi.opscloud.domain.generator.opscloud.TerminalSessionInstanceCommand;
import com.baiyi.opscloud.domain.param.terminal.TerminalSessionInstanceCommandParam;
import com.baiyi.opscloud.mapper.opscloud.TerminalSessionInstanceCommandMapper;
import com.baiyi.opscloud.service.terminal.TerminalSessionInstanceCommandService;
import com.baiyi.opscloud.util.SQLUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author baiyi
 * @Date 2021/7/28 10:50 上午
 * @Version 1.0
 */
@Service
public class TerminalSessionInstanceCommandServiceImpl implements TerminalSessionInstanceCommandService {

    @Resource
    private TerminalSessionInstanceCommandMapper commandMapper;

    @Override
    public void add(TerminalSessionInstanceCommand command) {
        commandMapper.insert(command);
    }

    @Override
    public List<TerminalSessionInstanceCommand> queryByInstanceId(Integer terminalSessionInstanceId) {
        Example example = new Example(TerminalSessionInstanceCommand.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("terminalSessionInstanceId", terminalSessionInstanceId);
        return commandMapper.selectByExample(example);
    }

    @Override
    public DataTable<TerminalSessionInstanceCommand> queryTerminalSessionInstanceCommandPage(TerminalSessionInstanceCommandParam.InstanceCommandPageQuery pageQuery) {
        Page page = PageHelper.startPage(pageQuery.getPage(), pageQuery.getLength());
        Example example = new Example(TerminalSessionInstanceCommand.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("terminalSessionInstanceId",pageQuery.getTerminalSessionInstanceId());
        if (StringUtils.isNotBlank(pageQuery.getQueryName())) {
            Example.Criteria criteria2 = example.createCriteria();
            String likeName = SQLUtil.toLike(pageQuery.getQueryName());
            criteria2.orLike("input", likeName)
                    .orLike("inputFormatted", likeName);
            example.and(criteria2);
        }
        List<TerminalSessionInstanceCommand> data = commandMapper.selectByExample(example);
        return new DataTable<>(data, page.getTotal());
    }


}
