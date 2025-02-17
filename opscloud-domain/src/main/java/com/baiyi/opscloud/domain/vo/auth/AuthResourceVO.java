package com.baiyi.opscloud.domain.vo.auth;

import com.baiyi.opscloud.domain.vo.base.BaseVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author baiyi
 * @Date 2020/2/14 9:52 下午
 * @Version 1.0
 */
public class AuthResourceVO {

    @Data
    @NoArgsConstructor
    @ApiModel
    public static class Resource extends BaseVO {

        // 资源组
        private AuthGroupVO.Group group;

        @ApiModelProperty(value = "主键",example="1")
        private Integer id;

        @ApiModelProperty(value = "资源组id",example="1")
        private Integer groupId;

        @ApiModelProperty(value = "资源路径")
        private String resourceName;

        @ApiModelProperty(value = "资源描述")
        private String comment;

        @ApiModelProperty(value = "需要鉴权")
        private Boolean needAuth;

        @ApiModelProperty(value = "用户界面")
        private Boolean ui;
    }


}
