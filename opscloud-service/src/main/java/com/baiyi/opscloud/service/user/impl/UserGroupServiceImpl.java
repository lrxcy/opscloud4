package com.baiyi.opscloud.service.user.impl;

import com.baiyi.opscloud.domain.DataTable;
import com.baiyi.opscloud.domain.generator.opscloud.UserGroup;
import com.baiyi.opscloud.domain.param.user.UserBusinessPermissionParam;
import com.baiyi.opscloud.domain.param.user.UserGroupParam;
import com.baiyi.opscloud.mapper.opscloud.UserGroupMapper;
import com.baiyi.opscloud.service.user.UserGroupService;
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
 * @Date 2021/6/16 3:16 下午
 * @Version 1.0
 */
@Service
public class UserGroupServiceImpl implements UserGroupService {

    @Resource
    private UserGroupMapper userGroupMapper;

    @Override
    public void add(UserGroup userGroup) {
        userGroupMapper.insert(userGroup);
    }

    @Override
    public void update(UserGroup userGroup) {
        userGroupMapper.updateByPrimaryKey(userGroup);
    }

    @Override
    public void deleteById(Integer id) {
        userGroupMapper.deleteByPrimaryKey(id);
    }

    @Override
    public UserGroup getByName(String name) {
        Example example = new Example(UserGroup.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("name", name);
        return userGroupMapper.selectOneByExample(example);
    }

    @Override
    public UserGroup getById(Integer id) {
        return userGroupMapper.selectByPrimaryKey(id);
    }

    @Override
    public DataTable<UserGroup> queryPageByParam(UserGroupParam.UserGroupPageQuery pageQuery) {
        Page page = PageHelper.startPage(pageQuery.getPage(), pageQuery.getLength());
        Example example = new Example(UserGroup.class);
        Example.Criteria criteria = example.createCriteria();
        if (StringUtils.isNotBlank(pageQuery.getQueryName())) {
            String likeName = SQLUtil.toLike(pageQuery.getQueryName());
            criteria.andLike("name", likeName);
        }
        example.setOrderByClause("create_time");
        List<UserGroup> data = userGroupMapper.selectByExample(example);
        return new DataTable<>(data, page.getTotal());

    }

    @Override
    public DataTable<UserGroup> queryPageByParam(UserBusinessPermissionParam.UserBusinessPermissionPageQuery pageQuery) {
        Page page = PageHelper.startPage(pageQuery.getPage(), pageQuery.getLength());
        List<UserGroup> data = userGroupMapper.queryUserPermissionGroupByParam(pageQuery);
        return new DataTable<>(data, page.getTotal());
    }

}
