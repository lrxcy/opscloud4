package com.baiyi.opscloud.domain.vo.application;

import com.baiyi.opscloud.domain.types.BusinessTypeEnum;
import com.baiyi.opscloud.domain.vo.base.BaseVO;
import com.baiyi.opscloud.domain.vo.business.IBusinessPermissionUser;
import com.baiyi.opscloud.domain.vo.user.UserVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * @Author baiyi
 * @Date 2021/7/12 1:11 下午
 * @Version 1.0
 */
public class ApplicationVO {

    @EqualsAndHashCode(callSuper = true)
    @Data
    @NoArgsConstructor
    @ApiModel
    public static class Application extends BaseVO implements IBusinessPermissionUser {

        private final Integer businessType = BusinessTypeEnum.APPLICATION.getType();

        @Override
        public Integer getBusinessId() {
            return id;
        }

        @ApiModelProperty(value = "授权用户")
        private List<UserVO.User> users;

        private List<ApplicationResourceVO.Resource> resources;

        private Map<String, List<ApplicationResourceVO.Resource>> resourceMap;

        @ApiModelProperty(value = "主键", example = "1")
        private Integer id;

        @NotNull(message = "应用名称不能为空")
        @ApiModelProperty(value = "应用名称")
        private String name;

        @NotNull(message = "应用关键字不能为空")
        @ApiModelProperty(value = "应用关键字")
        private String applicationKey;

        private Integer applicationType;

        @ApiModelProperty(value = "描述")
        private String comment;

    }
}
