package com.baiyi.opscloud.datasource.asset;

import com.baiyi.opscloud.domain.types.BusinessTypeEnum;
import com.baiyi.opscloud.domain.vo.business.BusinessAssetRelationVO;
import com.baiyi.opscloud.domain.vo.datasource.DsAssetVO;

import java.util.List;
import java.util.Map;

/**
 * @Author baiyi
 * @Date 2021/7/30 1:22 下午
 * @Version 1.0
 */
public interface IAssetConvert extends IAssetType {

    List<BusinessTypeEnum> getBusinessTypes();

    Map<BusinessTypeEnum, BusinessAssetRelationVO.IBusinessAssetRelation> toBusinessTypes(DsAssetVO.Asset asset);
}
