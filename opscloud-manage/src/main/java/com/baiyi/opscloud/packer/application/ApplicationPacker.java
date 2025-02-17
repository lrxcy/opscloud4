package com.baiyi.opscloud.packer.application;

import com.baiyi.opscloud.common.builder.SimpleDictBuilder;
import com.baiyi.opscloud.common.util.BeanCopierUtil;
import com.baiyi.opscloud.datasource.kubernetes.provider.KubernetesPodProvider;
import com.baiyi.opscloud.domain.builder.asset.AssetContainer;
import com.baiyi.opscloud.domain.generator.opscloud.Application;
import com.baiyi.opscloud.domain.generator.opscloud.ApplicationResource;
import com.baiyi.opscloud.domain.generator.opscloud.DatasourceInstance;
import com.baiyi.opscloud.domain.generator.opscloud.DatasourceInstanceAsset;
import com.baiyi.opscloud.domain.param.IExtend;
import com.baiyi.opscloud.domain.types.BusinessTypeEnum;
import com.baiyi.opscloud.domain.types.DsAssetTypeEnum;
import com.baiyi.opscloud.domain.vo.application.ApplicationResourceVO;
import com.baiyi.opscloud.domain.vo.application.ApplicationVO;
import com.baiyi.opscloud.domain.vo.datasource.DsAssetVO;
import com.baiyi.opscloud.packer.business.BusinessPermissionUserPacker;
import com.baiyi.opscloud.packer.datasource.DsInstancePacker;
import com.baiyi.opscloud.service.application.ApplicationResourceService;
import com.baiyi.opscloud.service.datasource.DsInstanceAssetService;
import com.baiyi.opscloud.service.datasource.DsInstanceService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author baiyi
 * @Date 2021/7/12 1:23 下午
 * @Version 1.0
 */
@Component
public class ApplicationPacker {

    @Resource
    private KubernetesPodProvider kubernetesPodProvider;

    @Resource
    private DsInstanceAssetService dsInstanceAssetService;

    @Resource
    private DsInstanceService dsInstanceService;

    @Resource
    private ApplicationResourceService applicationResourceService;

    @Resource
    private DsInstancePacker dsInstancePacker;

    @Resource
    private BusinessPermissionUserPacker businessPermissionUserPacker;

    public List<ApplicationVO.Application> wrapVOList(List<Application> data) {
        return BeanCopierUtil.copyListProperties(data, ApplicationVO.Application.class);
    }

    public List<ApplicationVO.Application> wrapVOList(List<Application> data, IExtend iExtend) {
        return data.stream().map(e -> wrapVO(e, iExtend)).collect(Collectors.toList());
    }

    /**
     * Kubernetes专用
     *
     * @param data
     * @return
     */
    public List<ApplicationVO.Application> wrapVOListByKubernetes(List<Application> data) {
        List<ApplicationVO.Application> voList = toVOList(data);
        voList.forEach(a -> {
            List<ApplicationResource> resources = applicationResourceService.queryByApplication(a.getId(), DsAssetTypeEnum.KUBERNETES_DEPLOYMENT.name());
            a.setResources(resources.stream().map(this::wrapPodByDeployment).collect(Collectors.toList()));
        });
        return voList;
    }

    private ApplicationResourceVO.Resource wrapPodByDeployment(ApplicationResource resource) {
        ApplicationResourceVO.Resource resourceVO = BeanCopierUtil.copyProperties(resource, ApplicationResourceVO.Resource.class);
        DatasourceInstanceAsset dsInstanceAsset = dsInstanceAssetService.getById(resource.getBusinessId());
        if (dsInstanceAsset == null) return resourceVO;
        Map<String, String> params = SimpleDictBuilder.newBuilder()
                .paramEntry("namespace", dsInstanceAsset.getAssetKey2())
                .paramEntry("deploymentName", dsInstanceAsset.getAssetKey())
                .build().getDict();
        resourceVO.setAsset(BeanCopierUtil.copyProperties(dsInstanceAsset, DsAssetVO.Asset.class));
        try {
            DatasourceInstance dsInstance = dsInstanceService.getByUuid(dsInstanceAsset.getInstanceUuid());
            if (dsInstance != null) {
                List<AssetContainer> assetContainers = kubernetesPodProvider.queryAssets(dsInstance.getId(), params);
                resourceVO.setAssetContainers(assetContainers);
            }
        } catch (NullPointerException ignored) {
        }
        wrapResourceInstance(resourceVO);
        return resourceVO;
    }

    private List<ApplicationVO.Application> toVOList(List<Application> data) {
        return BeanCopierUtil.copyListProperties(data, ApplicationVO.Application.class);
    }

    public ApplicationVO.Application wrapVO(Application application, IExtend iExtend) {
        ApplicationVO.Application vo = BeanCopierUtil.copyProperties(application, ApplicationVO.Application.class);
        if (iExtend.getExtend()) {
            List<ApplicationResource> applicationResourceList = applicationResourceService.queryByApplication(application.getId());
            List<ApplicationResourceVO.Resource> resources = BeanCopierUtil.copyListProperties(applicationResourceList, ApplicationResourceVO.Resource.class);
            resources.forEach(this::wrapResourceInstance);
            Map<String, List<ApplicationResourceVO.Resource>> resourcesMap = resources.stream()
                    .collect(Collectors.groupingBy(ApplicationResourceVO.Resource::getResourceType));
            vo.setResourceMap(resourcesMap);
            businessPermissionUserPacker.wrap(vo);
        }
        return vo;
    }

    private void wrapResourceInstance(ApplicationResourceVO.Resource r){
        if (r.getBusinessType() == BusinessTypeEnum.ASSET.getType()) {
            DatasourceInstanceAsset asset = dsInstanceAssetService.getById(r.getBusinessId());
            r.setInstanceUuid(asset.getInstanceUuid());
            dsInstancePacker.wrap(r);
        }
    }

}
