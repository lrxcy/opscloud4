package com.baiyi.opscloud.datasource.asset.impl;

import com.baiyi.opscloud.datasource.asset.impl.base.BaseAssetToBO;
import com.baiyi.opscloud.domain.types.BusinessTypeEnum;
import com.baiyi.opscloud.domain.types.DsAssetTypeEnum;
import com.baiyi.opscloud.domain.vo.business.BusinessAssetRelationVO;
import com.baiyi.opscloud.domain.vo.datasource.DsAssetVO;
import com.baiyi.opscloud.domain.vo.user.UserVO;
import com.google.common.collect.Lists;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author baiyi
 * @Date 2021/8/6 11:11 上午
 * @Version 1.0
 */
@Component
public class LdapUserAssetToUser extends BaseAssetToBO {

    @Override
    public String getAssetType() {
        return DsAssetTypeEnum.USER.getType();
    }

    protected BusinessAssetRelationVO.IBusinessAssetRelation toBO(DsAssetVO.Asset asset, BusinessTypeEnum businessTypeEnum) {
        return UserVO.User.builder()
                .username(asset.getAssetKey())
                .displayName(asset.getName())
                .email(asset.getAssetKey2())
                .phone(asset.getProperties().get("mobile"))
                .build();
    }

    @Override
    public List<BusinessTypeEnum> getBusinessTypes() {
        return Lists.newArrayList(BusinessTypeEnum.USER);
    }

}
