package com.baiyi.opscloud.service.user;

import com.baiyi.opscloud.domain.generator.opscloud.UserPermission;

import java.util.List;

/**
 * @Author baiyi
 * @Date 2021/5/26 5:39 下午
 * @Version 1.0
 */
public interface UserPermissionService {

    UserPermission getById(Integer id);

    UserPermission getByUserPermission(UserPermission userPermission);

    void update(UserPermission userPermission);

    /**
     * 新增并触发事件
     * @param userPermission
     */
    void add(UserPermission userPermission);

    void deleteById(Integer id);

    /**
     * 删除并触发事件
     * @param userPermission
     */
    void delete(UserPermission userPermission);

    /**
     * 只查询数量
     *
     * @param userPermission
     * @return
     */
    int countByBusiness(UserPermission userPermission);

    List<UserPermission> queryByBusiness(UserPermission userPermission);

    /**
     * 查询用户授权信息
     * @param userId
     * @param businessType
     * @return
     */
    List<UserPermission> queryByUserPermission(Integer userId, Integer businessType);
}
