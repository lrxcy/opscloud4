package com.baiyi.opscloud.facade.server.impl;

import com.baiyi.opscloud.algorithm.ServerPack;
import com.baiyi.opscloud.ansible.ServerGroupingAlgorithm;
import com.baiyi.opscloud.common.base.AccessLevel;
import com.baiyi.opscloud.common.exception.common.CommonRuntimeException;
import com.baiyi.opscloud.common.util.BeanCopierUtil;
import com.baiyi.opscloud.common.util.RegexUtil;
import com.baiyi.opscloud.domain.DataTable;
import com.baiyi.opscloud.domain.ErrorEnum;
import com.baiyi.opscloud.domain.annotation.*;
import com.baiyi.opscloud.domain.generator.opscloud.ServerGroup;
import com.baiyi.opscloud.domain.generator.opscloud.ServerGroupType;
import com.baiyi.opscloud.domain.generator.opscloud.User;
import com.baiyi.opscloud.domain.param.application.ApplicationResourceParam;
import com.baiyi.opscloud.domain.param.server.ServerGroupParam;
import com.baiyi.opscloud.domain.param.server.ServerGroupTypeParam;
import com.baiyi.opscloud.domain.param.user.UserBusinessPermissionParam;
import com.baiyi.opscloud.domain.types.ApplicationResTypeEnum;
import com.baiyi.opscloud.domain.types.BusinessTypeEnum;
import com.baiyi.opscloud.domain.vo.application.ApplicationResourceVO;
import com.baiyi.opscloud.domain.vo.server.ServerGroupTypeVO;
import com.baiyi.opscloud.domain.vo.server.ServerGroupVO;
import com.baiyi.opscloud.domain.vo.server.ServerTreeVO;
import com.baiyi.opscloud.domain.vo.user.UserVO;
import com.baiyi.opscloud.facade.server.ServerGroupFacade;
import com.baiyi.opscloud.facade.user.UserPermissionFacade;
import com.baiyi.opscloud.facade.user.base.IUserBusinessPermissionPageQuery;
import com.baiyi.opscloud.facade.user.factory.UserBusinessPermissionFactory;
import com.baiyi.opscloud.factory.resource.base.AbstractApplicationResourceQuery;
import com.baiyi.opscloud.packer.server.ServerGroupPacker;
import com.baiyi.opscloud.packer.server.ServerGroupTypePacker;
import com.baiyi.opscloud.packer.user.UserPermissionPacker;
import com.baiyi.opscloud.service.server.ServerGroupService;
import com.baiyi.opscloud.service.server.ServerGroupTypeService;
import com.baiyi.opscloud.service.server.ServerService;
import com.baiyi.opscloud.util.ServerTreeUtil;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


/**
 * @Author baiyi
 * @Date 2021/5/24 10:33 上午
 * @Version 1.0
 */
@ApplicationResType(ApplicationResTypeEnum.SERVERGROUP)
@BusinessType(BusinessTypeEnum.SERVERGROUP)
@Service
public class ServerGroupFacadeImpl extends AbstractApplicationResourceQuery implements ServerGroupFacade, IUserBusinessPermissionPageQuery, InitializingBean {

    @Resource
    private ServerGroupService serverGroupService;

    @Resource
    private ServerGroupTypeService serverGroupTypeService;

    @Resource
    private ServerGroupPacker serverGroupPacker;

    @Resource
    private UserPermissionPacker userPermissionPacker;

    @Resource
    private ServerGroupTypePacker serverGroupTypePacker;

    @Resource
    private UserPermissionFacade userPermissionFacade;

    @Resource
    private ServerService serverService;

    @Resource
    private ServerGroupingAlgorithm serverAlgorithm;

    @Resource
    private ServerTreeUtil serverTreeUtil;

    @Override
    public DataTable<ServerGroupVO.ServerGroup> queryServerGroupPage(ServerGroupParam.ServerGroupPageQuery pageQuery) {
        DataTable<ServerGroup> table = serverGroupService.queryPageByParam(pageQuery);
        return new DataTable<>(serverGroupPacker.wrapVOList(table.getData(), pageQuery), table.getTotalNum());
    }

    @Override
    public DataTable<ApplicationResourceVO.Resource> queryResourcePage(ApplicationResourceParam.ResourcePageQuery pageQuery) {
        ServerGroupParam.ServerGroupPageQuery query = ServerGroupParam.ServerGroupPageQuery.builder()
                .name(pageQuery.getQueryName())
                .build();
        query.setLength(pageQuery.getLength());
        query.setPage(pageQuery.getPage());
        DataTable<ServerGroupVO.ServerGroup> table = queryServerGroupPage(query);
        return new DataTable<>(table.getData().stream().map(e ->
                ApplicationResourceVO.Resource.builder()
                        .name(e.getName())
                        .applicationId(pageQuery.getApplicationId())
                        .resourceType(getApplicationResType())
                        .businessType(getBusinessType())
                        .businessId(e.getBusinessId())
                        .comment(e.getComment())
                        .build()
        ).collect(Collectors.toList()), table.getTotalNum());
    }

    @Override
    public DataTable<UserVO.IUserPermission> queryUserBusinessPermissionPage(UserBusinessPermissionParam.UserBusinessPermissionPageQuery pageQuery) {
        pageQuery.setBusinessType(getBusinessType());
        DataTable<ServerGroup> table = serverGroupService.queryPageByParam(pageQuery);
        List<ServerGroupVO.ServerGroup> data = serverGroupPacker.wrapVOList(table.getData(), pageQuery);
        if (pageQuery.getAuthorized())
            data.forEach(e -> {
                e.setUserId(pageQuery.getUserId());
                userPermissionPacker.wrap(e);
            });
        return new DataTable<>(Lists.newArrayList(data), table.getTotalNum());
    }

    @Override
    public void addServerGroup(ServerGroupVO.ServerGroup serverGroup) {
        if (serverGroupService.getByName(serverGroup.getName()) != null)
            throw new CommonRuntimeException(ErrorEnum.SERVERGROUP_NAME_ALREADY_EXIST);
        serverGroupService.add(toDO(serverGroup));
    }

    @Override
    public void updateServerGroup(ServerGroupVO.ServerGroup serverGroup) {
        ServerGroup group = serverGroupService.getById(serverGroup.getId());
        serverGroup.setName(group.getName()); // 不允许修改名称
        serverGroupService.update(toDO(serverGroup));
    }

    @TagClear
    @BusinessPropertyClear
    @RevokeUserPermission
    @Override
    public void deleteServerGroupById(int id) {
        ServerGroup serverGroup = serverGroupService.getById(id);
        if (serverGroup == null) return;
        if (serverService.countByServerGroupId(id) > 0)
            throw new CommonRuntimeException("服务器组不为空：必须删除组内服务器成员！");
        serverGroupService.delete(serverGroup);
    }

    private ServerGroup toDO(ServerGroupVO.ServerGroup serverGroup) {
        ServerGroup pre = BeanCopierUtil.copyProperties(serverGroup, ServerGroup.class);
        RegexUtil.tryServerGroupNameRule(pre.getName()); // 名称规范
        return pre;
    }

    @Override
    public DataTable<ServerGroupTypeVO.ServerGroupType> queryServerGroupTypePage(ServerGroupTypeParam.ServerGroupTypePageQuery pageQuery) {
        DataTable<ServerGroupType> table = serverGroupTypeService.queryPageByParam(pageQuery);
        return new DataTable<>(serverGroupTypePacker.wrapVOList(table.getData(), pageQuery), table.getTotalNum());
    }

    @Override
    public void addServerGroupType(ServerGroupTypeVO.ServerGroupType serverGroupType) {
        serverGroupTypeService.add(BeanCopierUtil.copyProperties(serverGroupType, ServerGroupType.class));
    }

    @Override
    public void updateServerGroupType(ServerGroupTypeVO.ServerGroupType serverGroupType) {
        serverGroupTypeService.update(BeanCopierUtil.copyProperties(serverGroupType, ServerGroupType.class));
    }

    @Override
    public void deleteServerGroupTypeById(int id) {
        if (serverGroupService.countByServerGroupTypeId(id) > 0)
            throw new CommonRuntimeException(ErrorEnum.SERVERGROUP_TYPE_HAS_USED);
        serverGroupTypeService.deleteById(id);
    }

    @Override
    public ServerTreeVO.ServerTree queryServerTree(ServerGroupParam.UserServerTreeQuery queryParam, User user) {
        // 过滤空服务器组
        int accessLevel = userPermissionFacade.getUserAccessLevel(user.getUsername());
        queryParam.setIsAdmin(accessLevel >= AccessLevel.OPS.getLevel());

        List<ServerGroup> groups
                = serverGroupService.queryUserServerGroupTreeByParam(queryParam).stream()
                .filter(g -> serverService.countByServerGroupId(g.getId()) != 0)
                .collect(Collectors.toList());

        List<ServerTreeVO.Tree> treeList = Lists.newArrayList();
        AtomicInteger treeSize = new AtomicInteger();

        for (ServerGroup group : groups) {
            Map<String, List<ServerPack>> serverGroupMap = serverAlgorithm.grouping(group);
            treeList.add(serverTreeUtil.wrap(group, serverGroupMap));
            treeSize.addAndGet(serverTreeUtil.getServerGroupMapSize(serverGroupMap));
        }

        return ServerTreeVO.ServerTree.builder()
                .userId(user.getId())
                .tree(treeList)
                .size(treeSize.get())
                .build();
    }

    /**
     * 注册
     */
    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        UserBusinessPermissionFactory.register(this);
    }
}
