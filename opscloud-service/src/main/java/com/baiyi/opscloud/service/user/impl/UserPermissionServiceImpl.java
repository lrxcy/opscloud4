package com.baiyi.opscloud.service.user.impl;

import com.baiyi.opscloud.common.annotation.EventPublisher;
import com.baiyi.opscloud.domain.annotation.BusinessType;
import com.baiyi.opscloud.domain.generator.opscloud.UserPermission;
import com.baiyi.opscloud.domain.types.BusinessTypeEnum;
import com.baiyi.opscloud.domain.types.EventActionTypeEnum;
import com.baiyi.opscloud.mapper.opscloud.UserPermissionMapper;
import com.baiyi.opscloud.service.user.UserPermissionService;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author baiyi
 * @Date 2021/5/26 5:39 下午
 * @Version 1.0
 */
@BusinessType(BusinessTypeEnum.USER_PERMISSION)
@Service
public class UserPermissionServiceImpl implements UserPermissionService {

    @Resource
    private UserPermissionMapper permissionMapper;

    @Override
    public UserPermission getById(Integer id) {
        return permissionMapper.selectByPrimaryKey(id);
    }

    @Override
    public void update(UserPermission userPermission) {
        permissionMapper.updateByPrimaryKey(userPermission);
    }

    @Override
    @EventPublisher(eventAction = EventActionTypeEnum.CREATE)
    public void add(UserPermission userPermission) {
        permissionMapper.insert(userPermission);
    }

    @Override
    public void deleteById(Integer id) {
        permissionMapper.deleteByPrimaryKey(id);
    }

    @Override
    @EventPublisher(eventAction = EventActionTypeEnum.DELETE)
    public void delete(UserPermission userPermission) {
        permissionMapper.delete(userPermission);
    }

    @Override
    public UserPermission getByUserPermission(UserPermission userPermission) {
        return permissionMapper.selectOne(userPermission);
    }

    @Override
    public int countByBusiness(UserPermission userPermission) {
        Example example = new Example(UserPermission.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("businessType", userPermission.getBusinessType())
                .andEqualTo("businessId", userPermission.getBusinessId());

        return permissionMapper.selectCountByExample(example);
    }

    @Override
    public List<UserPermission> queryByBusiness(UserPermission userPermission) {
        Example example = new Example(UserPermission.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("businessType", userPermission.getBusinessType())
                .andEqualTo("businessId", userPermission.getBusinessId());
        return permissionMapper.selectByExample(example);
    }

    @Override
    public List<UserPermission> queryByUserPermission(Integer userId, Integer businessType) {
        Example example = new Example(UserPermission.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("businessType", businessType)
                .andEqualTo("userId", userId);
        return permissionMapper.selectByExample(example);
    }

}
