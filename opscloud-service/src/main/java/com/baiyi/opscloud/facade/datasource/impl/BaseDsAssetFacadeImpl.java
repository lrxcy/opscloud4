package com.baiyi.opscloud.facade.datasource.impl;

import com.baiyi.opscloud.domain.annotation.TagClear;
import com.baiyi.opscloud.domain.generator.opscloud.DatasourceInstanceAssetProperty;
import com.baiyi.opscloud.domain.generator.opscloud.DatasourceInstanceAssetRelation;
import com.baiyi.opscloud.domain.types.BusinessTypeEnum;
import com.baiyi.opscloud.facade.datasource.BaseDsAssetFacade;
import com.baiyi.opscloud.service.datasource.DsInstanceAssetPropertyService;
import com.baiyi.opscloud.service.datasource.DsInstanceAssetRelationService;
import com.baiyi.opscloud.service.datasource.DsInstanceAssetService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author baiyi
 * @Date 2021/6/29 4:36 下午
 * @Version 1.0
 */
@Service
public class BaseDsAssetFacadeImpl implements BaseDsAssetFacade {

    @Resource
    private DsInstanceAssetService dsInstanceAssetService;

    @Resource
    private DsInstanceAssetRelationService dsInstanceAssetRelationService;

    @Resource
    private DsInstanceAssetPropertyService dsInstanceAssetPropertyService;

    @TagClear(type = BusinessTypeEnum.ASSET)
    @Override
    public void deleteAssetById(Integer id) {
        // 删除关系
        List<DatasourceInstanceAssetRelation> relations = dsInstanceAssetRelationService.queryByAssetId(id);
        if (!CollectionUtils.isEmpty(relations))
            for (DatasourceInstanceAssetRelation relation : relations) {
                dsInstanceAssetRelationService.deleteById(relation.getId());
            }
        // 删除属性
        List<DatasourceInstanceAssetProperty> properties = dsInstanceAssetPropertyService.queryByAssetId(id);
        if (!CollectionUtils.isEmpty(properties))
            for (DatasourceInstanceAssetProperty property : properties) {
                dsInstanceAssetPropertyService.deleteById(property.getId());
            }
        // 删除自己
        dsInstanceAssetService.deleteById(id);
    }
}