package com.baiyi.opscloud.service.business.impl;

import com.baiyi.opscloud.domain.generator.opscloud.BusinessAssetRelation;
import com.baiyi.opscloud.mapper.opscloud.BusinessAssetRelationMapper;
import com.baiyi.opscloud.service.business.BusinessAssetRelationService;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author baiyi
 * @Date 2021/7/30 3:06 下午
 * @Version 1.0
 */
@Service
public class BusinessAssetRelationServiceImpl implements BusinessAssetRelationService {

    @Resource
    private BusinessAssetRelationMapper businessAssetRelationMapper;

    @Override
    public BusinessAssetRelation getByUniqueKey(BusinessAssetRelation businessAssetRelation) {
        Example example = new Example(BusinessAssetRelation.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("businessType", businessAssetRelation.getBusinessType())
                .andEqualTo("businessId", businessAssetRelation.getBusinessId())
                .andEqualTo("datasourceInstanceAssetId", businessAssetRelation.getDatasourceInstanceAssetId());
        return businessAssetRelationMapper.selectOneByExample(example);
    }

    @Override
    public void add(BusinessAssetRelation businessAssetRelation) {
        businessAssetRelationMapper.insert(businessAssetRelation);
    }

    @Override
    public void deleteById(Integer id) {
        businessAssetRelationMapper.deleteByPrimaryKey(id);
    }

    @Override
    public List<BusinessAssetRelation> queryAssetRelations(int businessType, int datasourceInstanceAssetId) {
        Example example = new Example(BusinessAssetRelation.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("datasourceInstanceAssetId", datasourceInstanceAssetId);
        if (businessType >= 0)
            criteria.andEqualTo("businessType", businessType);
        return businessAssetRelationMapper.selectByExample(example);
    }

    @Override
    public List<BusinessAssetRelation> queryBusinessRelations(int businessType, int businessId) {
        Example example = new Example(BusinessAssetRelation.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("businessType", businessType)
                .andEqualTo("businessId", businessId);
        return businessAssetRelationMapper.selectByExample(example);
    }

}
