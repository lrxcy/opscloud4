package com.baiyi.opscloud.packer.business;

import com.baiyi.opscloud.common.util.BeanCopierUtil;
import com.baiyi.opscloud.domain.generator.opscloud.UserPermission;
import com.baiyi.opscloud.domain.vo.business.IBusinessPermissionUser;
import com.baiyi.opscloud.domain.vo.user.UserVO;
import com.baiyi.opscloud.service.user.UserPermissionService;
import com.baiyi.opscloud.service.user.UserService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author baiyi
 * @Date 2021/9/13 10:30 上午
 * @Version 1.0
 */
@Component
public class BusinessPermissionUserPacker {

    @Resource
    private UserPermissionService userPermissionService;

    @Resource
    private UserService userService;

    /**
     * 业务授权用户
     *
     * @param iBusinessPermissionUser
     */
    public void wrap(IBusinessPermissionUser iBusinessPermissionUser) {
        UserPermission userPermission = UserPermission.builder()
                .businessType(iBusinessPermissionUser.getBusinessType())
                .businessId(iBusinessPermissionUser.getBusinessId())
                .build();
        List<UserPermission> userPermissions = userPermissionService.queryByBusiness(userPermission);
        iBusinessPermissionUser.setUsers(
                userPermissions.stream().map(e ->
                        BeanCopierUtil.copyProperties(userService.getById(e.getUserId()), UserVO.User.class)
                ).collect(Collectors.toList())
        );
    }
}
