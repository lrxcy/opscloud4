package com.baiyi.opscloud.domain.param.application;

import com.baiyi.opscloud.domain.param.IExtend;
import com.baiyi.opscloud.domain.param.PageParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotNull;

/**
 * @Author baiyi
 * @Date 2021/7/12 1:10 下午
 * @Version 1.0
 */
public class ApplicationParam {

    @Builder
    @Data
    @EqualsAndHashCode(callSuper = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel
    public static class ApplicationPageQuery extends PageParam implements IExtend {

        @ApiModelProperty(value = "应用名称")
        private String queryName;

        @ApiModelProperty(value = "展开")
        private Boolean extend;

    }

    @Builder
    @Data
    @EqualsAndHashCode(callSuper = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel
    public static class UserPermissionApplicationPageQuery extends PageParam implements IExtend {

        @ApiModelProperty(value = "应用名称")
        private String queryName;

        @ApiModelProperty(value = "用户id", example = "1")
        private Integer userId;

        @ApiModelProperty(value = "展开")
        private Boolean extend;

    }

    @Data
    public static class Query {

        @NotNull
        @ApiModelProperty(value = "应用id")
        private Integer applicationId;

    }
}
