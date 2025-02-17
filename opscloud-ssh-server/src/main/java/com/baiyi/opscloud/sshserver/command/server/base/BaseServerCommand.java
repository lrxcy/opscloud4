package com.baiyi.opscloud.sshserver.command.server.base;

import com.baiyi.opscloud.domain.DataTable;
import com.baiyi.opscloud.domain.generator.opscloud.Server;
import com.baiyi.opscloud.domain.generator.opscloud.ServerAccount;
import com.baiyi.opscloud.domain.param.server.ServerParam;
import com.baiyi.opscloud.domain.vo.server.ServerVO;
import com.baiyi.opscloud.service.server.ServerService;
import com.baiyi.opscloud.sshcore.account.SshAccount;
import com.baiyi.opscloud.sshcore.table.PrettyTable;
import com.baiyi.opscloud.sshserver.PromptColor;
import com.baiyi.opscloud.sshserver.SshShellHelper;
import com.baiyi.opscloud.sshserver.command.context.ListServerCommand;
import com.baiyi.opscloud.sshserver.command.context.SessionCommandContext;
import com.baiyi.opscloud.sshserver.command.util.ServerUtil;
import com.baiyi.opscloud.sshserver.packer.SshServerPacker;
import com.baiyi.opscloud.sshserver.util.ServerTableUtil;
import com.baiyi.opscloud.sshserver.util.SessionUtil;
import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.jline.terminal.Terminal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * @Author baiyi
 * @Date 2021/7/1 11:07 上午
 * @Version 1.0
 */
public class BaseServerCommand {

    protected static final int PAGE_FOOTER_SIZE = 6;

    private static final int COMMENT_MAX_SIZE = 20;

    @Resource
    protected SshShellHelper helper;

    @Resource
    private ServerService serverService;

    @Resource
    private SshAccount sshAccount;

    @Resource
    private SshServerPacker sshServerPacker;

    private Terminal terminal;

    @Autowired
    @Lazy
    public void setTerminal(Terminal terminal) {
        this.terminal = terminal;
    }

    public interface LoginType {
        int LOW_AUTHORITY = 0;
        int HIGH_AUTHORITY = 1;
    }

    protected void doListServer(ListServerCommand commandContext) {
        PrettyTable pt = PrettyTable
                .fieldNames("ID",
                        "Server Name",
                        "ServerGroup Name",
                        "Env",
                        "IP",
                        "Tag",
                        "Account",
                        "Comment"
                );
        ServerParam.UserPermissionServerPageQuery pageQuery = commandContext.getQueryParam();
        pageQuery.setUserId(com.baiyi.opscloud.common.util.SessionUtil.getIsAdmin() ? null : com.baiyi.opscloud.common.util.SessionUtil.getUserId());
        pageQuery.setLength(terminal.getSize().getRows() - PAGE_FOOTER_SIZE);
        SessionCommandContext.setServerQuery(pageQuery); // 设置上下文
        DataTable<Server> table = serverService.queryUserPermissionServerPage(pageQuery);
        Map<Integer, Integer> idMapper = Maps.newHashMap();
        int id = 1;
        for (ServerVO.Server s : sshServerPacker.wrapToVO(table.getData())) {
            idMapper.put(id, s.getId());
            pt.addRow(id,
                    s.getDisplayName(),
                    s.getServerGroup().getName(),
                    ServerUtil.toDisplayEnv(s.getEnv()),
                    ServerUtil.toDisplayIp(s),
                    ServerUtil.toDisplayTag(s),
                    toDisplayAccount(s, com.baiyi.opscloud.common.util.SessionUtil.getIsAdmin()),
                    toDisplayComment(s.getComment())
            );
            id++;
        }
        SessionCommandContext.setIdMapper(idMapper);
        helper.print(pt.toString());
        helper.print(ServerTableUtil.buildPagination(table.getTotalNum(),
                pageQuery.getPage(),
                pageQuery.getLength()),
                PromptColor.GREEN);
    }

    private String toDisplayComment(String comment) {
        if (StringUtils.isEmpty(comment))
            return "";
        if (comment.length() >= COMMENT_MAX_SIZE) {
            return comment.substring(0, COMMENT_MAX_SIZE) + "...";
        }
        return comment;
    }

    private String toDisplayAccount(ServerVO.Server server, boolean isAdmin) {
        String displayAccount = "";
        Map<Integer, List<ServerAccount>> accountCatMap = sshAccount.getServerAccountCatMap(server.getId());
        if (accountCatMap.containsKey(LoginType.LOW_AUTHORITY)) {
            displayAccount = Joiner.on(" ").skipNulls().join(accountCatMap.get(LoginType.LOW_AUTHORITY).stream().map(a ->
                    "[" + SshShellHelper.getColoredMessage(a.getUsername(), PromptColor.GREEN) + "]"
            ).collect(Collectors.toList()));
        }
        if (isAdmin || "admin".equalsIgnoreCase(server.getServerGroup().getUserPermission().getPermissionRole()))
            if (accountCatMap.containsKey(LoginType.HIGH_AUTHORITY)) {
                displayAccount = Joiner.on(" ").skipNulls().join(displayAccount, accountCatMap.get(LoginType.HIGH_AUTHORITY).stream().map(a ->
                        SshShellHelper.getColoredMessage(a.getUsername(), PromptColor.RED)
                ).collect(Collectors.toList()));
            }
        return displayAccount;
    }

    protected String buildSessionId() {
        return SessionUtil.buildSessionId(helper.getSshSession().getIoSession());
    }

}
