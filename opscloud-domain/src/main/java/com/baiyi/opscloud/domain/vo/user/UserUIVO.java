package com.baiyi.opscloud.domain.vo.user;

import com.baiyi.opscloud.domain.vo.sys.MenuVO;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author baiyi
 * @Date 2021/6/4 5:29 下午
 * @Version 1.0
 */
public class UserUIVO {


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @ApiModel
    @Builder
    public static class UIInfo {

        private List<MenuVO.Menu> menuInfo;

    }
}
