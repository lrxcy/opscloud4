package com.baiyi.opscloud.facade.server.impl;

import com.baiyi.opscloud.common.util.BeanCopierUtil;
import com.baiyi.opscloud.common.util.IdUtil;
import com.baiyi.opscloud.domain.DataTable;
import com.baiyi.opscloud.domain.annotation.*;
import com.baiyi.opscloud.domain.generator.opscloud.Server;
import com.baiyi.opscloud.domain.param.application.ApplicationResourceParam;
import com.baiyi.opscloud.domain.param.server.ServerParam;
import com.baiyi.opscloud.domain.types.ApplicationResTypeEnum;
import com.baiyi.opscloud.domain.types.BusinessTypeEnum;
import com.baiyi.opscloud.domain.vo.application.ApplicationResourceVO;
import com.baiyi.opscloud.domain.vo.server.ServerVO;
import com.baiyi.opscloud.facade.server.ServerFacade;
import com.baiyi.opscloud.factory.resource.base.AbstractApplicationResourceQuery;
import com.baiyi.opscloud.packer.server.ServerPacker;
import com.baiyi.opscloud.service.server.ServerService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.stream.Collectors;

/**
 * @Author baiyi
 * @Date 2021/5/24 5:45 下午
 * @Version 1.0
 */
@ApplicationResType(ApplicationResTypeEnum.SERVER)
@BusinessType(BusinessTypeEnum.SERVER)
@Service
public class ServerFacadeImpl extends AbstractApplicationResourceQuery implements ServerFacade {

    @Resource
    private ServerService serverService;

    @Resource
    private ServerPacker serverPacker;

    @Override
    public DataTable<ServerVO.Server> queryServerPage(ServerParam.ServerPageQuery pageQuery) {
        DataTable<Server> table = serverService.queryServerPage(pageQuery);
        return new DataTable<>(serverPacker.wrapVOList(table.getData(), pageQuery), table.getTotalNum());
    }

    @Override
    public DataTable<ApplicationResourceVO.Resource> queryResourcePage(ApplicationResourceParam.ResourcePageQuery pageQuery) {
        ServerParam.ServerPageQuery query = ServerParam.ServerPageQuery.builder()
                .queryName(pageQuery.getQueryName())
                .build();
        query.setLength(pageQuery.getLength());
        query.setPage(pageQuery.getPage());
        DataTable<ServerVO.Server> table = queryServerPage(query);
        return new DataTable<>(table.getData().stream().map(e ->
                ApplicationResourceVO.Resource.builder()
                        .name(e.getDisplayName())
                        .applicationId(pageQuery.getApplicationId())
                        .businessId(e.getBusinessId())
                        .resourceType(getApplicationResType())
                        .businessType(getBusinessType())
                        .comment(e.getPrivateIp())
                        .build()
        ).collect(Collectors.toList()), table.getTotalNum());
    }

    @Override
    @AssetBusinessRelation // 资产绑定业务对象
    public void addServer(ServerVO.Server server) {
        Server pre = toDO(server);
        serverService.add(pre);
        server.setId(pre.getId()); // 绑定资产
    }

    @Override
    public void updateServer(ServerVO.Server server) {
        Server pre = toDO(server);
        serverService.update(pre);
    }

    private Server toDO(ServerVO.Server server) {
        Server pre = BeanCopierUtil.copyProperties(server, Server.class);
        if (IdUtil.isEmpty(pre.getSerialNumber())) {
            Server maxSerialNumberServer = serverService.getMaxSerialNumberServer(pre.getServerGroupId(), pre.getEnvType());
            pre.setSerialNumber(null == maxSerialNumberServer ? 1 : maxSerialNumberServer.getSerialNumber() + 1);
        }
        return pre;
    }

    @TagClear
    @BusinessPropertyClear
    @Override
    public void deleteServerById(Integer id) {
        Server server = serverService.getById(id);
        if (server == null) return;
        serverService.delete(server);
    }

    @Override
    public DataTable<ServerVO.Server> queryUserRemoteServerPage(ServerParam.UserRemoteServerPageQuery pageQuery) {
        DataTable<Server> table = serverService.queryUserRemoteServerPage(pageQuery);
        return new DataTable<>(serverPacker.wrapVOList(table.getData(), pageQuery), table.getTotalNum());
    }

}
